package org.sp.payroll_service.domain.common.exception;

import org.springframework.http.HttpStatus;

/**
 * Exception thrown when authentication-related errors occur
 */
public class DuplicateEntryException extends BusinessException {
    private static final long serialVersionUID = 1L;

    public DuplicateEntryException(String message) {
        super(message, ErrorCodes.RESOURCE_ALREADY_EXISTS, ErrorCategory.RESOURCE, HttpStatus.CONFLICT);
    }

    public DuplicateEntryException(String message, String errorCode) {
        super(message, errorCode, ErrorCategory.RESOURCE, HttpStatus.CONFLICT);
    }

    public static DuplicateEntryException forEntity(String entityName, String field, Object identifier) {
        return new DuplicateEntryException(
                String.format("%s found with field:%s identifier: %s", entityName, field, identifier)
        );
    }
}
