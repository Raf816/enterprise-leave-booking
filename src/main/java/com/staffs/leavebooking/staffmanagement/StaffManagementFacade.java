package com.staffs.leavebooking.staffmanagement;

import com.staffs.leavebooking.staffmanagement.application.commands.AddStaffMemberCommand;
import com.staffs.leavebooking.staffmanagement.application.commands.UpdateDepartmentCommand;
import com.staffs.leavebooking.staffmanagement.application.commands.UpdatePlacementCommand;
import com.staffs.leavebooking.staffmanagement.application.commands.UpdateStatusCommand;
import com.staffs.leavebooking.staffmanagement.application.dto.StaffMemberDTO;
import com.staffs.leavebooking.staffmanagement.application.handlers.StaffApplicationService;
import com.staffs.leavebooking.staffmanagement.application.handlers.StaffQueryHandler;
import lombok.AllArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@AllArgsConstructor
public class StaffManagementFacade {

    private final StaffQueryHandler staffQueryHandler;

    private final StaffApplicationService staffApplicationService;

    @PreAuthorize("hasRole('ADMIN')")
    public List<StaffMemberDTO> findAllStaffMembers() {
        return staffQueryHandler.findAllStaffMembers();
    }

    @PreAuthorize("hasAnyRole('MANAGER', 'ADMIN')")
    public StaffMemberDTO findStaffMemberById(String staffMemberId) {
        return staffQueryHandler.findStaffMemberById(staffMemberId);
    }

    @PreAuthorize("hasRole('ADMIN')")
    public List<StaffMemberDTO> findStaffByDepartment(String department) {
        return staffQueryHandler.findByDepartment(department);
    }

    @PreAuthorize("hasRole('ADMIN')")
    public List<StaffMemberDTO> findStaffByStatus(String status) {
        return staffQueryHandler.findByStatus(status);
    }

    @PreAuthorize("hasAnyRole('MANAGER', 'ADMIN')")
    public List<StaffMemberDTO> findMyTeam(String managerId) {
        return staffQueryHandler.findByManagerId(managerId);
    }

    @PreAuthorize("hasRole('ADMIN')")
    public List<StaffMemberDTO> searchStaff(com.staffs.leavebooking.staffmanagement.application.dto.StaffSearchCriteria criteria) {
        return staffQueryHandler.searchStaff(criteria);
    }

    @PreAuthorize("hasRole('ADMIN')")
    public String addStaffMember(AddStaffMemberCommand command) {
        return staffApplicationService.addNewStaffMember(command);
    }

    @PreAuthorize("hasRole('ADMIN')")
    public String addStaffMemberWithId(String firebaseUid, AddStaffMemberCommand command) {
        return staffApplicationService.addNewStaffMemberWithId(firebaseUid, command);
    }

    public String createSkeletonStaffMember(String firebaseUid, String firstName, String surname, String email) {
        return staffApplicationService.createSkeletonStaffMember(firebaseUid, firstName, surname, email);
    }

    public StaffMemberDTO findStaffMemberByIdInternal(String staffMemberId) {
        return staffQueryHandler.findStaffMemberById(staffMemberId);
    }

    @PreAuthorize("hasRole('ADMIN')")
    public void updateDepartment(UpdateDepartmentCommand command) {
        staffApplicationService.updateDepartment(command);
    }

    @PreAuthorize("hasRole('ADMIN')")
    public void updatePlacement(UpdatePlacementCommand command) {
        staffApplicationService.updatePlacement(command);
    }

    @PreAuthorize("hasRole('ADMIN')")
    public void updateStatus(UpdateStatusCommand command) {
        staffApplicationService.updateStatus(command);
    }
}
