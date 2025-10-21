package org.sp.payroll_service.api.core.dto;

import java.time.Instant;
import java.util.UUID;

/**
 * DTO for returning a Bank Branch entity (Response).
 */
public record BranchResponse(
    UUID id,
    String branchName,
    String address,
    UUID bankId, // Expose the parent Bank ID
    String bankName, // Optional: for display convenience
    Instant createdAt,
    UUID createdBy
) {}