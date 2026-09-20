package com.staffs.leavebooking.common.events;

import com.fasterxml.jackson.core.JacksonException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

@Service
@Slf4j
@AllArgsConstructor
public class EventStoreService {

    public enum StatusOfMessageDelivery {
        LOCAL,
        PENDING,
        PUBLISHED,
        FAILED,
        UNROUTABLE
    }

    private final EventStoreRepository eventsStore;

    private final ObjectMapper objectMapper;

    @Transactional
    public EventStoreJpa append(Event event, String sourceContext) {
        try {
            EventStoreJpa newEventJpa = new EventStoreJpa();

            newEventJpa.setId(null);

            newEventJpa.setEventType(event.getClass().getSimpleName());

            newEventJpa.setOccurredOn(LocalDate.now());

            newEventJpa.setEventBody(objectMapper.writeValueAsString(event));

            newEventJpa.setSourceContext(sourceContext);

            newEventJpa.setRetryCount(0);

            // remote events start as PENDING until published to RabbitMQ, local events stay in memory only
            if (event instanceof RemoteEvent) {
                newEventJpa.setStatus(StatusOfMessageDelivery.PENDING.name());
            } else {
                newEventJpa.setStatus(StatusOfMessageDelivery.LOCAL.name());
            }

            return eventsStore.save(newEventJpa);
        } catch (JacksonException je) {
            throw new IllegalArgumentException("Failed to serialise event payload", je);
        }
    }

    @Transactional
    public void updateStatus(Long eventId, StatusOfMessageDelivery status, boolean incrementRetryCount) {
        eventsStore.findById(eventId).ifPresent(event -> {
            event.setStatus(status.name());

            if (incrementRetryCount) {
                event.setRetryCount(event.getRetryCount() + 1);
            }

            eventsStore.save(event);

            log.info("Event {} marked as {}", eventId, event.getStatus());
        });
    }

    @Transactional
    public int purgeOldEvents(int retentionDays) {
        LocalDate cutoffDate = LocalDate.now().minusDays(retentionDays);

        // only purge completed events, PENDING and FAILED are left for recovery
        var publishedEvents = eventsStore.findByStatusAndOccurredOnBefore(
                StatusOfMessageDelivery.PUBLISHED.name(), cutoffDate);

        var localEvents = eventsStore.findByStatusAndOccurredOnBefore(
                StatusOfMessageDelivery.LOCAL.name(), cutoffDate);

        int purgedCount = publishedEvents.size() + localEvents.size();

        eventsStore.deleteAll(publishedEvents);
        eventsStore.deleteAll(localEvents);

        if (purgedCount > 0) {
            log.info("Event store cleanup: purged {} events older than {} days (cutoff: {})",
                    purgedCount, retentionDays, cutoffDate);
        }

        return purgedCount;
    }

    public java.util.List<EventStoreJpa> findStrandedEvents() {
        var pending = eventsStore.findByStatus(StatusOfMessageDelivery.PENDING.name());
        var failed = eventsStore.findByStatus(StatusOfMessageDelivery.FAILED.name());

        var stranded = new java.util.ArrayList<>(pending);
        stranded.addAll(failed);
        return stranded;
    }
}
