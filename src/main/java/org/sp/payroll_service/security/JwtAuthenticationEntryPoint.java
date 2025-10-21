package org.sp.payroll_service.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.sp.payroll_service.domain.common.dto.response.ErrorResponse;
import org.sp.payroll_service.domain.common.exception.ErrorCategory;
import org.sp.payroll_service.domain.common.exception.ErrorCodes;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * Handles unauthorized access attempts (HTTP 401).
 * Used when authentication fails, typically due to a missing or bad JWT.
 */
@Component
@Slf4j
public class JwtAuthenticationEntryPoint implements AuthenticationEntryPoint {

    private final ObjectMapper objectMapper;

    // Constructor injection for ObjectMapper
    public JwtAuthenticationEntryPoint(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public void commence(HttpServletRequest request,
                         HttpServletResponse response,
                         AuthenticationException authException) throws IOException {

        log.warn("Unauthorized access attempt at {} - Message: {}", request.getRequestURI(), authException.getMessage());

        // Create a standard error response
        ErrorResponse errorResponse = ErrorResponse.buildFromException(
                ErrorCodes.AUTH_UNAUTHORIZED,
                "Authentication required or token is invalid/expired.",
                ErrorCategory.SECURITY,
                request.getRequestURI(),
                null,
                null // Trace ID not typically used for 401
        );

        // Set the response transactionStatus and content type
        response.setStatus(HttpStatus.UNAUTHORIZED.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        
        // Write the custom error response body
        objectMapper.writeValue(response.getWriter(), errorResponse);
    }
}