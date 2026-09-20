package com.staffs.leavebooking.leavemanagement.ui.exceptions;

public class LeaveAllowanceNotFoundException extends RuntimeException {

    public LeaveAllowanceNotFoundException(String identifier) {
        super("Leave allowance not found for: " + identifier);
    }
}
