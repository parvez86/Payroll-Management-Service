package org.sp.payroll_service.domain.payroll.exception;

/**
 * Exception thrown when company account has insufficient funds for payroll processing.
 */
public class InsufficientFundsException extends RuntimeException {
    
    public InsufficientFundsException(String message) {
        super(message);
    }
    
    public InsufficientFundsException(String message, Throwable cause) {
        super(message, cause);
    }
}