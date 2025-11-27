package org.sp.payroll_service.api.payroll.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import org.sp.payroll_service.domain.common.enums.TransactionCategory;
import org.sp.payroll_service.domain.common.enums.TransactionType;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * DTO for requesting money transfer between accounts.
 */
@Builder
@Schema(description = "Request to transfer money between accounts")
public record TransferRequest(
    
    @NotNull(message = "Debit account ID is required")
    @Schema(description = "Account to debit from", example = "123e4567-e89b-12d3-a456-426614174000")
    UUID debitAccountId,
    
    @NotNull(message = "Credit account ID is required")
    @Schema(description = "Account to credit to", example = "123e4567-e89b-12d3-a456-426614174001")
    UUID creditAccountId,
    
    @NotNull(message = "Amount is required")
    @DecimalMin(value = "0.01", message = "Amount must be greater than zero")
    @Schema(description = "Amount to transfer", example = "1500.00")
    BigDecimal amount,
    
    @Schema(description = "Reference ID for tracking", example = "PAY-2025-10-001")
    String referenceId,
    
    @Schema(description = "Description of the transfer", example = "Salary payment for October 2025")
    String description,

    @Schema(description = "Payroll batch ID (for batch-related transfers)", example = "123e4567-e89b-12d3-a456-426614174002")
    UUID payrollBatchId,

    @Schema(description = "Payroll item ID (for linking to specific salary item)", example = "123e4567-e89b-12d3-a456-426614174003")
    UUID payrollItemId,

    @Schema(description = "Transaction type (auto-detected if not specified)", example = "SALARY_DISBURSEMENT")
    TransactionType transactionType,

    @Schema(description = "Transaction category (auto-detected if not specified)", example = "PAYROLL")
    TransactionCategory transactionCategory,

    @Schema(description = "Transaction created by")
    UUID createdBy
) {}