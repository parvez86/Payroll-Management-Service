package org.sp.payroll_service.api.wallet.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.sp.payroll_service.domain.common.enums.AccountType;

import java.math.BigDecimal;

/**
 * Bank account creation request record.
 * This DTO is used to create a new financial account (wallet/bank linkage) for an owner.
 */
public record UpdateAccountRequest(
        @NotNull(message = "Account type is required (e.g., SAVINGS, CHECKING).")
        AccountType accountType,

        @NotBlank(message = "Account name (Holder name) is required.")
        @Size(max = 100, message = "Account name must be less than 100 characters.")
        String accountName,

        @NotNull(message = "Overdraft limit is required.")
        @DecimalMin(value = "0.00", inclusive = true, message = "Overdraft limit cannot be negative.")
        BigDecimal overdraftLimit
) {}
