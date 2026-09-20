package com.staffs.leavebooking.leavemanagement.application.listeners;

import com.staffs.leavebooking.common.events.StaffMemberAddedEvent;
import com.staffs.leavebooking.leavemanagement.application.handlers.LeaveAllowanceApplicationService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
@AllArgsConstructor
@Slf4j
@RabbitListener(queues = "leave-management.staff-member-added")
public class StaffMemberAddedListener {

    private final LeaveAllowanceApplicationService leaveAllowanceApplicationService;

    @RabbitHandler
    public void receive(StaffMemberAddedEvent event) {
        try {
            log.info("StaffMemberAddedEvent received from RabbitMQ â€” creating allowance for staff {}",
                    event.staffMemberId());

            leaveAllowanceApplicationService.createAllowanceForNewStaff(
                    event.staffMemberId(),
                    event.managerId(),
                    event.firstName(),
                    event.surname(),
                    event.department(),
                    event.defaultEntitlement()
            );
        } catch (Exception e) {
            log.error("Failed to process StaffMemberAddedEvent for staff {}: {}",
                    event.staffMemberId(), e.getMessage());
        }
    }
}
