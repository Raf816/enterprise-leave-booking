package com.staffs.leavebooking.staffmanagement.application.mappers;

import com.staffs.leavebooking.common.domain.Email;
import com.staffs.leavebooking.common.domain.FullName;
import com.staffs.leavebooking.common.domain.Identity;
import com.staffs.leavebooking.staffmanagement.domain.EmploymentStatus;
import com.staffs.leavebooking.staffmanagement.domain.EmploymentType;
import com.staffs.leavebooking.staffmanagement.domain.StaffMember;
import com.staffs.leavebooking.staffmanagement.infrastructure.entities.StaffMemberJpa;

import java.util.Objects;

public class StaffMemberJpaToDomainMapper {

    public static StaffMember toDomain(StaffMemberJpa jpa) {
        Objects.requireNonNull(jpa, "StaffMember JPA entity cannot be null");

        return StaffMember.reconstitute(
                Identity.of(jpa.getId()),
                new FullName(jpa.getFirstName(), jpa.getSurname()),
                new Email(jpa.getEmail()),
                jpa.getDepartment(),
                jpa.getLineManagerId(),
                jpa.getHireDate(),
                jpa.getCurrentRole(),
                jpa.getStartDateCurrentRole(),
                jpa.getJobLevel(),
                EmploymentType.valueOf(jpa.getEmploymentType()),
                EmploymentStatus.valueOf(jpa.getEmploymentStatus())
        );
    }
}
