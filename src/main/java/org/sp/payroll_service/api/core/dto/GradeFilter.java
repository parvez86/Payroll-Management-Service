package org.sp.payroll_service.api.core.dto;

import java.util.UUID;

/**
 * DTO for searching/filtering Grade entities.
 */
public record GradeFilter(
        String keyword, // Search by name
        UUID parentId,  // Filter by a specific parent
        Integer minRank // Filter grades above a certain rank
) {}