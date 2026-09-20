package com.staffs.leavebooking.identity.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record LoginResponse(
        @JsonProperty("localId") String uid,              // Firebase UID (their field: "localId")
        String email,
        @JsonProperty("displayName") String username,     // Display name (their field: "displayName")
        @JsonProperty("idToken") String accessToken,      // JWT token (their field: "idToken")
        String refreshToken,
        @JsonProperty("expiresIn") String expiresInSeconds // Expiry in seconds (their field: "expiresIn")
) {
}
