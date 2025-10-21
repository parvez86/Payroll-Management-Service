package org.sp.payroll_service.api.payroll.dto;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;

/**
 * DTO for creating or updating a SalaryDistributionFormula.
 */
public record SalaryDistributionFormulaCreateRequest(
    
    @NotBlank(message = "Name cannot be empty")
    @Size(max = 100, message = "Name must be less than 100 characters")
    String name,

    /**
     * Base grade for salary calculation (e.g., 6)
     */
    @NotNull(message = "Base Salary Grade is required")
    @Min(value = 1, message = "Base Grade must be 1 or higher")
    Integer baseSalaryGrade,

    /**
     * HRA percentage (e.g., 0.20 for 20%)
     */
    @NotNull(message = "HRA Percentage is required")
    @DecimalMin(value = "0.0000", inclusive = true, message = "HRA Percentage must be non-negative")
    // Max value is 1.0000 (100%). We'll use 1.0001 to avoid floating point issues.
    @DecimalMax(value = "1.0001", inclusive = false, message = "HRA Percentage must be less than or equal to 100%")
    BigDecimal hraPercentage,

    /**
     * Medical allowance percentage (e.g., 0.15 for 15%)
     */
    @NotNull(message = "Medical Percentage is required")
    @DecimalMin(value = "0.0000", inclusive = true, message = "Medical Percentage must be non-negative")
    @DecimalMax(value = "1.0001", inclusive = false, message = "Medical Percentage must be less than or equal to 100%")
    BigDecimal medicalPercentage,

    /**
     * Amount increment per grade level (e.g., 5000.00)
     */
    @NotNull(message = "Grade Increment Amount is required")
    @DecimalMin(value = "0.00", inclusive = true, message = "Increment Amount must be non-negative")
    // Ensure the scale is respected for amount fields
    @Digits(integer = 8, fraction = 2, message = "Increment Amount must have at most 8 digits and 2 decimals")
    BigDecimal gradeIncrementAmount
) {
}