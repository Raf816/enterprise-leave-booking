package com.staffs.leavebooking.leavemanagement.domain;

import com.staffs.leavebooking.common.domain.AggregateRoot;
import com.staffs.leavebooking.common.domain.Identity;

import static com.staffs.leavebooking.common.domain.DomainAssertions.*;

public class LeaveAllowance extends AggregateRoot<LeaveAllowance> {

    public static final String STAFF_MEMBER_ID_REQUIRED = "Staff member ID is required";

    public static final String MANAGER_ID_REQUIRED = "Manager ID is required";

    public static final String BUSINESS_YEAR_REQUIRED = "Business year is required";

    public static final String ENTITLEMENT_MUST_BE_POSITIVE = "Total entitlement must be greater than zero";

    public static final String INSUFFICIENT_BALANCE = "Insufficient leave balance";

    public static final String DAYS_MUST_BE_POSITIVE = "Days must be a positive number";

    public static final String CANNOT_RELEASE_MORE_THAN_PENDING = "Cannot release more days than currently pending";

    public static final String CANNOT_CREDIT_MORE_THAN_USED = "Cannot credit back more days than have been used";

    public static final String NEW_ENTITLEMENT_TOO_LOW = "New entitlement cannot be less than days already used";

    private final String staffMemberId;

    private String managerId;

    private String firstName;

    private String surname;

    private String department;

    private final BusinessYear businessYear;

    private int totalEntitlement;

    private int daysUsed;

    private int daysPending;

    private LeaveAllowance(Identity<LeaveAllowance> id, String staffMemberId, String managerId,
                           String firstName, String surname, String department,
                           BusinessYear businessYear, int totalEntitlement,
                           int daysUsed, int daysPending) {
        super(id);
        this.staffMemberId = argumentNotEmpty(staffMemberId, STAFF_MEMBER_ID_REQUIRED);
        this.managerId = argumentNotEmpty(managerId, MANAGER_ID_REQUIRED);
        this.firstName = firstName;
        this.surname = surname;
        this.department = department;
        argumentNotNull(businessYear, BUSINESS_YEAR_REQUIRED);
        this.businessYear = businessYear;
        argumentPositive(totalEntitlement, ENTITLEMENT_MUST_BE_POSITIVE);
        this.totalEntitlement = totalEntitlement;
        argumentNotNegative(daysUsed, "Days used cannot be negative");
        argumentNotNegative(daysPending, "Days pending cannot be negative");
        this.daysUsed = daysUsed;
        this.daysPending = daysPending;
    }

    public static LeaveAllowance createNew(Identity<LeaveAllowance> id, String staffMemberId,
                                            String managerId, String firstName, String surname,
                                            String department, int defaultEntitlement) {
        return new LeaveAllowance(id, staffMemberId, managerId, firstName, surname,
                department, BusinessYear.current(), defaultEntitlement, 0, 0);
    }

    public static LeaveAllowance reconstitute(Identity<LeaveAllowance> id, String staffMemberId,
                                               String managerId, String firstName, String surname,
                                               String department, BusinessYear businessYear,
                                               int totalEntitlement, int daysUsed, int daysPending) {
        return new LeaveAllowance(id, staffMemberId, managerId, firstName, surname,
                department, businessYear, totalEntitlement, daysUsed, daysPending);
    }

    // holds days as pending until the request is approved or rejected
    public void reserveDays(int days) {
        argumentPositive(days, DAYS_MUST_BE_POSITIVE);

        if (daysUsed + daysPending + days > totalEntitlement) {
            int available = totalEntitlement - daysUsed - daysPending;
            throw new IllegalStateException(
                    INSUFFICIENT_BALANCE + ". Available: " + available + " days, Requested: " + days + " days"
            );
        }

        this.daysPending += days;
    }

    // moves days from pending to used when a request is approved
    public void confirmDays(int days) {
        argumentPositive(days, DAYS_MUST_BE_POSITIVE);

        if (days > daysPending) {
            throw new IllegalStateException(CANNOT_RELEASE_MORE_THAN_PENDING);
        }

        this.daysPending -= days;
        this.daysUsed += days;
    }

    // returns pending days back to available when a request is rejected
    public void releasePendingDays(int days) {
        argumentPositive(days, DAYS_MUST_BE_POSITIVE);

        if (days > daysPending) {
            throw new IllegalStateException(CANNOT_RELEASE_MORE_THAN_PENDING);
        }

        this.daysPending -= days;
    }

    // returns used days back to available when an approved request is cancelled
    public void creditBackDays(int days) {
        argumentPositive(days, DAYS_MUST_BE_POSITIVE);

        if (days > daysUsed) {
            throw new IllegalStateException(CANNOT_CREDIT_MORE_THAN_USED);
        }

        this.daysUsed -= days;
    }

    public void amendEntitlement(int newEntitlement) {
        argumentPositive(newEntitlement, ENTITLEMENT_MUST_BE_POSITIVE);

        if (newEntitlement < daysUsed) {
            throw new IllegalStateException(NEW_ENTITLEMENT_TOO_LOW);
        }

        this.totalEntitlement = newEntitlement;
    }

    public void updateStaffDetails(String managerId, String department) {
        this.managerId = argumentNotEmpty(managerId, MANAGER_ID_REQUIRED);
        this.department = department;
    }

    public int remainingDays() {
        return totalEntitlement - daysUsed;
    }

    public int availableDays() {
        return totalEntitlement - daysUsed - daysPending;
    }

    public String staffMemberId() { return staffMemberId; }

    public String managerId() { return managerId; }

    public String firstName() { return firstName; }

    public String surname() { return surname; }

    public String department() { return department; }

    public BusinessYear businessYear() { return businessYear; }

    public int totalEntitlement() { return totalEntitlement; }

    public int daysUsed() { return daysUsed; }

    public int daysPending() { return daysPending; }
}
