package com.staffs.leavebooking.leavemanagement.domain;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("BusinessYear Value Object")
class BusinessYearTest {

    @Nested
    @DisplayName("Construction validation")
    class ConstructionValidation {

        @Test
        @DisplayName("Should reject zero start year")
        void shouldRejectZeroStartYear() {
            IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                    () -> new BusinessYear(0, 1));
            assertEquals(BusinessYear.INVALID_START_YEAR, ex.getMessage());
        }

        @Test
        @DisplayName("Should reject negative start year")
        void shouldRejectNegativeStartYear() {
            IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                    () -> new BusinessYear(-1, 0));
            assertEquals(BusinessYear.INVALID_START_YEAR, ex.getMessage());
        }

        @Test
        @DisplayName("Should reject end year not equal to start year + 1")
        void shouldRejectEndYearNotFollowingStart() {
            IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                    () -> new BusinessYear(2026, 2028));
            assertEquals(BusinessYear.END_YEAR_MUST_FOLLOW_START, ex.getMessage());
        }

        @Test
        @DisplayName("Should reject end year same as start year")
        void shouldRejectEndYearSameAsStart() {
            IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                    () -> new BusinessYear(2026, 2026));
            assertEquals(BusinessYear.END_YEAR_MUST_FOLLOW_START, ex.getMessage());
        }

        @Test
        @DisplayName("Should reject end year before start year")
        void shouldRejectEndYearBeforeStart() {
            IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                    () -> new BusinessYear(2026, 2025));
            assertEquals(BusinessYear.END_YEAR_MUST_FOLLOW_START, ex.getMessage());
        }

        @Test
        @DisplayName("Should accept valid business year")
        void shouldAcceptValidBusinessYear() {
            BusinessYear year = new BusinessYear(2026, 2027);

            assertEquals(2026, year.startYear());
            assertEquals(2027, year.endYear());
        }
    }

    @Nested
    @DisplayName("Factory methods")
    class FactoryMethods {

        @Test
        @DisplayName("current() should return business year starting with current year")
        void currentShouldReturnCurrentYear() {
            int expectedStart = LocalDate.now().getYear();

            BusinessYear current = BusinessYear.current();

            assertEquals(expectedStart, current.startYear());
            assertEquals(expectedStart + 1, current.endYear());
        }
    }

    @Nested
    @DisplayName("toString")
    class ToStringBehaviour {

        @Test
        @DisplayName("toString should return 'startYear-endYear' format")
        void toStringShouldReturnCorrectFormat() {
            BusinessYear year = new BusinessYear(2026, 2027);

            assertEquals("2026-2027", year.toString());
        }
    }

    @Nested
    @DisplayName("Equality semantics")
    class EqualitySemantics {

        @Test
        @DisplayName("Two business years with same values should be equal")
        void sameValuesShouldBeEqual() {
            BusinessYear year1 = new BusinessYear(2026, 2027);
            BusinessYear year2 = new BusinessYear(2026, 2027);

            assertEquals(year1, year2);
            assertEquals(year1.hashCode(), year2.hashCode());
        }

        @Test
        @DisplayName("Two business years with different values should not be equal")
        void differentValuesShouldNotBeEqual() {
            BusinessYear year1 = new BusinessYear(2026, 2027);
            BusinessYear year2 = new BusinessYear(2027, 2028);

            assertNotEquals(year1, year2);
        }
    }
}
