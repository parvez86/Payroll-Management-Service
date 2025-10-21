package org.sp.payroll_service.domain.common.exception;

/**
 * Exception thrown when a provided security token is invalid, expired, or malformed.
 * Mapped to HTTP 401 UNAUTHORIZED.
 */
public class InvalidTokenException extends AuthenticationException {
    private static final long serialVersionUID = 1L;

    /**
     * Constructs an InvalidTokenException.
     * @param reason The specific reason for the invalid token (e.g., "Expired", "Malformed").
     */
    public InvalidTokenException(String reason) {
        super(
            String.format("Invalid authentication token: %s", reason),
            ErrorCodes.AUTH_TOKEN_INVALID // Assuming you define this specific code
        );
    }

    /**
     * Constructs an InvalidTokenException with a cause.
     */
    public InvalidTokenException(String reason, Throwable cause) {
        super(
            String.format("Invalid authentication token: %s", reason),
            ErrorCodes.AUTH_TOKEN_INVALID,
            cause
        );
    }
}