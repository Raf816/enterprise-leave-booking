package com.staffs.leavebooking.identity.security;

import com.fasterxml.jackson.annotation.JsonCreator;

public enum Role {

    STAFF,
    MANAGER,
    ADMIN;

    public static final String PREFIX = "ROLE_";

    public String getAuthority() {
        return PREFIX + name();
    }

    @JsonCreator
    public static Role fromString(String roleAsString) {
        if (roleAsString == null || roleAsString.isBlank()) {
            throw new IllegalArgumentException("Role cannot be null or empty");
        }
        try {
            return Role.valueOf(roleAsString.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid role: " + roleAsString +
                    ". Valid roles are: STAFF, MANAGER, ADMIN");
        }
    }
}
