package com.staffs.leavebooking.leavemanagement.application.commands;

public record CancelLeaveRequestCommand(
        String leaveRequestId,
        String cancelledBy,
        String reason
) {
}
