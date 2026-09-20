package com.staffs.leavebooking.leavemanagement.application.mappers;

import com.staffs.leavebooking.leavemanagement.domain.BusinessYear;
import com.staffs.leavebooking.leavemanagement.domain.LeaveAllowance;
import com.staffs.leavebooking.leavemanagement.infrastructure.entities.LeaveAllowanceJpa;
import com.staffs.leavebooking.testfixtures.JpaEntityMother;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
@DisplayName("LeaveAllowanceJpaToDomainMapper")
class LeaveAllowanceJpaToDomainMapperTest {

    @Nested
    @DisplayName("toDomain()")
    class ToDomain {

        @Test
        @DisplayName("Should map all fields correctly")
        void shouldMapAllFields() {
            LeaveAllowanceJpa jpa = JpaEntityMother.leaveAllowanceJpa();

            LeaveAllowance domain = LeaveAllowanceJpaToDomainMapper.toDomain(jpa);

            assertEquals(jpa.getId(), domain.id().id());
            assertEquals(jpa.getStaffMemberId(), domain.staffMemberId());
            assertEquals(jpa.getManagerId(), domain.managerId());
            assertEquals(jpa.getFirstName(), domain.firstName());
            assertEquals(jpa.getSurname(), domain.surname());
            assertEquals(jpa.getDepartment(), domain.department());
            assertEquals(new BusinessYear(2026, 2027), domain.businessYear());
            assertEquals(25, domain.totalEntitlement());
            assertEquals(5, domain.daysUsed());
            assertEquals(3, domain.daysPending());
        }

        @Test
        @DisplayName("Should not raise events (uses reconstitute)")
        void shouldNotRaiseEvents() {
            LeaveAllowanceJpa jpa = JpaEntityMother.leaveAllowanceJpa();

            LeaveAllowance domain = LeaveAllowanceJpaToDomainMapper.toDomain(jpa);

            assertTrue(domain.listOfDomainEvents().isEmpty());
        }

        @Test
        @DisplayName("Should throw NullPointerException for null JPA entity")
        void shouldThrowForNullJpa() {
            assertThrows(NullPointerException.class,
                    () -> LeaveAllowanceJpaToDomainMapper.toDomain(null));
        }
    }
}
