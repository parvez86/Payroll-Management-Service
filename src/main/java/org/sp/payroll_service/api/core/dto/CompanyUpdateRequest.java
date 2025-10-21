package org.sp.payroll_service.api.core.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.util.UUID;

/**
 * DTO for updating an existing Company.
 */
public record CompanyUpdateRequest(
    @Size(max = 100, message = "Name must be less than 100 characters.")
    String name,
    
    @Size(max = 500, message = "Description must be less than 500 characters.")
    String description,
    
    // UUID of the SalaryDistributionFormula entity
    UUID salaryFormulaId
) {}