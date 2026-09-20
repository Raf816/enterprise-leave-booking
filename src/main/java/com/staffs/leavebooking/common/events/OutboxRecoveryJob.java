package com.staffs.leavebooking.common.events;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.AmqpException;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@Slf4j
@AllArgsConstructor
public class OutboxRecoveryJob {

    private static final int MAX_RECOVERY_RETRIES = 10;

    private final EventStoreService eventStoreService;
    private final RabbitTemplate rabbitTemplate;
    private final RabbitOutboxRouter rabbitOutboxRouter;
    private final ObjectMapper objectMapper;

    @Scheduled(fixedRate = 300_000)
    public void recoverStrandedEvents() {
        List<EventStoreJpa> stranded = eventStoreService.findStrandedEvents();

        if (stranded.isEmpty()) {
            return;
        }

        log.info("Outbox recovery: found {} stranded event(s). Attempting re-publish.", stranded.size());

        int recovered = 0;
        int skipped = 0;

        for (EventStoreJpa event : stranded) {
            if (event.getRetryCount() >= MAX_RECOVERY_RETRIES) {
                skipped++;
                log.warn("Outbox recovery: skipping event {} (type={}, retries={}) â€” exceeds max retries. Manual investigation required.",
                        event.getId(), event.getEventType(), event.getRetryCount());
                continue;
            }

            try {
                String eventClassName = "com.staffs.leavebooking.common.events." + event.getEventType();
                Class<?> eventClass = Class.forName(eventClassName);
                Object eventObject = objectMapper.readValue(event.getEventBody(), eventClass);

                RabbitOutboxRouter.Destination destination = rabbitOutboxRouter.resolve((Event) eventObject);

                rabbitTemplate.convertAndSend(destination.exchange(), destination.routingKey(), eventObject);

                eventStoreService.updateStatus(event.getId(),
                        EventStoreService.StatusOfMessageDelivery.PUBLISHED, false);
                recovered++;

                log.info("Outbox recovery: successfully re-published event {} (type={})",
                        event.getId(), event.getEventType());

            } catch (AmqpException e) {
                eventStoreService.updateStatus(event.getId(),
                        EventStoreService.StatusOfMessageDelivery.PENDING, true);
                log.warn("Outbox recovery: failed to re-publish event {} (type={}, retry={}). Will retry next cycle.",
                        event.getId(), event.getEventType(), event.getRetryCount() + 1, e);

            } catch (ClassNotFoundException e) {
                eventStoreService.updateStatus(event.getId(),
                        EventStoreService.StatusOfMessageDelivery.FAILED, true);
                log.error("Outbox recovery: unknown event type '{}' for event {}. Marked as FAILED.",
                        event.getEventType(), event.getId(), e);
                skipped++;

            } catch (Exception e) {
                eventStoreService.updateStatus(event.getId(),
                        EventStoreService.StatusOfMessageDelivery.FAILED, true);
                log.error("Outbox recovery: error processing event {} (type={}). Marked as FAILED.",
                        event.getId(), event.getEventType(), e);
            }
        }

        log.info("Outbox recovery complete: {} recovered, {} skipped, {} remaining",
                recovered, skipped, stranded.size() - recovered - skipped);
    }
}
