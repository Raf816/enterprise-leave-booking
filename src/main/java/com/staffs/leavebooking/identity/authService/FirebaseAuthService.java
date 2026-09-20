package com.staffs.leavebooking.identity.authService;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.UserRecord;
import com.google.firebase.auth.UserRecord.CreateRequest;
import com.staffs.leavebooking.identity.dto.LoginResponse;
import com.staffs.leavebooking.identity.security.Role;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;

import java.util.Map;
@Service
@Slf4j
public class FirebaseAuthService {
    private final FirebaseAuth firebaseAuth;
    private final RestClient restClient;
    @Value("${firebase.web-api-key}")
    private String firebaseApiKey;
    public FirebaseAuthService(FirebaseAuth firebaseAuth) {
        this.firebaseAuth = firebaseAuth;
        this.restClient = RestClient.create();
    }
    public UserRecord registerUser(String username, String email,
                                    String password, String role) throws FirebaseAuthException {
        String confirmedRole = (role != null)
                ? Role.fromString(role).name()
                : Role.STAFF.name();

        CreateRequest createRequest = new CreateRequest()
                .setEmail(email)
                .setPassword(password)
                .setDisplayName(username)
                .setEmailVerified(false);

        UserRecord userRecord = firebaseAuth.createUser(createRequest);

        Map<String, Object> customClaims = Map.of(
                "role", confirmedRole,                              // e.g., "ADMIN"
                "admin", confirmedRole.equals(Role.ADMIN.name())
        );
        firebaseAuth.setCustomUserClaims(userRecord.getUid(), customClaims);

        log.info("Registered user {} with role {}", email, confirmedRole);
        return userRecord;
    }
    public LoginResponse loginUser(String email, String password) {
        if (email == null || password == null) {
            throw new IllegalArgumentException("Email and password must not be null");
        }

        String firebaseLoginUrl =
                "https://identitytoolkit.googleapis.com/v1/accounts:signInWithPassword?key=" + firebaseApiKey;

        Map<String, Object> requestBody = Map.of(
                "email", email,
                "password", password,
                "returnSecureToken", true
        );

        try {
            return restClient.post()
                    .uri(firebaseLoginUrl)
                    .body(requestBody)
                    .retrieve()
                    .body(LoginResponse.class);
        } catch (HttpClientErrorException e) {
            log.error("Firebase login failed [{}]: {}", e.getStatusCode(), e.getResponseBodyAsString());
            String errorMessage = extractFirebaseErrorMessage(e.getResponseBodyAsString());
            throw new IllegalArgumentException(errorMessage);
        }
    }
    public UserRecord findUserByEmail(String email) throws FirebaseAuthException {
        return firebaseAuth.getUserByEmail(email);
    }
    public void updateUserRole(String uid, String newRole) throws FirebaseAuthException {
        String confirmedRole = Role.fromString(newRole).name();

        Map<String, Object> customClaims = Map.of(
                "role", confirmedRole,
                "admin", confirmedRole.equals(Role.ADMIN.name())
        );
        firebaseAuth.setCustomUserClaims(uid, customClaims);
        log.info("Updated role for user {} to {}", uid, confirmedRole);
    }
    public void changePassword(String uid, String newPassword) throws FirebaseAuthException {
        if (newPassword == null || newPassword.length() < 6) {
            throw new IllegalArgumentException("Password must be at least 6 characters");
        }
        var updateRequest = new com.google.firebase.auth.UserRecord.UpdateRequest(uid)
                .setPassword(newPassword);
        firebaseAuth.updateUser(updateRequest);
        log.info("Password changed for user {}", uid);
    }
    private String extractFirebaseErrorMessage(String responseBody) {
        if (responseBody != null) {
            if (responseBody.contains("INVALID_LOGIN_CREDENTIALS")) {
                return "Invalid email or password";
            } else if (responseBody.contains("EMAIL_NOT_FOUND")) {
                return "No account found with this email address";
            } else if (responseBody.contains("INVALID_PASSWORD")) {
                return "Incorrect password";
            } else if (responseBody.contains("USER_DISABLED")) {
                return "This account has been disabled";
            } else if (responseBody.contains("TOO_MANY_ATTEMPTS_TRY_LATER")) {
                return "Too many failed login attempts. Please try again later";
            }
        }
        return "Authentication failed. Please check your credentials and try again";
    }
}
