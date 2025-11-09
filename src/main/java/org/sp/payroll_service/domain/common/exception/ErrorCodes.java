package org.sp.payroll_service.domain.common.exception;

/**
 * Centralized error codes for the application
 */
public final class ErrorCodes {
    
    // Authentication & Authorization (10xxx)
    public static final String AUTH_INVALID_CREDENTIALS = "10001";
    public static final String AUTH_TOKEN_EXPIRED = "10002";
    public static final String AUTH_TOKEN_INVALID = "10003";
    public static final String AUTH_UNAUTHORIZED = "10004";
    public static final String AUTH_INSUFFICIENT_PRIVILEGES = "10005";
    public static final String AUTH_ACCOUNT_EXPIRED = "10006";
    public static final String AUTH_ACCOUNT_DISABLED = "10007";
    public static final String AUTH_ACCOUNT_LOCKED = "10008";
    public static final String AUTH_TOKEN_REVOKED = "10009";
    public static final String AUTH_INVALID_TOKEN = "10010";

    // Validation Errors (20xxx)
    public static final String VALIDATION_REQUIRED_FIELD = "20001";
    public static final String VALIDATION_INVALID_FORMAT = "20002";
    public static final String VALIDATION_INVALID_STATE = "20003";
    public static final String VALIDATION_BUSINESS_RULE = "20004";
    public static final String VALIDATION_DUPLICATE_ENTRY = "20005";
    
    // Business Logic Errors (30xxx)
    public static final String BUSINESS_ENTITY_NOT_FOUND = "30001";
    public static final String BUSINESS_DUPLICATE_ENTITY = "30002";
    public static final String BUSINESS_INVALID_STATUS = "30003";
    public static final String BUSINESS_OPERATION_NOT_ALLOWED = "30004";
    
    // Integration/External Errors (40xxx)
    public static final String EXTERNAL_SERVICE_UNAVAILABLE = "40001";
    public static final String EXTERNAL_SERVICE_TIMEOUT = "40002";
    public static final String EXTERNAL_SERVICE_ERROR = "40003";
    public static final String REMOTE_API_ERROR = "40004";
    
    // System Errors (50xxx)
    public static final String SYSTEM_INTERNAL_ERROR = "50001";
    public static final String SYSTEM_DATABASE_ERROR = "50002";
    public static final String SYSTEM_IO_ERROR = "50003";
    public static final String SYSTEM_CONFIGURATION_ERROR = "50004";

    // Resource Errors (60xxx)
    public static final String RESOURCE_NOT_FOUND = "60001";
    public static final String RESOURCE_ALREADY_EXISTS = "60002";
    public static final String RESOURCE_STATE_CONFLICT = "60003";
    public static final String RESOURCE_USER_NOT_FOUND = "60004";
    public static final String RESOURCE_AUTH_ACCOUNT_NOT_FOUND = "60005";

    // Feature-specific codes (70xxx onwards)
    public static final String USER_NOT_FOUND = "70001";
    public static final String USER_ALREADY_EXISTS = "70002";
    public static final String USER_INACTIVE = "70003";
    public static final String USER_LOCKED = "70004";

    // User Management
    public static final String USER_INVALID_PASSWORD = "71001";
    public static final String USER_INVALID_EMAIL = "71002";
    public static final String USER_INVALID_PHONE = "71003";

    // payroll management
    public static final String PAYROLL_INSUFFICIENT_FUND = "81001";
    public static final String PAYROLL_PROCESS_ERROR = "81002";


    
    // Validation Messages
    public static final class Messages {
        public static final String SYSTEM_INTERNAL_ERROR = "An unexpected internal error occurred. Please contact support with the trace ID";
        public static final String AUTH_UNAUTHORIZED = "Authentication failed: invalid credentials or token";
        public static final String AUTH_PROVIDER_UNAUTHORIZED = "Authentication failed: invalid auth provider";
        public static final String AUTH_INSUFFICIENT_PRIVILEGES = "Access denied: insufficient privileges or missing role";
        public static final String AUTH_ACCOUNT_EXPIRED = "AccountEntity has expired and cannot be used for authentication";
        public static final String AUTH_ACCOUNT_DISABLED = "AccountEntity is disabled and cannot be used for authentication";
        public static final String AUTH_ACCOUNT_LOCKED = "AccountEntity is locked due to security policy violation";


        public static final String VALIDATION_REQUIRED_FIELD = "Validation failed for request body";
        public static final String VALIDATION_INVALID_FORMAT = "Invalid request body format: check JSON syntax or data types";
        public static final String VALIDATION_BUSINESS_RULE = "Constraint violation in entity or service layer";
        public static final String VALIDATION_DUPLICATE_ENTRY = "Duplicate entry: resource already exists with the specified unique constraint";


        public static final String REQUIRED_FIELD = "Field '%s' is required";
        public static final String INVALID_FORMAT = "Invalid format for field '%s'";
        public static final String INVALID_LENGTH = "Length of field '%s' must be between %d and %d";
        public static final String INVALID_VALUE = "Invalid value for field '%s'";
        public static final String DUPLICATE_VALUE = "Value '%s' already exists for field '%s'";

        public static final String RESOURCE_NOT_FOUND = "'%s' not found with identifier: '%s'";

        private Messages() {
            // Prevent instantiation
        }
    }

    
    private ErrorCodes() {
        // Prevent instantiation
    }
}
