package com.staffs.leavebooking.identity.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.Instant;
import java.util.Map;

@Component
@Slf4j
public class UnauthorisedAccessLogger implements AuthenticationEntryPoint, AccessDeniedHandler {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response,
                         AuthenticationException authException) throws IOException {

        String clientIp = getClientIp(request);
        String method = request.getMethod();
        String uri = request.getRequestURI();

        log.warn("UNAUTHORISED ACCESS [401] | IP: {} | {} {} | Reason: {}",
                clientIp, method, uri, authException.getMessage());

        writeErrorResponse(response, HttpStatus.UNAUTHORIZED,
                "Authentication required. Please provide a valid Bearer token.");
    }

    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response,
                       AccessDeniedException accessDeniedException) throws IOException {

        String clientIp = getClientIp(request);
        String method = request.getMethod();
        String uri = request.getRequestURI();
        String principal = (request.getUserPrincipal() != null)
                ? request.getUserPrincipal().getName()
                : "anonymous";

        log.warn("FORBIDDEN ACCESS [403] | IP: {} | User: {} | {} {} | Reason: {}",
                clientIp, principal, method, uri, accessDeniedException.getMessage());

        writeErrorResponse(response, HttpStatus.FORBIDDEN,
                "Access denied. You do not have permission to access this resource.");
    }

    private void writeErrorResponse(HttpServletResponse response, HttpStatus status,
                                    String message) throws IOException {
        response.setStatus(status.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);

        Map<String, Object> body = Map.of(
                "status", status.value(),
                "error", status.getReasonPhrase(),  // e.g., "Unauthorized" or "Forbidden"
                "message", message,
                "timestamp", Instant.now().toString()
        );

        objectMapper.writeValue(response.getOutputStream(), body);
    }

    private String getClientIp(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isBlank()) {
            return xForwardedFor.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
}
