package com.staffs.leavebooking.common.domain;

import java.math.BigDecimal;

public final class DomainAssertions {

    private DomainAssertions() {
    }

    public static String argumentNotEmpty(String argument, String message) {
        if (argument == null || argument.isBlank()) {
            throw new IllegalArgumentException(message);
        }
        return argument.trim();
    }

    public static void argumentNotNull(Object argument, String message) {
        if (argument == null) {
            throw new IllegalArgumentException(message);
        }
    }

    public static void argumentLength(String argument, int minLength, int maxLength, String message) {
        if (argument.length() < minLength || argument.length() > maxLength) {
            throw new IllegalArgumentException(message);
        }
    }

    public static void argumentPositive(int argument, String message) {
        if (argument <= 0) {
            throw new IllegalArgumentException(message);
        }
    }

    public static void argumentNotNegative(int argument, String message) {
        if (argument < 0) {
            throw new IllegalArgumentException(message);
        }
    }

    public static void argumentNotEmpty(BigDecimal argument, String message) {
        if (argument == null) {
            throw new IllegalArgumentException(message);
        }
    }

    public static void argumentMatchesPattern(String argument, String regex, String message) {
        if (argument == null || !argument.matches(regex)) {
            throw new IllegalArgumentException(message);
        }
    }
}
