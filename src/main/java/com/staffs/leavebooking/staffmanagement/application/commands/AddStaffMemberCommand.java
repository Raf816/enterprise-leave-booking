package com.staffs.leavebooking.staffmanagement.application.commands;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public record AddStaffMemberCommand(

        @NotBlank(message = "First name is required")
        @Size(max = 50, message = "First name must not exceed 50 characters")
        String firstName,

        @NotBlank(message = "Surname is required")
        @Size(max = 50, message = "Surname must not exceed 50 characters")
        String surname,

        @NotBlank(message = "Email is required")
        String email,

        @NotBlank(message = "Department is required")
        String department,

        String lineManagerId,

        @NotNull(message = "Hire date is required")
        @PastOrPresent(message = "Hire date cannot be in the future")
        LocalDate hireDate,

        @NotBlank(message = "Current role is required")
        String currentRole,

        @NotNull(message = "Start date of current role is required")
        LocalDate startDateOfCurrentRole,

        String jobLevel,

        @NotBlank(message = "Employment type is required")
        String employmentType,

        String password,
        String role
) {
    public static final String DEFAULT_PASSWORD = "Password123!";
    public static final String DEFAULT_ROLE = "STAFF";

    public String effectivePassword() {
        return (password != null && !password.isBlank()) ? password : DEFAULT_PASSWORD;
    }

    public String effectiveRole() {
        return (role != null && !role.isBlank()) ? role : DEFAULT_ROLE;
    }
}
