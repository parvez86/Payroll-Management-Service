package org.sp.payroll_service.api.core.dto;

import java.time.Instant;
import java.util.UUID;

/**
 * DTO for returning a Bank entity (Response).
 */
public record BankResponse(
    UUID id,
    String name,
    String swiftCode,
    String countryCode,
    Instant createdAt,
    UUID createdBy
) {}