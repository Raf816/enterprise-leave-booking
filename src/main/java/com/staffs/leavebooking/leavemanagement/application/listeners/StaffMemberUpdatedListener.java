package com.staffs.leavebooking.leavemanagement.application.listeners;

import com.staffs.leavebooking.common.events.StaffMemberUpdatedEvent;
import com.staffs.leavebooking.leavemanagement.application.handlers.LeaveAllowanceApplicationService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
@AllArgsConstructor
@Slf4j
@RabbitListener(queues = "leave-management.staff-member-updated")
public class StaffMemberUpdatedListener {

    private final LeaveAllowanceApplicationService leaveAllowanceApplicationService;

    @RabbitHandler
    public void receive(StaffMemberUpdatedEvent event) {
        try {
            log.info("StaffMemberUpdatedEvent received from RabbitMQ â€” updating details for staff {}",
                    event.staffMemberId());

            leaveAllowanceApplicationService.updateStaffDetails(
                    event.staffMemberId(),
                    event.managerId(),
                    event.department()
            );
        } catch (Exception e) {
            log.error("Failed to process StaffMemberUpdatedEvent for staff {}: {}",
                    event.staffMemberId(), e.getMessage());
        }
    }
}
