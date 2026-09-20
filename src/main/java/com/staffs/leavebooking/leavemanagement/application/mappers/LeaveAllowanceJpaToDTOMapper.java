package com.staffs.leavebooking.leavemanagement.application.mappers;

import com.staffs.leavebooking.leavemanagement.application.dto.LeaveAllowanceDTO;
import com.staffs.leavebooking.leavemanagement.infrastructure.entities.LeaveAllowanceJpa;

import java.util.Objects;

public class LeaveAllowanceJpaToDTOMapper {

    public static LeaveAllowanceDTO toDTO(LeaveAllowanceJpa jpa) {
        Objects.requireNonNull(jpa, "LeaveAllowance JPA entity cannot be null");

        int remaining = jpa.getTotalEntitlement() - jpa.getDaysUsed();

        int available = jpa.getTotalEntitlement() - jpa.getDaysUsed() - jpa.getDaysPending();

        String businessYear = jpa.getBusinessYearStart() + "-" + jpa.getBusinessYearEnd();

        String staffName = jpa.getFirstName() + " " + jpa.getSurname();

        return new LeaveAllowanceDTO(
                jpa.getId(),

                jpa.getStaffMemberId(),

                staffName,

                jpa.getManagerId(),

                jpa.getDepartment(),

                businessYear,

                jpa.getTotalEntitlement(),

                jpa.getDaysUsed(),

                jpa.getDaysPending(),

                remaining,

                available
        );
    }
}
