package com.staffs.leavebooking.leavemanagement.application.handlers;

import com.staffs.leavebooking.common.domain.Identity;
import com.staffs.leavebooking.common.events.DomainEventManager;
import com.staffs.leavebooking.leavemanagement.application.commands.CancelLeaveRequestCommand;
import com.staffs.leavebooking.leavemanagement.application.commands.SubmitLeaveRequestCommand;
import com.staffs.leavebooking.leavemanagement.application.mappers.LeaveRequestDomainToJpaMapper;
import com.staffs.leavebooking.leavemanagement.application.mappers.LeaveRequestJpaToDomainMapper;
import com.staffs.leavebooking.leavemanagement.domain.DateRange;
import com.staffs.leavebooking.leavemanagement.domain.LeaveRequest;
import com.staffs.leavebooking.leavemanagement.domain.LeaveType;
import com.staffs.leavebooking.leavemanagement.infrastructure.repositories.LeaveRequestRepository;
import com.staffs.leavebooking.leavemanagement.ui.exceptions.LeaveRequestNotFoundException;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
@AllArgsConstructor
public class LeaveRequestApplicationService {

    private final LeaveRequestRepository leaveRequestRepository;

    private final DomainEventManager domainEventManager;

    @Transactional
    public String submitNewRequest(SubmitLeaveRequestCommand command) {
        Identity<LeaveRequest> newId = Identity.generateId();

        LeaveRequest leaveRequest = LeaveRequest.submitNew(
                newId,
                command.staffMemberId(),
                command.managerId(),
                parseLeaveType(command.leaveType()),
                new DateRange(command.startDate(), command.endDate()),
                command.reason()
        );

        leaveRequestRepository.save(LeaveRequestDomainToJpaMapper.toJpa(leaveRequest));
        dispatchAndClear(leaveRequest);

        log.info("Leave request {} submitted by staff member {}",
                newId.id(), command.staffMemberId());
        return newId.id();
    }

    @Transactional
    public void approveRequest(String leaveRequestId, String decidedBy, String reason) {
        LeaveRequest leaveRequest = loadDomainAggregate(leaveRequestId);
        leaveRequest.approve(decidedBy, reason);

        leaveRequestRepository.save(LeaveRequestDomainToJpaMapper.toJpa(leaveRequest));
        dispatchAndClear(leaveRequest);

        log.info("Leave request {} approved by {}", leaveRequestId, decidedBy);
    }

    @Transactional
    public void rejectRequest(String leaveRequestId, String decidedBy, String reason) {
        LeaveRequest leaveRequest = loadDomainAggregate(leaveRequestId);
        leaveRequest.reject(decidedBy, reason);

        leaveRequestRepository.save(LeaveRequestDomainToJpaMapper.toJpa(leaveRequest));
        dispatchAndClear(leaveRequest);

        log.info("Leave request {} rejected by {}", leaveRequestId, decidedBy);
    }

    @Transactional
    public void cancelRequest(CancelLeaveRequestCommand command) {
        LeaveRequest leaveRequest = loadDomainAggregate(command.leaveRequestId());
        leaveRequest.cancel(command.cancelledBy(), command.reason());

        leaveRequestRepository.save(LeaveRequestDomainToJpaMapper.toJpa(leaveRequest));
        dispatchAndClear(leaveRequest);

        log.info("Leave request {} cancelled by {}", command.leaveRequestId(), command.cancelledBy());
    }

    private void dispatchAndClear(LeaveRequest aggregate) {
        if (aggregate.domainEventsExist()) {
            domainEventManager.manageDomainEvents(
                    this.getClass().getSimpleName(),
                    aggregate.listOfDomainEvents()
            );
            aggregate.clearDomainEvents();
        }
    }

    private LeaveRequest loadDomainAggregate(String leaveRequestId) {
        return leaveRequestRepository.findById(leaveRequestId)
                .map(LeaveRequestJpaToDomainMapper::toDomain)
                .orElseThrow(() -> new LeaveRequestNotFoundException(leaveRequestId));
    }

    private LeaveType parseLeaveType(String type) {
        try {
            return LeaveType.valueOf(type);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException(
                    "Invalid leave type: '" + type + "'. Valid values are: ANNUAL");
        }
    }
}
