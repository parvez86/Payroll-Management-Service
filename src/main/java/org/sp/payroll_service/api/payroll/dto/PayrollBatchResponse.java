package org.sp.payroll_service.api.payroll.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import org.sp.payroll_service.domain.common.dto.response.AuditInfo;
import org.sp.payroll_service.domain.common.dto.response.Money;
import org.sp.payroll_service.domain.common.enums.PayrollStatus;

import java.time.LocalDate;
import java.util.UUID;

/**
 * DTO representing a payroll batch response.
 */
@Builder
@Schema(description = "Payroll batch details")
public record PayrollBatchResponse(
    
    @Schema(description = "Unique identifier of the payroll batch")
    UUID id,
    
    @Schema(description = "Name of the payroll batch", example = "October 2025 Payroll")
    String name,
    
    @Schema(description = "Month for which payroll is being processed")
    LocalDate payrollMonth,
    
    @Schema(description = "Current transactionStatus of the payroll batch")
    PayrollStatus payrollStatus,
    
    @Schema(description = "Total amount for this payroll batch")
    Money totalAmount,
    
    @Schema(description = "Total amount executed successfully")
    Money executedAmount,
    
    @Schema(description = "Company ID associated with this batch")
    UUID companyId,
    
    @Schema(description = "Company name")
    String companyName,
    
    @Schema(description = "Funding account ID")
    UUID fundingAccountId,
    
    @Schema(description = "Number of employees in this batch")
    Integer employeeCount,
    
    @Schema(description = "Number of successfully processed payments")
    Integer successfulPayments,
    
    @Schema(description = "Number of failed payments")
    Integer failedPayments,
    
    @Schema(description = "Optional description")
    String description,
    
    @Schema(description = "Audit information")
    AuditInfo auditInfo
) {}