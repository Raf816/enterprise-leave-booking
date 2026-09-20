package com.staffs.leavebooking.identity.dto;

import jakarta.validation.constraints.NotBlank;

public record LoginRequest(

        @NotBlank(message = "Email is required")
        String emailOrUsername,

        @NotBlank(message = "Password is required")
        String password
) {
}
