package org.sp.payroll_service.api.payroll.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import org.sp.payroll_service.domain.common.dto.response.Money;
import org.sp.payroll_service.domain.common.enums.PayrollItemStatus;

import java.util.UUID;

/**
 * DTO representing a payroll item response.
 */
@Builder
@Schema(description = "Payroll item details for individual employee")
public record PayrollItemResponse(
    
    @Schema(description = "Unique identifier of the payroll item")
    UUID id,
    
    @Schema(description = "Employee ID")
    UUID employeeId,
    
    @Schema(description = "Employee business ID")
    String employeeBizId,
    
    @Schema(description = "Employee name")
    String employeeName,
    
    @Schema(description = "Employee grade")
    String grade,
    
    @Schema(description = "Basic salary amount")
    Money basicSalary,
    
    @Schema(description = "HRA allowance amount")
    Money hra,
    
    @Schema(description = "Medical allowance amount")
    Money medicalAllowance,
    
    @Schema(description = "Gross salary amount")
    Money grossSalary,
    
    @Schema(description = "Net amount to be paid")
    Money netAmount,
    
    @Schema(description = "Status of this payroll item")
    PayrollItemStatus status,
    
    @Schema(description = "Failure reason if payment failed")
    String failureReason,
    
    @Schema(description = "Employee account number")
    String accountNumber
) {}