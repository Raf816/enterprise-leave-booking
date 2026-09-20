package com.staffs.leavebooking.leavemanagement.ui.exceptions;

public class LeaveRequestNotFoundException extends RuntimeException {

    public LeaveRequestNotFoundException(String leaveRequestId) {
        super("Leave request not found: " + leaveRequestId);
    }
}
