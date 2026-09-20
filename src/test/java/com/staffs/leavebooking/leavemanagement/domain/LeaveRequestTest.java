package com.staffs.leavebooking.leavemanagement.domain;

import com.staffs.leavebooking.common.domain.Identity;
import com.staffs.leavebooking.common.events.Event;
import com.staffs.leavebooking.leavemanagement.domain.events.LeaveRequestApprovedEvent;
import com.staffs.leavebooking.leavemanagement.domain.events.LeaveRequestCancelledEvent;
import com.staffs.leavebooking.leavemanagement.domain.events.LeaveRequestRejectedEvent;
import com.staffs.leavebooking.leavemanagement.domain.events.LeaveRequestSubmittedEvent;
import com.staffs.leavebooking.testfixtures.LeaveRequestMother;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("LeaveRequest Aggregate Root")
class LeaveRequestTest {

    private static final String STAFF_ID = LeaveRequestMother.STAFF_MEMBER_ID;
    private static final String MANAGER_ID = LeaveRequestMother.MANAGER_ID;
    private static final String DECIDER_ID = LeaveRequestMother.DECIDER_ID;

    @Nested
    @DisplayName("submitNew() factory method")
    class SubmitNew {

        @Test
        @DisplayName("Should create a PENDING leave request with correct fields")
        void shouldCreatePendingRequest() {
            Identity<LeaveRequest> id = Identity.generateId();
            DateRange dateRange = LeaveRequestMother.futureDateRange(7, 5);

            LeaveRequest request = LeaveRequest.submitNew(
                    id, STAFF_ID, MANAGER_ID, LeaveType.ANNUAL, dateRange, "Holiday");

            assertEquals(id, request.id());
            assertEquals(STAFF_ID, request.staffMemberId());
            assertEquals(MANAGER_ID, request.managerId());
            assertEquals(LeaveType.ANNUAL, request.leaveType());
            assertEquals(dateRange, request.dateRange());
            assertEquals(LeaveRequestStatus.PENDING, request.status());
            assertEquals(LocalDate.now(), request.submittedOn());
            assertNull(request.decidedOn());
            assertNull(request.decidedBy());
            assertNull(request.cancellationReason());
        }

        @Test
        @DisplayName("Should raise LeaveRequestSubmittedEvent")
        void shouldRaiseSubmittedEvent() {
            Identity<LeaveRequest> id = Identity.generateId();
            DateRange dateRange = LeaveRequestMother.futureDateRange(7, 5);

            LeaveRequest request = LeaveRequest.submitNew(
                    id, STAFF_ID, MANAGER_ID, LeaveType.ANNUAL, dateRange, "Holiday");

            List<Event> events = request.listOfDomainEvents();
            assertEquals(1, events.size());
            assertInstanceOf(LeaveRequestSubmittedEvent.class, events.get(0));
            LeaveRequestSubmittedEvent event = (LeaveRequestSubmittedEvent) events.get(0);
            assertEquals(id.id(), event.leaveRequestId());
            assertEquals(STAFF_ID, event.staffMemberId());
            assertTrue(event.numberOfDays() > 0);
        }

        @Test
        @DisplayName("Should calculate working days correctly")
        void shouldCalculateWorkingDays() {
            LocalDate monday = LocalDate.now().plusDays(7);
            while (monday.getDayOfWeek().getValue() != 1) {
                monday = monday.plusDays(1);
            }
            LocalDate friday = monday.plusDays(4);
            DateRange dateRange = new DateRange(monday, friday);

            LeaveRequest request = LeaveRequest.submitNew(
                    Identity.generateId(), STAFF_ID, MANAGER_ID, LeaveType.ANNUAL, dateRange, null);

            assertEquals(5, request.numberOfDays());
        }

        @Test
        @DisplayName("Should reject past start date")
        void shouldRejectPastStartDate() {
            DateRange pastRange = new DateRange(LocalDate.now().minusDays(5), LocalDate.now().plusDays(5));

            assertThrows(IllegalArgumentException.class,
                    () -> LeaveRequest.submitNew(
                            Identity.generateId(), STAFF_ID, MANAGER_ID,
                            LeaveType.ANNUAL, pastRange, "Holiday"));
        }

