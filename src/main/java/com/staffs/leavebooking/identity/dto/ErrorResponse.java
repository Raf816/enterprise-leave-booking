package com.staffs.leavebooking.identity.dto;

import java.time.Instant;
import java.util.Map;

public record ErrorResponse(
        int status,
        String error,
        String message,
        String timestamp
) {
    
    public static Map<String, Object> of(int status, String error, String message) {
        return Map.of(
                "status", status,
                "error", error,
                "message", message,
                "timestamp", Instant.now().toString()
        );
    }
}
