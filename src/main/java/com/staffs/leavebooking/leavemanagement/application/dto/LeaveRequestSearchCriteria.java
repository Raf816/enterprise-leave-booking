package com.staffs.leavebooking.leavemanagement.application.dto;

import java.time.LocalDate;

public record LeaveRequestSearchCriteria(
        String status,
        String staffMemberId,
        String managerId,
        LocalDate from,
        LocalDate to
) {

    public boolean hasFilters() {
        return (status != null && !status.isBlank())
                || (staffMemberId != null && !staffMemberId.isBlank())
                || (managerId != null && !managerId.isBlank())
                || from != null
                || to != null;
    }

    public String normalizedStatus() {
        return (status != null && !status.isBlank()) ? status.toUpperCase() : null;
    }
}
