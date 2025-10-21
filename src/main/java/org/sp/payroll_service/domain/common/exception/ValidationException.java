package org.sp.payroll_service.domain.common.exception;

import org.springframework.http.HttpStatus;

/**
 * Exception thrown when a validation error occurs
 */
public class ValidationException extends BusinessException {
    private static final long serialVersionUID = 1L;

    public ValidationException(String message) {
        super(message, ErrorCodes.VALIDATION_BUSINESS_RULE, ErrorCategory.VALIDATION, HttpStatus.BAD_REQUEST);
    }

    public ValidationException(String message, String errorCode) {
        super(message, errorCode, ErrorCategory.VALIDATION, HttpStatus.BAD_REQUEST);
    }

    public ValidationException(String field, String value, String constraint) {
        super(
            String.format("Field '%s' with value '%s' violates constraint: %s", field, value, constraint),
            ErrorCodes.VALIDATION_BUSINESS_RULE,
            ErrorCategory.VALIDATION,
            HttpStatus.BAD_REQUEST
        );
    }
}
