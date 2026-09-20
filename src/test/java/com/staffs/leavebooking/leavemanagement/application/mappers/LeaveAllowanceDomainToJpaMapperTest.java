package com.staffs.leavebooking.leavemanagement.application.mappers;

import com.staffs.leavebooking.leavemanagement.domain.LeaveAllowance;
import com.staffs.leavebooking.leavemanagement.infrastructure.entities.LeaveAllowanceJpa;
import com.staffs.leavebooking.testfixtures.JpaEntityMother;
import com.staffs.leavebooking.testfixtures.LeaveAllowanceMother;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("LeaveAllowanceDomainToJpaMapper")
class LeaveAllowanceDomainToJpaMapperTest {

    @Nested
    @DisplayName("toJpa()")
    class ToJpa {

        @Test
        @DisplayName("Should map all fields correctly")
        void shouldMapAllFields() {
            LeaveAllowance domain = LeaveAllowanceMother.partiallyUsedAllowance(10, 3);

            LeaveAllowanceJpa jpa = LeaveAllowanceDomainToJpaMapper.toJpa(domain);

            assertEquals(domain.id().id(), jpa.getId());
            assertEquals(domain.staffMemberId(), jpa.getStaffMemberId());
            assertEquals(domain.managerId(), jpa.getManagerId());
            assertEquals(domain.firstName(), jpa.getFirstName());
            assertEquals(domain.surname(), jpa.getSurname());
            assertEquals(domain.department(), jpa.getDepartment());
            assertEquals(domain.businessYear().startYear(), jpa.getBusinessYearStart());
            assertEquals(domain.businessYear().endYear(), jpa.getBusinessYearEnd());
            assertEquals(25, jpa.getTotalEntitlement());
            assertEquals(10, jpa.getDaysUsed());
            assertEquals(3, jpa.getDaysPending());
        }

        @Test
        @DisplayName("Should throw NullPointerException for null domain")
        void shouldThrowForNullDomain() {
            assertThrows(NullPointerException.class,
                    () -> LeaveAllowanceDomainToJpaMapper.toJpa(null));
        }
    }

    @Nested
    @DisplayName("updateJpa()")
    class UpdateJpa {

        @Test
        @DisplayName("Should update mutable fields on existing JPA entity")
        void shouldUpdateMutableFields() {
            LeaveAllowance domain = LeaveAllowanceMother.partiallyUsedAllowance(8, 2);
            LeaveAllowanceJpa jpa = JpaEntityMother.leaveAllowanceJpa();
            String originalId = jpa.getId();
            String originalStaffId = jpa.getStaffMemberId();

            LeaveAllowanceDomainToJpaMapper.updateJpa(domain, jpa);

            assertEquals(originalId, jpa.getId());
            assertEquals(originalStaffId, jpa.getStaffMemberId());
            assertEquals(domain.managerId(), jpa.getManagerId());
            assertEquals(domain.firstName(), jpa.getFirstName());
            assertEquals(domain.surname(), jpa.getSurname());
            assertEquals(domain.department(), jpa.getDepartment());
            assertEquals(25, jpa.getTotalEntitlement());
            assertEquals(8, jpa.getDaysUsed());
            assertEquals(2, jpa.getDaysPending());
        }

        @Test
        @DisplayName("Should throw NullPointerException for null domain")
        void shouldThrowForNullDomain() {
            LeaveAllowanceJpa jpa = JpaEntityMother.leaveAllowanceJpa();

            assertThrows(NullPointerException.class,
                    () -> LeaveAllowanceDomainToJpaMapper.updateJpa(null, jpa));
        }

        @Test
        @DisplayName("Should throw NullPointerException for null JPA entity")
        void shouldThrowForNullJpa() {
            LeaveAllowance domain = LeaveAllowanceMother.freshAllowance();

            assertThrows(NullPointerException.class,
                    () -> LeaveAllowanceDomainToJpaMapper.updateJpa(domain, null));
        }
    }
}
