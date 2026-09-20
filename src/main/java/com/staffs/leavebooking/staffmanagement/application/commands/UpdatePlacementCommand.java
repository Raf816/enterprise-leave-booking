package com.staffs.leavebooking.staffmanagement.application.commands;

import java.time.LocalDate;

public record UpdatePlacementCommand(
        String staffMemberId,
        String currentRole,
        LocalDate startDateOfCurrentRole,
        String jobLevel,
        String employmentType
) {
}
