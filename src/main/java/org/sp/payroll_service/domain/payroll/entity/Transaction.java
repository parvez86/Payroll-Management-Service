package org.sp.payroll_service.domain.payroll.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.sp.payroll_service.domain.common.entity.BaseEntity;
import org.sp.payroll_service.domain.common.enums.TransactionCategory;
import org.sp.payroll_service.domain.common.enums.TransactionStatus;
import org.sp.payroll_service.domain.common.enums.TransactionType;
import org.sp.payroll_service.domain.wallet.entity.Account;

import java.math.BigDecimal;
import java.time.Instant;

/**
 * Enhanced transaction entity supporting double-entry accounting.
 * Records atomic financial movements with proper debit/credit structure.
 */
@Entity
@Table(name = "transactions")
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class Transaction extends BaseEntity {

    /**
     * Account being debited (money going out).
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "debit_acc_id")
    private Account debitAccount;

    /**
     * Account being credited (money coming in).
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "credit_acc_id")
    private Account creditAccount;

    @Column(name = "amount", precision = 15, scale = 2, nullable = false)
    private BigDecimal amount;

    @Enumerated(EnumType.STRING)
    @Column(name = "transaction_status", nullable = false)
    @Builder.Default
    private TransactionStatus transactionStatus = TransactionStatus.PENDING;

    @Column(name = "requested_at")
    @Builder.Default
    private Instant requestedAt = Instant.now();

    @Column(name = "processed_at")
    private Instant processedAt;

    /**
     * Link back to the specific payroll item for traceability.
     */
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "source_item_id", unique = true)
    private PayrollItem sourceItem;

    // Legacy fields for backward compatibility
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "account_id")
    private Account account;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "batch_id")
    private PayrollBatch payrollBatch;

    @Enumerated(EnumType.STRING)
    @Column(name = "type")
    private TransactionType type;

    @Enumerated(EnumType.STRING)
    @Column(name = "category")
    private TransactionCategory category;

    @Column(name = "reference_id")
    private String referenceId;

    @Column(name = "description")
    private String description;

    @Column(name = "failure_reason")
    private String failureReason;

    /**
     * Mark transaction as processed successfully.
     */
    public void markAsProcessed() {
        this.transactionStatus = TransactionStatus.COMPLETED;
        this.processedAt = Instant.now();
    }

    /**
     * Mark transaction as failed.
     */
    public void markAsFailed() {
        this.transactionStatus = TransactionStatus.FAILED;
        this.processedAt = Instant.now();
    }
}
