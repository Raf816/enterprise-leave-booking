package com.staffs.leavebooking.leavemanagement;

import com.staffs.leavebooking.leavemanagement.application.commands.AmendEntitlementCommand;
import com.staffs.leavebooking.leavemanagement.application.commands.CancelLeaveRequestCommand;
import com.staffs.leavebooking.leavemanagement.application.commands.SubmitLeaveRequestCommand;
import com.staffs.leavebooking.leavemanagement.application.dto.LeaveAllowanceDTO;
import com.staffs.leavebooking.leavemanagement.application.dto.LeaveRequestDTO;
import com.staffs.leavebooking.leavemanagement.application.dto.LeaveRequestSearchCriteria;
import com.staffs.leavebooking.leavemanagement.application.handlers.LeaveAllowanceApplicationService;
import com.staffs.leavebooking.leavemanagement.application.handlers.LeaveAllowanceQueryHandler;
import com.staffs.leavebooking.leavemanagement.application.handlers.LeaveRequestApplicationService;
import com.staffs.leavebooking.leavemanagement.application.handlers.LeaveRequestQueryHandler;
import lombok.AllArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@AllArgsConstructor
public class LeaveManagementFacade {

    private final LeaveRequestQueryHandler leaveRequestQueryHandler;

    private final LeaveRequestApplicationService leaveRequestApplicationService;

    private final LeaveAllowanceQueryHandler leaveAllowanceQueryHandler;

    private final LeaveAllowanceApplicationService leaveAllowanceApplicationService;

    @PreAuthorize("hasAnyRole('STAFF', 'MANAGER', 'ADMIN')")
    public List<LeaveRequestDTO> findMyRequests(String staffMemberId) {
        return leaveRequestQueryHandler.findRequestsByStaffMemberId(staffMemberId);
    }

    @PreAuthorize("hasAnyRole('MANAGER', 'ADMIN')")
    public List<LeaveRequestDTO> findTeamRequests(String managerId) {
        return leaveRequestQueryHandler.findRequestsByManagerId(managerId);
    }

    @PreAuthorize("hasRole('ADMIN')")
    public List<LeaveRequestDTO> findAllRequests() {
        return leaveRequestQueryHandler.findAllRequests();
    }

    @PreAuthorize("hasAnyRole('STAFF', 'MANAGER', 'ADMIN')")
    public LeaveRequestDTO findRequestById(String leaveRequestId) {
        return leaveRequestQueryHandler.findRequestById(leaveRequestId);
    }

    @PreAuthorize("hasAnyRole('STAFF', 'MANAGER', 'ADMIN')")
    public List<LeaveRequestDTO> searchMyRequests(String staffMemberId, LeaveRequestSearchCriteria criteria) {
        return leaveRequestQueryHandler.searchByStaffMember(staffMemberId, criteria);
    }

    @PreAuthorize("hasAnyRole('MANAGER', 'ADMIN')")
    public List<LeaveRequestDTO> searchTeamRequests(String managerId, LeaveRequestSearchCriteria criteria) {
        return leaveRequestQueryHandler.searchByManager(managerId, criteria);
    }

    @PreAuthorize("hasRole('ADMIN')")
    public List<LeaveRequestDTO> searchAllRequests(LeaveRequestSearchCriteria criteria) {
        return leaveRequestQueryHandler.searchAll(criteria);
    }

    @PreAuthorize("hasAnyRole('STAFF', 'MANAGER', 'ADMIN')")
    public String submitLeaveRequest(SubmitLeaveRequestCommand command) {
        return leaveRequestApplicationService.submitNewRequest(command);
    }

    @PreAuthorize("hasAnyRole('MANAGER', 'ADMIN')")
    public void approveLeaveRequest(String leaveRequestId, String decidedBy, String reason) {
        leaveRequestApplicationService.approveRequest(leaveRequestId, decidedBy, reason);
    }

    @PreAuthorize("hasAnyRole('MANAGER', 'ADMIN')")
    public void rejectLeaveRequest(String leaveRequestId, String decidedBy, String reason) {
        leaveRequestApplicationService.rejectRequest(leaveRequestId, decidedBy, reason);
    }

    @PreAuthorize("hasAnyRole('STAFF', 'MANAGER', 'ADMIN')")
    public void cancelLeaveRequest(CancelLeaveRequestCommand command) {
        leaveRequestApplicationService.cancelRequest(command);
    }

    @PreAuthorize("hasAnyRole('STAFF', 'MANAGER', 'ADMIN')")
    public LeaveAllowanceDTO findMyAllowance(String staffMemberId) {
        return leaveAllowanceQueryHandler.findAllowanceByStaffMemberId(staffMemberId);
    }

    public LeaveAllowanceDTO findMyAllowanceInternal(String staffMemberId) {
        return leaveAllowanceQueryHandler.findAllowanceByStaffMemberId(staffMemberId);
    }

    @PreAuthorize("hasRole('ADMIN')")
    public LeaveAllowanceDTO findAllowanceById(String allowanceId) {
        return leaveAllowanceQueryHandler.findAllowanceById(allowanceId);
    }

    @PreAuthorize("hasAnyRole('MANAGER', 'ADMIN')")
    public LeaveAllowanceDTO findAllowanceForStaffMember(String staffMemberId) {
        return leaveAllowanceQueryHandler.findAllowanceByStaffMemberId(staffMemberId);
    }

    @PreAuthorize("hasAnyRole('MANAGER', 'ADMIN')")
    public List<LeaveAllowanceDTO> findTeamAllowances(String managerId) {
        return leaveAllowanceQueryHandler.findAllowancesByManagerId(managerId);
    }

    @PreAuthorize("hasRole('ADMIN')")
    public List<LeaveAllowanceDTO> findAllAllowances() {
        return leaveAllowanceQueryHandler.findAllAllowances();
    }

    @PreAuthorize("hasRole('ADMIN')")
    public List<LeaveAllowanceDTO> findAllowancesByDepartment(String department) {
        return leaveAllowanceQueryHandler.findAllowancesByDepartment(department);
    }

    @PreAuthorize("hasRole('ADMIN')")
    public void amendEntitlement(AmendEntitlementCommand command) {
        leaveAllowanceApplicationService.amendEntitlement(command);
    }
}
