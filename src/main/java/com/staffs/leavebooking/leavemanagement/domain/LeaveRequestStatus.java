package com.staffs.leavebooking.leavemanagement.domain;

public enum LeaveRequestStatus {

    PENDING("Awaiting manager approval"),

    APPROVED("Leave request approved"),

    REJECTED("Leave request rejected"),

    CANCELLED("Leave request cancelled");

    private final String description;

    LeaveRequestStatus(String description) {
        this.description = description;
    }

    public String description() {
        return description;
    }
}
