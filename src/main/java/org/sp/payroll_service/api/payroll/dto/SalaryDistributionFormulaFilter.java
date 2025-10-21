package org.sp.payroll_service.api.payroll.dto;

import java.util.UUID;

/**
 * DTO for filtering a list of SalaryDistributionFormula objects.
 * All fields are optional and used for searching/filtering.
 */
public record SalaryDistributionFormulaFilter(
    String name, // For LIKE search (e.g., containing "Standard")
    Integer baseSalaryGrade, // For exact matching
    
    // Optional: add a field to filter by formulas created by a specific user
    UUID createdBy
) {
}