package com.staffs.leavebooking.staffmanagement.ui;

import java.time.Instant;

public record StaffMemberCreatedResponse(
        String id,
        String email,
        String message,
        String timestamp
) {
    
    public static StaffMemberCreatedResponse of(String id, String email) {
        return new StaffMemberCreatedResponse(
                id, email, "Staff member created successfully", Instant.now().toString()
        );
    }
}
