package com.staffs.leavebooking.leavemanagement.application.handlers;

import com.staffs.leavebooking.leavemanagement.application.dto.LeaveRequestDTO;
import com.staffs.leavebooking.leavemanagement.application.dto.LeaveRequestSearchCriteria;
import com.staffs.leavebooking.leavemanagement.application.mappers.LeaveRequestJpaToDTOMapper;
import com.staffs.leavebooking.leavemanagement.infrastructure.entities.LeaveRequestJpa;
import com.staffs.leavebooking.leavemanagement.infrastructure.repositories.LeaveRequestRepository;
import com.staffs.leavebooking.leavemanagement.ui.exceptions.LeaveRequestNotFoundException;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.StreamSupport;

import static java.util.stream.Collectors.toList;

@Service
@AllArgsConstructor
public class LeaveRequestQueryHandler {

    private final LeaveRequestRepository leaveRequestRepository;

    public List<LeaveRequestDTO> findRequestsByStaffMemberId(String staffMemberId) {
        return leaveRequestRepository.findByStaffMemberId(staffMemberId).stream()
                .map(LeaveRequestJpaToDTOMapper::toDTO)
                .collect(toList());
    }

    public List<LeaveRequestDTO> findRequestsByManagerId(String managerId) {
        return leaveRequestRepository.findByManagerId(managerId).stream()
                .map(LeaveRequestJpaToDTOMapper::toDTO)
                .collect(toList());
    }

    public List<LeaveRequestDTO> findAllRequests() {
        return StreamSupport.stream(leaveRequestRepository.findAll().spliterator(), false)
                .map(LeaveRequestJpaToDTOMapper::toDTO)
                .collect(toList());
    }

    public LeaveRequestDTO findRequestById(String leaveRequestId) {
        return leaveRequestRepository.findById(leaveRequestId)
                .map(LeaveRequestJpaToDTOMapper::toDTO)
                .orElseThrow(() -> new LeaveRequestNotFoundException(leaveRequestId));
    }

    public List<LeaveRequestDTO> searchByStaffMember(String staffMemberId, LeaveRequestSearchCriteria criteria) {
        String status = criteria.normalizedStatus();
        boolean hasDateRange = criteria.from() != null && criteria.to() != null;

        List<LeaveRequestJpa> results;

        if (status != null && hasDateRange) {
            results = leaveRequestRepository.findByStaffMemberIdAndStatusAndDateOverlap(
                    staffMemberId, status, criteria.from(), criteria.to());
        } else if (status != null) {
            results = leaveRequestRepository.findByStaffMemberIdAndStatus(staffMemberId, status);
        } else if (hasDateRange) {
            results = leaveRequestRepository.findByStaffMemberIdAndDateOverlap(
                    staffMemberId, criteria.from(), criteria.to());
        } else {
            results = leaveRequestRepository.findByStaffMemberId(staffMemberId);
        }

        return results.stream().map(LeaveRequestJpaToDTOMapper::toDTO).collect(toList());
    }

    public List<LeaveRequestDTO> searchByManager(String managerId, LeaveRequestSearchCriteria criteria) {
        String status = criteria.normalizedStatus();
        boolean hasDateRange = criteria.from() != null && criteria.to() != null;

        List<LeaveRequestJpa> results;

        if (status != null && hasDateRange) {
            results = leaveRequestRepository.findByManagerIdAndStatusAndDateOverlap(
                    managerId, status, criteria.from(), criteria.to());
        } else if (status != null) {
            results = leaveRequestRepository.findByManagerIdAndStatus(managerId, status);
        } else if (hasDateRange) {
            results = leaveRequestRepository.findByManagerIdAndDateOverlap(
                    managerId, criteria.from(), criteria.to());
        } else {
            results = leaveRequestRepository.findByManagerId(managerId);
        }

        return results.stream().map(LeaveRequestJpaToDTOMapper::toDTO).collect(toList());
    }

    public List<LeaveRequestDTO> searchAll(LeaveRequestSearchCriteria criteria) {
        String status = criteria.normalizedStatus();
        boolean hasDateRange = criteria.from() != null && criteria.to() != null;
        String staffId = (criteria.staffMemberId() != null && !criteria.staffMemberId().isBlank())
                ? criteria.staffMemberId() : null;
        String mgrId = (criteria.managerId() != null && !criteria.managerId().isBlank())
                ? criteria.managerId() : null;

        List<LeaveRequestJpa> results;

        if (staffId != null) {
            if (status != null && hasDateRange) {
                results = leaveRequestRepository.findByStaffMemberIdAndStatusAndDateOverlap(
                        staffId, status, criteria.from(), criteria.to());
            } else if (status != null) {
                results = leaveRequestRepository.findByStaffMemberIdAndStatus(staffId, status);
            } else if (hasDateRange) {
                results = leaveRequestRepository.findByStaffMemberIdAndDateOverlap(
                        staffId, criteria.from(), criteria.to());
            } else {
                results = leaveRequestRepository.findByStaffMemberId(staffId);
            }
        }
        else if (mgrId != null) {
            if (status != null && hasDateRange) {
                results = leaveRequestRepository.findByManagerIdAndStatusAndDateOverlap(
                        mgrId, status, criteria.from(), criteria.to());
            } else if (status != null) {
                results = leaveRequestRepository.findByManagerIdAndStatus(mgrId, status);
            } else if (hasDateRange) {
                results = leaveRequestRepository.findByManagerIdAndDateOverlap(
                        mgrId, criteria.from(), criteria.to());
            } else {
                results = leaveRequestRepository.findByManagerId(mgrId);
            }
        }
        else {
            if (status != null && hasDateRange) {
                results = leaveRequestRepository.findByStatusAndDateOverlap(
                        status, criteria.from(), criteria.to());
            } else if (status != null) {
                results = leaveRequestRepository.findByStatus(status);
            } else if (hasDateRange) {
                results = leaveRequestRepository.findByDateOverlap(criteria.from(), criteria.to());
            } else {
                results = StreamSupport.stream(leaveRequestRepository.findAll().spliterator(), false)
                        .collect(toList());
            }
        }

        return results.stream().map(LeaveRequestJpaToDTOMapper::toDTO).collect(toList());
    }
}
