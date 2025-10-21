package org.sp.payroll_service.api.wallet.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

/**
 * DTO for performing a balance altering transaction.
 * In a real system, this would trigger a separate Transaction entity creation.
 */
public record AccountTransactionRequest(
    @NotNull(message = "Amount is required.")
    @DecimalMin(value = "0.01", inclusive = true, message = "Transaction amount must be greater than zero.")
    BigDecimal amount,
    
    @NotBlank(message = "Description is required.")
    @Size(max = 255)
    String description
) {}