package org.sp.payroll_service.api.payroll.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import org.sp.payroll_service.domain.common.dto.response.AuditInfo;
import org.sp.payroll_service.domain.common.dto.response.Money;
import org.sp.payroll_service.domain.common.enums.EntityStatus;
import org.sp.payroll_service.domain.common.enums.TransactionCategory;
import org.sp.payroll_service.domain.common.enums.TransactionStatus;
import org.sp.payroll_service.domain.common.enums.TransactionType;

import java.time.Instant;
import java.util.UUID;

/**
 * DTO representing a transaction response.
 */
@Builder
@Schema(description = "Transaction details")
public record TransactionResponse(
    
    @Schema(description = "Unique identifier of the transaction")
    UUID id,
    
    @Schema(description = "Transaction amount")
    Money amount,
    
    @Schema(description = "Type of transaction")
    TransactionType type,
    
    @Schema(description = "Category of transaction")
    TransactionCategory category,
    
    @Schema(description = "Current transactionStatus of the transaction")
    TransactionStatus transactionStatus,

    @Schema(description = "Current entity status of the transaction")
    EntityStatus status,
    
    @Schema(description = "Debit account ID")
    UUID debitAccountId,
    
    @Schema(description = "Debit account name")
    String debitAccountName,
    
    @Schema(description = "Credit account ID")
    UUID creditAccountId,
    
    @Schema(description = "Credit account name")
    String creditAccountName,
    
    @Schema(description = "Associated payroll batch ID (if applicable)")
    UUID payrollBatchId,
    
    @Schema(description = "Reference ID for external tracking")
    String referenceId,
    
    @Schema(description = "Transaction description")
    String description,
    
    @Schema(description = "When the transaction was requested")
    Instant requestedAt,
    
    @Schema(description = "When the transaction was processed")
    Instant processedAt,
    
    @Schema(description = "Audit information")
    AuditInfo auditInfo
) {}