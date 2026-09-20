package com.staffs.leavebooking.leavemanagement.ui;

import com.staffs.leavebooking.leavemanagement.LeaveManagementFacade;
import com.staffs.leavebooking.leavemanagement.application.commands.AmendEntitlementCommand;
import com.staffs.leavebooking.leavemanagement.application.dto.LeaveAllowanceDTO;
import com.staffs.leavebooking.staffmanagement.StaffManagementFacade;
import com.staffs.leavebooking.staffmanagement.application.dto.StaffMemberDTO;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RestController
@RequestMapping("/leave-allowances")
@AllArgsConstructor
public class LeaveAllowanceController {

    private final LeaveManagementFacade facade;

    private final StaffManagementFacade staffFacade;

    @GetMapping("/my")
    @ResponseStatus(HttpStatus.OK)
    public LeaveAllowanceDTO getMyAllowance(Authentication authentication) {
        String staffMemberId = authentication.getName();
        return facade.findMyAllowance(staffMemberId);
    }

    @GetMapping("/staff/{staffMemberId}")
    @ResponseStatus(HttpStatus.OK)
    public LeaveAllowanceDTO getAllowanceForStaff(@PathVariable String staffMemberId,
                                                  Authentication authentication) {
        verifyTeamMemberOrAdmin(staffMemberId, authentication);
        return facade.findAllowanceForStaffMember(staffMemberId);
    }

    @GetMapping("/team")
    @ResponseStatus(HttpStatus.OK)
    public List<LeaveAllowanceDTO> getTeamAllowances(Authentication authentication) {
        String managerId = authentication.getName();
        return facade.findTeamAllowances(managerId);
    }

    @GetMapping("/all")
    @ResponseStatus(HttpStatus.OK)
    public List<LeaveAllowanceDTO> getAllAllowances(
            @RequestParam(required = false) String department) {

        if (department != null && !department.isBlank()) {
            return facade.findAllowancesByDepartment(department);
        }
        return facade.findAllAllowances();
    }

    @PatchMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    public LeaveAllowanceDTO amendEntitlement(
            @PathVariable String id,
            @jakarta.validation.Valid @RequestBody AmendEntitlementBody body) {

        AmendEntitlementCommand command = new AmendEntitlementCommand(id, body.newEntitlement());
        facade.amendEntitlement(command);

        return facade.findAllowanceById(id);
    }

    public record AmendEntitlementBody(
            @jakarta.validation.constraints.Min(value = 1, message = "Entitlement must be at least 1 day")
            int newEntitlement
    ) {}

    private void verifyTeamMemberOrAdmin(String staffMemberId, Authentication authentication) {
        if (isAdmin(authentication)) return;

        try {
            StaffMemberDTO staff = staffFacade.findStaffMemberByIdInternal(staffMemberId);
            String requesterId = authentication.getName();
            if (!requesterId.equals(staff.lineManagerId())) {
                throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                        "You can only view allowances for your own team members.");
            }
        } catch (ResponseStatusException e) {
            throw e;
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND,
                    "Staff member not found: " + staffMemberId);
        }
    }

    private boolean isAdmin(Authentication authentication) {
        return authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
    }
}
