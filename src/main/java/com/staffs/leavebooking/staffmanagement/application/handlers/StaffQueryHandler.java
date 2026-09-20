package com.staffs.leavebooking.staffmanagement.application.handlers;

import com.staffs.leavebooking.staffmanagement.application.dto.StaffMemberDTO;
import com.staffs.leavebooking.staffmanagement.application.mappers.StaffMemberJpaToDTOMapper;
import com.staffs.leavebooking.staffmanagement.infrastructure.repositories.StaffMemberRepository;
import com.staffs.leavebooking.staffmanagement.ui.exceptions.StaffMemberNotFoundException;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

import static java.util.stream.Collectors.toList;

@Service
@AllArgsConstructor
public class StaffQueryHandler {

    private final StaffMemberRepository staffMemberRepository;

    public List<StaffMemberDTO> findAllStaffMembers() {
        return staffMemberRepository.findAll().stream()
                .map(StaffMemberJpaToDTOMapper::toDTO)
                .collect(toList());
    }

    public StaffMemberDTO findStaffMemberById(String staffMemberId) {
        return staffMemberRepository.findById(staffMemberId)
                .map(StaffMemberJpaToDTOMapper::toDTO)
                .orElseThrow(() -> new StaffMemberNotFoundException(staffMemberId));
    }

    public List<StaffMemberDTO> findByDepartment(String department) {
        return staffMemberRepository.findByDepartment(department).stream()
                .map(StaffMemberJpaToDTOMapper::toDTO)
                .collect(toList());
    }

    public List<StaffMemberDTO> findByStatus(String status) {
        return staffMemberRepository.findByEmploymentStatus(status).stream()
                .map(StaffMemberJpaToDTOMapper::toDTO)
                .collect(toList());
    }

    public List<StaffMemberDTO> findByManagerId(String managerId) {
        return staffMemberRepository.findByLineManagerId(managerId).stream()
                .map(StaffMemberJpaToDTOMapper::toDTO)
                .collect(toList());
    }

    public List<StaffMemberDTO> searchStaff(com.staffs.leavebooking.staffmanagement.application.dto.StaffSearchCriteria criteria) {
        String dept = (criteria.department() != null && !criteria.department().isBlank()) ? criteria.department() : null;
        String status = (criteria.status() != null && !criteria.status().isBlank()) ? criteria.status().toUpperCase() : null;

        if (dept != null && status != null) {
            return staffMemberRepository.findByDepartmentAndEmploymentStatus(dept, status).stream()
                    .map(StaffMemberJpaToDTOMapper::toDTO).collect(toList());
        } else if (dept != null) {
            return staffMemberRepository.findByDepartment(dept).stream()
                    .map(StaffMemberJpaToDTOMapper::toDTO).collect(toList());
        } else if (status != null) {
            return staffMemberRepository.findByEmploymentStatus(status).stream()
                    .map(StaffMemberJpaToDTOMapper::toDTO).collect(toList());
        } else {
            return findAllStaffMembers();
        }
    }
}
