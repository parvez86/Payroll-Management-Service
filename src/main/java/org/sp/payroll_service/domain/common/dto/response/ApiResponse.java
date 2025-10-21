package org.sp.payroll_service.domain.common.dto.response;

import org.sp.payroll_service.utils.RandomUtils;

import java.time.Instant;
import java.util.List;

/**
 * API response wrapper record.
 * @param success operation success transactionStatus
 * @param message response message
 * @param data response data
 * @param errors list of errors
 * @param timestamp response timestamp
 * @param requestId request correlation ID
 */
public record ApiResponse<T>(
    boolean success,
    String message,
    T data,
    List<String> errors,
    Instant timestamp,
    String requestId
) {
    /**
     * Creates successful response.
     * @param data response data
     * @param message success message
     * @return successful API response
     */
    public static <T> ApiResponse<T> success(T data, String message) {
        return new ApiResponse<>(
            true,
            message,
            data,
            List.of(),
            Instant.now(),
            generateRequestId()
        );
    }
    
    /**
     * Creates error response.
     * @param message error message
     * @param errors list of errors
     * @return error API response
     */
    public static <T> ApiResponse<T> error(String message, List<String> errors) {
        return new ApiResponse<>(
            false,
            message,
            null,
            errors,
            Instant.now(),
            generateRequestId()
        );
    }
    
    private static String generateRequestId() {
        return RandomUtils.getRandom();
    }
}