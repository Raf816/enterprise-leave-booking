package com.staffs.leavebooking.integration;

import com.staffs.leavebooking.common.events.DomainEventManager;
import com.staffs.leavebooking.common.events.EventStoreService;
import com.staffs.leavebooking.leavemanagement.application.commands.SubmitLeaveRequestCommand;
import com.staffs.leavebooking.leavemanagement.application.handlers.LeaveAllowanceApplicationService;
import com.staffs.leavebooking.leavemanagement.application.handlers.LeaveRequestApplicationService;
import com.staffs.leavebooking.leavemanagement.application.listeners.LeaveRequestSubmittedListener;
import com.staffs.leavebooking.leavemanagement.infrastructure.entities.LeaveAllowanceJpa;
import com.staffs.leavebooking.leavemanagement.infrastructure.entities.LeaveRequestJpa;
import com.staffs.leavebooking.leavemanagement.infrastructure.repositories.LeaveAllowanceRepository;
import com.staffs.leavebooking.leavemanagement.infrastructure.repositories.LeaveRequestRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@Import({
        LeaveRequestApplicationService.class,
        LeaveAllowanceApplicationService.class,
        DomainEventManager.class,
        EventStoreService.class,
        JacksonAutoConfiguration.class,
        LeaveRequestSubmittedListener.class
})
@ActiveProfiles("test")
@Transactional(propagation = Propagation.NOT_SUPPORTED)
@DisplayName("Atomic Allowance Consistency (BEFORE_COMMIT)")
class AtomicAllowanceConsistencyIntegrationTest {

    @Autowired
    private LeaveRequestApplicationService leaveRequestService;

    @Autowired
    private LeaveAllowanceRepository leaveAllowanceRepository;

    @Autowired
    private LeaveRequestRepository leaveRequestRepository;

    @Autowired
    private TransactionTemplate transactionTemplate;

    private static final String STAFF_ID = "atomic-test-staff";
    private static final String MANAGER_ID = "atomic-test-mgr";

    @BeforeEach
    void setUp() {
        transactionTemplate.executeWithoutResult(status -> {
            leaveRequestRepository.findByStaffMemberId(STAFF_ID)
                    .forEach(leaveRequestRepository::delete);
            leaveAllowanceRepository.findFirstByStaffMemberIdOrderByBusinessYearStartDesc(STAFF_ID)
                    .ifPresent(leaveAllowanceRepository::delete);
        });
    }

    @AfterEach
    void tearDown() {
        transactionTemplate.executeWithoutResult(status -> {
            leaveRequestRepository.findByStaffMemberId(STAFF_ID)
                    .forEach(leaveRequestRepository::delete);
            leaveAllowanceRepository.findFirstByStaffMemberIdOrderByBusinessYearStartDesc(STAFF_ID)
                    .ifPresent(leaveAllowanceRepository::delete);
        });
    }

    @Test
    @DisplayName("Sufficient balance: request + daysPending committed atomically")
    void shouldCommitRequestAndReserveDaysAtomically() {
        transactionTemplate.executeWithoutResult(status -> {
            LeaveAllowanceJpa allowance = createAllowance(25, 0, 0);
            leaveAllowanceRepository.save(allowance);
        });

        LocalDate start = findNextMonday().plusWeeks(20);
        LocalDate end = start.plusDays(4);

        transactionTemplate.executeWithoutResult(status -> {
            leaveRequestService.submitNewRequest(new SubmitLeaveRequestCommand(
                    STAFF_ID, MANAGER_ID, start, end, "ANNUAL", "atomic-success"));
        });

        LeaveAllowanceJpa allowance = leaveAllowanceRepository
                .findFirstByStaffMemberIdOrderByBusinessYearStartDesc(STAFF_ID)
                .orElseThrow();
        assertThat(allowance.getDaysPending()).isEqualTo(5);

        List<LeaveRequestJpa> requests = leaveRequestRepository.findByStaffMemberId(STAFF_ID);
        assertThat(requests).hasSize(1);
        assertThat(requests.get(0).getStatus()).isEqualTo("PENDING");
        assertThat(requests.get(0).getReason()).isEqualTo("atomic-success");
    }

    @Test
    @DisplayName("Insufficient balance: transaction rolls back, no request persisted")
    void shouldRollBackWhenInsufficientAllowance() {
        transactionTemplate.executeWithoutResult(status -> {
            LeaveAllowanceJpa allowance = createAllowance(2, 0, 0);
            leaveAllowanceRepository.save(allowance);
        });

        LocalDate start = findNextMonday().plusWeeks(25);
        LocalDate end = start.plusDays(4);

        assertThrows(IllegalStateException.class, () ->
                transactionTemplate.executeWithoutResult(status -> {
                    leaveRequestService.submitNewRequest(new SubmitLeaveRequestCommand(
                            STAFF_ID, MANAGER_ID, start, end, "ANNUAL", "atomic-fail"));
                })
        );

        List<LeaveRequestJpa> requests = leaveRequestRepository.findByStaffMemberId(STAFF_ID);
        assertThat(requests.stream().noneMatch(r -> "atomic-fail".equals(r.getReason()))).isTrue();

        LeaveAllowanceJpa allowance = leaveAllowanceRepository
                .findFirstByStaffMemberIdOrderByBusinessYearStartDesc(STAFF_ID)
                .orElseThrow();
        assertThat(allowance.getDaysPending()).isEqualTo(0);
        assertThat(allowance.getTotalEntitlement()).isEqualTo(2);
    }

    private LeaveAllowanceJpa createAllowance(int entitlement, int used, int pending) {
        LeaveAllowanceJpa a = new LeaveAllowanceJpa();
        a.setId(UUID.randomUUID().toString());
        a.setStaffMemberId(STAFF_ID);
        a.setManagerId(MANAGER_ID);
        a.setFirstName("Atomic");
        a.setSurname("Test");
        a.setDepartment("Engineering");
        a.setBusinessYearStart(LocalDate.now().getYear());
        a.setBusinessYearEnd(LocalDate.now().getYear() + 1);
        a.setTotalEntitlement(entitlement);
        a.setDaysUsed(used);
        a.setDaysPending(pending);
        return a;
    }

    private LocalDate findNextMonday() {
        LocalDate date = LocalDate.now().plusDays(1);
        while (date.getDayOfWeek().getValue() != 1) date = date.plusDays(1);
        return date;
    }
}
