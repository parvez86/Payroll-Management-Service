package org.sp.payroll_service.domain.payroll.exception;

import org.sp.payroll_service.domain.common.exception.BusinessException;
import org.sp.payroll_service.domain.common.exception.ErrorCategory;
import org.sp.payroll_service.domain.common.exception.ErrorCodes;
import org.springframework.http.HttpStatus;

import java.io.Serial;
import java.util.UUID;

/**
 * Exception thrown when company account has insufficient funds for payroll processing.
 */
public class InsufficientFundsException extends BusinessException {
    @Serial
    private static final long serialVersionUID = 1L;


    public InsufficientFundsException(String message) {
        super(message, ErrorCodes.PAYROLL_INSUFFICIENT_FUND, ErrorCategory.BUSINESS, HttpStatus.BAD_REQUEST);
    }

    public InsufficientFundsException(UUID companyId, UUID accountId) {
        super(String.format("Insufficient funds in the company's funding account. Company Id: %s, Funding Account ID: %s.",
                companyId.toString(), accountId.toString()), ErrorCodes.PAYROLL_INSUFFICIENT_FUND, ErrorCategory.BUSINESS, HttpStatus.BAD_REQUEST);
    }

    public InsufficientFundsException(String message, String errorCode) {
        super(message, errorCode, ErrorCategory.BUSINESS, HttpStatus.BAD_REQUEST);
    }

    public InsufficientFundsException(String message, Throwable cause) {
        super(message, cause);
    }
}