        @Test
        @DisplayName("Should reject today as start date")
        void shouldRejectTodayAsStartDate() {
            DateRange todayRange = new DateRange(LocalDate.now(), LocalDate.now().plusDays(5));

            assertThrows(IllegalArgumentException.class,
                    () -> LeaveRequest.submitNew(
                            Identity.generateId(), STAFF_ID, MANAGER_ID,
                            LeaveType.ANNUAL, todayRange, "Holiday"));
        }

        @Test
        @DisplayName("Should reject weekend-only date range (zero working days)")
        void shouldRejectZeroWorkingDays() {
            LocalDate saturday = LocalDate.now().plusDays(7);
            while (saturday.getDayOfWeek().getValue() != 6) {
                saturday = saturday.plusDays(1);
            }
            LocalDate sunday = saturday.plusDays(1);
            DateRange weekendRange = new DateRange(saturday, sunday);

            assertThrows(IllegalArgumentException.class,
                    () -> LeaveRequest.submitNew(
                            Identity.generateId(), STAFF_ID, MANAGER_ID,
                            LeaveType.ANNUAL, weekendRange, "Weekend"));
        }

        @Test
        @DisplayName("Should reject null staff member ID")
        void shouldRejectNullStaffMemberId() {
            DateRange dateRange = LeaveRequestMother.futureDateRange(7, 5);

            assertThrows(IllegalArgumentException.class,
                    () -> LeaveRequest.submitNew(
                            Identity.generateId(), null, MANAGER_ID,
                            LeaveType.ANNUAL, dateRange, "Holiday"));
        }

        @Test
        @DisplayName("Should reject blank manager ID")
        void shouldRejectBlankManagerId() {
            DateRange dateRange = LeaveRequestMother.futureDateRange(7, 5);

            assertThrows(IllegalArgumentException.class,
                    () -> LeaveRequest.submitNew(
                            Identity.generateId(), STAFF_ID, "   ",
                            LeaveType.ANNUAL, dateRange, "Holiday"));
        }

        @Test
        @DisplayName("Should reject null leave type")
        void shouldRejectNullLeaveType() {
            DateRange dateRange = LeaveRequestMother.futureDateRange(7, 5);

            assertThrows(IllegalArgumentException.class,
                    () -> LeaveRequest.submitNew(
                            Identity.generateId(), STAFF_ID, MANAGER_ID,
                            null, dateRange, "Holiday"));
        }

        @Test
        @DisplayName("Should reject null date range")
        void shouldRejectNullDateRange() {
            assertThrows(NullPointerException.class,
                    () -> LeaveRequest.submitNew(
                            Identity.generateId(), STAFF_ID, MANAGER_ID,
                            LeaveType.ANNUAL, null, "Holiday"));
        }

        @Test
        @DisplayName("Should allow null reason")
        void shouldAllowNullReason() {
            DateRange dateRange = LeaveRequestMother.futureDateRange(7, 5);

            LeaveRequest request = LeaveRequest.submitNew(
                    Identity.generateId(), STAFF_ID, MANAGER_ID,
                    LeaveType.ANNUAL, dateRange, null);

            assertNull(request.reason());
        }
    }

    @Nested
    @DisplayName("reconstitute() factory method")
    class Reconstitute {

        @Test
        @DisplayName("Should create aggregate without raising events")
        void shouldNotRaiseEvents() {
            LeaveRequest request = LeaveRequestMother.pendingRequest();

            assertTrue(request.listOfDomainEvents().isEmpty());
        }

