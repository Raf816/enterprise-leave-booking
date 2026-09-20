package com.staffs.leavebooking.leavemanagement.ui;

import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public record SubmitLeaveRequestBody(

        @NotNull(message = "Start date is required")
        @FutureOrPresent(message = "Start date must be today or in the future")
        LocalDate startDate,

        @NotNull(message = "End date is required")
        @FutureOrPresent(message = "End date must be today or in the future")
        LocalDate endDate,

        @NotBlank(message = "Leave type is required")
        String leaveType,

        @Size(max = 500, message = "Reason must not exceed 500 characters")
        String reason
) {}
