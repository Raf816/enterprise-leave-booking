package com.staffs.leavebooking.staffmanagement.ui.exceptions;

public class StaffMemberNotFoundException extends RuntimeException {

    public StaffMemberNotFoundException(String staffMemberId) {
        super("Staff member not found: " + staffMemberId);
    }
}
