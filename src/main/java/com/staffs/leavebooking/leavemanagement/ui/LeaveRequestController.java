package com.staffs.leavebooking.leavemanagement.ui;

import com.staffs.leavebooking.leavemanagement.LeaveManagementFacade;
import com.staffs.leavebooking.leavemanagement.application.commands.CancelLeaveRequestCommand;
import com.staffs.leavebooking.leavemanagement.application.commands.SubmitLeaveRequestCommand;
import com.staffs.leavebooking.leavemanagement.application.dto.LeaveAllowanceDTO;
import com.staffs.leavebooking.leavemanagement.application.dto.LeaveRequestDTO;
import com.staffs.leavebooking.leavemanagement.application.dto.LeaveRequestSearchCriteria;
import com.staffs.leavebooking.leavemanagement.domain.DateRange;
import com.staffs.leavebooking.staffmanagement.StaffManagementFacade;
import com.staffs.leavebooking.staffmanagement.application.dto.StaffMemberDTO;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/leave-requests")
@AllArgsConstructor
public class LeaveRequestController {

    private final LeaveManagementFacade facade;

    private final StaffManagementFacade staffFacade;

    @GetMapping("/my")
    @ResponseStatus(HttpStatus.OK)
    public List<LeaveRequestDTO> getMyRequests(Authentication authentication) {
        return facade.findMyRequests(extractStaffMemberId(authentication));
    }

    @GetMapping("/team")
    @ResponseStatus(HttpStatus.OK)
    public List<LeaveRequestDTO> getTeamRequests(Authentication authentication) {
        return facade.findTeamRequests(extractStaffMemberId(authentication));
    }

    @GetMapping("/all")
    @ResponseStatus(HttpStatus.OK)
    public List<LeaveRequestDTO> getAllRequests() {
        return facade.findAllRequests();
    }

    @GetMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    public LeaveRequestDTO getRequestById(@PathVariable String id, Authentication authentication) {
        LeaveRequestDTO request = facade.findRequestById(id);

        if (!isAdmin(authentication)) {
            String userId = authentication.getName();
            if (!request.staffMemberId().equals(userId) && !request.managerId().equals(userId)) {
                throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                        "You can only view your own leave requests or those assigned to you as a manager.");
            }
        }

