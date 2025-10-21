package org.sp.payroll_service.api.payroll.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import org.sp.payroll_service.domain.common.dto.response.Money;
import org.sp.payroll_service.domain.common.enums.PayrollStatus;

import java.util.List;
import java.util.UUID;

/**
 * DTO representing the result of payroll processing.
 */
@Builder
@Schema(description = "Result of payroll processing operation")
public record PayrollResult(
    
    @Schema(description = "Whether the operation was successful")
    boolean success,
    
    @Schema(description = "Payroll batch ID")
    UUID batchId,
    
    @Schema(description = "Final transactionStatus of the batch")
    PayrollStatus batchStatus,
    
    @Schema(description = "Total amount in the batch")
    Money totalAmount,
    
    @Schema(description = "Amount successfully processed")
    Money processedAmount,
    
    @Schema(description = "Amount that failed to process")
    Money failedAmount,
    
    @Schema(description = "Number of employees in batch")
    Integer totalEmployees,
    
    @Schema(description = "Number of successful payments")
    Integer successfulPayments,
    
    @Schema(description = "Number of failed payments")
    Integer failedPayments,
    
    @Schema(description = "Company account balance before processing")
    Money companyBalanceBefore,
    
    @Schema(description = "Company account balance after processing")
    Money companyBalanceAfter,
    
    @Schema(description = "List of individual payroll items")
    List<PayrollItemResponse> payrollItems,
    
    @Schema(description = "Any error messages")
    List<String> errorMessages,
    
    @Schema(description = "Processing summary message")
    String message
) {}