package com.staffs.leavebooking.leavemanagement.application.mappers;

import com.staffs.leavebooking.leavemanagement.application.dto.LeaveRequestDTO;
import com.staffs.leavebooking.leavemanagement.infrastructure.entities.LeaveRequestJpa;
import com.staffs.leavebooking.testfixtures.JpaEntityMother;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
@DisplayName("LeaveRequestJpaToDTOMapper")
class LeaveRequestJpaToDTOMapperTest {

    @Nested
    @DisplayName("toDTO()")
    class ToDTO {

        @Test
        @DisplayName("Should map all fields correctly")
        void shouldMapAllFields() {
            LeaveRequestJpa jpa = JpaEntityMother.leaveRequestJpa();

            LeaveRequestDTO dto = LeaveRequestJpaToDTOMapper.toDTO(jpa);

            assertEquals(jpa.getId(), dto.id());
            assertEquals(jpa.getStaffMemberId(), dto.staffMemberId());
            assertEquals(jpa.getManagerId(), dto.managerId());
            assertEquals("ANNUAL", dto.leaveType());
            assertEquals(jpa.getStartDate(), dto.startDate());
            assertEquals(jpa.getEndDate(), dto.endDate());
            assertEquals(jpa.getNumberOfDays(), dto.numberOfDays());
            assertEquals(jpa.getReason(), dto.reason());
            assertEquals("PENDING", dto.status());
            assertEquals(jpa.getSubmittedOn(), dto.submittedOn());
            assertNull(dto.decidedOn());
            assertNull(dto.decidedBy());
            assertNull(dto.cancellationReason());
        }

        @Test
        @DisplayName("Should throw NullPointerException for null JPA entity")
        void shouldThrowForNullJpa() {
            assertThrows(NullPointerException.class,
                    () -> LeaveRequestJpaToDTOMapper.toDTO(null));
        }
    }
}
