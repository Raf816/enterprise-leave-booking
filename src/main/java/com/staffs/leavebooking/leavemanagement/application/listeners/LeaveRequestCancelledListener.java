package com.staffs.leavebooking.leavemanagement.application.listeners;

import com.staffs.leavebooking.leavemanagement.application.handlers.LeaveAllowanceApplicationService;
import com.staffs.leavebooking.leavemanagement.domain.events.LeaveRequestCancelledEvent;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@Slf4j
@AllArgsConstructor
public class LeaveRequestCancelledListener {

    private final LeaveAllowanceApplicationService leaveAllowanceApplicationService;

    @TransactionalEventListener(phase = TransactionPhase.BEFORE_COMMIT)
    public void handle(LeaveRequestCancelledEvent event) {
        log.info("LeaveRequestCancelledEvent received â€” {} {} days for staff {}, cancelled by {}",
                event.wasPreviouslyApproved() ? "crediting back" : "releasing pending",
                event.numberOfDays(), event.staffMemberId(), event.cancelledBy());

        // approved requests had days moved to used, so credit them back
        // pending requests still have days in the pending bucket, so just release them
        if (event.wasPreviouslyApproved()) {
            leaveAllowanceApplicationService.creditBackDays(
                    event.staffMemberId(),
                    event.numberOfDays()
            );
        } else {
            leaveAllowanceApplicationService.releasePendingDays(
                    event.staffMemberId(),
                    event.numberOfDays()
            );
        }
    }
}
