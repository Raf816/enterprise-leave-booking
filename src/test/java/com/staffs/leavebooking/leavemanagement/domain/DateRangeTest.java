package com.staffs.leavebooking.leavemanagement.domain;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("DateRange Value Object")
class DateRangeTest {

    @Nested
    @DisplayName("Construction validation")
    class ConstructionValidation {

        @Test
        @DisplayName("Should reject null start date")
        void shouldRejectNullStartDate() {
            IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                    () -> new DateRange(null, LocalDate.of(2027, 1, 10)));
            assertEquals(DateRange.START_DATE_NOT_NULL, ex.getMessage());
        }

        @Test
        @DisplayName("Should reject null end date")
        void shouldRejectNullEndDate() {
            IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                    () -> new DateRange(LocalDate.of(2027, 1, 5), null));
            assertEquals(DateRange.END_DATE_NOT_NULL, ex.getMessage());
        }

        @Test
        @DisplayName("Should reject end date before start date")
        void shouldRejectEndBeforeStart() {
            LocalDate start = LocalDate.of(2027, 3, 15);
            LocalDate end = LocalDate.of(2027, 3, 10);

            IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                    () -> new DateRange(start, end));
            assertEquals(DateRange.END_BEFORE_START, ex.getMessage());
        }

        @Test
        @DisplayName("Should accept same start and end date")
        void shouldAcceptSameStartAndEnd() {
            LocalDate date = LocalDate.of(2027, 3, 17);

            DateRange range = new DateRange(date, date);

            assertEquals(date, range.startDate());
            assertEquals(date, range.endDate());
        }

        @Test
        @DisplayName("Should accept valid date range")
        void shouldAcceptValidRange() {
            LocalDate start = LocalDate.of(2027, 6, 1);
            LocalDate end = LocalDate.of(2027, 6, 5);

            DateRange range = new DateRange(start, end);

            assertEquals(start, range.startDate());
            assertEquals(end, range.endDate());
        }
    }

    @Nested
    @DisplayName("validateFutureStart()")
    class ValidateFutureStart {

        @Test
        @DisplayName("Should throw if start date is today")
        void shouldThrowIfStartIsToday() {
            DateRange range = new DateRange(LocalDate.now(), LocalDate.now().plusDays(5));

            IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                    range::validateFutureStart);
            assertEquals(DateRange.START_DATE_IN_PAST, ex.getMessage());
        }

        @Test
        @DisplayName("Should throw if start date is in the past")
        void shouldThrowIfStartInPast() {
            DateRange range = new DateRange(LocalDate.now().minusDays(1), LocalDate.now().plusDays(5));

            IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                    range::validateFutureStart);
            assertEquals(DateRange.START_DATE_IN_PAST, ex.getMessage());
        }

        @Test
        @DisplayName("Should pass if start date is tomorrow")
        void shouldPassIfStartIsTomorrow() {
            DateRange range = new DateRange(LocalDate.now().plusDays(1), LocalDate.now().plusDays(5));

            assertDoesNotThrow(range::validateFutureStart);
        }
    }

    @Nested
    @DisplayName("workingDays() calculation")
    class WorkingDaysCalculation {

        @Test
        @DisplayName("Should count 5 working days for a full Monday-Friday week")
        void shouldCount5DaysForFullWeek() {
            DateRange range = new DateRange(
                    LocalDate.of(2027, 1, 4),
                    LocalDate.of(2027, 1, 8));

            int days = range.workingDays();

            assertEquals(5, days);
        }

        @Test
        @DisplayName("Should exclude weekends from count")
        void shouldExcludeWeekends() {
            DateRange range = new DateRange(
                    LocalDate.of(2027, 1, 4),
                    LocalDate.of(2027, 1, 10));

            int days = range.workingDays();

            assertEquals(5, days);
        }

        @Test
        @DisplayName("Should return 0 for Saturday-Sunday only range")
        void shouldReturnZeroForWeekendOnly() {
            DateRange range = new DateRange(
                    LocalDate.of(2027, 1, 9),
                    LocalDate.of(2027, 1, 10));

            int days = range.workingDays();

            assertEquals(0, days);
        }

        @Test
        @DisplayName("Should return 1 for a single weekday")
        void shouldReturn1ForSingleWeekday() {
            LocalDate wednesday = LocalDate.of(2027, 1, 6);
            DateRange range = new DateRange(wednesday, wednesday);

            int days = range.workingDays();

            assertEquals(1, days);
        }

        @Test
        @DisplayName("Should return 0 for a single Saturday")
        void shouldReturn0ForSingleSaturday() {
            LocalDate saturday = LocalDate.of(2027, 1, 9);
            DateRange range = new DateRange(saturday, saturday);

            int days = range.workingDays();

            assertEquals(0, days);
        }

        @Test
        @DisplayName("Should count 10 working days across two full weeks")
        void shouldCount10DaysForTwoWeeks() {
            DateRange range = new DateRange(
                    LocalDate.of(2027, 1, 4),
                    LocalDate.of(2027, 1, 15));

            int days = range.workingDays();

            assertEquals(10, days);
        }
    }

    @Nested
    @DisplayName("Equality semantics")
    class EqualitySemantics {

        @Test
        @DisplayName("Two date ranges with same dates should be equal")
        void sameDatesShouldBeEqual() {
            LocalDate start = LocalDate.of(2027, 3, 1);
            LocalDate end = LocalDate.of(2027, 3, 5);

            DateRange range1 = new DateRange(start, end);
            DateRange range2 = new DateRange(start, end);

            assertEquals(range1, range2);
            assertEquals(range1.hashCode(), range2.hashCode());
        }
    }
}
