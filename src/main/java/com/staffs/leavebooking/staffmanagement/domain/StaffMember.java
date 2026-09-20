package com.staffs.leavebooking.staffmanagement.domain;

import com.staffs.leavebooking.common.domain.AggregateRoot;
import com.staffs.leavebooking.common.domain.Email;
import com.staffs.leavebooking.common.domain.FullName;
import com.staffs.leavebooking.common.domain.Identity;
import com.staffs.leavebooking.common.events.StaffMemberAddedEvent;
import com.staffs.leavebooking.common.events.StaffMemberUpdatedEvent;

import java.time.LocalDate;

import static com.staffs.leavebooking.common.domain.DomainAssertions.argumentNotEmpty;
import static com.staffs.leavebooking.common.domain.DomainAssertions.argumentNotNull;

public class StaffMember extends AggregateRoot<StaffMember> {

    public static final String FULL_NAME_REQUIRED = "Full name is required";
    public static final String EMAIL_REQUIRED = "Email is required";
    public static final String DEPARTMENT_REQUIRED = "Department is required";
    public static final String HIRE_DATE_REQUIRED = "Hire date is required";
    public static final String HIRE_DATE_IN_FUTURE = "Hire date cannot be in the future";
    public static final String CURRENT_ROLE_REQUIRED = "Current role is required";
    public static final String ROLE_START_DATE_REQUIRED = "Role start date is required";
    public static final String EMPLOYMENT_TYPE_REQUIRED = "Employment type is required";
    public static final String EMPLOYMENT_STATUS_REQUIRED = "Employment status is required";
    public static final String CANNOT_REACTIVATE_TERMINATED = "A terminated staff member cannot be reactivated";

    public static final int DEFAULT_LEAVE_ENTITLEMENT = 25;

    private FullName fullName;
    private Email email;
    private String department;
    private String lineManagerId;
    private LocalDate hireDate;
    private String currentRole;
    private LocalDate startDateOfCurrentRole;
    private String jobLevel;
    private EmploymentType employmentType;
    private EmploymentStatus employmentStatus;
    private int defaultLeaveEntitlement;

    private StaffMember(Identity<StaffMember> id, FullName fullName, Email email,
                        String department, String lineManagerId, LocalDate hireDate,
                        String currentRole, LocalDate startDateOfCurrentRole,
                        String jobLevel, EmploymentType employmentType,
                        EmploymentStatus employmentStatus, int defaultLeaveEntitlement) {
        super(id);

        argumentNotNull(fullName, FULL_NAME_REQUIRED);
        argumentNotNull(email, EMAIL_REQUIRED);
        argumentNotEmpty(department, DEPARTMENT_REQUIRED);
        argumentNotNull(hireDate, HIRE_DATE_REQUIRED);
        argumentNotEmpty(currentRole, CURRENT_ROLE_REQUIRED);
        argumentNotNull(startDateOfCurrentRole, ROLE_START_DATE_REQUIRED);
        argumentNotNull(employmentType, EMPLOYMENT_TYPE_REQUIRED);
        argumentNotNull(employmentStatus, EMPLOYMENT_STATUS_REQUIRED);

        this.fullName = fullName;
        this.email = email;
        this.department = department;
        this.lineManagerId = lineManagerId;
        this.hireDate = hireDate;
        this.currentRole = currentRole;
        this.startDateOfCurrentRole = startDateOfCurrentRole;
        this.jobLevel = jobLevel;
        this.employmentType = employmentType;
        this.employmentStatus = employmentStatus;
        this.defaultLeaveEntitlement = defaultLeaveEntitlement;
    }

