package org.sp.payroll_service.api.core.dto;

import org.sp.payroll_service.domain.common.enums.EntityStatus;

import java.util.UUID;

/**
 * DTO for searching/filtering Company entities.
 */
public record CompanyFilter(
        String keyword, // Search by name or description
        UUID salaryFormulaId,
        EntityStatus status // Assuming BaseEntity has a transactionStatus field
) {}