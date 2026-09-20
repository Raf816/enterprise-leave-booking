package com.staffs.leavebooking.integration;

import com.staffs.leavebooking.leavemanagement.infrastructure.entities.LeaveRequestJpa;
import com.staffs.leavebooking.leavemanagement.infrastructure.repositories.LeaveRequestRepository;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
@DisplayName("Date Overlap Query Integration Tests")
class DateOverlapQueryIntegrationTest {

    @Autowired
    private LeaveRequestRepository repository;

    private static final String STAFF_ID = "staff-overlap-test";
    private static final String MANAGER_ID = "mgr-overlap-test";

    private static final LocalDate SEARCH_FROM = LocalDate.of(2026, 9, 1);
    private static final LocalDate SEARCH_TO = LocalDate.of(2026, 9, 30);

    @BeforeEach
    void setUp() {
        repository.deleteAll();

        repository.save(createRequest("inside", STAFF_ID,
                LocalDate.of(2026, 9, 10), LocalDate.of(2026, 9, 15), "PENDING"));

        repository.save(createRequest("span-start", STAFF_ID,
                LocalDate.of(2026, 8, 28), LocalDate.of(2026, 9, 5), "APPROVED"));

        repository.save(createRequest("span-end", STAFF_ID,
                LocalDate.of(2026, 9, 25), LocalDate.of(2026, 10, 3), "PENDING"));

        repository.save(createRequest("span-all", STAFF_ID,
                LocalDate.of(2026, 8, 15), LocalDate.of(2026, 10, 15), "APPROVED"));

        repository.save(createRequest("before", STAFF_ID,
                LocalDate.of(2026, 8, 1), LocalDate.of(2026, 8, 20), "PENDING"));

        repository.save(createRequest("after", STAFF_ID,
                LocalDate.of(2026, 10, 10), LocalDate.of(2026, 10, 20), "PENDING"));
    }

    @Nested
    @DisplayName("findByStaffMemberIdAndDateOverlap")
    class StaffOverlap {

        @Test
        @DisplayName("Should return 4 overlapping requests and exclude 2 non-overlapping")
        void shouldFindAllOverlappingRequests() {
            List<LeaveRequestJpa> results = repository.findByStaffMemberIdAndDateOverlap(
                    STAFF_ID, SEARCH_FROM, SEARCH_TO);

            assertThat(results).hasSize(4);
            assertThat(results).extracting(LeaveRequestJpa::getId)
                    .containsExactlyInAnyOrder("inside", "span-start", "span-end", "span-all");
        }
    }

    @Nested
    @DisplayName("findByStaffMemberIdAndStatusAndDateOverlap")
    class StaffStatusOverlap {

        @Test
        @DisplayName("Should filter by both status and date overlap")
        void shouldFilterByStatusAndOverlap() {
            List<LeaveRequestJpa> results = repository.findByStaffMemberIdAndStatusAndDateOverlap(
                    STAFF_ID, "PENDING", SEARCH_FROM, SEARCH_TO);

            assertThat(results).hasSize(2);
            assertThat(results).extracting(LeaveRequestJpa::getId)
                    .containsExactlyInAnyOrder("inside", "span-end");
        }
    }

    @Nested
    @DisplayName("findByManagerIdAndDateOverlap")
    class ManagerOverlap {

        @Test
        @DisplayName("Should find overlapping requests by manager")
        void shouldFindByManagerAndOverlap() {
            List<LeaveRequestJpa> results = repository.findByManagerIdAndDateOverlap(
                    MANAGER_ID, SEARCH_FROM, SEARCH_TO);

            assertThat(results).hasSize(4);
        }

        @Test
        @DisplayName("Should filter by manager + status + date overlap")
        void shouldFindByManagerAndStatusAndOverlap() {
            List<LeaveRequestJpa> results = repository.findByManagerIdAndStatusAndDateOverlap(
                    MANAGER_ID, "APPROVED", SEARCH_FROM, SEARCH_TO);

            assertThat(results).hasSize(2);
            assertThat(results).extracting(LeaveRequestJpa::getId)
                    .containsExactlyInAnyOrder("span-start", "span-all");
        }
    }

    @Nested
    @DisplayName("findByDateOverlap (company-wide)")
    class CompanyWideOverlap {

        @Test
        @DisplayName("Should find all overlapping requests regardless of person")
        void shouldFindAllOverlapping() {
            List<LeaveRequestJpa> results = repository.findByDateOverlap(SEARCH_FROM, SEARCH_TO);

            assertThat(results).hasSize(4);
        }

        @Test
        @DisplayName("Should find zero overlapping for a range with no requests")
        void shouldFindNoneForEmptyRange() {
            List<LeaveRequestJpa> results = repository.findByDateOverlap(
                    LocalDate.of(2027, 1, 1), LocalDate.of(2027, 1, 31));

            assertThat(results).isEmpty();
        }
    }

    @Nested
    @DisplayName("findByStatusAndDateOverlap")
    class StatusOverlap {

        @Test
        @DisplayName("Should filter by status and date overlap company-wide")
        void shouldFilterByStatusCompanyWide() {
            List<LeaveRequestJpa> results = repository.findByStatusAndDateOverlap(
                    "APPROVED", SEARCH_FROM, SEARCH_TO);

            assertThat(results).hasSize(2);
            assertThat(results).extracting(LeaveRequestJpa::getId)
                    .containsExactlyInAnyOrder("span-start", "span-all");
        }
    }

    private LeaveRequestJpa createRequest(String id, String staffId,
                                           LocalDate startDate, LocalDate endDate,
                                           String status) {
        LeaveRequestJpa jpa = new LeaveRequestJpa();
        jpa.setId(id);
        jpa.setStaffMemberId(staffId);
        jpa.setManagerId(MANAGER_ID);
        jpa.setLeaveType("ANNUAL");
        jpa.setStartDate(startDate);
        jpa.setEndDate(endDate);
        jpa.setNumberOfDays(5);
        jpa.setStatus(status);
        jpa.setSubmittedOn(LocalDate.now());
        return jpa;
    }
}
