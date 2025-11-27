package org.sp.payroll_service.domain.common.exception;

/**
 * Exception thrown when a user attempts an action they are not authorized to perform.
 */
public class AuthorizationException extends RuntimeException {
    
    public AuthorizationException(String message) {
        super(message);
    }
    
    public AuthorizationException(String message, Throwable cause) {
        super(message, cause);
    }
}
