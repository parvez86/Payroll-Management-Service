package org.sp.payroll_service.api.payroll.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

/**
 * DTO for returning a SalaryDistributionFormula.
 */
public record SalaryDistributionFormulaResponse(
    UUID id,
    String name,
    Integer baseSalaryGrade,
    BigDecimal hraPercentage,
    BigDecimal medicalPercentage,
    BigDecimal gradeIncrementAmount,
    Instant createdAt,
    UUID createdBy
) {
}