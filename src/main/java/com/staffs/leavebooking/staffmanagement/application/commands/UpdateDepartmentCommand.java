package com.staffs.leavebooking.staffmanagement.application.commands;

public record UpdateDepartmentCommand(
        String staffMemberId,
        String department,
        String lineManagerId
) {
}
