package com.staffs.leavebooking.staffmanagement.application.mappers;

import com.staffs.leavebooking.staffmanagement.domain.StaffMember;
import com.staffs.leavebooking.staffmanagement.infrastructure.entities.StaffMemberJpa;

import java.util.Objects;

public class StaffMemberDomainToJpaMapper {

    public static StaffMemberJpa toJpa(StaffMember domain) {
        Objects.requireNonNull(domain, "StaffMember domain entity cannot be null");

        StaffMemberJpa jpa = new StaffMemberJpa();
        jpa.setId(domain.id().id());
        jpa.setFirstName(domain.fullName().firstName());
        jpa.setSurname(domain.fullName().surname());
        jpa.setEmail(domain.email().address());
        jpa.setDepartment(domain.department());
        jpa.setLineManagerId(domain.lineManagerId());
        jpa.setHireDate(domain.hireDate());
        jpa.setCurrentRole(domain.currentRole());
        jpa.setStartDateCurrentRole(domain.startDateOfCurrentRole());
        jpa.setJobLevel(domain.jobLevel());
        jpa.setEmploymentType(domain.employmentType().name());
        jpa.setEmploymentStatus(domain.employmentStatus().name());
        return jpa;
    }
}
