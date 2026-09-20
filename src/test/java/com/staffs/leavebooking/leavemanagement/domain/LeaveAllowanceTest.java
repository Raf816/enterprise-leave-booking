package com.staffs.leavebooking.leavemanagement.domain;

import com.staffs.leavebooking.common.domain.Identity;
import com.staffs.leavebooking.testfixtures.LeaveAllowanceMother;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("LeaveAllowance Aggregate Root")
class LeaveAllowanceTest {

    @Nested
    @DisplayName("createNew() factory method")
    class CreateNew {

        @Test
        @DisplayName("Should create allowance with zero days used and pending")
        void shouldCreateWithZeroDays() {
            Identity<LeaveAllowance> id = Identity.generateId();

            LeaveAllowance allowance = LeaveAllowance.createNew(
                    id, "staff-id-123", "manager-id-456",
                    "John", "Smith", "Engineering", 25);

            assertEquals(0, allowance.daysUsed());
            assertEquals(0, allowance.daysPending());
            assertEquals(25, allowance.totalEntitlement());
            assertEquals(BusinessYear.current(), allowance.businessYear());
        }

        @Test
        @DisplayName("Should reject blank staff member ID")
        void shouldRejectBlankStaffId() {
            IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                    () -> LeaveAllowance.createNew(
                            Identity.generateId(), "   ", "manager-id",
                            "John", "Smith", "Eng", 25));
            assertEquals(LeaveAllowance.STAFF_MEMBER_ID_REQUIRED, ex.getMessage());
        }

