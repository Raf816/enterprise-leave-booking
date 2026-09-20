package com.staffs.leavebooking.staffmanagement.application.dto;

public record StaffSearchCriteria(
        String department,
        String status
) {
    
    public boolean hasFilters() {
        return (department != null && !department.isBlank())
                || (status != null && !status.isBlank());
    }
}
