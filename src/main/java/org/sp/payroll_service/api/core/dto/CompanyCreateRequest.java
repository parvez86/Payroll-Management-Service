package org.sp.payroll_service.api.core.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.UUID;

/**
 * DTO for creating a new Company.
 */
public record CompanyCreateRequest(
    @NotBlank(message = "Company name is required.")
    @Size(max = 100, message = "Name must be less than 100 characters.")
    String name,
    
    @Size(max = 500, message = "Description must be less than 500 characters.")
    String description,

    @NotNull(message = "Main payroll account setup is required.")
    @Valid
    CompanyMainAccountRequest mainAccountRequest,

    // UUID of the SalaryDistributionFormula entity
    UUID salaryFormulaId
) {}

