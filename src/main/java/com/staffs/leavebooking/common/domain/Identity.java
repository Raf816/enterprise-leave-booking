package com.staffs.leavebooking.common.domain;

import java.util.UUID;

public record Identity<T>(String id) implements ValueObject {

    public static final String IDENTITY_CANNOT_BE_NULL = "Identity cannot be null or blank";

    public Identity {
        if (id == null || id.isBlank()) {
            throw new IllegalArgumentException(IDENTITY_CANNOT_BE_NULL);
        }
    }

    public static <T> Identity<T> of(String id) {
        return new Identity<>(id);
    }

    public static <T> Identity<T> generateId() {
        return new Identity<>(UUID.randomUUID().toString());
    }
}
