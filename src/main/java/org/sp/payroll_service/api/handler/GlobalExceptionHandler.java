package org.sp.payroll_service.api.handler;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.sp.payroll_service.domain.common.dto.response.ErrorResponse;
import org.sp.payroll_service.domain.common.exception.*; // Assuming your custom exceptions are here
import org.sp.payroll_service.domain.payroll.exception.InsufficientFundsException;
import org.sp.payroll_service.domain.payroll.exception.PayrollProcessingException;
import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.*; // Grouped Spring Security exceptions
import org.springframework.security.core.AuthenticationException;
import org.springframework.transaction.TransactionException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.ServletWebRequest;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Global exception handler for all REST endpoints in the application.
 * <p>
 * This handler intercepts exceptions thrown by controllers and translates them
 * into a standardized {@link ErrorResponse} format for the external API client.
 * It handles common Spring MVC exceptions (via inheritance) as well as
 * custom business, validation, remote, and security exceptions.
 */
@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

    // --- Overridden Spring Handlers (Customizing common errors) ---

    /**
     * Handles exceptions when method arguments fail validation (e.g., using @Valid or @Validated).
     *
     * @param ex      The exception
     * @param headers HTTP headers
     * @param status  HTTP transactionStatus code
     * @param request The current web request
     * @return Standardized error response
     */
    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(
            MethodArgumentNotValidException ex,
            HttpHeaders headers,
            HttpStatusCode status,
            WebRequest request) {

        Map<String, Object> validationErrors = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .collect(Collectors.toMap(
                        FieldError::getField,
                        error -> error.getDefaultMessage() != null ? error.getDefaultMessage() : "Invalid value",
                        (first, second) -> first,
                        HashMap::new
                ));

        ErrorResponse errorResponse = getErrorResponse(
                ErrorCodes.VALIDATION_REQUIRED_FIELD,
                ErrorCodes.Messages.VALIDATION_REQUIRED_FIELD,
                ErrorCategory.VALIDATION,
                getRequestPath(request),
                Map.of("errors", validationErrors),
                null
        );

        log.error("Validation error for path {}: {}", getRequestPath(request), validationErrors);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
    }

    /**
     * Handles exceptions when required request parameters are missing.
     *
     * @param ex      The exception
     * @param headers HTTP headers
     * @param status  HTTP transactionStatus code
     * @param request The current web request
     * @return Standardized error response
     */
    @Override
    protected ResponseEntity<Object> handleMissingServletRequestParameter(
            MissingServletRequestParameterException ex,
            HttpHeaders headers,
            HttpStatusCode status,
            WebRequest request) {

        ErrorResponse errorResponse = getErrorResponse(
                ErrorCodes.VALIDATION_REQUIRED_FIELD,
                String.format("Missing required parameter: %s", ex.getParameterName()),
                ErrorCategory.VALIDATION,
                getRequestPath(request),
                Map.of(),
                null
        );

        log.error("Missing parameter: {}", ex.getParameterName());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
    }

    /**
     * Handles exceptions when the request body cannot be read (e.g., malformed JSON).
     *
     * @param ex      The exception
     * @param headers HTTP headers
     * @param status  HTTP transactionStatus code
     * @param request The current web request
     * @return Standardized error response
     */
    @Override
    protected ResponseEntity<Object> handleHttpMessageNotReadable(
            HttpMessageNotReadableException ex,
            HttpHeaders headers,
            HttpStatusCode status,
            WebRequest request) {

        ErrorResponse errorResponse = getErrorResponse(
                ErrorCodes.VALIDATION_INVALID_FORMAT,
                ErrorCodes.Messages.VALIDATION_INVALID_FORMAT,
                ErrorCategory.VALIDATION,
                getRequestPath(request),
                Map.of(),
                null
        );

        log.error("Message not readable: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
    }

    // --- Custom Business Exception Handlers ---

    /**
     * Handles {@link ResourceNotFoundException} for entities that do not exist. (HTTP 404).
     */
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleResourceNotFoundException(
            ResourceNotFoundException ex, WebRequest request) {
        ErrorResponse errorResponse = getErrorResponse(
                ex.getErrorCode(),
                ex.getMessage(),
                ex.getCategory(),
                getRequestPath(request),
                Map.of(),
                null
        );

        log.warn("Resource not found: {} | Code: {}", ex.getMessage(), ex.getErrorCode());
        return ResponseEntity.status(ex.getStatus()).body(errorResponse);
    }

    /**
     * Handles general {@link ValidationException} for service layer business validation failures. (HTTP 400).
     */
    @ExceptionHandler(ValidationException.class)
    public ResponseEntity<ErrorResponse> handleValidationException(
            ValidationException ex, WebRequest request) {
        ErrorResponse errorResponse = getErrorResponse(
                ex.getErrorCode(),
                ex.getMessage(),
                ex.getCategory(),
                getRequestPath(request),
                Map.of(),
                null
        );

        log.warn("Validation exception: {} | Code: {}", ex.getMessage(), ex.getErrorCode());
        return ResponseEntity.status(ex.getStatus()).body(errorResponse);
    }

    /**
     * Handles general {@link AuthenticationException} for service layer business validation failures. (HTTP 400).
     */
    @ExceptionHandler(org.sp.payroll_service.domain.common.exception.AuthenticationException.class)
    public ResponseEntity<ErrorResponse> handleCustomAuthenticationException(
            org.sp.payroll_service.domain.common.exception.AuthenticationException ex, WebRequest request) {
        ErrorResponse errorResponse = getErrorResponse(
                ex.getErrorCode(),
                ex.getMessage(),
                ex.getCategory(),
                getRequestPath(request),
                Map.of(),
                null
        );

        log.warn("Custom authentication exception: {} | Code: {}", ex.getMessage(), ex.getErrorCode());
        return ResponseEntity.status(ex.getStatus()).body(errorResponse);
    }

    /**
     * Handles {@link BusinessException} for general domain-specific conflicts or rules. (HTTP 409).
     */
    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ErrorResponse> handleBusinessException(BusinessException ex, WebRequest request) {
        ErrorResponse errorResponse = getErrorResponse(
                ex.getErrorCode(),
                ex.getMessage(),
                ex.getCategory(),
                getRequestPath(request),
                Map.of(),
                null
        );

        log.warn("Business exception: {} | Code: {}", ex.getMessage(), ex.getErrorCode());
        return ResponseEntity.status(ex.getStatus()).body(errorResponse);
    }

    /**
     * Handles JPA-level {@link ConstraintViolationException} (e.g., from {@literal @Validated} on service methods). (HTTP 400).
     */
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ErrorResponse> handleConstraintViolation(
            ConstraintViolationException ex, WebRequest request) {

        Map<String, Object> violations = ex.getConstraintViolations()
                .stream()
                .collect(Collectors.toMap(
                        violation -> violation.getPropertyPath().toString(),
                        ConstraintViolation::getMessage,
                        (first, second) -> first,
                        HashMap::new
                ));

        ErrorResponse errorResponse = getErrorResponse(
                ErrorCodes.VALIDATION_BUSINESS_RULE,
                ErrorCodes.Messages.VALIDATION_BUSINESS_RULE,
                ErrorCategory.VALIDATION,
                getRequestPath(request),
                Map.of("violations", violations),
                null
        );

        log.error("Constraint violation: {}", violations);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
    }

    // --- Security and Authentication Handlers ---

    /**
     * Handles access denied exceptions when a user is authenticated but lacks permission. (HTTP 403).
     */
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErrorResponse> handleAccessDeniedException(
            AccessDeniedException ex,
            WebRequest request) {

        ErrorResponse errorResponse = getErrorResponse(
                ErrorCodes.AUTH_INSUFFICIENT_PRIVILEGES,
                ErrorCodes.Messages.AUTH_INSUFFICIENT_PRIVILEGES,
                ErrorCategory.SECURITY,
                getRequestPath(request),
                Map.of(),
                null
        );

        log.warn("Access denied: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(errorResponse);
    }

    /**
     * Handles specific {@link BadCredentialsException} (wrong username/password). (HTTP 401).
     */
    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ErrorResponse> handleBadCredentialsException(
            BadCredentialsException ex,
            WebRequest request) {

        ErrorResponse errorResponse = getErrorResponse(
                ErrorCodes.AUTH_INVALID_CREDENTIALS,
                ErrorCodes.Messages.AUTH_UNAUTHORIZED,
                ErrorCategory.SECURITY,
                getRequestPath(request),
                Map.of(),
                null
        );

        log.warn("Bad credentials attempt: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorResponse);
    }

    /**
     * Handles Spring Security's generic {@link InsufficientAuthenticationException} (missing token). (HTTP 401).
     */
    @ExceptionHandler({InsufficientAuthenticationException.class})
    public ResponseEntity<ErrorResponse> handleInsufficientAuthenticationException(
            InsufficientAuthenticationException ex,
            WebRequest request) {

        ErrorResponse errorResponse = getErrorResponse(
                ErrorCodes.AUTH_INSUFFICIENT_PRIVILEGES,
                "Authentication required to access this resource",
                ErrorCategory.SECURITY,
                getRequestPath(request),
                Map.of(),
                null
        );

        log.warn("Insufficient authentication: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorResponse);
    }

    /**
     * Handles Spring Security's generic {@link InsufficientFundsException} (HTTP 400).
     */
    @ExceptionHandler({InsufficientFundsException.class})
    public ResponseEntity<ErrorResponse> handleInsufficientFundsException(
            InsufficientFundsException ex,
            WebRequest request) {

        ErrorResponse errorResponse = getErrorResponse(
                ErrorCodes.PAYROLL_INSUFFICIENT_FUND,
                ex.getMessage(),
                ex.getCategory(),
                getRequestPath(request),
                Map.of(),
                null
        );

        log.warn("Insufficient authentication: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
    }

    /**
     * Handles Spring Security's generic {@link PayrollProcessingException} (HTTP 400).
     */
    @ExceptionHandler({PayrollProcessingException.class})
    public ResponseEntity<ErrorResponse> handlePayrollProcessingException(
            PayrollProcessingException ex,
            WebRequest request) {

        ErrorResponse errorResponse = getErrorResponse(
                ErrorCodes.PAYROLL_PROCESS_ERROR,
                ex.getMessage(),
                ex.getCategory(),
                getRequestPath(request),
                Map.of(),
                null
        );

        log.warn("Insufficient authentication: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
    }

    /**
     * Handles authentication failures related to account transactionStatus (expired, disabled, locked). (HTTP 401).
     */
    @ExceptionHandler({
            AccountExpiredException.class,
            DisabledException.class,
            LockedException.class
    })
    public ResponseEntity<ErrorResponse> handleAccountStatusExceptions(
            AuthenticationException ex,
            WebRequest request) {

        String errorCode;
        String message;

        if (ex instanceof AccountExpiredException) {
            errorCode = ErrorCodes.AUTH_ACCOUNT_EXPIRED;
            message = ErrorCodes.Messages.AUTH_ACCOUNT_EXPIRED;
        } else if (ex instanceof DisabledException) {
            errorCode = ErrorCodes.AUTH_ACCOUNT_DISABLED;
            message = ErrorCodes.Messages.AUTH_ACCOUNT_DISABLED;
        } else if (ex instanceof LockedException) {
            errorCode = ErrorCodes.AUTH_ACCOUNT_LOCKED;
            message = ErrorCodes.Messages.AUTH_ACCOUNT_LOCKED;
        } else {
            // Fallback for any other AuthenticationException that may hit this handler
            errorCode = ErrorCodes.AUTH_UNAUTHORIZED;
            message = ErrorCodes.Messages.AUTH_UNAUTHORIZED;
        }

        ErrorResponse errorResponse = getErrorResponse(
                errorCode,
                message,
                ErrorCategory.AUTHENTICATION,
                getRequestPath(request),
                Map.of(),
                null
        );

        log.warn("Account transactionStatus authentication error ({}): {}", ex.getClass().getSimpleName(), ex.getMessage());
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorResponse);
    }

    // --- General Runtime and System Handlers ---

    /**
     * Handles {@link IllegalArgumentException} for general runtime input errors. (HTTP 400).
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleIllegalArgumentException(
            IllegalArgumentException ex, WebRequest request) {

        ErrorResponse errorResponse = getErrorResponse(
                ErrorCodes.VALIDATION_INVALID_FORMAT,
                ex.getMessage(),
                ErrorCategory.VALIDATION,
                getRequestPath(request),
                Map.of(),
                null
        );

        log.warn("Illegal argument: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
    }

    /**
     * Handles {@link IllegalStateException} for errors related to invalid application state. (HTTP 409).
     */
    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<ErrorResponse> handleIllegalStateException(
            IllegalStateException ex, WebRequest request) {

        ErrorResponse errorResponse = getErrorResponse(
                ErrorCodes.BUSINESS_INVALID_STATUS,
                ex.getMessage(),
                ErrorCategory.BUSINESS,
                getRequestPath(request),
                Map.of(),
                null
        );

        log.warn("Illegal state: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.CONFLICT).body(errorResponse);
    }

    /**
     * Handles {@link DataAccessException} for database failures. Logs stack trace. (HTTP 500).
     */
    @ExceptionHandler(DataAccessException.class)
    public ResponseEntity<ErrorResponse> handleDataAccessException(
            DataAccessException ex, WebRequest request) {

        String traceId = UUID.randomUUID().toString();

        ErrorResponse errorResponse = getErrorResponse(
                ErrorCodes.SYSTEM_DATABASE_ERROR,
                "Database operation failed",
                ErrorCategory.SYSTEM,
                getRequestPath(request),
                Map.of(),
                traceId
        );

        log.error("Database access error (TraceId: {}): {}", traceId, ex.getMessage(), ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
    }

    /**
     * Handles {@link TransactionException} for transaction management failures. Logs stack trace. (HTTP 500).
     */
    @ExceptionHandler(TransactionException.class)
    public ResponseEntity<ErrorResponse> handleTransactionException(
            TransactionException ex, WebRequest request) {

        String traceId = UUID.randomUUID().toString();

        ErrorResponse errorResponse = getErrorResponse(
                ErrorCodes.SYSTEM_DATABASE_ERROR,
                "Transaction operation failed",
                ErrorCategory.SYSTEM,
                getRequestPath(request),
                Map.of(),
                traceId
        );

        log.error("Transaction error (TraceId: {}): {}", traceId, ex.getMessage(), ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
    }

    // --- Fallback Handler (Catch all uncaught exceptions) ---

    /**
     * Catches all remaining exceptions (the ultimate fallback). Logs stack trace. (HTTP 500).
     *
     * @param ex      The uncaught exception
     * @param request The current web request
     * @return Standardized internal server error response
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleAllUncaughtException(Exception ex, WebRequest request) {

        String traceId = UUID.randomUUID().toString();

        ErrorResponse errorResponse = getErrorResponse(
                ErrorCodes.SYSTEM_INTERNAL_ERROR,
                ErrorCodes.Messages.SYSTEM_INTERNAL_ERROR,
                ErrorCategory.SYSTEM,
                getRequestPath(request),
                Map.of(),
                traceId
        );

        log.error("Uncaught exception: {} (TraceId: {})", ex.getMessage(), traceId, ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
    }

    // --- Helper Methods ---

    /**
     * Retrieves the actual request URI from the web request.
     *
     * @param request The current web request
     * @return The URI path
     */
    private String getRequestPath(WebRequest request) {
        if (request instanceof ServletWebRequest servletWebRequest) {
            return servletWebRequest.getRequest().getRequestURI();
        }
        return request.getDescription(false);
    }

    /**
     * Centralized factory for creating the ErrorResponse DTO.
     */
    protected ErrorResponse getErrorResponse(
            String errorCode,
            String errorMessage,
            ErrorCategory category,
            String requestPath,
            Map<String, Object> errorDetails,
            String traceId
    ) {
        // Assuming ErrorResponse.buildFromException() is correctly implemented
        // to set timestamp and other fields.
        return ErrorResponse.buildFromException(
                errorCode,
                errorMessage,
                category,
                requestPath,
                errorDetails,
                traceId
        );
    }
}