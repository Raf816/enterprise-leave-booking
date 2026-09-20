package com.staffs.leavebooking.common.domain;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Email Value Object")
class EmailTest {

    @Nested
    @DisplayName("Null and blank rejection")
    class NullBlankRejection {

        @Test
        @DisplayName("Should reject null email address")
        void shouldRejectNullEmail() {
            IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                    () -> new Email(null));
            assertEquals(Email.EMAIL_NOT_EMPTY, ex.getMessage());
        }

        @Test
        @DisplayName("Should reject blank email address")
        void shouldRejectBlankEmail() {
            IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                    () -> new Email("   "));
            assertEquals(Email.EMAIL_NOT_EMPTY, ex.getMessage());
        }

        @Test
        @DisplayName("Should reject empty email address")
        void shouldRejectEmptyEmail() {
            IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                    () -> new Email(""));
            assertEquals(Email.EMAIL_NOT_EMPTY, ex.getMessage());
        }
    }

    @Nested
    @DisplayName("Invalid format rejection")
    class InvalidFormatRejection {

        @ParameterizedTest(name = "Should reject invalid email: {0}")
        @ValueSource(strings = {
                "notanemail",
                "@domain.com",
                "user@",
                "user@domain",
                "user@domain.c",
                "user@.com",
                "user domain@test.com",
                "user@@domain.com"
        })
        @DisplayName("Should reject invalid email formats")
        void shouldRejectInvalidFormats(String invalidEmail) {
            IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                    () -> new Email(invalidEmail));
            assertEquals(Email.EMAIL_INVALID_FORMAT, ex.getMessage());
        }
    }

    @Nested
    @DisplayName("Valid email acceptance")
    class ValidEmailAcceptance {

        @ParameterizedTest(name = "Should accept valid email: {0}")
        @ValueSource(strings = {
                "user@domain.com",
                "john.doe@company.co.uk",
                "test+label@gmail.com",
                "user_name@sub.domain.org",
                "First.Last@Example.COM"
        })
        @DisplayName("Should accept valid email formats")
        void shouldAcceptValidFormats(String validEmail) {
            Email email = new Email(validEmail);

            assertNotNull(email.address());
        }

        @Test
        @DisplayName("Should trim whitespace from email address")
        void shouldTrimWhitespace() {
            Email email = new Email("  user@domain.com  ");

            assertEquals("user@domain.com", email.address());
        }
    }

    @Nested
    @DisplayName("Equality semantics")
    class EqualitySemantics {

        @Test
        @DisplayName("Two emails with same address should be equal")
        void sameAddressShouldBeEqual() {
            Email email1 = new Email("user@domain.com");
            Email email2 = new Email("user@domain.com");

            assertEquals(email1, email2);
            assertEquals(email1.hashCode(), email2.hashCode());
        }

        @Test
        @DisplayName("Two emails with different addresses should not be equal")
        void differentAddressesShouldNotBeEqual() {
            Email email1 = new Email("user@domain.com");
            Email email2 = new Email("other@domain.com");

            assertNotEquals(email1, email2);
        }
    }
}
