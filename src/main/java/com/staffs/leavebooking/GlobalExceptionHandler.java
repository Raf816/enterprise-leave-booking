package com.staffs.leavebooking;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.validation.FieldError;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@ControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleAllExceptions(Exception ex) {
        HttpStatus status = HttpStatus.INTERNAL_SERVER_ERROR;
        String message = ex.getMessage();
        Map<String, String> validationErrors = null;

        if (ex instanceof org.springframework.security.access.AccessDeniedException
                || ex instanceof org.springframework.security.authorization.AuthorizationDeniedException) {
            status = HttpStatus.FORBIDDEN;
            message = "Access denied. You do not have permission to access this resource.";

        } else if (ex instanceof org.springframework.security.core.AuthenticationException) {
            status = HttpStatus.UNAUTHORIZED;
            message = "Authentication required. Please provide a valid Bearer token.";

        } else if (ex instanceof ResponseStatusException rse) {
            status = HttpStatus.valueOf(rse.getStatusCode().value());
            message = rse.getReason();

        } else if (ex instanceof com.staffs.leavebooking.staffmanagement.ui.exceptions.StaffMemberNotFoundException
                || ex instanceof com.staffs.leavebooking.leavemanagement.ui.exceptions.LeaveRequestNotFoundException
                || ex instanceof com.staffs.leavebooking.leavemanagement.ui.exceptions.LeaveAllowanceNotFoundException) {
            status = HttpStatus.NOT_FOUND;
            message = ex.getMessage();

        } else if (ex instanceof MethodArgumentNotValidException manve) {
            status = HttpStatus.BAD_REQUEST;
            message = "Validation failed for one or more fields.";
            validationErrors = manve.getBindingResult().getFieldErrors().stream()
                    .collect(Collectors.toMap(
                            FieldError::getField,
                            error -> Objects.requireNonNullElse(error.getDefaultMessage(), "Invalid value"),
                            (existing, replacement) -> existing
                    ));

        } else if (ex instanceof ConstraintViolationException cve) {
            status = HttpStatus.BAD_REQUEST;
            message = "Database constraint validation failed.";
            validationErrors = cve.getConstraintViolations().stream()
                    .collect(Collectors.toMap(
                            violation -> violation.getPropertyPath().toString(),
                            ConstraintViolation::getMessage
                    ));

        } else if (ex instanceof DataIntegrityViolationException dive) {
            status = HttpStatus.CONFLICT;
            message = dive.getMessage() != null ? dive.getMessage() : "A duplicate record already exists.";

        } else if (ex instanceof IllegalArgumentException) {
            status = HttpStatus.BAD_REQUEST;
            message = ex.getMessage();

        } else if (ex instanceof IllegalStateException) {
            status = HttpStatus.CONFLICT;
            message = ex.getMessage();
        }

        log.error("Exception handled: [{}] {}", status.value(), message, ex);

        Map<String, Object> responseBody = new HashMap<>(Map.of(
                "status", status.value(),
                "error", status.getReasonPhrase(),                                     // Status text (e.g., "Bad Request")
                "message", Objects.requireNonNullElse(message, "No message provided"),
                "timestamp", Instant.now().toString()
        ));

        if (validationErrors != null) {
            responseBody.put("errors", validationErrors); // e.g., { "email": "must not be blank" }
        }

        return ResponseEntity.status(status).body(responseBody);
    }
}
