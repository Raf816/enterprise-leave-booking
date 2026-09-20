package com.staffs.leavebooking.leavemanagement.domain;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("LeaveReason Value Object")
class LeaveReasonTest {

    @Nested
    @DisplayName("Construction validation")
    class ConstructionValidation {

        @Test
        @DisplayName("Should reject null reason")
        void shouldRejectNullReason() {
            IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                    () -> new LeaveReason(null));
            assertEquals(LeaveReason.REASON_NOT_EMPTY, ex.getMessage());
        }

        @Test
        @DisplayName("Should reject blank reason")
        void shouldRejectBlankReason() {
            IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                    () -> new LeaveReason("   "));
            assertEquals(LeaveReason.REASON_NOT_EMPTY, ex.getMessage());
        }

        @Test
        @DisplayName("Should reject empty reason")
        void shouldRejectEmptyReason() {
            IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                    () -> new LeaveReason(""));
            assertEquals(LeaveReason.REASON_NOT_EMPTY, ex.getMessage());
        }

        @Test
        @DisplayName("Should reject reason exceeding 500 characters")
        void shouldRejectReasonTooLong() {
            String longReason = "A".repeat(501);

            IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                    () -> new LeaveReason(longReason));
            assertEquals(LeaveReason.REASON_TOO_LONG, ex.getMessage());
        }

        @Test
        @DisplayName("Should accept reason at exactly 500 characters")
        void shouldAcceptReasonAtMaxLength() {
            String maxReason = "A".repeat(500);

            LeaveReason reason = new LeaveReason(maxReason);

            assertEquals(maxReason, reason.reason());
        }
    }

    @Nested
    @DisplayName("Valid construction")
    class ValidConstruction {

        @Test
        @DisplayName("Should create reason with valid text")
        void shouldCreateValidReason() {
            LeaveReason reason = new LeaveReason("Family holiday");

            assertEquals("Family holiday", reason.reason());
        }

        @Test
        @DisplayName("Should trim whitespace from reason")
        void shouldTrimReason() {
            LeaveReason reason = new LeaveReason("  Holiday trip  ");

            assertEquals("Holiday trip", reason.reason());
        }

        @Test
        @DisplayName("Should accept single character reason")
        void shouldAcceptSingleChar() {
            LeaveReason reason = new LeaveReason("X");

            assertEquals("X", reason.reason());
        }
    }

    @Nested
    @DisplayName("Equality semantics")
    class EqualitySemantics {

        @Test
        @DisplayName("Two reasons with same text should be equal")
        void sameTextShouldBeEqual() {
            LeaveReason reason1 = new LeaveReason("Holiday");
            LeaveReason reason2 = new LeaveReason("Holiday");

            assertEquals(reason1, reason2);
            assertEquals(reason1.hashCode(), reason2.hashCode());
        }

        @Test
        @DisplayName("Two reasons with different text should not be equal")
        void differentTextShouldNotBeEqual() {
            LeaveReason reason1 = new LeaveReason("Holiday");
            LeaveReason reason2 = new LeaveReason("Sick leave");

            assertNotEquals(reason1, reason2);
        }
    }
}