        return request;
    }

    @PostMapping("/my/search")
    @ResponseStatus(HttpStatus.OK)
    public List<LeaveRequestDTO> searchMyRequests(
            Authentication authentication,
            @RequestBody LeaveRequestSearchCriteria criteria) {

        validateSearchCriteria(criteria, "GET /leave-requests/my");
        rejectPersonFilters(criteria, "/my/search");
        return facade.searchMyRequests(extractStaffMemberId(authentication), criteria);
    }

    @PostMapping("/team/search")
    @ResponseStatus(HttpStatus.OK)
    public List<LeaveRequestDTO> searchTeamRequests(
            Authentication authentication,
            @RequestBody LeaveRequestSearchCriteria criteria) {

        validateSearchCriteria(criteria, "GET /leave-requests/team");
        rejectPersonFilters(criteria, "/team/search");
        return facade.searchTeamRequests(extractStaffMemberId(authentication), criteria);
    }

    @PostMapping("/all/search")
    @ResponseStatus(HttpStatus.OK)
    public List<LeaveRequestDTO> searchAllRequests(
            @RequestBody LeaveRequestSearchCriteria criteria) {

        validateSearchCriteria(criteria, "GET /leave-requests/all");
        return facade.searchAllRequests(criteria);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public LeaveRequestDTO submitLeaveRequest(
            Authentication authentication,
            @Valid @RequestBody SubmitLeaveRequestBody body) {

        String staffMemberId = extractStaffMemberId(authentication);

        // pre-flight checks before hitting the domain layer
        verifyStaffIsActive(staffMemberId);

        String managerId = resolveLineManager(staffMemberId);

        verifyNoDateOverlap(staffMemberId, body.startDate(), body.endDate());

        verifyAllowanceSufficiency(staffMemberId, body.startDate(), body.endDate());

        SubmitLeaveRequestCommand command = new SubmitLeaveRequestCommand(
                staffMemberId,
                managerId,
                body.startDate(),
                body.endDate(),
                body.leaveType(),
                body.reason()
        );

        String id = facade.submitLeaveRequest(command);
        return facade.findRequestById(id);
    }

    @PatchMapping("/{id}/approve")
    @ResponseStatus(HttpStatus.OK)
    public LeaveRequestDTO approveRequest(
            @PathVariable String id,
            Authentication authentication,
            @RequestBody(required = false) Map<String, String> body) {

        String decidedBy = extractStaffMemberId(authentication);
        verifyManagerOrAdmin(id, decidedBy, authentication);

        String reason = (body != null) ? body.get("reason") : null;
        validateReasonLength(reason);
        facade.approveLeaveRequest(id, decidedBy, reason);
        return facade.findRequestById(id);
    }

    @PatchMapping("/{id}/reject")
    @ResponseStatus(HttpStatus.OK)
    public LeaveRequestDTO rejectRequest(
            @PathVariable String id,
            Authentication authentication,
            @RequestBody(required = false) Map<String, String> body) {

        String decidedBy = extractStaffMemberId(authentication);
        verifyManagerOrAdmin(id, decidedBy, authentication);

        String reason = (body != null) ? body.get("reason") : null;
        validateReasonLength(reason);
        facade.rejectLeaveRequest(id, decidedBy, reason);
        return facade.findRequestById(id);
    }

    @PatchMapping("/{id}/cancel")
    @ResponseStatus(HttpStatus.OK)
    public LeaveRequestDTO cancelRequest(
            @PathVariable String id,
            Authentication authentication,
            @RequestBody(required = false) Map<String, String> body) {

        String cancelledBy = extractStaffMemberId(authentication);
        verifyOwnerOrAdmin(id, cancelledBy, authentication);

        String reason = (body != null) ? body.get("reason") : null;
        validateReasonLength(reason);

        CancelLeaveRequestCommand command = new CancelLeaveRequestCommand(id, cancelledBy, reason);
        facade.cancelLeaveRequest(command);
        return facade.findRequestById(id);
    }

    private String extractStaffMemberId(Authentication authentication) {
        return authentication.getName();
    }

    private void validateSearchCriteria(LeaveRequestSearchCriteria criteria, String getEndpoint) {
        if (!criteria.hasFilters()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "At least one search filter is required. Use " + getEndpoint + " for unfiltered results.");
        }
        if (criteria.status() != null && !criteria.status().isBlank()) {
            try {
                com.staffs.leavebooking.leavemanagement.domain.LeaveRequestStatus.valueOf(criteria.status().toUpperCase());
            } catch (IllegalArgumentException e) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "Invalid status filter: '" + criteria.status() + "'. Valid values are: PENDING, APPROVED, REJECTED, CANCELLED.");
            }
        }
        if ((criteria.from() != null) != (criteria.to() != null)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Both 'from' and 'to' dates must be provided for date range filtering.");
        }
        if (criteria.from() != null && criteria.to() != null && criteria.from().isAfter(criteria.to())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "'from' date must be on or before 'to' date.");
        }
        boolean hasStaffFilter = criteria.staffMemberId() != null && !criteria.staffMemberId().isBlank();
        boolean hasManagerFilter = criteria.managerId() != null && !criteria.managerId().isBlank();
        if (hasStaffFilter && hasManagerFilter) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "staffMemberId and managerId cannot be supplied together. Search by one or the other.");
        }
    }

    private void rejectPersonFilters(LeaveRequestSearchCriteria criteria, String endpoint) {
        boolean hasStaffFilter = criteria.staffMemberId() != null && !criteria.staffMemberId().isBlank();
        boolean hasManagerFilter = criteria.managerId() != null && !criteria.managerId().isBlank();
        if (hasStaffFilter || hasManagerFilter) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "staffMemberId and managerId filters are not accepted on " + endpoint +
                    ". The search scope is determined by your authentication token. " +
                    "Use POST /leave-requests/all/search for person-filtered searches (admin only).");
        }
    }

    private boolean isAdmin(Authentication authentication) {
        return authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
    }

    private void verifyManagerOrAdmin(String leaveRequestId, String userId, Authentication authentication) {
        if (isAdmin(authentication)) return;

        LeaveRequestDTO request = facade.findRequestById(leaveRequestId);
        if (!request.managerId().equals(userId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                    "Only the assigned manager or an admin can approve/reject this request.");
        }
    }

    private void verifyOwnerOrAdmin(String leaveRequestId, String userId, Authentication authentication) {
        if (isAdmin(authentication)) return;

        LeaveRequestDTO request = facade.findRequestById(leaveRequestId);
        if (!request.staffMemberId().equals(userId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                    "You can only cancel your own leave requests.");
        }
    }

    private void verifyStaffIsActive(String staffMemberId) {
        try {
            StaffMemberDTO staff = staffFacade.findStaffMemberByIdInternal(staffMemberId);
            if ("PENDING_SETUP".equals(staff.employmentStatus())) {
                throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                        "Your account is pending setup. An administrator must complete your profile " +
                        "and activate your account before you can submit leave requests.");
            }
            if ("TERMINATED".equals(staff.employmentStatus())) {
                throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                        "Your account has been terminated. You cannot submit leave requests.");
            }
        } catch (ResponseStatusException e) {
            throw e;
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                    "Your staff profile has not been set up. Please contact your administrator.");
        }
    }

    private void validateReasonLength(String reason) {
        if (reason != null && reason.length() > 500) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Reason must not exceed 500 characters (provided: " + reason.length() + ").");
        }
    }

    private String resolveLineManager(String staffMemberId) {
        try {
            StaffMemberDTO staff = staffFacade.findStaffMemberByIdInternal(staffMemberId);
            if (staff.lineManagerId() != null && !staff.lineManagerId().isBlank()) {
                String managerId = staff.lineManagerId();
                try {
                    StaffMemberDTO manager = staffFacade.findStaffMemberByIdInternal(managerId);
                    if ("TERMINATED".equals(manager.employmentStatus())) {
                        throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                                "Your assigned line manager's account has been terminated. " +
                                "Please contact an administrator to update your line manager assignment.");
                    }
                } catch (ResponseStatusException e) {
                    throw e;
                } catch (Exception e) {
                    throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                            "Your assigned line manager (ID: " + managerId + ") no longer exists. " +
                            "Please contact an administrator to update your line manager assignment.");
                }
                return managerId;
            }
        } catch (ResponseStatusException e) {
            throw e;
        } catch (Exception e) {
        }

        throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                "No line manager assigned to your profile. An administrator must assign " +
                "a line manager before you can submit leave requests.");
    }

    private void verifyNoDateOverlap(String staffMemberId, java.time.LocalDate startDate, java.time.LocalDate endDate) {
        java.util.List<LeaveRequestDTO> existingRequests = facade.findMyRequests(staffMemberId);

        for (LeaveRequestDTO existing : existingRequests) {
            if (!"PENDING".equals(existing.status()) && !"APPROVED".equals(existing.status())) {
                continue;
            }

            boolean overlaps = !startDate.isAfter(existing.endDate()) && !endDate.isBefore(existing.startDate());

            if (overlaps) {
                throw new ResponseStatusException(HttpStatus.CONFLICT,
                        "Leave request dates overlap with an existing " + existing.status() +
                        " request (" + existing.startDate() + " to " + existing.endDate() + ")." +
                        " Cancel the existing request first or choose different dates.");
            }
        }
    }

    private void verifyAllowanceSufficiency(String staffMemberId,
                                             java.time.LocalDate startDate,
                                             java.time.LocalDate endDate) {
        try {
            LeaveAllowanceDTO allowance = facade.findMyAllowanceInternal(staffMemberId);
            int requestedDays = new DateRange(startDate, endDate).workingDays();

            if (requestedDays > allowance.availableDays()) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "Insufficient leave balance. You have " + allowance.availableDays() +
                        " days available but requested " + requestedDays + " days.");
            }
        } catch (ResponseStatusException e) {
            throw e;
        } catch (Exception e) {
        }
    }
}
