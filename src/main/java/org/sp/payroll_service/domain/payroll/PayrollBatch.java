package org.sp.payroll_service.domain.payroll;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.sp.payroll_service.domain.common.entity.BaseEntity;
import org.sp.payroll_service.domain.common.enums.PayrollStatus;

import java.time.LocalDate;
import java.util.UUID;

/**
 * Represents a single payroll run (e.g., "October 2025 Payroll").
 * Serves as the high-level orchestrator for the Payroll Saga.
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

    @Column(name = "payroll_month", nullable = false)
    private LocalDate payrollMonth; // Date representing the month this payroll run covers (e.g., 2025-10-01)

    @Enumerated(EnumType.STRING)
    @Column(name = "payroll_status", nullable = false)
    @Builder.Default
    private PayrollStatus payrollStatus = PayrollStatus.PENDING;

    // Store the ID of the Company Account that funded this batch
    @Column(name = "funding_account_id", nullable = false)
    private UUID fundingAccountId;
}
