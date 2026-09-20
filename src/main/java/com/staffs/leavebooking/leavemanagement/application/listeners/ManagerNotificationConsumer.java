package com.staffs.leavebooking.leavemanagement.application.listeners;

import com.staffs.leavebooking.common.events.ManagerNotificationEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@RabbitListener(queues = "notifications.manager-pending-request")
public class ManagerNotificationConsumer {

    @RabbitHandler
    public void receive(ManagerNotificationEvent event) {
        log.info("NOTIFICATION â†’ Manager {} alerted: new pending leave request {} from staff {} ({} days, {}-{})",
                event.managerId(), event.leaveRequestId(), event.staffMemberId(),
                event.numberOfDays(), event.startDate(), event.endDate());
    }
}
