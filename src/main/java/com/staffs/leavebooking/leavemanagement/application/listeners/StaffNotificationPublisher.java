package com.staffs.leavebooking.leavemanagement.application.listeners;

import com.staffs.leavebooking.common.events.DomainEventManager;
import com.staffs.leavebooking.common.events.Event;
import com.staffs.leavebooking.common.events.StaffNotificationEvent;
import com.staffs.leavebooking.leavemanagement.domain.events.LeaveRequestApprovedEvent;
import com.staffs.leavebooking.leavemanagement.domain.events.LeaveRequestRejectedEvent;
import com.staffs.leavebooking.leavemanagement.domain.events.LeaveRequestCancelledEvent;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.time.LocalDate;
import java.util.List;

@Component
@Slf4j
@AllArgsConstructor
public class StaffNotificationPublisher {

    private final DomainEventManager domainEventManager;

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void onLeaveRequestApproved(LeaveRequestApprovedEvent event) {
        log.info("Publishing staff notification: request {} APPROVED", event.leaveRequestId());
        domainEventManager.manageDomainEvents("StaffNotificationPublisher", List.of(new StaffNotificationEvent(
                LocalDate.now(), event.staffMemberId(), event.leaveRequestId(),
                "APPROVED", event.managerId(), event.numberOfDays())));
    }

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void onLeaveRequestRejected(LeaveRequestRejectedEvent event) {
        log.info("Publishing staff notification: request {} REJECTED", event.leaveRequestId());
        domainEventManager.manageDomainEvents("StaffNotificationPublisher", List.of(new StaffNotificationEvent(
                LocalDate.now(), event.staffMemberId(), event.leaveRequestId(),
                "REJECTED", event.managerId(), event.numberOfDays())));
    }

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void onLeaveRequestCancelled(LeaveRequestCancelledEvent event) {
        log.info("Publishing staff notification: request {} CANCELLED", event.leaveRequestId());
        domainEventManager.manageDomainEvents("StaffNotificationPublisher", List.of(new StaffNotificationEvent(
                LocalDate.now(), event.staffMemberId(), event.leaveRequestId(),
                "CANCELLED", event.cancelledBy(), event.numberOfDays())));
    }
}