        @Test
        @DisplayName("Should reject blank manager ID")
        void shouldRejectBlankManagerId() {
            IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                    () -> LeaveAllowance.createNew(
                            Identity.generateId(), "staff-id", "   ",
                            "John", "Smith", "Eng", 25));
            assertEquals(LeaveAllowance.MANAGER_ID_REQUIRED, ex.getMessage());
        }

        @Test
        @DisplayName("Should reject zero entitlement")
        void shouldRejectZeroEntitlement() {
            IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                    () -> LeaveAllowance.createNew(
                            Identity.generateId(), "staff-id", "manager-id",
                            "John", "Smith", "Eng", 0));
            assertEquals(LeaveAllowance.ENTITLEMENT_MUST_BE_POSITIVE, ex.getMessage());
        }

        @Test
        @DisplayName("Should reject negative entitlement")
        void shouldRejectNegativeEntitlement() {
            assertThrows(IllegalArgumentException.class,
                    () -> LeaveAllowance.createNew(
                            Identity.generateId(), "staff-id", "manager-id",
                            "John", "Smith", "Eng", -5));
        }
    }

    @Nested
    @DisplayName("reserveDays() command")
    class ReserveDays {

        @Test
        @DisplayName("Should increase days pending by requested amount")
        void shouldIncreaseDaysPending() {
            LeaveAllowance allowance = LeaveAllowanceMother.freshAllowance();

            allowance.reserveDays(5);

            assertEquals(5, allowance.daysPending());
            assertEquals(0, allowance.daysUsed());
        }

        @Test
        @DisplayName("Should allow multiple reservations up to entitlement")
        void shouldAllowMultipleReservations() {
            LeaveAllowance allowance = LeaveAllowanceMother.freshAllowance();

            allowance.reserveDays(10);
            allowance.reserveDays(10);
            allowance.reserveDays(5);

            assertEquals(25, allowance.daysPending());
        }

        @Test
        @DisplayName("Should throw when over-booking invariant violated")
        void shouldThrowOnOverbooking() {
            LeaveAllowance allowance = LeaveAllowanceMother.freshAllowance();

            allowance.reserveDays(20);

            IllegalStateException ex = assertThrows(IllegalStateException.class,
                    () -> allowance.reserveDays(6));
            assertTrue(ex.getMessage().startsWith(LeaveAllowance.INSUFFICIENT_BALANCE));
        }

        @Test
        @DisplayName("Should consider daysUsed in over-booking check")
        void shouldConsiderDaysUsedInOverbookingCheck() {
            LeaveAllowance allowance = LeaveAllowanceMother.partiallyUsedAllowance(10, 10);

            assertDoesNotThrow(() -> allowance.reserveDays(5));
            assertThrows(IllegalStateException.class, () -> allowance.reserveDays(1));
        }

        @Test
        @DisplayName("Should reject zero days")
        void shouldRejectZeroDays() {
            LeaveAllowance allowance = LeaveAllowanceMother.freshAllowance();

            IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                    () -> allowance.reserveDays(0));
            assertEquals(LeaveAllowance.DAYS_MUST_BE_POSITIVE, ex.getMessage());
        }

        @Test
        @DisplayName("Should reject negative days")
        void shouldRejectNegativeDays() {
            LeaveAllowance allowance = LeaveAllowanceMother.freshAllowance();

            assertThrows(IllegalArgumentException.class, () -> allowance.reserveDays(-1));
        }
    }

    @Nested
    @DisplayName("confirmDays() command")
    class ConfirmDays {

        @Test
        @DisplayName("Should move days from pending to used")
        void shouldMoveDaysFromPendingToUsed() {
            LeaveAllowance allowance = LeaveAllowanceMother.partiallyUsedAllowance(0, 5);

            allowance.confirmDays(5);

            assertEquals(5, allowance.daysUsed());
            assertEquals(0, allowance.daysPending());
        }

        @Test
        @DisplayName("Should throw when confirming more days than pending")
        void shouldThrowWhenConfirmingMoreThanPending() {
            LeaveAllowance allowance = LeaveAllowanceMother.partiallyUsedAllowance(0, 3);

            IllegalStateException ex = assertThrows(IllegalStateException.class,
                    () -> allowance.confirmDays(5));
            assertEquals(LeaveAllowance.CANNOT_RELEASE_MORE_THAN_PENDING, ex.getMessage());
        }

        @Test
        @DisplayName("Should reject zero days")
        void shouldRejectZeroDays() {
            LeaveAllowance allowance = LeaveAllowanceMother.partiallyUsedAllowance(0, 5);

            assertThrows(IllegalArgumentException.class, () -> allowance.confirmDays(0));
        }
    }

    @Nested
    @DisplayName("releasePendingDays() command")
    class ReleasePendingDays {

        @Test
        @DisplayName("Should reduce days pending")
        void shouldReduceDaysPending() {
            LeaveAllowance allowance = LeaveAllowanceMother.partiallyUsedAllowance(0, 5);

            allowance.releasePendingDays(3);

            assertEquals(2, allowance.daysPending());
        }

        @Test
        @DisplayName("Should throw when releasing more than pending")
        void shouldThrowWhenReleasingMoreThanPending() {
            LeaveAllowance allowance = LeaveAllowanceMother.partiallyUsedAllowance(0, 3);

            IllegalStateException ex = assertThrows(IllegalStateException.class,
                    () -> allowance.releasePendingDays(5));
            assertEquals(LeaveAllowance.CANNOT_RELEASE_MORE_THAN_PENDING, ex.getMessage());
        }

        @Test
        @DisplayName("Should reject zero days")
        void shouldRejectZeroDays() {
            LeaveAllowance allowance = LeaveAllowanceMother.partiallyUsedAllowance(0, 5);

            assertThrows(IllegalArgumentException.class, () -> allowance.releasePendingDays(0));
        }
    }

    @Nested
    @DisplayName("creditBackDays() command")
    class CreditBackDays {

        @Test
        @DisplayName("Should reduce days used")
        void shouldReduceDaysUsed() {
            LeaveAllowance allowance = LeaveAllowanceMother.partiallyUsedAllowance(10, 0);

            allowance.creditBackDays(5);

            assertEquals(5, allowance.daysUsed());
        }

        @Test
        @DisplayName("Should throw when crediting back more than used")
        void shouldThrowWhenCreditingMoreThanUsed() {
            LeaveAllowance allowance = LeaveAllowanceMother.partiallyUsedAllowance(3, 0);

            IllegalStateException ex = assertThrows(IllegalStateException.class,
                    () -> allowance.creditBackDays(5));
            assertEquals(LeaveAllowance.CANNOT_CREDIT_MORE_THAN_USED, ex.getMessage());
        }

        @Test
        @DisplayName("Should reject zero days")
        void shouldRejectZeroDays() {
            LeaveAllowance allowance = LeaveAllowanceMother.partiallyUsedAllowance(10, 0);

            assertThrows(IllegalArgumentException.class, () -> allowance.creditBackDays(0));
        }
    }

    @Nested
    @DisplayName("amendEntitlement() command")
    class AmendEntitlement {

        @Test
        @DisplayName("Should update total entitlement")
        void shouldUpdateEntitlement() {
            LeaveAllowance allowance = LeaveAllowanceMother.freshAllowance();

            allowance.amendEntitlement(30);

            assertEquals(30, allowance.totalEntitlement());
        }

        @Test
        @DisplayName("Should allow reducing to daysUsed exactly")
        void shouldAllowReducingToDaysUsed() {
            LeaveAllowance allowance = LeaveAllowanceMother.partiallyUsedAllowance(10, 0);

            allowance.amendEntitlement(10);

            assertEquals(10, allowance.totalEntitlement());
        }

        @Test
        @DisplayName("Should reject new entitlement below days used")
        void shouldRejectEntitlementBelowDaysUsed() {
            LeaveAllowance allowance = LeaveAllowanceMother.partiallyUsedAllowance(10, 0);

            IllegalStateException ex = assertThrows(IllegalStateException.class,
                    () -> allowance.amendEntitlement(9));
            assertEquals(LeaveAllowance.NEW_ENTITLEMENT_TOO_LOW, ex.getMessage());
        }

        @Test
        @DisplayName("Should reject zero entitlement")
        void shouldRejectZeroEntitlement() {
            LeaveAllowance allowance = LeaveAllowanceMother.freshAllowance();

            assertThrows(IllegalArgumentException.class, () -> allowance.amendEntitlement(0));
        }

        @Test
        @DisplayName("Should reject negative entitlement")
        void shouldRejectNegativeEntitlement() {
            LeaveAllowance allowance = LeaveAllowanceMother.freshAllowance();

            assertThrows(IllegalArgumentException.class, () -> allowance.amendEntitlement(-5));
        }
    }

    @Nested
    @DisplayName("updateStaffDetails() command")
    class UpdateStaffDetails {

        @Test
        @DisplayName("Should update manager and department")
        void shouldUpdateManagerAndDepartment() {
            LeaveAllowance allowance = LeaveAllowanceMother.freshAllowance();

            allowance.updateStaffDetails("new-manager-id", "Finance");

            assertEquals("new-manager-id", allowance.managerId());
            assertEquals("Finance", allowance.department());
        }

        @Test
        @DisplayName("Should reject blank manager ID")
        void shouldRejectBlankManagerId() {
            LeaveAllowance allowance = LeaveAllowanceMother.freshAllowance();

            assertThrows(IllegalArgumentException.class,
                    () -> allowance.updateStaffDetails("  ", "Finance"));
        }
    }

    @Nested
    @DisplayName("Derived accessors")
    class DerivedAccessors {

        @Test
        @DisplayName("remainingDays() should return entitlement minus days used")
        void remainingDaysShouldExcludeUsed() {
            LeaveAllowance allowance = LeaveAllowanceMother.partiallyUsedAllowance(10, 3);

            assertEquals(15, allowance.remainingDays());
        }

        @Test
        @DisplayName("availableDays() should return entitlement minus used and pending")
        void availableDaysShouldExcludeUsedAndPending() {
            LeaveAllowance allowance = LeaveAllowanceMother.partiallyUsedAllowance(10, 3);

            assertEquals(12, allowance.availableDays());
        }

        @Test
        @DisplayName("Fresh allowance should have full entitlement available")
        void freshAllowanceShouldHaveFullEntitlement() {
            LeaveAllowance allowance = LeaveAllowanceMother.freshAllowance();

            assertEquals(25, allowance.remainingDays());
            assertEquals(25, allowance.availableDays());
        }
    }
}
