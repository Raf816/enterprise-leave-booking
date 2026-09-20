package com.staffs.leavebooking.identity.authService;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.UserRecord;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
@ExtendWith(MockitoExtension.class)
@DisplayName("Firebase Auth Service")
class FirebaseAuthServiceTest {

    @Mock
    private FirebaseAuth firebaseAuth;

    @InjectMocks
    private FirebaseAuthService firebaseAuthService;

    @Nested
    @DisplayName("registerUser")
    class RegisterUser {

        @Test
        @DisplayName("Should create user in Firebase and set custom claims")
        void shouldCreateUserAndSetClaims() throws FirebaseAuthException {
            UserRecord mockRecord = mock(UserRecord.class);
            when(mockRecord.getUid()).thenReturn("uid-123");
            when(firebaseAuth.createUser(any())).thenReturn(mockRecord);

            UserRecord result = firebaseAuthService.registerUser("testuser", "test@email.com", "pass123", "ADMIN");

            assertNotNull(result);
            assertEquals("uid-123", result.getUid());
            verify(firebaseAuth).createUser(any());
            verify(firebaseAuth).setCustomUserClaims(eq("uid-123"), any());
        }

        @Test
        @DisplayName("Should default to STAFF role when role is null")
        void shouldDefaultToStaffRole() throws FirebaseAuthException {
            UserRecord mockRecord = mock(UserRecord.class);
            when(mockRecord.getUid()).thenReturn("uid-456");
            when(firebaseAuth.createUser(any())).thenReturn(mockRecord);

            firebaseAuthService.registerUser("testuser", "test@email.com", "pass123", null);

            verify(firebaseAuth).setCustomUserClaims(eq("uid-456"), argThat(claims ->
                    "STAFF".equals(claims.get("role")) && Boolean.FALSE.equals(claims.get("admin"))
            ));
        }

        @Test
        @DisplayName("Should throw on invalid role string without creating Firebase account")
        void shouldThrowOnInvalidRole() throws FirebaseAuthException {

            assertThrows(IllegalArgumentException.class,
                    () -> firebaseAuthService.registerUser("testuser", "test@email.com", "pass123", "INVALID_ROLE"));

            verify(firebaseAuth, never()).createUser(any());
        }

        @Test
        @DisplayName("Should propagate FirebaseAuthException on creation failure")
        void shouldPropagateException() throws FirebaseAuthException {
            when(firebaseAuth.createUser(any())).thenThrow(mock(FirebaseAuthException.class));

            assertThrows(FirebaseAuthException.class,
                    () -> firebaseAuthService.registerUser("testuser", "test@email.com", "pass123", "STAFF"));
        }
    }

    @Nested
    @DisplayName("loginUser")
    class LoginUser {

        @Test
        @DisplayName("Should throw IllegalArgumentException when email is null")
        void shouldThrowOnNullEmail() {
            assertThrows(IllegalArgumentException.class,
                    () -> firebaseAuthService.loginUser(null, "password"));
        }

        @Test
        @DisplayName("Should throw IllegalArgumentException when password is null")
        void shouldThrowOnNullPassword() {
            assertThrows(IllegalArgumentException.class,
                    () -> firebaseAuthService.loginUser("email@test.com", null));
        }
    }
}
