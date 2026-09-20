package com.staffs.leavebooking.leavemanagement.application.handlers;

import com.staffs.leavebooking.leavemanagement.application.commands.AmendEntitlementCommand;
import com.staffs.leavebooking.leavemanagement.infrastructure.entities.LeaveAllowanceJpa;
import com.staffs.leavebooking.leavemanagement.infrastructure.repositories.LeaveAllowanceRepository;
import com.staffs.leavebooking.leavemanagement.ui.exceptions.LeaveAllowanceNotFoundException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("LeaveAllowanceApplicationService (CQRS Command Handler)")
class LeaveAllowanceApplicationServiceTest {

    @Mock
    private LeaveAllowanceRepository leaveAllowanceRepository;

    @InjectMocks
    private LeaveAllowanceApplicationService service;

    private static final String STAFF_MEMBER_ID = "11111111-1111-1111-1111-111111111111";
    private static final String ALLOWANCE_ID = "aaaaaaaa-bbbb-cccc-dddd-eeeeeeeeeeee";

    @Nested
    @DisplayName("reserveDays")
    class ReserveDays {

        @Test
        @DisplayName("Should increment daysPending when reserving days")
        void shouldReserveDays() {
            LeaveAllowanceJpa jpa = createAllowanceJpa(25, 5, 3);
            when(leaveAllowanceRepository.findFirstByStaffMemberIdOrderByBusinessYearStartDesc(STAFF_MEMBER_ID))
                    .thenReturn(Optional.of(jpa));

            service.reserveDays(STAFF_MEMBER_ID, 4);

            verify(leaveAllowanceRepository).save(jpa);
            assertThat(jpa.getDaysPending()).isEqualTo(7);
            assertThat(jpa.getDaysUsed()).isEqualTo(5);
        }

        @Test
        @DisplayName("Should throw when insufficient balance for reservation")
        void shouldThrowWhenInsufficientBalance() {
            LeaveAllowanceJpa jpa = createAllowanceJpa(25, 20, 4);
            when(leaveAllowanceRepository.findFirstByStaffMemberIdOrderByBusinessYearStartDesc(STAFF_MEMBER_ID))
                    .thenReturn(Optional.of(jpa));

            assertThatThrownBy(() -> service.reserveDays(STAFF_MEMBER_ID, 5))
                    .isInstanceOf(IllegalStateException.class);

            verify(leaveAllowanceRepository, never()).save(any());
        }

        @Test
        @DisplayName("Should throw LeaveAllowanceNotFoundException when staff has no allowance")
        void shouldThrowWhenNoAllowance() {
            when(leaveAllowanceRepository.findFirstByStaffMemberIdOrderByBusinessYearStartDesc(STAFF_MEMBER_ID))
                    .thenReturn(Optional.empty());

            assertThatThrownBy(() -> service.reserveDays(STAFF_MEMBER_ID, 3))
                    .isInstanceOf(LeaveAllowanceNotFoundException.class);
        }
    }

    @Nested
    @DisplayName("confirmDays")
    class ConfirmDays {

        @Test
        @DisplayName("Should move days from pending to used")
        void shouldConfirmDays() {
            LeaveAllowanceJpa jpa = createAllowanceJpa(25, 5, 3);
            when(leaveAllowanceRepository.findFirstByStaffMemberIdOrderByBusinessYearStartDesc(STAFF_MEMBER_ID))
                    .thenReturn(Optional.of(jpa));

            service.confirmDays(STAFF_MEMBER_ID, 3);

            verify(leaveAllowanceRepository).save(jpa);
            assertThat(jpa.getDaysPending()).isEqualTo(0);
            assertThat(jpa.getDaysUsed()).isEqualTo(8);
        }

        @Test
        @DisplayName("Should throw when confirming more than pending")
        void shouldThrowWhenConfirmingMoreThanPending() {
            LeaveAllowanceJpa jpa = createAllowanceJpa(25, 5, 3);
            when(leaveAllowanceRepository.findFirstByStaffMemberIdOrderByBusinessYearStartDesc(STAFF_MEMBER_ID))
                    .thenReturn(Optional.of(jpa));

            assertThatThrownBy(() -> service.confirmDays(STAFF_MEMBER_ID, 5))
                    .isInstanceOf(IllegalStateException.class);
        }
    }

    @Nested
    @DisplayName("releasePendingDays")
    class ReleasePendingDays {

        @Test
        @DisplayName("Should decrement daysPending when releasing")
        void shouldReleasePendingDays() {
            LeaveAllowanceJpa jpa = createAllowanceJpa(25, 5, 4);
            when(leaveAllowanceRepository.findFirstByStaffMemberIdOrderByBusinessYearStartDesc(STAFF_MEMBER_ID))
                    .thenReturn(Optional.of(jpa));

            service.releasePendingDays(STAFF_MEMBER_ID, 4);

            verify(leaveAllowanceRepository).save(jpa);
            assertThat(jpa.getDaysPending()).isEqualTo(0);
            assertThat(jpa.getDaysUsed()).isEqualTo(5);
        }
    }

    @Nested
    @DisplayName("creditBackDays")
    class CreditBackDays {

        @Test
        @DisplayName("Should decrement daysUsed when crediting back")
        void shouldCreditBackDays() {
            LeaveAllowanceJpa jpa = createAllowanceJpa(25, 10, 0);
            when(leaveAllowanceRepository.findFirstByStaffMemberIdOrderByBusinessYearStartDesc(STAFF_MEMBER_ID))
                    .thenReturn(Optional.of(jpa));

            service.creditBackDays(STAFF_MEMBER_ID, 5);

            verify(leaveAllowanceRepository).save(jpa);
            assertThat(jpa.getDaysUsed()).isEqualTo(5);
        }
    }

