package org.sp.payroll_service.domain.common.exception;

import org.springframework.http.HttpStatus;

/**
 * Exception thrown when authentication-related errors occur
 */
public class AuthenticationException extends BusinessException {
    private static final long serialVersionUID = 1L;

    public AuthenticationException(String message, String errorCode) {
        super(message, errorCode, ErrorCategory.AUTHENTICATION, HttpStatus.UNAUTHORIZED);
    }

    public AuthenticationException(String message, String errorCode, Throwable cause) {
        super(message, errorCode, ErrorCategory.AUTHENTICATION, HttpStatus.UNAUTHORIZED, cause);
    }
}
