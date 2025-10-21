package org.sp.payroll_service.domain.payroll.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.sp.payroll_service.domain.core.entity.Grade;
import org.sp.payroll_service.domain.payroll.entity.Employee;
import org.sp.payroll_service.domain.payroll.entity.PayrollItem;
import org.sp.payroll_service.domain.payroll.entity.SalaryDistributionFormula;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Service for calculating employee salaries based on business rules.
 * Implements the configurable salary formula system.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class SalaryCalculationService {

    /**
     * Calculate salary breakdown for an employee based on the salary distribution formula.
     * 
     * Business Rule: Basic(Grade) = Grade6Base + (6 - GradeNumber) × 5000
     * HRA = Basic × 20%
     * Medical = Basic × 15%
     * Gross = Basic + HRA + Medical
     */
    public PayrollItem calculateSalary(Employee employee, SalaryDistributionFormula formula) {
        log.debug("Calculating salary for employee: {} with grade: {}", 
                employee.getCode(), employee.getGrade().getName());

        Grade grade = employee.getGrade();
        
        // Calculate basic salary using the formula
        BigDecimal basicSalary = calculateBasicSalary(grade, formula);
        
        // Calculate allowances
        BigDecimal hra = basicSalary
                .multiply(formula.getHraPercentage())
                .setScale(2, RoundingMode.HALF_UP);
        
        BigDecimal medicalAllowance = basicSalary
                .multiply(formula.getMedicalPercentage())
                .setScale(2, RoundingMode.HALF_UP);
        
        // Calculate gross salary
        BigDecimal grossSalary = basicSalary
                .add(hra)
                .add(medicalAllowance);

        // Create payroll item
        PayrollItem payrollItem = PayrollItem.builder()
                .employee(employee)
                .basics(basicSalary)
                .hra(hra)
                .medicalAllowance(medicalAllowance)
                .gross(grossSalary)
                .amount(grossSalary) // Net = Gross for now (no deductions)
                .build();

        log.debug("Calculated salary for {}: Basic={}, HRA={}, Medical={}, Gross={}", 
                employee.getCode(), basicSalary, hra, medicalAllowance, grossSalary);

        return payrollItem;
    }

    /**
     * Calculate basic salary for a grade using the formula.
     * Formula: Basic(Grade) = Grade6Base + (6 - GradeNumber) × IncrementAmount
     */
    private BigDecimal calculateBasicSalary(Grade grade, SalaryDistributionFormula formula) {
        // Get the base salary for the lowest grade (typically grade 6)
        BigDecimal baseSalary = getBaseSalaryForGrade(formula.getBaseSalaryGrade(), formula);
        
        // Calculate increment based on grade difference
        int gradeDifference = formula.getBaseSalaryGrade() - grade.getRank();
        BigDecimal increment = formula.getGradeIncrementAmount()
                .multiply(BigDecimal.valueOf(gradeDifference));
        
        return baseSalary.add(increment).setScale(2, RoundingMode.HALF_UP);
    }

    /**
     * Get base salary for the specified grade.
     * This could be configurable in the future.
     */
    private BigDecimal getBaseSalaryForGrade(Integer gradeNumber, SalaryDistributionFormula formula) {
        // For now, assume a base amount for grade 6
        // This could be made configurable or stored in the formula
        return BigDecimal.valueOf(30000.00); // Base salary for grade 6
    }

    /**
     * Validate salary calculation inputs.
     */
    public void validateCalculationInputs(Employee employee, SalaryDistributionFormula formula) {
        if (employee == null) {
            throw new IllegalArgumentException("Employee cannot be null");
        }
        if (employee.getGrade() == null) {
            throw new IllegalArgumentException("Employee must have a grade assigned");
        }
        if (formula == null) {
            throw new IllegalArgumentException("Salary distribution formula cannot be null");
        }
        if (formula.getHraPercentage() == null || formula.getHraPercentage().compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("HRA percentage must be valid and non-negative");
        }
        if (formula.getMedicalPercentage() == null || formula.getMedicalPercentage().compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Medical percentage must be valid and non-negative");
        }
        if (formula.getGradeIncrementAmount() == null || formula.getGradeIncrementAmount().compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Grade increment amount must be valid and non-negative");
        }
    }
}