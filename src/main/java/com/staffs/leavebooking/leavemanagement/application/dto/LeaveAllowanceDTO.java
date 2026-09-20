package com.staffs.leavebooking.leavemanagement.application.dto;

public record LeaveAllowanceDTO(
        String id,
        String staffMemberId,
        String staffName,
        String managerId,
        String department,
        String businessYear,
        int totalEntitlement,
        int daysUsed,
        int daysPending,
        int remainingDays,
        int availableDays
) {
}
