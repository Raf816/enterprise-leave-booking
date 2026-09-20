package com.staffs.leavebooking.leavemanagement.application.dto;

import java.time.LocalDate;

public record LeaveRequestDTO(
        String id,
        String staffMemberId,
        String managerId,
        String leaveType,
        LocalDate startDate,
        LocalDate endDate,
        int numberOfDays,
        String reason,
        String status,
        LocalDate submittedOn,
        LocalDate decidedOn,
        String decidedBy,
        String decisionReason,
        String cancellationReason
) {
}
