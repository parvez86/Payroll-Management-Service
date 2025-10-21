package org.sp.payroll_service.domain.common.exception;

import org.springframework.http.HttpStatus;

/**
 * Exception thrown when a requested resource is not found
 */
public class ResourceNotFoundException extends BusinessException {
    private static final long serialVersionUID = 1L;

    public ResourceNotFoundException(String message) {
        super(message, ErrorCodes.RESOURCE_NOT_FOUND, ErrorCategory.RESOURCE, HttpStatus.NOT_FOUND);
    }

    public ResourceNotFoundException(String message, String errorCode) {
        super(message, errorCode, ErrorCategory.RESOURCE, HttpStatus.NOT_FOUND);
    }

    public static ResourceNotFoundException forEntity(String entityName, Object identifier) {
        return new ResourceNotFoundException(
            String.format("%s not found with identifier: %s", entityName, identifier)
        );
    }
}
