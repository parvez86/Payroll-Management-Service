package org.sp.payroll_service.api.payroll.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import org.sp.payroll_service.domain.common.dto.response.Money;

import java.util.UUID;

/**
 * DTO representing salary calculation for an employee.
 */
@Builder
@Schema(description = "Salary calculation breakdown for an employee")
public record SalaryCalculation(
    
    @Schema(description = "Employee ID")
    UUID employeeId,
    
    @Schema(description = "Employee business ID")
    String employeeBizId,
    
    @Schema(description = "Employee name")
    String employeeName,
    
    @Schema(description = "Grade name")
    String gradeName,
    
    @Schema(description = "Grade rank (1-6)")
    Integer gradeRank,
    
    @Schema(description = "Basic salary amount")
    Money basicSalary,
    
    @Schema(description = "HRA allowance (percentage of basic)")
    Money hra,
    
    @Schema(description = "Medical allowance (percentage of basic)")
    Money medicalAllowance,
    
    @Schema(description = "Gross salary (basic + allowances)")
    Money grossSalary,
    
    @Schema(description = "Net amount after deductions")
    Money netAmount,
    
    @Schema(description = "Employee account ID")
    UUID accountId,
    
    @Schema(description = "Employee account number")
    String accountNumber,
    
    @Schema(description = "Current account balance")
    Money currentBalance
) {}