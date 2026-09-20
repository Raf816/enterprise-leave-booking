package com.staffs.leavebooking.common.domain;

import static com.staffs.leavebooking.common.domain.DomainAssertions.argumentMatchesPattern;
import static com.staffs.leavebooking.common.domain.DomainAssertions.argumentNotEmpty;

public record Email(String address) implements ValueObject {

    public static final String EMAIL_NOT_EMPTY = "Email address cannot be empty";
    public static final String EMAIL_INVALID_FORMAT = "Email address must be a valid format";

    private static final String EMAIL_REGEX = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$";

    public Email {
        address = argumentNotEmpty(address, EMAIL_NOT_EMPTY);
        argumentMatchesPattern(address, EMAIL_REGEX, EMAIL_INVALID_FORMAT);
    }
}
