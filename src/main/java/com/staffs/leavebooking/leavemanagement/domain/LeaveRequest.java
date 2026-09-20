package com.staffs.leavebooking.leavemanagement.domain;

import com.staffs.leavebooking.common.domain.AggregateRoot;
import com.staffs.leavebooking.common.domain.Identity;
import com.staffs.leavebooking.leavemanagement.domain.events.LeaveRequestApprovedEvent;
import com.staffs.leavebooking.leavemanagement.domain.events.LeaveRequestCancelledEvent;
import com.staffs.leavebooking.leavemanagement.domain.events.LeaveRequestRejectedEvent;
import com.staffs.leavebooking.leavemanagement.domain.events.LeaveRequestSubmittedEvent;

import java.time.LocalDate;

import static com.staffs.leavebooking.common.domain.DomainAssertions.argumentNotEmpty;
import static com.staffs.leavebooking.common.domain.DomainAssertions.argumentNotNull;

public class LeaveRequest extends AggregateRoot<LeaveRequest> {

    public static final String STAFF_MEMBER_ID_REQUIRED = "Staff member ID is required";

    public static final String MANAGER_ID_REQUIRED = "Manager ID is required";

    public static final String LEAVE_TYPE_REQUIRED = "Leave type is required";

    public static final String DATE_RANGE_REQUIRED = "Date range is required";

    public static final String CANNOT_APPROVE_NON_PENDING = "Only PENDING requests can be approved";

    public static final String CANNOT_REJECT_NON_PENDING = "Only PENDING requests can be rejected";

    public static final String CANNOT_CANCEL_TERMINAL = "Cannot cancel a request that is already REJECTED or CANCELLED";

    public static final String DECIDED_BY_REQUIRED = "Decided by (approver/rejector ID) is required";

    public static final String CANCELLED_BY_REQUIRED = "Cancelled by (user ID) is required";

    private final String staffMemberId;

    private final String managerId;

    private final LeaveType leaveType;

    private final DateRange dateRange;

    private final int numberOfDays;

    private final String reason;

    private LeaveRequestStatus status;

    private final LocalDate submittedOn;

    private LocalDate decidedOn;

    private String decidedBy;

    private String decisionReason;

    private String cancellationReason;

    private LeaveRequest(Identity<LeaveRequest> id, String staffMemberId, String managerId,
                         LeaveType leaveType, DateRange dateRange, int numberOfDays,
                         String reason, LeaveRequestStatus status, LocalDate submittedOn,
                         LocalDate decidedOn, String decidedBy, String decisionReason,
                         String cancellationReason) {
        super(id);
        this.staffMemberId = argumentNotEmpty(staffMemberId, STAFF_MEMBER_ID_REQUIRED);
        this.managerId = argumentNotEmpty(managerId, MANAGER_ID_REQUIRED);
        argumentNotNull(leaveType, LEAVE_TYPE_REQUIRED);
        argumentNotNull(dateRange, DATE_RANGE_REQUIRED);
        this.leaveType = leaveType;
        this.dateRange = dateRange;
        this.numberOfDays = numberOfDays;
        this.reason = reason;
        this.status = status;
        this.submittedOn = submittedOn;
        this.decidedOn = decidedOn;
        this.decidedBy = decidedBy;
        this.decisionReason = decisionReason;
        this.cancellationReason = cancellationReason;
    }

    public static LeaveRequest submitNew(Identity<LeaveRequest> id, String staffMemberId,
                                          String managerId, LeaveType leaveType,
                                          DateRange dateRange, String reason) {
        dateRange.validateFutureStart();

        int workingDays = dateRange.workingDays();

        if (workingDays <= 0) {
            throw new IllegalArgumentException("Leave request must include at least one working day");
        }

        LeaveRequest request = new LeaveRequest(
                id, staffMemberId, managerId, leaveType, dateRange, workingDays,
                reason, LeaveRequestStatus.PENDING, LocalDate.now(),
                null, null, null, null
        );

        request.addDomainEvent(new LeaveRequestSubmittedEvent(
                LocalDate.now(), id.id(), staffMemberId, workingDays
        ));

        return request;
    }

    public static LeaveRequest reconstitute(Identity<LeaveRequest> id, String staffMemberId,
                                             String managerId, LeaveType leaveType,
                                             DateRange dateRange, int numberOfDays,
                                             String reason, LeaveRequestStatus status,
                                             LocalDate submittedOn, LocalDate decidedOn,
                                             String decidedBy, String decisionReason,
                                             String cancellationReason) {
        return new LeaveRequest(id, staffMemberId, managerId, leaveType, dateRange,
                numberOfDays, reason, status, submittedOn, decidedOn, decidedBy,
                decisionReason, cancellationReason);
    }

    public void approve(String decidedBy, String reason) {
        argumentNotEmpty(decidedBy, DECIDED_BY_REQUIRED);

        if (this.status != LeaveRequestStatus.PENDING) {
            throw new IllegalStateException(CANNOT_APPROVE_NON_PENDING);
        }

        this.status = LeaveRequestStatus.APPROVED;
        this.decidedOn = LocalDate.now();
        this.decidedBy = decidedBy;
        this.decisionReason = reason;

        addDomainEvent(new LeaveRequestApprovedEvent(
                LocalDate.now(), this.id.id(), this.staffMemberId, decidedBy, this.numberOfDays
        ));
    }

    public void reject(String decidedBy, String reason) {
        argumentNotEmpty(decidedBy, DECIDED_BY_REQUIRED);

        if (this.status != LeaveRequestStatus.PENDING) {
            throw new IllegalStateException(CANNOT_REJECT_NON_PENDING);
        }

        this.status = LeaveRequestStatus.REJECTED;
        this.decidedOn = LocalDate.now();
        this.decidedBy = decidedBy;
        this.decisionReason = reason;

        addDomainEvent(new LeaveRequestRejectedEvent(
                LocalDate.now(), this.id.id(), this.staffMemberId, decidedBy, this.numberOfDays
        ));
    }

    public void cancel(String cancelledBy, String reason) {
        argumentNotEmpty(cancelledBy, CANCELLED_BY_REQUIRED);

        if (this.status == LeaveRequestStatus.REJECTED || this.status == LeaveRequestStatus.CANCELLED) {
            throw new IllegalStateException(CANNOT_CANCEL_TERMINAL);
        }

        // capture before overwriting, determines whether to credit back or release pending days
        boolean wasPreviouslyApproved = (this.status == LeaveRequestStatus.APPROVED);

        this.status = LeaveRequestStatus.CANCELLED;
        this.cancellationReason = reason;

        addDomainEvent(new LeaveRequestCancelledEvent(
                LocalDate.now(), this.id.id(), this.staffMemberId, cancelledBy,
                this.numberOfDays, wasPreviouslyApproved
        ));
    }

    public String staffMemberId() { return staffMemberId; }

    public String managerId() { return managerId; }

    public LeaveType leaveType() { return leaveType; }

    public DateRange dateRange() { return dateRange; }

    public int numberOfDays() { return numberOfDays; }

    public String reason() { return reason; }

    public LeaveRequestStatus status() { return status; }

    public LocalDate submittedOn() { return submittedOn; }

    public LocalDate decidedOn() { return decidedOn; }

    public String decidedBy() { return decidedBy; }

    public String decisionReason() { return decisionReason; }

    public String cancellationReason() { return cancellationReason; }
}
