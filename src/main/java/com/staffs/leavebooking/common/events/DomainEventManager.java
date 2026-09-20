package com.staffs.leavebooking.common.events;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;

@Service
@Slf4j
@AllArgsConstructor
public class DomainEventManager {

    private final ApplicationEventPublisher eventPublisher;

    private final EventStoreService eventStoreService;

    @Transactional
    public void manageDomainEvents(String sourceContext, List<Event> events) {
        Objects.requireNonNull(sourceContext, "Source context cannot be null");
        Objects.requireNonNull(events, "Events list cannot be null");

        for (Event event : events) {
            log.info("{} -> {}", sourceContext, event);

            EventStoreJpa savedEvent = eventStoreService.append(event, sourceContext);

            // publish with the database assigned id so downstream listeners can update delivery status
            eventPublisher.publishEvent(event.withId(savedEvent.getId()));
        }
    }
}
