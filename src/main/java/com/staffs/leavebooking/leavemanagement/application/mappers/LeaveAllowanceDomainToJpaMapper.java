package com.staffs.leavebooking.leavemanagement.application.mappers;

import com.staffs.leavebooking.leavemanagement.domain.LeaveAllowance;
import com.staffs.leavebooking.leavemanagement.infrastructure.entities.LeaveAllowanceJpa;

import java.util.Objects;

public class LeaveAllowanceDomainToJpaMapper {

    public static LeaveAllowanceJpa toJpa(LeaveAllowance domain) {
        Objects.requireNonNull(domain, "LeaveAllowance domain entity cannot be null");

        LeaveAllowanceJpa jpa = new LeaveAllowanceJpa();

        jpa.setId(domain.id().id());

        jpa.setStaffMemberId(domain.staffMemberId());

        jpa.setManagerId(domain.managerId());

        jpa.setFirstName(domain.firstName());

        jpa.setSurname(domain.surname());

        jpa.setDepartment(domain.department());

        jpa.setBusinessYearStart(domain.businessYear().startYear());

        jpa.setBusinessYearEnd(domain.businessYear().endYear());

        jpa.setTotalEntitlement(domain.totalEntitlement());

        jpa.setDaysUsed(domain.daysUsed());

        jpa.setDaysPending(domain.daysPending());

        return jpa;
    }

    public static void updateJpa(LeaveAllowance domain, LeaveAllowanceJpa jpa) {
        Objects.requireNonNull(domain, "LeaveAllowance domain entity cannot be null");
        Objects.requireNonNull(jpa, "LeaveAllowance JPA entity cannot be null");

        jpa.setManagerId(domain.managerId());

        jpa.setFirstName(domain.firstName());

        jpa.setSurname(domain.surname());

        jpa.setDepartment(domain.department());

        jpa.setTotalEntitlement(domain.totalEntitlement());

        jpa.setDaysUsed(domain.daysUsed());

        jpa.setDaysPending(domain.daysPending());
    }
}
