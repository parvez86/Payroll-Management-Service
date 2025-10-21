package org.sp.payroll_service.api.wallet.dto;

import lombok.Builder;
import org.sp.payroll_service.domain.common.enums.AccountType;
import org.sp.payroll_service.domain.common.enums.EntityStatus;
import org.sp.payroll_service.domain.common.enums.OwnerType;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

/**
 * DTO for returning an Account entity (Response).
 * NOTE: Financial data (balance) is included.
 */
@Builder
public record AccountResponse(
    UUID id,
    OwnerType ownerType,
    UUID ownerId,
    AccountType accountType,
    String accountName,
    String accountNumber,
    BigDecimal currentBalance,
    BigDecimal overdraftLimit,
    UUID branchId,
    String branchName,
    EntityStatus status,// For display convenience
    Instant createdAt,
    UUID createdBy
) {}