package com.staffs.leavebooking.leavemanagement.application.listeners;

import com.staffs.leavebooking.leavemanagement.application.handlers.LeaveAllowanceApplicationService;
import com.staffs.leavebooking.leavemanagement.domain.events.LeaveRequestSubmittedEvent;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@Slf4j
@AllArgsConstructor
public class LeaveRequestSubmittedListener {

    private final LeaveAllowanceApplicationService leaveAllowanceApplicationService;

    @TransactionalEventListener(phase = TransactionPhase.BEFORE_COMMIT)
    public void handle(LeaveRequestSubmittedEvent event) {
        log.info("LeaveRequestSubmittedEvent received â€” reserving {} days for staff {}",
                event.numberOfDays(), event.staffMemberId());

        leaveAllowanceApplicationService.reserveDays(
                event.staffMemberId(),
                event.numberOfDays()
        );
    }
}
