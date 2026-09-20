package com.staffs.leavebooking.common.domain;

import jakarta.persistence.Embeddable;

import static com.staffs.leavebooking.common.domain.DomainAssertions.argumentLength;
import static com.staffs.leavebooking.common.domain.DomainAssertions.argumentMatchesPattern;
import static com.staffs.leavebooking.common.domain.DomainAssertions.argumentNotEmpty;

@Embeddable
public record FullName(
        String firstName,
        String surname
) implements ValueObject {

    public static final int MAX_FIRST_NAME_LENGTH = 50;

    public static final int MAX_SURNAME_LENGTH = 50;

    public static final String FIRST_NAME_NOT_EMPTY = "First name cannot be empty";
    public static final String SURNAME_NOT_EMPTY = "Surname cannot be empty";
    public static final String FIRST_NAME_LENGTH = "First name must be between 1 and " + MAX_FIRST_NAME_LENGTH + " characters";
    public static final String SURNAME_LENGTH = "Surname must be between 1 and " + MAX_SURNAME_LENGTH + " characters";

    public static final String NAME_PATTERN = "^[a-zA-Z' \\-]+$";

    public static final String FIRST_NAME_INVALID_CHARS = "First name must contain only letters, hyphens, apostrophes, and spaces";
    public static final String SURNAME_INVALID_CHARS = "Surname must contain only letters, hyphens, apostrophes, and spaces";

    public FullName {
        firstName = argumentNotEmpty(firstName, FIRST_NAME_NOT_EMPTY);
        surname = argumentNotEmpty(surname, SURNAME_NOT_EMPTY);

        argumentLength(firstName, 1, MAX_FIRST_NAME_LENGTH, FIRST_NAME_LENGTH);
        argumentLength(surname, 1, MAX_SURNAME_LENGTH, SURNAME_LENGTH);

        argumentMatchesPattern(firstName, NAME_PATTERN, FIRST_NAME_INVALID_CHARS);
        argumentMatchesPattern(surname, NAME_PATTERN, SURNAME_INVALID_CHARS);
    }
}
