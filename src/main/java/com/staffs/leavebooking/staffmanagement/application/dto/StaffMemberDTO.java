package com.staffs.leavebooking.staffmanagement.application.dto;

import java.time.LocalDate;

public record StaffMemberDTO(
        String id,
        String firstName,
        String surname,
        String email,
        String department,
        String lineManagerId,
        LocalDate hireDate,
        String currentRole,
        LocalDate startDateOfCurrentRole,
        String jobLevel,
        String employmentType,
        String employmentStatus
) {
}
