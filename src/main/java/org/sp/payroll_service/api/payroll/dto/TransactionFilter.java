package org.sp.payroll_service.api.payroll.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import org.sp.payroll_service.domain.common.enums.TransactionCategory;
import org.sp.payroll_service.domain.common.enums.TransactionStatus;
import org.sp.payroll_service.domain.common.enums.TransactionType;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

/**
 * Filter criteria for transaction queries.
 */
@Builder
@Schema(description = "Filter criteria for transaction searches")
public record TransactionFilter(
    
    @Schema(description = "Filter by transaction type")
    TransactionType type,
    
    @Schema(description = "Filter by transaction category")
    TransactionCategory category,
    
    @Schema(description = "Filter by transaction transactionStatus")
    TransactionStatus status,
    
    @Schema(description = "Filter by debit account ID")
    UUID debitAccountId,
    
    @Schema(description = "Filter by credit account ID")
    UUID creditAccountId,
    
    @Schema(description = "Filter by payroll batch ID")
    UUID payrollBatchId,
    
    @Schema(description = "Filter by minimum amount")
    BigDecimal minAmount,
    
    @Schema(description = "Filter by maximum amount")
    BigDecimal maxAmount,
    
    @Schema(description = "Filter by date range (start)")
    Instant fromDate,
    
    @Schema(description = "Filter by date range (end)")
    Instant toDate,
    
    @Schema(description = "Search by reference ID or description")
    String searchText
) {}