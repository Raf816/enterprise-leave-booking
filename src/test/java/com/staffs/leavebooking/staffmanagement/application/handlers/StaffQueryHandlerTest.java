package com.staffs.leavebooking.staffmanagement.application.handlers;

import com.staffs.leavebooking.staffmanagement.application.dto.StaffMemberDTO;
import com.staffs.leavebooking.staffmanagement.infrastructure.entities.StaffMemberJpa;
import com.staffs.leavebooking.staffmanagement.infrastructure.repositories.StaffMemberRepository;
import com.staffs.leavebooking.staffmanagement.ui.exceptions.StaffMemberNotFoundException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Staff Query Handler")
class StaffQueryHandlerTest {

    @Mock
    private StaffMemberRepository staffMemberRepository;

    @InjectMocks
    private StaffQueryHandler queryHandler;

    @Nested
    @DisplayName("findAllStaffMembers")
    class FindAll {

        @Test
        @DisplayName("Should return all staff members as DTOs")
        void shouldReturnAllStaffMembers() {
            when(staffMemberRepository.findAll())
                    .thenReturn(List.of(
                            createTestStaffJpa("staff-1", "James", "Wilson"),
                            createTestStaffJpa("staff-2", "Emily", "Chen")
                    ));

            List<StaffMemberDTO> result = queryHandler.findAllStaffMembers();

            assertEquals(2, result.size());
            assertEquals("James", result.get(0).firstName());
            assertEquals("Emily", result.get(1).firstName());
        }

        @Test
        @DisplayName("Should return empty list when no staff exist")
        void shouldReturnEmptyWhenNoStaff() {
            when(staffMemberRepository.findAll()).thenReturn(Collections.emptyList());

            List<StaffMemberDTO> result = queryHandler.findAllStaffMembers();

            assertTrue(result.isEmpty());
        }
    }

    @Nested
    @DisplayName("findStaffMemberById")
    class FindById {

        @Test
        @DisplayName("Should return a single staff member DTO")
        void shouldReturnStaffById() {
            when(staffMemberRepository.findById("staff-1"))
                    .thenReturn(Optional.of(createTestStaffJpa("staff-1", "James", "Wilson")));

            StaffMemberDTO result = queryHandler.findStaffMemberById("staff-1");

            assertEquals("staff-1", result.id());
            assertEquals("James", result.firstName());
            assertEquals("Wilson", result.surname());
            assertEquals("Engineering", result.department());
        }

        @Test
        @DisplayName("Should throw StaffMemberNotFoundException when not found")
        void shouldThrowWhenNotFound() {
            when(staffMemberRepository.findById("unknown"))
                    .thenReturn(Optional.empty());

            assertThrows(StaffMemberNotFoundException.class,
                    () -> queryHandler.findStaffMemberById("unknown"));
        }
    }

    @Nested
    @DisplayName("findByDepartment")
    class FindByDepartment {

        @Test
        @DisplayName("Should filter staff by department")
        void shouldFilterByDepartment() {
            when(staffMemberRepository.findByDepartment("Engineering"))
                    .thenReturn(List.of(createTestStaffJpa("staff-1", "James", "Wilson")));

            List<StaffMemberDTO> result = queryHandler.findByDepartment("Engineering");

            assertEquals(1, result.size());
            assertEquals("Engineering", result.get(0).department());
        }
    }

    @Nested
    @DisplayName("findByStatus")
    class FindByStatus {

        @Test
        @DisplayName("Should filter staff by employment status")
        void shouldFilterByStatus() {
            when(staffMemberRepository.findByEmploymentStatus("ACTIVE"))
                    .thenReturn(List.of(createTestStaffJpa("staff-1", "James", "Wilson")));

            List<StaffMemberDTO> result = queryHandler.findByStatus("ACTIVE");

            assertEquals(1, result.size());
            verify(staffMemberRepository).findByEmploymentStatus("ACTIVE");
        }
    }

    @Nested
    @DisplayName("findByManagerId")
    class FindByManagerId {

        @Test
        @DisplayName("Should return staff managed by a specific manager")
        void shouldReturnStaffForManager() {
            when(staffMemberRepository.findByLineManagerId("mgr-1"))
                    .thenReturn(List.of(
                            createTestStaffJpa("staff-1", "James", "Wilson"),
                            createTestStaffJpa("staff-2", "Emily", "Chen")
                    ));

            List<StaffMemberDTO> result = queryHandler.findByManagerId("mgr-1");

            assertEquals(2, result.size());
            verify(staffMemberRepository).findByLineManagerId("mgr-1");
        }
    }

    private StaffMemberJpa createTestStaffJpa(String id, String firstName, String surname) {
        StaffMemberJpa jpa = new StaffMemberJpa();
        jpa.setId(id);
        jpa.setFirstName(firstName);
        jpa.setSurname(surname);
        jpa.setEmail(firstName.toLowerCase() + "." + surname.toLowerCase() + "@company.com");
        jpa.setDepartment("Engineering");
        jpa.setLineManagerId("mgr-1");
        jpa.setHireDate(LocalDate.of(2022, 6, 1));
        jpa.setCurrentRole("Software Engineer");
        jpa.setStartDateCurrentRole(LocalDate.of(2022, 6, 1));
        jpa.setJobLevel("L4");
        jpa.setEmploymentType("FULL_TIME");
        jpa.setEmploymentStatus("ACTIVE");
        return jpa;
    }
}
