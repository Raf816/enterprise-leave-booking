package com.staffs.leavebooking.identity.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class SecurityHeadersFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        response.setHeader("Server", "");
        response.setHeader("X-Powered-By", "");

        response.setHeader("X-Content-Type-Options", "nosniff");

        if (request.getRequestURI().startsWith("/h2-console")) {
            response.setHeader("X-Frame-Options", "SAMEORIGIN");
        } else {
            response.setHeader("X-Frame-Options", "DENY");
        }

        response.setHeader("Strict-Transport-Security", "max-age=31536000; includeSubDomains");

        response.setHeader("Cache-Control", "no-store");

        response.setHeader("X-XSS-Protection", "0");

        filterChain.doFilter(request, response);
    }
}
