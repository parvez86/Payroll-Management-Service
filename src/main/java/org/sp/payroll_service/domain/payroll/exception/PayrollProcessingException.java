package org.sp.payroll_service.domain.payroll.exception;

import org.sp.payroll_service.domain.common.exception.BusinessException;
import org.sp.payroll_service.domain.common.exception.ErrorCategory;
import org.sp.payroll_service.domain.common.exception.ErrorCodes;
import org.springframework.http.HttpStatus;

import java.io.Serial;
import java.util.UUID;

/**
 * Exception thrown when payroll processing fails.
 */
public class PayrollProcessingException extends BusinessException {
    @Serial
    private static final long serialVersionUID = 1L;

    public PayrollProcessingException(String message) {
        super(message, ErrorCodes.PAYROLL_PROCESS_ERROR, ErrorCategory.BUSINESS, HttpStatus.BAD_REQUEST);
    }

    public PayrollProcessingException(String message, Throwable cause) {
        super(message, cause);
    }

    public PayrollProcessingException(UUID companyId, UUID batchId) {
        super(String.format("The payroll process failed unexpectedly during execution. " +
                        "Review logs for details. Company Id: %s, Batch ID: %s.",
                companyId.toString(), batchId.toString()), ErrorCodes.PAYROLL_PROCESS_ERROR, ErrorCategory.BUSINESS, HttpStatus.BAD_REQUEST);
    }

    public PayrollProcessingException(UUID companyId, UUID batchId, UUID payrollBatchItemId) {
        super(String.format("The payroll process failed unexpectedly during execution. " +
                        "Review logs for details. Company Id: %s, Batch ID: %s, Batch item ID: %s.",
                companyId.toString(), batchId.toString(), payrollBatchItemId.toString()), ErrorCodes.PAYROLL_PROCESS_ERROR, ErrorCategory.BUSINESS, HttpStatus.BAD_REQUEST);
    }

    public PayrollProcessingException(String message, String errorCode) {
        super(message, errorCode, ErrorCategory.BUSINESS, HttpStatus.BAD_REQUEST);
    }
}