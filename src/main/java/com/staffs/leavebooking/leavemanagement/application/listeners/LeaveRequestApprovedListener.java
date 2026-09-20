package com.staffs.leavebooking.leavemanagement.application.listeners;

import com.staffs.leavebooking.leavemanagement.application.handlers.LeaveAllowanceApplicationService;
import com.staffs.leavebooking.leavemanagement.domain.events.LeaveRequestApprovedEvent;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@Slf4j
@AllArgsConstructor
public class LeaveRequestApprovedListener {

    private final LeaveAllowanceApplicationService leaveAllowanceApplicationService;

    @TransactionalEventListener(phase = TransactionPhase.BEFORE_COMMIT)
    public void handle(LeaveRequestApprovedEvent event) {
        log.info("LeaveRequestApprovedEvent received â€” confirming {} days for staff {}, approved by {}",
                event.numberOfDays(), event.staffMemberId(), event.managerId());

        leaveAllowanceApplicationService.confirmDays(
                event.staffMemberId(),
                event.numberOfDays()
        );
    }
}
