package com.staffs.leavebooking.leavemanagement.application.mappers;

import com.staffs.leavebooking.common.domain.Identity;
import com.staffs.leavebooking.leavemanagement.domain.*;
import com.staffs.leavebooking.leavemanagement.infrastructure.entities.LeaveRequestJpa;

import java.util.Objects;

public class LeaveRequestJpaToDomainMapper {

    public static LeaveRequest toDomain(LeaveRequestJpa jpa) {
        Objects.requireNonNull(jpa, "LeaveRequest JPA entity cannot be null");

        return LeaveRequest.reconstitute(
                Identity.of(jpa.getId()),

                jpa.getStaffMemberId(),

                jpa.getManagerId(),

                LeaveType.valueOf(jpa.getLeaveType()),

                new DateRange(jpa.getStartDate(), jpa.getEndDate()),

                jpa.getNumberOfDays(),

                jpa.getReason(),

                LeaveRequestStatus.valueOf(jpa.getStatus()),

                jpa.getSubmittedOn(),

                jpa.getDecidedOn(),

                jpa.getDecidedBy(),

                jpa.getDecisionReason(),

                jpa.getCancellationReason()
        );
    }
}
