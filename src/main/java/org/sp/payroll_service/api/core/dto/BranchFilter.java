package org.sp.payroll_service.api.core.dto;

import java.util.UUID;

/**
 * DTO for searching/filtering Branch entities.
 */
public record BranchFilter(
        String keyword, // Search by branchName or address
        UUID bankId // Filter by parent Bank
) {}