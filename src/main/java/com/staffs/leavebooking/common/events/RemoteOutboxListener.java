package com.staffs.leavebooking.common.events;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.AmqpException;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Recover;
import org.springframework.retry.annotation.Retryable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@Slf4j
@AllArgsConstructor
public class RemoteOutboxListener {

    private final EventStoreService eventStoreService;

    private final RabbitTemplate rabbitTemplate;

    private final RabbitOutboxRouter rabbitOutboxRouter;

    // runs async after the source transaction commits, retries up to 3 times on broker failure
    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Retryable(
            retryFor = AmqpException.class,
            maxAttempts = 3,
            backoff = @Backoff(delay = 500, multiplier = 2.0)
    )
    public void handleRemoteEvent(RemoteEvent event) {
        RabbitOutboxRouter.Destination destination;

        try {
            destination = rabbitOutboxRouter.resolve(event);
        } catch (IllegalArgumentException e) {
            log.error("Unroutable event [{}]. Check RabbitOutboxRouter configuration.",
                    event.getClass().getSimpleName(), e);
            eventStoreService.updateStatus(event.id(),
                    EventStoreService.StatusOfMessageDelivery.UNROUTABLE, false);
            return;
        }

        rabbitTemplate.convertAndSend(
                destination.exchange(),
                destination.routingKey(),
                event
        );

        eventStoreService.updateStatus(event.id(),
                EventStoreService.StatusOfMessageDelivery.PUBLISHED, false);
    }

    // called when all retries are exhausted
    @Recover
    public void recover(AmqpException e, RemoteEvent event) {
        log.error("Failed to publish event {} to RabbitMQ after retries. Marking as FAILED.",
                event.id(), e);
        eventStoreService.updateStatus(event.id(),
                EventStoreService.StatusOfMessageDelivery.FAILED, true);
    }
}