    public static StaffMember createNew(Identity<StaffMember> id, FullName fullName, Email email,
                                         String department, String lineManagerId, LocalDate hireDate,
                                         String currentRole, LocalDate startDateOfCurrentRole,
                                         String jobLevel, EmploymentType employmentType) {
        if (hireDate.isAfter(LocalDate.now())) {
            throw new IllegalArgumentException(HIRE_DATE_IN_FUTURE);
        }

        return new StaffMember(id, fullName, email, department, lineManagerId,
                hireDate, currentRole, startDateOfCurrentRole, jobLevel,
                employmentType, EmploymentStatus.PENDING_SETUP, DEFAULT_LEAVE_ENTITLEMENT);
    }

    public static StaffMember createSkeleton(Identity<StaffMember> id, FullName fullName, Email email) {
        return new StaffMember(id, fullName, email,
                "Unassigned",
                null,
                LocalDate.now(),
                "Pending Setup",
                LocalDate.now(),
                null,
                EmploymentType.FULL_TIME,
                EmploymentStatus.PENDING_SETUP,
                DEFAULT_LEAVE_ENTITLEMENT);
    }

    public static StaffMember reconstitute(Identity<StaffMember> id, FullName fullName, Email email,
                                            String department, String lineManagerId, LocalDate hireDate,
                                            String currentRole, LocalDate startDateOfCurrentRole,
                                            String jobLevel, EmploymentType employmentType,
                                            EmploymentStatus employmentStatus) {
        return new StaffMember(id, fullName, email, department, lineManagerId,
                hireDate, currentRole, startDateOfCurrentRole, jobLevel,
                employmentType, employmentStatus, DEFAULT_LEAVE_ENTITLEMENT);
    }

    public void updateDepartment(String newDepartment, String newLineManagerId) {
        argumentNotEmpty(newDepartment, DEPARTMENT_REQUIRED);
        this.department = newDepartment;
        this.lineManagerId = newLineManagerId;

        addDomainEvent(new StaffMemberUpdatedEvent(
                LocalDate.now(), this.id.id(), newLineManagerId, newDepartment
        ));
    }

    public void updatePlacement(String newRole, LocalDate newStartDate,
                                String newJobLevel, EmploymentType newType) {
        argumentNotEmpty(newRole, CURRENT_ROLE_REQUIRED);
        argumentNotNull(newStartDate, ROLE_START_DATE_REQUIRED);
        argumentNotNull(newType, EMPLOYMENT_TYPE_REQUIRED);
        this.currentRole = newRole;
        this.startDateOfCurrentRole = newStartDate;
        this.jobLevel = newJobLevel;
        this.employmentType = newType;
    }

    public void updateStatus(EmploymentStatus newStatus) {
        argumentNotNull(newStatus, EMPLOYMENT_STATUS_REQUIRED);

        // terminated staff cannot be reactivated
        if (this.employmentStatus == EmploymentStatus.TERMINATED && newStatus != EmploymentStatus.TERMINATED) {
            throw new IllegalStateException(CANNOT_REACTIVATE_TERMINATED);
        }

        boolean isActivation = (this.employmentStatus == EmploymentStatus.PENDING_SETUP
                && newStatus == EmploymentStatus.ACTIVE);

        this.employmentStatus = newStatus;

        // activation publishes event to create leave allowance in the other context
        if (isActivation) {
            addDomainEvent(new StaffMemberAddedEvent(
                    LocalDate.now(),
                    this.id.id(),
                    this.fullName.firstName(),
                    this.fullName.surname(),
                    this.email.address(),
                    this.lineManagerId,
                    this.department,
                    this.defaultLeaveEntitlement
            ));
        }
    }

    public FullName fullName() { return fullName; }
    public Email email() { return email; }
    public String department() { return department; }
    public String lineManagerId() { return lineManagerId; }
    public LocalDate hireDate() { return hireDate; }
    public String currentRole() { return currentRole; }
    public LocalDate startDateOfCurrentRole() { return startDateOfCurrentRole; }
    public String jobLevel() { return jobLevel; }
    public EmploymentType employmentType() { return employmentType; }
    public EmploymentStatus employmentStatus() { return employmentStatus; }
    public int defaultLeaveEntitlement() { return defaultLeaveEntitlement; }
}
