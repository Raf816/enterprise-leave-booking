package com.staffs.leavebooking.staffmanagement.application.commands;

public record UpdateStatusCommand(
        String staffMemberId,
        String employmentStatus
) {
}
