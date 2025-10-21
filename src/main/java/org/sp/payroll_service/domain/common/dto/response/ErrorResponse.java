package org.sp.payroll_service.domain.common.dto.response;

import lombok.Builder;
import org.sp.payroll_service.domain.common.exception.ErrorCategory;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

/**
 * Standardized DTO for all error responses returned by the API, defined as an
 * immutable Java Record for conciseness and safety.
 */
@Builder
public record ErrorResponse(
        // --- Standard HTTP Response Fields ---
        Instant timestamp,

        String path,

        String errorId, // Unique ID for this specific error occurrence

        // --- Custom Error Fields ---
        String errorCode, // E.g., "VALIDATION_REQUIRED_FIELD"

        String message,   // Human-readable message

        ErrorCategory category, // E.g., VALIDATION, BUSINESS, SYSTEM

        String traceId,   // Unique ID for logging and tracing (if internal error)

        Map<String, Object> details // Dynamic map for validation errors, remote transactionStatus, etc.
) {
    /**
     * Centralized factory to construct the ErrorResponse from exception data.
     * This ensures the record is built with default, required values.
     */
    public static ErrorResponse buildFromException(
            String errorCode,
            String errorMessage,
            ErrorCategory category,
            String requestPath,
            Map<String, Object> errorDetails,
            String traceId
    ) {
        // Records have an implicit constructor, but we use this static method
        // to manage default values (timestamp, errorId).
        return new ErrorResponse(
                Instant.now(),
                requestPath,
                UUID.randomUUID().toString(),
                errorCode,
                errorMessage,
                category,
                traceId,
                errorDetails
        );
    }
}