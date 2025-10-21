package org.sp.payroll_service.domain.payroll.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.sp.payroll_service.domain.common.entity.BaseEntity;

import java.math.BigDecimal;

/**
 * Configurable salary distribution formula entity.
 * Allows dynamic configuration of salary calculation rules.
 */
@Entity
@Table(name = "salary_distribution_formulas")
@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@SuperBuilder
public class SalaryDistributionFormula extends BaseEntity {

    @Column(nullable = false)
    private String name;

    /**
     * Base grade for salary calculation (e.g., Grade 6)
     */
    @Column(name = "base_salary_grade", nullable = false)
    private Integer baseSalaryGrade;

    /**
     * HRA percentage (e.g., 0.20 for 20%)
     */
    @Column(name = "hra_percentage", nullable = false, precision = 5, scale = 4)
    private BigDecimal hraPercentage;

    /**
     * Medical allowance percentage (e.g., 0.15 for 15%)
     */
    @Column(name = "medical_percentage", nullable = false, precision = 5, scale = 4)
    private BigDecimal medicalPercentage;

    /**
     * Amount increment per grade level (e.g., 5000.00)
     */
    @Column(name = "grade_increment_amount", nullable = false, precision = 10, scale = 2)
    private BigDecimal gradeIncrementAmount;

    /**
     * Calculate basic salary for a given grade.
     * Formula: Grade6Base + (baseSalaryGrade - gradeNumber) × gradeIncrementAmount
     */
    public BigDecimal calculateBasicSalary(Integer gradeNumber, BigDecimal baseSalary) {
        if (gradeNumber == null || baseSalary == null) {
            throw new IllegalArgumentException("Grade number and base salary cannot be null");
        }
        
        BigDecimal gradeMultiplier = BigDecimal.valueOf(baseSalaryGrade - gradeNumber);
        return baseSalary.add(gradeMultiplier.multiply(gradeIncrementAmount));
    }

    /**
     * Calculate HRA amount based on basic salary.
     */
    public BigDecimal calculateHRA(BigDecimal basicSalary) {
        if (basicSalary == null) return BigDecimal.ZERO;
        return basicSalary.multiply(hraPercentage);
    }

    /**
     * Calculate medical allowance based on basic salary.
     */
    public BigDecimal calculateMedicalAllowance(BigDecimal basicSalary) {
        if (basicSalary == null) return BigDecimal.ZERO;
        return basicSalary.multiply(medicalPercentage);
    }

    /**
     * Calculate total gross salary.
     */
    public BigDecimal calculateGrossSalary(BigDecimal basicSalary) {
        if (basicSalary == null) return BigDecimal.ZERO;
        
        BigDecimal hra = calculateHRA(basicSalary);
        BigDecimal medical = calculateMedicalAllowance(basicSalary);
        
        return basicSalary.add(hra).add(medical);
    }
}