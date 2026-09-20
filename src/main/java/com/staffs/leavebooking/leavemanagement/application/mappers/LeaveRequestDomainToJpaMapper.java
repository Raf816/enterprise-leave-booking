package com.staffs.leavebooking.leavemanagement.application.mappers;

import com.staffs.leavebooking.leavemanagement.domain.LeaveRequest;
import com.staffs.leavebooking.leavemanagement.infrastructure.entities.LeaveRequestJpa;

import java.util.Objects;

public class LeaveRequestDomainToJpaMapper {

    public static LeaveRequestJpa toJpa(LeaveRequest domain) {
        Objects.requireNonNull(domain, "LeaveRequest domain entity cannot be null");

        LeaveRequestJpa jpa = new LeaveRequestJpa();

        jpa.setId(domain.id().id());

        jpa.setStaffMemberId(domain.staffMemberId());

        jpa.setManagerId(domain.managerId());

        jpa.setLeaveType(domain.leaveType().name());

        jpa.setStartDate(domain.dateRange().startDate());

        jpa.setEndDate(domain.dateRange().endDate());

        jpa.setNumberOfDays(domain.numberOfDays());

        jpa.setReason(domain.reason());

        jpa.setStatus(domain.status().name());

        jpa.setSubmittedOn(domain.submittedOn());

        jpa.setDecidedOn(domain.decidedOn());

        jpa.setDecidedBy(domain.decidedBy());

        jpa.setDecisionReason(domain.decisionReason());

        jpa.setCancellationReason(domain.cancellationReason());

        return jpa;
    }
}
