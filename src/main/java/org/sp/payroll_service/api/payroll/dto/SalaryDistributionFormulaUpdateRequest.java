package org.sp.payroll_service.api.payroll.dto;

import jakarta.validation.constraints.*;

import java.math.BigDecimal;

/**
 * DTO for updating an existing salary distribution formula configuration.
 */
public record SalaryDistributionFormulaUpdateRequest(
    @NotBlank(message = "Formula name is required.")
    @Size(max = 100, message = "Name must be less than 100 characters.")
    String name,
    
    @NotNull(message = "Base salary grade is required.")
    @Min(value = 1, message = "Base grade must be 1 or greater.")
    Integer baseSalaryGrade,

    @NotNull(message = "HRA percentage is required.")
    @DecimalMin(value = "0.0000", inclusive = true, message = "HRA percentage cannot be negative.")
    @DecimalMax(value = "1.0000", inclusive = true, message = "HRA percentage cannot exceed 100% (1.0).")
    BigDecimal hraPercentage,

    @NotNull(message = "Medical percentage is required.")
    @DecimalMin(value = "0.0000", inclusive = true, message = "Medical percentage cannot be negative.")
    @DecimalMax(value = "1.0000", inclusive = true, message = "Medical percentage cannot exceed 100% (1.0).")
    BigDecimal medicalPercentage,

    @NotNull(message = "Grade increment amount is required.")
    @DecimalMin(value = "0.00", inclusive = true, message = "Grade increment amount cannot be negative.")
    BigDecimal gradeIncrementAmount
) {}