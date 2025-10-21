package org.sp.payroll_service.api.core.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * DTO for setting up the Company's Main Account (Payroll Source).
 * This structure reuses validation rules for linking to a Branch and setting limits.
 */
public record CompanyMainAccountRequest(
        @NotBlank(message = "Account name is required.")
        @Size(max = 100, message = "Account name must be less than 100 characters.")
        String accountName,
        
        @NotBlank(message = "Account number is required and unique.")
        @Size(min = 10, max = 38, message = "Account number length is invalid.")
        String accountNumber,

        @NotNull(message = "Branch ID is required for bank branch linkage.")
        UUID branchId,

        @DecimalMin(value = "0.0", inclusive = true, message = "Initial balance cannot be negative.")
        @NotNull(message = "Initial balance is required (use 0.00 if unknown).")
        BigDecimal initialBalance
) {}