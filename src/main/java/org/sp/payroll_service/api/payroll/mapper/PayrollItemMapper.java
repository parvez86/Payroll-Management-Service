package org.sp.payroll_service.api.payroll.mapper;

import org.sp.payroll_service.api.payroll.dto.PayrollItemResponse;
import org.sp.payroll_service.api.payroll.dto.SalaryCalculation;
import org.sp.payroll_service.domain.common.dto.response.Money;
import org.sp.payroll_service.domain.payroll.entity.Employee;
import org.sp.payroll_service.domain.payroll.entity.PayrollItem;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

/**
 * Mapper class for converting between PayrollItem entities and DTOs.
 */
@Component
public class PayrollItemMapper {

    public PayrollItemResponse toResponse(PayrollItem item) {
        Employee employee = item.getEmployee();
        return PayrollItemResponse.builder()
                .id(item.getId())
                .employeeId(employee.getId())
                .employeeBizId(employee.getCode())
                .employeeName(employee.getName())
                .grade(employee.getGrade() != null ? employee.getGrade().getName() : null)
                .basicSalary(Money.of(item.getBasics()))
                .hra(Money.of(item.getHra()))
                .medicalAllowance(Money.of(item.getMedicalAllowance()))
                .grossSalary(Money.of(item.getGross()))
                .netAmount(Money.of(item.getAmount()))
                .status(item.getPayrollItemStatus())
                .failureReason(item.getFailureReason())
                .accountNumber(employee.getAccount() != null ? employee.getAccount().getAccountNumber() : null)
                .build();
    }

    public SalaryCalculation toSalaryCalculation(Employee employee, PayrollItem item) {
        return SalaryCalculation.builder()
                .employeeId(employee.getId())
                .employeeBizId(employee.getCode())
                .employeeName(employee.getName())
                .gradeName(employee.getGrade() != null ? employee.getGrade().getName() : null)
                .gradeRank(employee.getGrade() != null ? employee.getGrade().getRank() : null)
                .basicSalary(Money.of(item.getBasics()))
                .hra(Money.of(item.getHra()))
                .medicalAllowance(Money.of(item.getMedicalAllowance()))
                .grossSalary(Money.of(item.getGross()))
                .netAmount(Money.of(item.getAmount()))
                .accountId(employee.getAccount() != null ? employee.getAccount().getId() : null)
                .accountNumber(employee.getAccount() != null ? employee.getAccount().getAccountNumber() : null)
                .currentBalance(employee.getAccount() != null ? Money.of(employee.getAccount().getCurrentBalance()) : Money.of(BigDecimal.ZERO))
                .build();
    }
}