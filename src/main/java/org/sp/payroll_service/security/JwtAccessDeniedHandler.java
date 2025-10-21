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
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * Handles access denied attempts (HTTP 403).
 * Used when an authenticated user lacks the necessary authority/role.
 */
@Component
@Slf4j
public class JwtAccessDeniedHandler implements AccessDeniedHandler {

    private final ObjectMapper objectMapper;

    // Constructor injection for ObjectMapper
    public JwtAccessDeniedHandler(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public void handle(HttpServletRequest request,
                       HttpServletResponse response,
                       AccessDeniedException accessDeniedException) throws IOException {

        log.warn("Access denied for authenticated user at {}: {}", request.getRequestURI(), accessDeniedException.getMessage());

        // Create a standard error response
        ErrorResponse errorResponse = ErrorResponse.buildFromException(
                ErrorCodes.AUTH_INSUFFICIENT_PRIVILEGES,
                "You do not have the required permissions to access this resource.",
                ErrorCategory.SECURITY,
                request.getRequestURI(),
                null,
                null
        );

        // Set the response transactionStatus and content type
        response.setStatus(HttpStatus.FORBIDDEN.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);

        // Write the custom error response body
        objectMapper.writeValue(response.getWriter(), errorResponse);
    }
}