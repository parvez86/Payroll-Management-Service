package org.sp.payroll_service.domain.common.exception;

import lombok.Getter;

@Getter
public enum ErrorCategory {
    AUTHENTICATION("Authentication Error"),
    AUTHORIZATION("Authorization Error"),
    SECURITY("Security Error"),
    VALIDATION("Validation Error"),
    BUSINESS("Business Error"),
    SYSTEM("System Error"),
    EXTERNAL("External System Error"),
    INTEGRATION("Integration Error"),
    RESOURCE("Resource Error"),;

    private final String description;

    ErrorCategory(String description) {
        this.description = description;
    }

}