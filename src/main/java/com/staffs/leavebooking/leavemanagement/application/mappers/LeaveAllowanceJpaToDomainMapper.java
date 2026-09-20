package com.staffs.leavebooking.leavemanagement.application.mappers;

import com.staffs.leavebooking.common.domain.Identity;
import com.staffs.leavebooking.leavemanagement.domain.BusinessYear;
import com.staffs.leavebooking.leavemanagement.domain.LeaveAllowance;
import com.staffs.leavebooking.leavemanagement.infrastructure.entities.LeaveAllowanceJpa;

import java.util.Objects;

public class LeaveAllowanceJpaToDomainMapper {

    public static LeaveAllowance toDomain(LeaveAllowanceJpa jpa) {
        Objects.requireNonNull(jpa, "LeaveAllowance JPA entity cannot be null");

        return LeaveAllowance.reconstitute(
                Identity.of(jpa.getId()),

                jpa.getStaffMemberId(),

                jpa.getManagerId(),

                jpa.getFirstName(),

                jpa.getSurname(),

                jpa.getDepartment(),

                new BusinessYear(jpa.getBusinessYearStart(), jpa.getBusinessYearEnd()),

                jpa.getTotalEntitlement(),

                jpa.getDaysUsed(),

                jpa.getDaysPending()
        );
    }
}
