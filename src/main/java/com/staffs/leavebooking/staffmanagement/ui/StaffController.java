package com.staffs.leavebooking.staffmanagement.ui;

import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.UserRecord;
import com.staffs.leavebooking.identity.authService.FirebaseAuthService;
import com.staffs.leavebooking.staffmanagement.StaffManagementFacade;
import com.staffs.leavebooking.staffmanagement.application.commands.AddStaffMemberCommand;
import com.staffs.leavebooking.staffmanagement.application.commands.UpdateDepartmentCommand;
import com.staffs.leavebooking.staffmanagement.application.commands.UpdatePlacementCommand;
import com.staffs.leavebooking.staffmanagement.application.commands.UpdateStatusCommand;
import com.staffs.leavebooking.staffmanagement.application.dto.StaffMemberDTO;
import com.staffs.leavebooking.staffmanagement.application.dto.StaffSearchCriteria;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.util.List;
@RestController
@RequestMapping("/staff")
@AllArgsConstructor
@Slf4j
public class StaffController {

    private final StaffManagementFacade facade;
    private final FirebaseAuthService firebaseAuthService;

    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    public List<StaffMemberDTO> getAllStaff() {
        return facade.findAllStaffMembers();
    }
    @GetMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    public StaffMemberDTO getStaffMemberById(@PathVariable String id, Authentication authentication) {
        StaffMemberDTO staff = facade.findStaffMemberById(id);
        if (!isAdmin(authentication)) {
            String requesterId = authentication.getName();
            if (!requesterId.equals(staff.lineManagerId())) {
                throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                        "You can only view staff members assigned to your team.");
            }
        }
        return staff;
    }

    @PostMapping("/search")
    @ResponseStatus(HttpStatus.OK)
    public List<StaffMemberDTO> searchStaff(@RequestBody StaffSearchCriteria criteria) {
        if (!criteria.hasFilters()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "At least one search filter is required (department, status). Use GET /staff for unfiltered results.");
        }
        if (criteria.status() != null && !criteria.status().isBlank()) {
            try {
                com.staffs.leavebooking.staffmanagement.domain.EmploymentStatus.valueOf(criteria.status().toUpperCase());
            } catch (IllegalArgumentException e) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "Invalid status filter: '" + criteria.status() + "'. Valid values are: PENDING_SETUP, ACTIVE, ON_LEAVE, TERMINATED.");
            }
        }
        return facade.searchStaff(criteria);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasRole('ADMIN')")
    public StaffMemberCreatedResponse addStaffMember(@jakarta.validation.Valid @RequestBody AddStaffMemberCommand command) {
        String firebaseUid;

        try {
            new com.staffs.leavebooking.common.domain.FullName(command.firstName(), command.surname());
            new com.staffs.leavebooking.common.domain.Email(command.email());
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
        }

        try {
            String displayName = command.firstName() + " " + command.surname();
            UserRecord userRecord = firebaseAuthService.registerUser(
                    displayName,
                    command.email(),
                    command.effectivePassword(),
                    command.effectiveRole()
            );
            firebaseUid = userRecord.getUid();
            log.info("Firebase user created for {} with UID {}", command.email(), firebaseUid);
        } catch (FirebaseAuthException e) {
            log.error("Failed to create Firebase user for {}: {}", command.email(), e.getMessage());
            String cleanMessage = parseFirebaseError(e.getMessage(), command.email());
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, cleanMessage);
        }

        String staffId = facade.addStaffMemberWithId(firebaseUid, command);
        return StaffMemberCreatedResponse.of(staffId, command.email());
    }
    @PatchMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("hasRole('ADMIN')")
    public StaffMemberDTO updateStaff(
            @PathVariable String id,
            @RequestBody UpdateStaffBody body) {

        if (body.department() != null || body.lineManagerId() != null) {
            facade.updateDepartment(new UpdateDepartmentCommand(
                    id, body.department(), body.lineManagerId()));
        }

        if (body.currentRole() != null || body.jobLevel() != null || body.employmentType() != null) {
            facade.updatePlacement(new UpdatePlacementCommand(
                    id, body.currentRole(), body.startDateOfCurrentRole(),
                    body.jobLevel(), body.employmentType()));
        }

        if (body.employmentStatus() != null) {
            facade.updateStatus(new UpdateStatusCommand(id, body.employmentStatus()));
        }

        if (body.role() != null) {
            try {
                firebaseAuthService.updateUserRole(id, body.role());
                log.info("Firebase role updated to {} for staff member {}", body.role(), id);
            } catch (FirebaseAuthException e) {
                log.error("Failed to update Firebase role for {}: {}", id, e.getMessage());
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "Failed to update role: " + e.getMessage());
            }
        }

        return facade.findStaffMemberById(id);
    }

    private String parseFirebaseError(String firebaseMessage, String email) {
        if (firebaseMessage == null) return "Failed to create user account";
        if (firebaseMessage.contains("EMAIL_EXISTS") || firebaseMessage.contains("email already exists")) {
            return "A user account with email " + email + " already exists";
        }
        if (firebaseMessage.contains("INVALID_EMAIL")) {
            return "The email address " + email + " is not valid";
        }
        if (firebaseMessage.contains("WEAK_PASSWORD")) {
            return "The password is too weak. It must be at least 6 characters";
        }
        return "Failed to create user account. Please check the details and try again";
    }

    public record UpdateStaffBody(
            String department,
            String lineManagerId,
            String currentRole,
            LocalDate startDateOfCurrentRole,
            String jobLevel,
            String employmentType,
            String employmentStatus,
            String role
    ) {}

    private boolean isAdmin(Authentication authentication) {
        return authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
    }
}
