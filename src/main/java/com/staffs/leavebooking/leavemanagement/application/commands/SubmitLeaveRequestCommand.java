package com.staffs.leavebooking.leavemanagement.application.commands;

import java.time.LocalDate;

public record SubmitLeaveRequestCommand(
        String staffMemberId,
        String managerId,
        LocalDate startDate,
        LocalDate endDate,
        String leaveType,
        String reason
) {
}
