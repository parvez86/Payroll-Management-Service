package org.sp.payroll_service.api.core.dto;

import org.sp.payroll_service.api.wallet.dto.AccountResponse;

import java.time.Instant;
import java.util.UUID;

/**
 * DTO for returning a Company entity (Response).
 */
public record CompanyResponse(
    UUID id,
    String name,
    String description,
    UUID salaryFormulaId, // Return just the ID for simplicity
    AccountResponse mainAccount,
    Instant createdAt,
    UUID createdBy
) {}