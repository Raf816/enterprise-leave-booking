package com.staffs.leavebooking.identity;

import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.UserRecord;
import com.staffs.leavebooking.identity.authService.FirebaseAuthService;
import com.staffs.leavebooking.identity.dto.*;
import com.staffs.leavebooking.staffmanagement.StaffManagementFacade;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.web.bind.annotation.*;

import java.util.stream.Collectors;

@RestController
@RequestMapping("/auth")
@AllArgsConstructor
@Slf4j
public class AuthController {

    public static final String USER_CREATED_CONFIRMATION = "User created successfully";

    private final FirebaseAuthService firebaseAuthService;

    private final StaffManagementFacade staffManagementFacade;

    @PostMapping("/register")
    public ResponseEntity<?> register(@jakarta.validation.Valid @RequestBody RegisterRequest request,
                                       Authentication authentication) {
        try {
            String effectiveRole = determineEffectiveRole(request.role(), authentication);

            log.info("Registering user: {} with role: {}", request.email(), effectiveRole);

            UserRecord userRecord = firebaseAuthService.registerUser(
                    request.username(),
                    request.email(),
                    request.password(),
                    effectiveRole
            );

            try {
                staffManagementFacade.createSkeletonStaffMember(
                        userRecord.getUid(),
                        request.username() != null ? request.username().split(" ")[0] : "Unknown",
                        request.username() != null && request.username().contains(" ")
                                ? request.username().substring(request.username().indexOf(" ") + 1)
                                : "Unknown",
                        request.email()
                );
            } catch (Exception e) {
                log.warn("Skeleton staff record creation failed for {}: {} (Firebase user was created successfully)",
                        request.email(), e.getMessage());
            }

            return ResponseEntity.status(HttpStatus.CREATED).body(new RegisterResponse(
                    userRecord.getUid(),
                    userRecord.getEmail(),
                    userRecord.getDisplayName(),
                    USER_CREATED_CONFIRMATION
            ));
        } catch (FirebaseAuthException e) {
            log.error("Registration failed: {}", e.getMessage());
            String cleanMessage = parseFirebaseRegistrationError(e.getMessage(), request.email());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ErrorResponse.of(400, "Bad Request", cleanMessage));
        }
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@jakarta.validation.Valid @RequestBody LoginRequest request) {
        LoginResponse response = firebaseAuthService.loginUser(
                request.emailOrUsername(),
                request.password()
        );
        return ResponseEntity.ok(response);
    }

    @PreAuthorize("isAuthenticated()")
    @GetMapping("/role-check")
    public ResponseEntity<String> roleCheck(Authentication authentication) {
        String roles = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.joining(", "));   // e.g., "ROLE_ADMIN, ROLE_STAFF"
        return ResponseEntity.ok(roles + " access granted");
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/users/{email}")
    public ResponseEntity<?> findUserByEmail(@PathVariable String email) {
        try {
            UserRecord user = firebaseAuthService.findUserByEmail(email);
            return ResponseEntity.ok(java.util.Map.of(
                    "uid", user.getUid(),
                    "email", user.getEmail(),
                    "displayName", user.getDisplayName() != null ? user.getDisplayName() : "",
                    "role", user.getCustomClaims() != null && user.getCustomClaims().get("role") != null
                            ? user.getCustomClaims().get("role") : "STAFF"
            ));
        } catch (FirebaseAuthException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ErrorResponse.of(404, "Not Found", "No user found with email: " + email));
        }
    }

    @PreAuthorize("isAuthenticated()")
    @PatchMapping("/password")
    public ResponseEntity<?> changePassword(Authentication authentication,
                                             @jakarta.validation.Valid @RequestBody ChangePasswordRequest body) {
        try {
            String uid = authentication.getName();

            firebaseAuthService.changePassword(uid, body.newPassword());

            return ResponseEntity.ok(java.util.Map.of(
                    "message", "Password changed successfully"
            ));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ErrorResponse.of(400, "Bad Request", e.getMessage()));
        } catch (FirebaseAuthException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ErrorResponse.of(500, "Internal Server Error",
                            "Failed to change password. Please try again later."));
        }
    }

    private String determineEffectiveRole(String requestedRole, Authentication authentication) {
        if (authentication != null && authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"))) {
            return requestedRole != null ? requestedRole : "STAFF";
        }
        // return "STAFF";

        //temp any role
        return requestedRole != null ? requestedRole : "STAFF";
    }

    private String parseFirebaseRegistrationError(String firebaseMessage, String email) {
        if (firebaseMessage == null) return "Registration failed. Please try again.";
        if (firebaseMessage.contains("EMAIL_EXISTS") || firebaseMessage.contains("email already exists")) {
            return "A user with email " + email + " already exists";
        }
        if (firebaseMessage.contains("INVALID_EMAIL")) {
            return "The email address " + email + " is not valid";
        }
        if (firebaseMessage.contains("WEAK_PASSWORD")) {
            return "The password is too weak. It must be at least 6 characters";
        }
        return "Registration failed. Please check your details and try again.";
    }
}
