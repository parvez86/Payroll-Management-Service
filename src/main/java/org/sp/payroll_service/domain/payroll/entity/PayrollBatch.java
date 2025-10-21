package org.sp.payroll_service.domain.payroll.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.sp.payroll_service.domain.common.entity.BaseEntity;
import org.sp.payroll_service.domain.common.enums.PayrollStatus;
import org.sp.payroll_service.domain.core.entity.Company;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

/**
 * Represents a single payroll run (e.g., "October 2025 Payroll").
 * Enhanced with company association and user tracking.
 */
@Entity
@Table(name = "payroll_batches")
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class PayrollBatch extends BaseEntity {

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "description")
    private String description;

    @Column(name = "payroll_month", nullable = false)
    private LocalDate payrollMonth;

    @Enumerated(EnumType.STRING)
    @Column(name = "payroll_status", nullable = false)
    @Builder.Default
    private PayrollStatus payrollStatus = PayrollStatus.PENDING;

    // Store the ID of the Company Account that funded this batch
    @Column(name = "funding_account_id", nullable = false)
    private UUID fundingAccountId;

    /**
     * Company that this payroll batch belongs to.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "company_id")
    private Company company;

    /**
     * Total amount for this payroll batch.
     */
    @Column(name = "total_amount", nullable = false, precision = 15, scale = 2)
    @Builder.Default
    private BigDecimal totalAmount = BigDecimal.ZERO;

    /**
     * Total amount for this payroll batch.
     */
    @Column(name = "executed_amount", nullable = false, precision = 15, scale = 2)
    @Builder.Default
    private BigDecimal executedAmount = BigDecimal.ZERO;

    /**
     * Timestamp when the payroll batch was executed.
     */
    @Column(name = "executed_at")
    private Instant executedAt;
}