        @Test
        @DisplayName("Should preserve all fields from persistence")
        void shouldPreserveAllFields() {
            Identity<LeaveRequest> id = Identity.generateId();
            DateRange range = new DateRange(LocalDate.of(2027, 6, 1), LocalDate.of(2027, 6, 5));

            LeaveRequest request = LeaveRequest.reconstitute(
                    id, STAFF_ID, MANAGER_ID, LeaveType.ANNUAL, range, 5,
                    "Holiday", LeaveRequestStatus.APPROVED, LocalDate.of(2027, 5, 20),
                    LocalDate.of(2027, 5, 22), DECIDER_ID, null, null);

            assertEquals(id, request.id());
            assertEquals(LeaveRequestStatus.APPROVED, request.status());
            assertEquals(LocalDate.of(2027, 5, 22), request.decidedOn());
            assertEquals(DECIDER_ID, request.decidedBy());
            assertEquals(5, request.numberOfDays());
            assertEquals("Holiday", request.reason());
        }
    }

    @Nested
    @DisplayName("approve() state transition")
    class Approve {

        @Test
        @DisplayName("Should transition from PENDING to APPROVED")
        void shouldTransitionFromPendingToApproved() {
            LeaveRequest request = LeaveRequestMother.pendingRequest();

            request.approve(DECIDER_ID, null);

            assertEquals(LeaveRequestStatus.APPROVED, request.status());
            assertEquals(LocalDate.now(), request.decidedOn());
            assertEquals(DECIDER_ID, request.decidedBy());
        }

        @Test
        @DisplayName("Should raise LeaveRequestApprovedEvent")
        void shouldRaiseApprovedEvent() {
            LeaveRequest request = LeaveRequestMother.pendingRequest();

            request.approve(DECIDER_ID, null);

            List<Event> events = request.listOfDomainEvents();
            assertEquals(1, events.size());
            assertInstanceOf(LeaveRequestApprovedEvent.class, events.get(0));
            LeaveRequestApprovedEvent event = (LeaveRequestApprovedEvent) events.get(0);
            assertEquals(request.id().id(), event.leaveRequestId());
            assertEquals(DECIDER_ID, event.managerId());
            assertEquals(request.numberOfDays(), event.numberOfDays());
        }

        @Test
        @DisplayName("Should reject approve from APPROVED state")
        void shouldRejectApproveFromApproved() {
            LeaveRequest request = LeaveRequestMother.approvedRequest();

            IllegalStateException ex = assertThrows(IllegalStateException.class,
                    () -> request.approve(DECIDER_ID, null));
            assertEquals(LeaveRequest.CANNOT_APPROVE_NON_PENDING, ex.getMessage());
        }

        @Test
        @DisplayName("Should reject approve from REJECTED state")
        void shouldRejectApproveFromRejected() {
            LeaveRequest request = LeaveRequestMother.rejectedRequest();

            IllegalStateException ex = assertThrows(IllegalStateException.class,
                    () -> request.approve(DECIDER_ID, null));
            assertEquals(LeaveRequest.CANNOT_APPROVE_NON_PENDING, ex.getMessage());
        }

        @Test
        @DisplayName("Should reject approve from CANCELLED state")
        void shouldRejectApproveFromCancelled() {
            LeaveRequest request = LeaveRequestMother.cancelledRequest();

            IllegalStateException ex = assertThrows(IllegalStateException.class,
                    () -> request.approve(DECIDER_ID, null));
            assertEquals(LeaveRequest.CANNOT_APPROVE_NON_PENDING, ex.getMessage());
        }

        @Test
        @DisplayName("Should reject approve with blank decidedBy")
        void shouldRejectBlankDecidedBy() {
            LeaveRequest request = LeaveRequestMother.pendingRequest();

            IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                    () -> request.approve("   ", null));
            assertEquals(LeaveRequest.DECIDED_BY_REQUIRED, ex.getMessage());
        }