    @Nested
    @DisplayName("amendEntitlement")
    class AmendEntitlement {

        @Test
        @DisplayName("Should update total entitlement")
        void shouldAmendEntitlement() {
            LeaveAllowanceJpa jpa = createAllowanceJpa(25, 5, 0);
            jpa.setId(ALLOWANCE_ID);
            when(leaveAllowanceRepository.findById(ALLOWANCE_ID)).thenReturn(Optional.of(jpa));

            AmendEntitlementCommand command = new AmendEntitlementCommand(ALLOWANCE_ID, 30);

            service.amendEntitlement(command);

            verify(leaveAllowanceRepository).save(jpa);
            assertThat(jpa.getTotalEntitlement()).isEqualTo(30);
        }

        @Test
        @DisplayName("Should throw LeaveAllowanceNotFoundException when ID does not exist")
        void shouldThrowWhenNotFound() {
            when(leaveAllowanceRepository.findById("99999999-9999-9999-9999-999999999999")).thenReturn(Optional.empty());
            AmendEntitlementCommand command = new AmendEntitlementCommand("99999999-9999-9999-9999-999999999999", 30);

            assertThatThrownBy(() -> service.amendEntitlement(command))
                    .isInstanceOf(LeaveAllowanceNotFoundException.class);
        }

        @Test
        @DisplayName("Should throw when new entitlement is below days used")
        void shouldThrowWhenBelowDaysUsed() {
            LeaveAllowanceJpa jpa = createAllowanceJpa(25, 10, 0);
            jpa.setId(ALLOWANCE_ID);
            when(leaveAllowanceRepository.findById(ALLOWANCE_ID)).thenReturn(Optional.of(jpa));

            AmendEntitlementCommand command = new AmendEntitlementCommand(ALLOWANCE_ID, 5);

            assertThatThrownBy(() -> service.amendEntitlement(command))
                    .isInstanceOf(IllegalStateException.class);
        }
    }

    @Nested
    @DisplayName("createAllowanceForNewStaff")
    class CreateAllowanceForNewStaff {

        @Test
        @DisplayName("Should create a new allowance for new staff member")
        void shouldCreateAllowance() {
            int currentYear = LocalDate.now().getYear();
            when(leaveAllowanceRepository.existsByStaffMemberIdAndBusinessYearStart(STAFF_MEMBER_ID, currentYear))
                    .thenReturn(false);

            service.createAllowanceForNewStaff(
                    STAFF_MEMBER_ID, "22222222-2222-2222-2222-222222222222", "John", "Smith", "Engineering", 25);

            verify(leaveAllowanceRepository).save(any(LeaveAllowanceJpa.class));
        }

        @Test
        @DisplayName("Should skip creation if allowance already exists (idempotency)")
        void shouldSkipIfAlreadyExists() {
            int currentYear = LocalDate.now().getYear();
            when(leaveAllowanceRepository.existsByStaffMemberIdAndBusinessYearStart(STAFF_MEMBER_ID, currentYear))
                    .thenReturn(true);

            service.createAllowanceForNewStaff(
                    STAFF_MEMBER_ID, "22222222-2222-2222-2222-222222222222", "John", "Smith", "Engineering", 25);

            verify(leaveAllowanceRepository, never()).save(any());
        }
    }

    @Nested
    @DisplayName("updateStaffDetails")
    class UpdateStaffDetails {

        @Test
        @DisplayName("Should update managerId and department on allowance")
        void shouldUpdateDetails() {
            LeaveAllowanceJpa jpa = createAllowanceJpa(25, 5, 0);
            when(leaveAllowanceRepository.findFirstByStaffMemberIdOrderByBusinessYearStartDesc(STAFF_MEMBER_ID))
                    .thenReturn(Optional.of(jpa));

            service.updateStaffDetails(STAFF_MEMBER_ID, "33333333-3333-3333-3333-333333333333", "Marketing");

            verify(leaveAllowanceRepository).save(jpa);
            assertThat(jpa.getManagerId()).isEqualTo("33333333-3333-3333-3333-333333333333");
            assertThat(jpa.getDepartment()).isEqualTo("Marketing");
        }

        @Test
        @DisplayName("Should throw when staff member has no allowance")
        void shouldThrowWhenNoAllowance() {
            when(leaveAllowanceRepository.findFirstByStaffMemberIdOrderByBusinessYearStartDesc("44444444-4444-4444-4444-444444444444"))
                    .thenReturn(Optional.empty());

            assertThatThrownBy(() -> service.updateStaffDetails("44444444-4444-4444-4444-444444444444", "22222222-2222-2222-2222-222222222222", "HR"))
                    .isInstanceOf(LeaveAllowanceNotFoundException.class);
        }
    }

    private LeaveAllowanceJpa createAllowanceJpa(int entitlement, int used, int pending) {
        LeaveAllowanceJpa jpa = new LeaveAllowanceJpa();
        jpa.setId(ALLOWANCE_ID);
        jpa.setStaffMemberId(STAFF_MEMBER_ID);
        jpa.setManagerId("22222222-2222-2222-2222-222222222222");
        jpa.setFirstName("John");
        jpa.setSurname("Smith");
        jpa.setDepartment("Engineering");
        jpa.setBusinessYearStart(LocalDate.now().getYear());
        jpa.setBusinessYearEnd(LocalDate.now().getYear() + 1);
        jpa.setTotalEntitlement(entitlement);
        jpa.setDaysUsed(used);
        jpa.setDaysPending(pending);
        return jpa;
    }
}
