package org.sp.payroll_service.api.payroll.mapper;

import org.sp.payroll_service.api.payroll.dto.TransactionResponse;
import org.sp.payroll_service.domain.common.dto.response.AuditInfo;
import org.sp.payroll_service.domain.common.dto.response.Money;
import org.sp.payroll_service.domain.payroll.entity.Transaction;
import org.springframework.stereotype.Component;

/**
 * Mapper class for converting between Transaction entities and DTOs.
 */
@Component
public class TransactionMapper {

    public TransactionResponse toResponse(Transaction transaction) {
        return TransactionResponse.builder()
                .id(transaction.getId())
                .amount(Money.of(transaction.getAmount()))
                .type(transaction.getType())
                .category(transaction.getCategory())
                .status(transaction.getStatus())
                .transactionStatus(transaction.getTransactionStatus())
                .debitAccountId(transaction.getDebitAccount() != null ? transaction.getDebitAccount().getId() : null)
                .debitAccountName(transaction.getDebitAccount() != null ? transaction.getDebitAccount().getAccountName() : null)
                .creditAccountId(transaction.getCreditAccount() != null ? transaction.getCreditAccount().getId() : null)
                .creditAccountName(transaction.getCreditAccount() != null ? transaction.getCreditAccount().getAccountName() : null)
                .payrollBatchId(transaction.getPayrollBatch() != null ? transaction.getPayrollBatch().getId() : null)
                .referenceId(transaction.getReferenceId())
                .description(transaction.getDescription())
                .requestedAt(transaction.getRequestedAt())
                .processedAt(transaction.getProcessedAt())
                .auditInfo(AuditInfo.builder()
                        .createdAt(transaction.getCreatedAt())
                        .lastModifiedAt(transaction.getUpdatedAt())
                        .createdBy(transaction.getCreatedBy() != null ? transaction.getCreatedBy().toString() : null)
                        .lastModifiedBy(transaction.getUpdatedBy() != null ? transaction.getUpdatedBy().toString() : null)
                        .version(transaction.getVersion())
                        .build())
                .build();
    }
}