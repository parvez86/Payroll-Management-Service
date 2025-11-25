package org.sp.payroll_service.api.payroll.mapper;

import lombok.extern.slf4j.Slf4j;
import org.sp.payroll_service.api.payroll.dto.*;
import org.sp.payroll_service.domain.common.dto.response.AuditInfo;
import org.sp.payroll_service.domain.common.dto.response.Money;
import org.sp.payroll_service.domain.payroll.entity.PayrollBatch;
import org.springframework.stereotype.Component;

/**
 * Mapper class for converting between PayrollBatch entities and DTOs.
 */
@Slf4j
@Component
public class PayrollBatchMapper {

    public PayrollBatchResponse toResponse(PayrollBatch batch, Integer employeeCount, 
                                         Integer successfulPayments, Integer failedPayments,
                                         Money totalAmount, Money executedAmount, Money basicBaseAmount) {
        // Safely extract company info to avoid LazyInitializationException
        String companyId = null;
        String companyName = null;
        try {
            if (batch.getCompany() != null) {
                companyId = batch.getCompany().getId() != null ? batch.getCompany().getId().toString() : null;
                companyName = batch.getCompany().getName();
            }
        } catch (org.hibernate.LazyInitializationException e) {
            log.warn("Company proxy not initialized for batch {}, skipping company details", batch.getId());
        }
        
        return PayrollBatchResponse.builder()
                .id(batch.getId())
                .name(batch.getName())
                .payrollMonth(batch.getPayrollMonth())
                .payrollStatus(batch.getPayrollStatus())
                .totalAmount(totalAmount)
                .executedAmount(executedAmount)
                .basicBaseAmount(basicBaseAmount)
                .companyId(companyId != null ? java.util.UUID.fromString(companyId) : null)
                .companyName(companyName)
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
        // Safely extract company name to avoid LazyInitializationException
        String companyName = null;
        try {
            if (batch.getCompany() != null) {
                companyName = batch.getCompany().getName();
            }
        } catch (org.hibernate.LazyInitializationException e) {
            log.warn("Company proxy not initialized for batch {}, skipping company name", batch.getId());
        }
        
        return PayrollBatchSummary.builder()
                .id(batch.getId())
                .name(batch.getName())
                .payrollMonth(batch.getPayrollMonth())
                .payrollStatus(batch.getPayrollStatus())
                .totalAmount(totalAmount)
                .companyName(companyName)
                .employeeCount(employeeCount)
                .successfulPayments(successfulPayments)
                .failedPayments(failedPayments)
                .build();
    }
}