package com.staffs.leavebooking.staffmanagement.application.mappers;

import com.staffs.leavebooking.staffmanagement.application.dto.StaffMemberDTO;
import com.staffs.leavebooking.staffmanagement.infrastructure.entities.StaffMemberJpa;

import java.util.Objects;

public class StaffMemberJpaToDTOMapper {

    public static StaffMemberDTO toDTO(StaffMemberJpa jpa) {
        Objects.requireNonNull(jpa, "StaffMember JPA entity cannot be null");

        return new StaffMemberDTO(
                jpa.getId(),
                jpa.getFirstName(),
                jpa.getSurname(),
                jpa.getEmail(),
                jpa.getDepartment(),
                jpa.getLineManagerId(),
                jpa.getHireDate(),
                jpa.getCurrentRole(),
                jpa.getStartDateCurrentRole(),
                jpa.getJobLevel(),
                jpa.getEmploymentType(),
                jpa.getEmploymentStatus()
        );
    }
}
