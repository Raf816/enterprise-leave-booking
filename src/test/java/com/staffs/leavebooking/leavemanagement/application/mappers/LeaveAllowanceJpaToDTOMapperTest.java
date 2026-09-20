package com.staffs.leavebooking.leavemanagement.application.mappers;

import com.staffs.leavebooking.leavemanagement.application.dto.LeaveAllowanceDTO;
import com.staffs.leavebooking.leavemanagement.infrastructure.entities.LeaveAllowanceJpa;
import com.staffs.leavebooking.testfixtures.JpaEntityMother;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("LeaveAllowanceJpaToDTOMapper")
class LeaveAllowanceJpaToDTOMapperTest {

    @Nested
    @DisplayName("toDTO()")
    class ToDTO {

        @Test
        @DisplayName("Should map all basic fields correctly")
        void shouldMapBasicFields() {
            LeaveAllowanceJpa jpa = JpaEntityMother.leaveAllowanceJpa();

            LeaveAllowanceDTO dto = LeaveAllowanceJpaToDTOMapper.toDTO(jpa);

            assertEquals(jpa.getId(), dto.id());
            assertEquals(jpa.getStaffMemberId(), dto.staffMemberId());
            assertEquals(jpa.getManagerId(), dto.managerId());
            assertEquals(jpa.getDepartment(), dto.department());
            assertEquals(25, dto.totalEntitlement());
            assertEquals(5, dto.daysUsed());
            assertEquals(3, dto.daysPending());
        }

        @Test
        @DisplayName("Should calculate remainingDays as entitlement minus daysUsed")
        void shouldCalculateRemainingDays() {
            LeaveAllowanceJpa jpa = JpaEntityMother.leaveAllowanceJpa();

            LeaveAllowanceDTO dto = LeaveAllowanceJpaToDTOMapper.toDTO(jpa);

            assertEquals(20, dto.remainingDays());
        }

        @Test
        @DisplayName("Should calculate availableDays as entitlement minus used minus pending")
        void shouldCalculateAvailableDays() {
            LeaveAllowanceJpa jpa = JpaEntityMother.leaveAllowanceJpa();

            LeaveAllowanceDTO dto = LeaveAllowanceJpaToDTOMapper.toDTO(jpa);

            assertEquals(17, dto.availableDays());
        }

        @Test
        @DisplayName("Should format staffName as 'firstName surname'")
        void shouldFormatStaffName() {
            LeaveAllowanceJpa jpa = JpaEntityMother.leaveAllowanceJpa();
            jpa.setFirstName("Alice");
            jpa.setSurname("Johnson");

            LeaveAllowanceDTO dto = LeaveAllowanceJpaToDTOMapper.toDTO(jpa);

            assertEquals("Alice Johnson", dto.staffName());
        }

        @Test
        @DisplayName("Should format businessYear as 'start-end' string")
        void shouldFormatBusinessYear() {
            LeaveAllowanceJpa jpa = JpaEntityMother.leaveAllowanceJpa();
            jpa.setBusinessYearStart(2026);
            jpa.setBusinessYearEnd(2027);

            LeaveAllowanceDTO dto = LeaveAllowanceJpaToDTOMapper.toDTO(jpa);

            assertEquals("2026-2027", dto.businessYear());
        }

        @Test
        @DisplayName("Should throw NullPointerException for null JPA entity")
        void shouldThrowForNullJpa() {
            assertThrows(NullPointerException.class,
                    () -> LeaveAllowanceJpaToDTOMapper.toDTO(null));
        }
    }
}
