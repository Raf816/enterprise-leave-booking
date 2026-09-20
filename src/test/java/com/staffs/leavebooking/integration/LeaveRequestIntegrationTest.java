package com.staffs.leavebooking.integration;

import com.staffs.leavebooking.common.events.DomainEventManager;
import com.staffs.leavebooking.common.events.EventStoreService;
import com.staffs.leavebooking.leavemanagement.application.commands.AmendEntitlementCommand;
import com.staffs.leavebooking.leavemanagement.application.commands.CancelLeaveRequestCommand;
import com.staffs.leavebooking.leavemanagement.application.commands.SubmitLeaveRequestCommand;
import com.staffs.leavebooking.leavemanagement.application.handlers.LeaveAllowanceApplicationService;
import com.staffs.leavebooking.leavemanagement.application.handlers.LeaveRequestApplicationService;
import com.staffs.leavebooking.leavemanagement.application.listeners.LeaveRequestSubmittedListener;
import com.staffs.leavebooking.leavemanagement.application.listeners.LeaveRequestApprovedListener;
import com.staffs.leavebooking.leavemanagement.application.listeners.LeaveRequestRejectedListener;
import com.staffs.leavebooking.leavemanagement.application.listeners.LeaveRequestCancelledListener;
import com.staffs.leavebooking.leavemanagement.infrastructure.entities.LeaveAllowanceJpa;
import com.staffs.leavebooking.leavemanagement.infrastructure.entities.LeaveRequestJpa;
import com.staffs.leavebooking.leavemanagement.infrastructure.repositories.LeaveAllowanceRepository;
import com.staffs.leavebooking.leavemanagement.infrastructure.repositories.LeaveRequestRepository;
import org.junit.jupiter.api.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@Import({
        LeaveRequestApplicationService.class,
        LeaveAllowanceApplicationService.class,
        DomainEventManager.class,
        EventStoreService.class,
        JacksonAutoConfiguration.class,
        LeaveRequestSubmittedListener.class,
        LeaveRequestApprovedListener.class,
        LeaveRequestRejectedListener.class,
        LeaveRequestCancelledListener.class
})
@ActiveProfiles("test")
@DisplayName("Leave Request Integration Tests (@DataJpaTest)")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class LeaveRequestIntegrationTest {

    @Autowired
    private LeaveRequestApplicationService leaveRequestService;

    @Autowired
    private LeaveAllowanceApplicationService leaveAllowanceService;

    @Autowired
    private LeaveRequestRepository leaveRequestRepository;

    @Autowired
    private LeaveAllowanceRepository leaveAllowanceRepository;

    private static final String STAFF_MEMBER_ID = "int-test-staff-001";
    private static final String MANAGER_ID = "int-test-mgr-001";

    @BeforeEach
    void setUp() {
        leaveRequestRepository.findByStaffMemberId(STAFF_MEMBER_ID)
                .forEach(leaveRequestRepository::delete);
        leaveAllowanceRepository.findFirstByStaffMemberIdOrderByBusinessYearStartDesc(STAFF_MEMBER_ID)
                .ifPresent(leaveAllowanceRepository::delete);

        LeaveAllowanceJpa allowance = new LeaveAllowanceJpa();
        allowance.setId(UUID.randomUUID().toString());
        allowance.setStaffMemberId(STAFF_MEMBER_ID);
        allowance.setManagerId(MANAGER_ID);
        allowance.setFirstName("Integration");
        allowance.setSurname("Test");
        allowance.setDepartment("Engineering");
        allowance.setBusinessYearStart(LocalDate.now().getYear());
        allowance.setBusinessYearEnd(LocalDate.now().getYear() + 1);
        allowance.setTotalEntitlement(25);
        allowance.setDaysUsed(0);
        allowance.setDaysPending(0);
        leaveAllowanceRepository.save(allowance);
    }

    @Nested
    @DisplayName("Submit Leave Request")
    class SubmitLeaveRequest {

        @Test
        @DisplayName("Should persist leave request with PENDING status")
        void shouldPersistLeaveRequest() {
            LocalDate start = findNextMonday().plusWeeks(2);
            LocalDate end = start.plusDays(4);

            SubmitLeaveRequestCommand command = new SubmitLeaveRequestCommand(
                    STAFF_MEMBER_ID, MANAGER_ID, start, end, "ANNUAL", "Integration test holiday"
            );

            leaveRequestService.submitNewRequest(command);

            List<LeaveRequestJpa> requests = leaveRequestRepository.findByStaffMemberId(STAFF_MEMBER_ID);
            assertFalse(requests.isEmpty(), "At least one leave request should be persisted");

            LeaveRequestJpa saved = requests.get(requests.size() - 1);
            assertEquals("PENDING", saved.getStatus());
            assertEquals(STAFF_MEMBER_ID, saved.getStaffMemberId());
            assertEquals(MANAGER_ID, saved.getManagerId());
            assertEquals("ANNUAL", saved.getLeaveType());
            assertEquals(5, saved.getNumberOfDays());
            assertEquals("Integration test holiday", saved.getReason());
            assertNotNull(saved.getSubmittedOn());
        }

        @Test
        @DisplayName("Should reject leave request with past start date")
        void shouldRejectPastStartDate() {
            LocalDate pastDate = LocalDate.now().minusDays(5);
            SubmitLeaveRequestCommand command = new SubmitLeaveRequestCommand(
                    STAFF_MEMBER_ID, MANAGER_ID, pastDate, pastDate.plusDays(4), "ANNUAL", "Past"
            );

            assertThrows(IllegalArgumentException.class,
                    () -> leaveRequestService.submitNewRequest(command));
        }

        @Test
        @DisplayName("Should reject leave request with end date before start date")
        void shouldRejectEndBeforeStart() {
            LocalDate start = findNextMonday().plusWeeks(2);
            LocalDate end = start.minusDays(1);
            SubmitLeaveRequestCommand command = new SubmitLeaveRequestCommand(
                    STAFF_MEMBER_ID, MANAGER_ID, start, end, "ANNUAL", "Invalid range"
            );

            assertThrows(IllegalArgumentException.class,
                    () -> leaveRequestService.submitNewRequest(command));
        }
    }

    @Nested
    @DisplayName("Approve Leave Request")
    class ApproveLeaveRequest {

        @Test
        @DisplayName("Should transition status to APPROVED with decided metadata")
        void shouldApprove() {
            String requestId = submitTestRequest("Approve test");

            leaveRequestService.approveRequest(requestId, MANAGER_ID, null);

            LeaveRequestJpa approved = leaveRequestRepository.findById(requestId).orElseThrow();
            assertEquals("APPROVED", approved.getStatus());
            assertEquals(MANAGER_ID, approved.getDecidedBy());
            assertNotNull(approved.getDecidedOn());
        }
    }

    @Nested
    @DisplayName("Reject Leave Request")
    class RejectLeaveRequest {

        @Test
        @DisplayName("Should transition status to REJECTED with decided metadata")
        void shouldReject() {
            String requestId = submitTestRequest("Reject test");

            leaveRequestService.rejectRequest(requestId, MANAGER_ID, null);

            LeaveRequestJpa rejected = leaveRequestRepository.findById(requestId).orElseThrow();
            assertEquals("REJECTED", rejected.getStatus());
            assertEquals(MANAGER_ID, rejected.getDecidedBy());
            assertNotNull(rejected.getDecidedOn());
        }
    }

    @Nested
    @DisplayName("Cancel Leave Request")
    class CancelLeaveRequest {

        @Test
        @DisplayName("Should cancel PENDING request with cancellation reason")
        void shouldCancelPending() {
            String requestId = submitTestRequest("Cancel test");

            CancelLeaveRequestCommand cancelCommand = new CancelLeaveRequestCommand(
                    requestId, STAFF_MEMBER_ID, "Changed plans"
            );
            leaveRequestService.cancelRequest(cancelCommand);

            LeaveRequestJpa cancelled = leaveRequestRepository.findById(requestId).orElseThrow();
            assertEquals("CANCELLED", cancelled.getStatus());
            assertEquals("Changed plans", cancelled.getCancellationReason());
        }

        @Test
        @DisplayName("Should cancel APPROVED request with cancellation reason")
        void shouldCancelApproved() {
            String requestId = submitTestRequest("Cancel approved test");
            leaveRequestService.approveRequest(requestId, MANAGER_ID, null);

            CancelLeaveRequestCommand cancelCommand = new CancelLeaveRequestCommand(
                    requestId, STAFF_MEMBER_ID, "No longer needed"
            );
            leaveRequestService.cancelRequest(cancelCommand);

            LeaveRequestJpa cancelled = leaveRequestRepository.findById(requestId).orElseThrow();
            assertEquals("CANCELLED", cancelled.getStatus());
            assertEquals("No longer needed", cancelled.getCancellationReason());
        }
    }

    @Nested
    @DisplayName("Allowance Operations")
    class AllowanceOperations {

        @Test
        @DisplayName("Should reserve pending days (simulates LeaveRequestSubmittedListener)")
        void shouldReserveDays() {
            leaveAllowanceService.reserveDays(STAFF_MEMBER_ID, 5);

            LeaveAllowanceJpa allowance = leaveAllowanceRepository
                    .findFirstByStaffMemberIdOrderByBusinessYearStartDesc(STAFF_MEMBER_ID)
                    .orElseThrow();
            assertEquals(5, allowance.getDaysPending());
            assertEquals(0, allowance.getDaysUsed());
        }

        @Test
        @DisplayName("Should confirm days (simulates LeaveRequestApprovedListener)")
        void shouldConfirmDays() {
            leaveAllowanceService.reserveDays(STAFF_MEMBER_ID, 5);

            leaveAllowanceService.confirmDays(STAFF_MEMBER_ID, 5);

            LeaveAllowanceJpa allowance = leaveAllowanceRepository
                    .findFirstByStaffMemberIdOrderByBusinessYearStartDesc(STAFF_MEMBER_ID)
                    .orElseThrow();
            assertEquals(5, allowance.getDaysUsed());
            assertEquals(0, allowance.getDaysPending());
        }

        @Test
        @DisplayName("Should release pending days (simulates LeaveRequestRejectedListener)")
        void shouldReleasePendingDays() {
            leaveAllowanceService.reserveDays(STAFF_MEMBER_ID, 5);

            leaveAllowanceService.releasePendingDays(STAFF_MEMBER_ID, 5);

            LeaveAllowanceJpa allowance = leaveAllowanceRepository
                    .findFirstByStaffMemberIdOrderByBusinessYearStartDesc(STAFF_MEMBER_ID)
                    .orElseThrow();
            assertEquals(0, allowance.getDaysPending());
            assertEquals(0, allowance.getDaysUsed());
        }

        @Test
        @DisplayName("Should credit back days when approved request is cancelled")
        void shouldCreditBackDays() {
            leaveAllowanceService.reserveDays(STAFF_MEMBER_ID, 5);
            leaveAllowanceService.confirmDays(STAFF_MEMBER_ID, 5);

            leaveAllowanceService.creditBackDays(STAFF_MEMBER_ID, 5);

            LeaveAllowanceJpa allowance = leaveAllowanceRepository
                    .findFirstByStaffMemberIdOrderByBusinessYearStartDesc(STAFF_MEMBER_ID)
                    .orElseThrow();
            assertEquals(0, allowance.getDaysUsed());
            assertEquals(0, allowance.getDaysPending());
        }

        @Test
        @DisplayName("Should create allowance for new staff member")
        void shouldCreateAllowanceForNewStaff() {
            String newStaffId = "new-staff-" + UUID.randomUUID().toString().substring(0, 8);

            leaveAllowanceService.createAllowanceForNewStaff(
                    newStaffId, MANAGER_ID, "New", "Person", "Engineering", 28
            );

            LeaveAllowanceJpa created = leaveAllowanceRepository
                    .findFirstByStaffMemberIdOrderByBusinessYearStartDesc(newStaffId)
                    .orElseThrow();
            assertEquals(28, created.getTotalEntitlement());
            assertEquals(0, created.getDaysUsed());
            assertEquals(0, created.getDaysPending());
            assertEquals("New", created.getFirstName());
            assertEquals("Person", created.getSurname());
        }

        @Test
        @DisplayName("Should not create duplicate allowance (idempotency guard)")
        void shouldNotCreateDuplicateAllowance() {
            String staffId = "idem-staff-" + UUID.randomUUID().toString().substring(0, 8);
            leaveAllowanceService.createAllowanceForNewStaff(
                    staffId, MANAGER_ID, "First", "Call", "Eng", 25
            );

            leaveAllowanceService.createAllowanceForNewStaff(
                    staffId, MANAGER_ID, "First", "Call", "Eng", 25
            );

            List<LeaveAllowanceJpa> all = leaveAllowanceRepository.findAll();
            long count = all.stream().filter(a -> a.getStaffMemberId().equals(staffId)).count();
            assertEquals(1, count);
        }

        @Test
        @DisplayName("Should amend entitlement via admin command")
        void shouldAmendEntitlement() {
            LeaveAllowanceJpa allowance = leaveAllowanceRepository
                    .findFirstByStaffMemberIdOrderByBusinessYearStartDesc(STAFF_MEMBER_ID)
                    .orElseThrow();

            AmendEntitlementCommand command = new AmendEntitlementCommand(
                    allowance.getId(), 30
            );

            leaveAllowanceService.amendEntitlement(command);

            LeaveAllowanceJpa updated = leaveAllowanceRepository.findById(allowance.getId()).orElseThrow();
            assertEquals(30, updated.getTotalEntitlement());
        }
    }

    private String submitTestRequest(String reason) {
        LocalDate start = findNextMonday().plusWeeks(10);
        LocalDate end = start.plusDays(4);

        SubmitLeaveRequestCommand command = new SubmitLeaveRequestCommand(
                STAFF_MEMBER_ID, MANAGER_ID, start, end, "ANNUAL", reason
        );
        leaveRequestService.submitNewRequest(command);

        return leaveRequestRepository.findByStaffMemberId(STAFF_MEMBER_ID).stream()
                .filter(r -> reason.equals(r.getReason()))
                .findFirst()
                .orElseThrow(() -> new AssertionError("Leave request not found after submission"))
                .getId();
    }

    private LocalDate findNextMonday() {
        LocalDate date = LocalDate.now().plusDays(1);
        while (date.getDayOfWeek().getValue() != 1) {
            date = date.plusDays(1);
        }
        return date;
    }
}
