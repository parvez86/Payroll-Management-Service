package org.sp.payroll_service.domain.common.exception;

import org.springframework.http.HttpStatus;

/**
 * Exception thrown when authorization-related errors occur
 */
public class AccessDeniedException extends BusinessException {
    private static final long serialVersionUID = 1L;

    public AccessDeniedException(String message) {
        super(message, ErrorCodes.AUTH_UNAUTHORIZED, ErrorCategory.AUTHORIZATION, HttpStatus.FORBIDDEN);
    }

    public AccessDeniedException(String message, String errorCode) {
        super(message, errorCode, ErrorCategory.AUTHORIZATION, HttpStatus.FORBIDDEN);
    }

    public AccessDeniedException(String message, String errorCode, Throwable cause) {
        super(message, errorCode, ErrorCategory.AUTHENTICATION, HttpStatus.FORBIDDEN, cause);
    }
}
