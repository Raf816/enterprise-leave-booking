package com.staffs.leavebooking.leavemanagement.application.listeners;

import com.staffs.leavebooking.leavemanagement.application.handlers.LeaveAllowanceApplicationService;
import com.staffs.leavebooking.leavemanagement.domain.events.LeaveRequestRejectedEvent;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@Slf4j
@AllArgsConstructor
public class LeaveRequestRejectedListener {

    private final LeaveAllowanceApplicationService leaveAllowanceApplicationService;

    @TransactionalEventListener(phase = TransactionPhase.BEFORE_COMMIT)
    public void handle(LeaveRequestRejectedEvent event) {
        log.info("LeaveRequestRejectedEvent received â€” releasing {} pending days for staff {}, rejected by {}",
                event.numberOfDays(), event.staffMemberId(), event.managerId());

        leaveAllowanceApplicationService.releasePendingDays(
                event.staffMemberId(),
                event.numberOfDays()
        );
    }
}
