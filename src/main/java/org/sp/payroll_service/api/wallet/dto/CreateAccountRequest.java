package org.sp.payroll_service.api.wallet.dto;

import jakarta.validation.constraints.*;
import org.sp.payroll_service.domain.common.enums.AccountType;
import org.sp.payroll_service.domain.common.enums.OwnerType;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Bank account creation request record.
 * This DTO is used to create a new financial account (wallet/bank linkage) for an owner.
 */
public record CreateAccountRequest(
        // --- Owner Details ---
        @NotNull(message = "Owner type is required.")
        OwnerType ownerType, // COMPANY or EMPLOYEE

        @NotNull(message = "Owner ID is required.")
        UUID ownerId, // ID of the Company or Employee

        @NotNull(message = "Account type is required (e.g., SAVINGS, CHECKING).")
        AccountType accountType,

        @NotBlank(message = "Account name (Holder name) is required.")
        @Size(max = 100, message = "Account name must be less than 100 characters.")
        String accountName,

        // --- Account Number (Validation Refinement) ---
        @NotBlank(message = "Account number is required and unique.")
        @Size(min = 10, max = 38, message = "Account number length is invalid.")
        String accountNumber,

        @NotNull(message = "Overdraft limit is required.")
        @DecimalMin(value = "0.00", inclusive = true, message = "Overdraft limit cannot be negative.")
        BigDecimal overdraftLimit,

        @NotNull(message = "Branch ID is required for bank branch linkage.")
        UUID branchId
) {}
