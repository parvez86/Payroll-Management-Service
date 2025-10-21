package org.sp.payroll_service.api.payroll.mapper;

import org.sp.payroll_service.api.payroll.dto.*;
import org.sp.payroll_service.domain.common.dto.response.AuditInfo;
import org.sp.payroll_service.domain.common.dto.response.Money;
import org.sp.payroll_service.domain.payroll.entity.PayrollBatch;
import org.springframework.stereotype.Component;

/**
 * Mapper class for converting between PayrollBatch entities and DTOs.
 */
@Component
public class PayrollBatchMapper {

    public PayrollBatchResponse toResponse(PayrollBatch batch, Integer employeeCount, 
                                         Integer successfulPayments, Integer failedPayments,
                                         Money totalAmount, Money executedAmount) {
        return PayrollBatchResponse.builder()
                .id(batch.getId())
                .name(batch.getName())
                .payrollMonth(batch.getPayrollMonth())
                .payrollStatus(batch.getPayrollStatus())
                .totalAmount(totalAmount)
                .executedAmount(executedAmount)
                .companyId(batch.getCompany() != null ? batch.getCompany().getId() : null)
                .companyName(batch.getCompany() != null ? batch.getCompany().getName() : null)
                .fundingAccountId(batch.getFundingAccountId())
                .employeeCount(employeeCount)
                .successfulPayments(successfulPayments)
                .failedPayments(failedPayments)
                .description(batch.getDescription())
                .auditInfo(AuditInfo.builder()
                        .createdAt(batch.getCreatedAt())
                        .lastModifiedAt(batch.getUpdatedAt())
                        .createdBy(batch.getCreatedBy() != null ? batch.getCreatedBy().toString() : null)
                        .lastModifiedBy(batch.getUpdatedBy() != null ? batch.getUpdatedBy().toString() : null)
                        .version(batch.getVersion())
                        .build())
                .build();
    }

    public PayrollBatchSummary toSummary(PayrollBatch batch, Integer employeeCount, 
                                        Integer successfulPayments, Integer failedPayments,
                                        Money totalAmount) {
        return PayrollBatchSummary.builder()
                .id(batch.getId())
                .name(batch.getName())
                .payrollMonth(batch.getPayrollMonth())
                .payrollStatus(batch.getPayrollStatus())
                .totalAmount(totalAmount)
                .companyName(batch.getCompany() != null ? batch.getCompany().getName() : null)
                .employeeCount(employeeCount)
                .successfulPayments(successfulPayments)
                .failedPayments(failedPayments)
                .build();
    }
}