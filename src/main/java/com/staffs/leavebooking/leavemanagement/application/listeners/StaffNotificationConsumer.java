package com.staffs.leavebooking.leavemanagement.application.listeners;

import com.staffs.leavebooking.common.events.StaffNotificationEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@RabbitListener(queues = "notifications.staff-request-decided")
public class StaffNotificationConsumer {

    @RabbitHandler
    public void receive(StaffNotificationEvent event) {
        log.info("NOTIFICATION â†’ Staff {} alerted: leave request {} has been {}",
                event.staffMemberId(), event.leaveRequestId(), event.decision());
    }
}
