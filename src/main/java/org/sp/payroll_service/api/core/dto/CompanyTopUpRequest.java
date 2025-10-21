package org.sp.payroll_service.api.core.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;

import java.math.BigDecimal;

/**
 * DTO for company account top-up request.
 */
@Builder
@Schema(description = "Request to add funds to company account")
public record CompanyTopUpRequest(
    
    @NotNull(message = "Amount is required")
    @DecimalMin(value = "0.01", message = "Amount must be greater than zero")
    @Schema(description = "Amount to add to company account", example = "50000.00")
    BigDecimal amount,
    
    @Schema(description = "Reference ID for tracking", example = "TOPUP-2025-10-001")
    String referenceId,
    
    @Schema(description = "Description of the top-up", example = "Monthly funding for payroll")
    String description
) {}