        @Test
        @DisplayName("Should reject approve with null decidedBy")
        void shouldRejectNullDecidedBy() {
            LeaveRequest request = LeaveRequestMother.pendingRequest();

            IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                    () -> request.approve(null, null));
            assertEquals(LeaveRequest.DECIDED_BY_REQUIRED, ex.getMessage());
        }
    }

    @Nested
    @DisplayName("reject() state transition")
    class Reject {

        @Test
        @DisplayName("Should transition from PENDING to REJECTED")
        void shouldTransitionFromPendingToRejected() {
            LeaveRequest request = LeaveRequestMother.pendingRequest();

            request.reject(DECIDER_ID, null);

            assertEquals(LeaveRequestStatus.REJECTED, request.status());
            assertEquals(LocalDate.now(), request.decidedOn());
            assertEquals(DECIDER_ID, request.decidedBy());
        }

        @Test
        @DisplayName("Should raise LeaveRequestRejectedEvent")
        void shouldRaiseRejectedEvent() {
            LeaveRequest request = LeaveRequestMother.pendingRequest();

            request.reject(DECIDER_ID, null);

            List<Event> events = request.listOfDomainEvents();
            assertEquals(1, events.size());
            assertInstanceOf(LeaveRequestRejectedEvent.class, events.get(0));
            LeaveRequestRejectedEvent event = (LeaveRequestRejectedEvent) events.get(0);
            assertEquals(request.id().id(), event.leaveRequestId());
            assertEquals(DECIDER_ID, event.managerId());
            assertEquals(request.numberOfDays(), event.numberOfDays());
        }

        @Test
        @DisplayName("Should reject reject from APPROVED state")
        void shouldRejectRejectFromApproved() {
            LeaveRequest request = LeaveRequestMother.approvedRequest();

            IllegalStateException ex = assertThrows(IllegalStateException.class,
                    () -> request.reject(DECIDER_ID, null));
            assertEquals(LeaveRequest.CANNOT_REJECT_NON_PENDING, ex.getMessage());
        }

        @Test
        @DisplayName("Should reject reject from REJECTED state")
        void shouldRejectRejectFromRejected() {
            LeaveRequest request = LeaveRequestMother.rejectedRequest();

            IllegalStateException ex = assertThrows(IllegalStateException.class,
                    () -> request.reject(DECIDER_ID, null));
            assertEquals(LeaveRequest.CANNOT_REJECT_NON_PENDING, ex.getMessage());
        }

        @Test
        @DisplayName("Should reject reject from CANCELLED state")
        void shouldRejectRejectFromCancelled() {
            LeaveRequest request = LeaveRequestMother.cancelledRequest();

            IllegalStateException ex = assertThrows(IllegalStateException.class,
                    () -> request.reject(DECIDER_ID, null));
            assertEquals(LeaveRequest.CANNOT_REJECT_NON_PENDING, ex.getMessage());
        }

        @Test
        @DisplayName("Should reject reject with null decidedBy")
        void shouldRejectNullDecidedBy() {
            LeaveRequest request = LeaveRequestMother.pendingRequest();

            assertThrows(IllegalArgumentException.class,
                    () -> request.reject(null, null));
        }
    }

    @Nested
    @DisplayName("cancel() state transition")
    class Cancel {

        @Test
        @DisplayName("Should transition from PENDING to CANCELLED")
        void shouldTransitionFromPendingToCancelled() {
            LeaveRequest request = LeaveRequestMother.pendingRequest();

            request.cancel(STAFF_ID, "Changed plans");

            assertEquals(LeaveRequestStatus.CANCELLED, request.status());
            assertEquals("Changed plans", request.cancellationReason());
        }

        @Test
        @DisplayName("Should transition from APPROVED to CANCELLED")
        void shouldTransitionFromApprovedToCancelled() {
            LeaveRequest request = LeaveRequestMother.approvedRequest();

            request.cancel(STAFF_ID, "Emergency");

            assertEquals(LeaveRequestStatus.CANCELLED, request.status());
        }

        @Test
        @DisplayName("Should raise event with wasPreviouslyApproved=false when cancelled from PENDING")
        void shouldSetWasPreviouslyApprovedFalseFromPending() {
            LeaveRequest request = LeaveRequestMother.pendingRequest();

            request.cancel(STAFF_ID, "Changed plans");

            LeaveRequestCancelledEvent event = (LeaveRequestCancelledEvent)
                    request.listOfDomainEvents().get(0);
            assertFalse(event.wasPreviouslyApproved());
        }

        @Test
        @DisplayName("Should raise event with wasPreviouslyApproved=true when cancelled from APPROVED")
        void shouldSetWasPreviouslyApprovedTrueFromApproved() {
            LeaveRequest request = LeaveRequestMother.approvedRequest();

            request.cancel(STAFF_ID, "Emergency");

            LeaveRequestCancelledEvent event = (LeaveRequestCancelledEvent)
                    request.listOfDomainEvents().get(0);
            assertTrue(event.wasPreviouslyApproved());
        }

        @Test
        @DisplayName("Should raise LeaveRequestCancelledEvent with correct fields")
        void shouldRaiseCancelledEvent() {
            LeaveRequest request = LeaveRequestMother.pendingRequest();

            request.cancel(STAFF_ID, "Changed plans");

            List<Event> events = request.listOfDomainEvents();
            assertEquals(1, events.size());
            assertInstanceOf(LeaveRequestCancelledEvent.class, events.get(0));
            LeaveRequestCancelledEvent event = (LeaveRequestCancelledEvent) events.get(0);
            assertEquals(request.id().id(), event.leaveRequestId());
            assertEquals(STAFF_ID, event.cancelledBy());
            assertEquals(request.numberOfDays(), event.numberOfDays());
        }

        @Test
        @DisplayName("Should reject cancel from REJECTED state")
        void shouldRejectCancelFromRejected() {
            LeaveRequest request = LeaveRequestMother.rejectedRequest();

            IllegalStateException ex = assertThrows(IllegalStateException.class,
                    () -> request.cancel(STAFF_ID, "Reason"));
            assertEquals(LeaveRequest.CANNOT_CANCEL_TERMINAL, ex.getMessage());
        }

        @Test
        @DisplayName("Should reject cancel from CANCELLED state")
        void shouldRejectCancelFromCancelled() {
            LeaveRequest request = LeaveRequestMother.cancelledRequest();

            IllegalStateException ex = assertThrows(IllegalStateException.class,
                    () -> request.cancel(STAFF_ID, "Reason"));
            assertEquals(LeaveRequest.CANNOT_CANCEL_TERMINAL, ex.getMessage());
        }

        @Test
        @DisplayName("Should reject cancel with null cancelledBy")
        void shouldRejectNullCancelledBy() {
            LeaveRequest request = LeaveRequestMother.pendingRequest();

            IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                    () -> request.cancel(null, "Reason"));
            assertEquals(LeaveRequest.CANCELLED_BY_REQUIRED, ex.getMessage());
        }

        @Test
        @DisplayName("Should reject cancel with blank cancelledBy")
        void shouldRejectBlankCancelledBy() {
            LeaveRequest request = LeaveRequestMother.pendingRequest();

            IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                    () -> request.cancel("   ", "Reason"));
            assertEquals(LeaveRequest.CANCELLED_BY_REQUIRED, ex.getMessage());
        }
    }

    @Nested
    @DisplayName("Entity equality")
    class EntityEquality {

        @Test
        @DisplayName("Two requests with same identity should be equal")
        void sameIdentityShouldBeEqual() {
            Identity<LeaveRequest> id = Identity.generateId();
            DateRange range = new DateRange(LocalDate.of(2027, 6, 1), LocalDate.of(2027, 6, 5));

            LeaveRequest r1 = LeaveRequest.reconstitute(id, STAFF_ID, MANAGER_ID,
                    LeaveType.ANNUAL, range, 5, null, LeaveRequestStatus.PENDING,
                    LocalDate.now(), null, null, null, null);
            LeaveRequest r2 = LeaveRequest.reconstitute(id, STAFF_ID, MANAGER_ID,
                    LeaveType.ANNUAL, range, 5, null, LeaveRequestStatus.APPROVED,
                    LocalDate.now(), LocalDate.now(), DECIDER_ID, null, null);

            assertEquals(r1, r2);
            assertEquals(r1.hashCode(), r2.hashCode());
        }

        @Test
        @DisplayName("Two requests with different identities should not be equal")
        void differentIdentitiesShouldNotBeEqual() {
            LeaveRequest r1 = LeaveRequestMother.pendingRequest();
            LeaveRequest r2 = LeaveRequestMother.pendingRequest();

            assertNotEquals(r1, r2);
        }
    }
}
