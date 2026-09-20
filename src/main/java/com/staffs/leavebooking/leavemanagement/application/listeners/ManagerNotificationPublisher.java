package com.staffs.leavebooking.leavemanagement.application.listeners;

import com.staffs.leavebooking.common.events.DomainEventManager;
import com.staffs.leavebooking.common.events.Event;
import com.staffs.leavebooking.common.events.ManagerNotificationEvent;
import com.staffs.leavebooking.leavemanagement.domain.events.LeaveRequestSubmittedEvent;
import com.staffs.leavebooking.leavemanagement.infrastructure.entities.LeaveRequestJpa;
import com.staffs.leavebooking.leavemanagement.infrastructure.repositories.LeaveRequestRepository;
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
public class ManagerNotificationPublisher {

    private final LeaveRequestRepository leaveRequestRepository;

    private final DomainEventManager domainEventManager;

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void onLeaveRequestSubmitted(LeaveRequestSubmittedEvent event) {
        log.info("Publishing manager notification for leave request {} by staff {}",
                event.leaveRequestId(), event.staffMemberId());

        leaveRequestRepository.findById(event.leaveRequestId()).ifPresent(request -> {
            ManagerNotificationEvent notification = new ManagerNotificationEvent(
                    LocalDate.now(),
                    request.getManagerId(),
                    event.staffMemberId(),
                    "Staff Member",
                    event.leaveRequestId(),
                    request.getStartDate(),
                    request.getEndDate(),
                    event.numberOfDays(),
                    request.getReason()
            );
            domainEventManager.manageDomainEvents("ManagerNotificationPublisher", List.of(notification));
        });
    }
}
