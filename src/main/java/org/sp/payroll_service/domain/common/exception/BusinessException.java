package org.sp.payroll_service.domain.common.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

/**
 * Base exception class for all business exceptions in the application
 */
@Getter
public class BusinessException extends RuntimeException {
    private static final long serialVersionUID = 1L;
    
    private final String errorCode;
    private final ErrorCategory category;
    private final HttpStatus status;
    private final transient Object[] args;

    public BusinessException(String message, String errorCode, ErrorCategory category, HttpStatus status, Object... args) {
        super(message);
        this.errorCode = errorCode;
        this.category = category;
        this.status = status;
        this.args = args;
    }

    public BusinessException(String message, String errorCode, ErrorCategory category, HttpStatus status, Throwable cause, Object... args) {
        super(message, cause);
        this.errorCode = errorCode;
        this.category = category;
        this.status = status;
        this.args = args;
    }

    public BusinessException(String message, Throwable cause) {
        this(message, ErrorCodes.SYSTEM_INTERNAL_ERROR, ErrorCategory.SYSTEM, HttpStatus.INTERNAL_SERVER_ERROR, cause);
    }
}
