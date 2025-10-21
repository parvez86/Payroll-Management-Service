package org.sp.payroll_service.api.payroll.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;

import java.time.LocalDate;
import java.util.UUID;

/**
 * DTO for creating a new payroll batch.
 */
@Builder
@Schema(description = "Request to create a new payroll batch")
public record CreatePayrollBatchRequest(
    
    @NotBlank(message = "Batch name is required")
    @Schema(description = "Name of the payroll batch", example = "October 2025 Payroll")
    String name,
    
    @NotNull(message = "Payroll month is required")
    @Schema(description = "Month for which payroll is being processed", example = "2025-10-01")
    LocalDate payrollMonth,
    
    @NotNull(message = "Company ID is required")
    @Schema(description = "ID of the company for this payroll batch")
    UUID companyId,
    
    @NotNull(message = "Funding account ID is required")
    @Schema(description = "ID of the company account that will fund this payroll")
    UUID fundingAccountId,
    
    @Schema(description = "Optional description for the payroll batch")
    String description
) {}