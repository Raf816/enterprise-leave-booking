package com.staffs.leavebooking.leavemanagement.domain;

public enum LeaveType {

    ANNUAL("Annual Leave");

    private final String description;

    LeaveType(String description) {
        this.description = description;
    }

    public String description() {
        return description;
    }
}
