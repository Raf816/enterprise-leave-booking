package com.staffs.leavebooking.leavemanagement.application.mappers;

import com.staffs.leavebooking.leavemanagement.application.dto.LeaveRequestDTO;
import com.staffs.leavebooking.leavemanagement.infrastructure.entities.LeaveRequestJpa;

import java.util.Objects;

public class LeaveRequestJpaToDTOMapper {

    public static LeaveRequestDTO toDTO(LeaveRequestJpa jpa) {
        Objects.requireNonNull(jpa, "LeaveRequest JPA entity cannot be null");

        return new LeaveRequestDTO(
                jpa.getId(),

                jpa.getStaffMemberId(),

                jpa.getManagerId(),

                jpa.getLeaveType(),

                jpa.getStartDate(),

                jpa.getEndDate(),

                jpa.getNumberOfDays(),

                jpa.getReason(),

                jpa.getStatus(),

                jpa.getSubmittedOn(),

                jpa.getDecidedOn(),

                jpa.getDecidedBy(),

                jpa.getDecisionReason(),

                jpa.getCancellationReason()
        );
    }
}
