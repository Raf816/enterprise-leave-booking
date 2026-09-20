package com.staffs.leavebooking.leavemanagement.application.handlers;

import com.staffs.leavebooking.leavemanagement.application.dto.LeaveAllowanceDTO;
import com.staffs.leavebooking.leavemanagement.application.mappers.LeaveAllowanceJpaToDTOMapper;
import com.staffs.leavebooking.leavemanagement.infrastructure.repositories.LeaveAllowanceRepository;
import com.staffs.leavebooking.leavemanagement.ui.exceptions.LeaveAllowanceNotFoundException;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

import static java.util.stream.Collectors.toList;

@Service
@AllArgsConstructor
public class LeaveAllowanceQueryHandler {

    private final LeaveAllowanceRepository leaveAllowanceRepository;

    public LeaveAllowanceDTO findAllowanceByStaffMemberId(String staffMemberId) {
        return leaveAllowanceRepository.findFirstByStaffMemberIdOrderByBusinessYearStartDesc(staffMemberId)
                .map(LeaveAllowanceJpaToDTOMapper::toDTO)
                .orElseThrow(() -> new LeaveAllowanceNotFoundException(staffMemberId));
    }

    public LeaveAllowanceDTO findAllowanceById(String allowanceId) {
        return leaveAllowanceRepository.findById(allowanceId)
                .map(LeaveAllowanceJpaToDTOMapper::toDTO)
                .orElseThrow(() -> new LeaveAllowanceNotFoundException(allowanceId));
    }

    public List<LeaveAllowanceDTO> findAllowancesByManagerId(String managerId) {
        return leaveAllowanceRepository.findByManagerId(managerId).stream()
                .map(LeaveAllowanceJpaToDTOMapper::toDTO)
                .collect(toList());
    }

    public List<LeaveAllowanceDTO> findAllAllowances() {
        return leaveAllowanceRepository.findAll().stream()
                .map(LeaveAllowanceJpaToDTOMapper::toDTO)
                .collect(toList());
    }

    public List<LeaveAllowanceDTO> findAllowancesByDepartment(String department) {
        return leaveAllowanceRepository.findByDepartment(department).stream()
                .map(LeaveAllowanceJpaToDTOMapper::toDTO)
                .collect(toList());
    }
}
