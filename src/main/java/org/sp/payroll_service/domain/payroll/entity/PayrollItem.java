package org.sp.payroll_service.domain.payroll.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.sp.payroll_service.domain.common.entity.BaseEntity;
import org.sp.payroll_service.domain.common.enums.PayrollItemStatus;

import java.math.BigDecimal;
import java.time.Instant;

/**
 * Represents the detailed salary/payslip calculation for one employee in a batch.
 * Enhanced with comprehensive salary breakdown components.
 */
@Entity
@Table(name = "payroll_items")
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class PayrollItem extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "batch_id", nullable = false)
    private PayrollBatch payrollBatch;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employee_id", nullable = false)
    private Employee employee;

    /**
     * Final net amount to be paid to employee (after all calculations).
     */
    @Column(name = "amount", precision = 10, scale = 2, nullable = false)
    private BigDecimal amount;

    /**
     * Basic salary component.
     */
    @Column(name = "basics", precision = 10, scale = 2, nullable = false)
    @Builder.Default
    private BigDecimal basics = BigDecimal.ZERO;

    /**
     * Housing Rent Allowance (HRA) component.
     */
    @Column(name = "hra", precision = 10, scale = 2, nullable = false)
    @Builder.Default
    private BigDecimal hra = BigDecimal.ZERO;

    /**
     * Medical allowance component.
     */
    @Column(name = "medical_allowance", precision = 10, scale = 2, nullable = false)
    @Builder.Default
    private BigDecimal medicalAllowance = BigDecimal.ZERO;

    /**
     * Total gross salary (basics + hra + medical_allowance).
     */
    @Column(name = "gross", precision = 10, scale = 2, nullable = false)
    @Builder.Default
    private BigDecimal gross = BigDecimal.ZERO;

    @Enumerated(EnumType.STRING)
    @Column(name = "payroll_item_status", nullable = false)
    @Builder.Default
    private PayrollItemStatus payrollItemStatus = PayrollItemStatus.PROCESSING;

    @Column(name = "failure_reason")
    private String failureReason;

    /**
     * Timestamp when payment was attempted/executed.
     */
    @Column(name = "executed_at")
    private Instant executedAt;

    /**
     * Calculate and set gross salary based on components.
     */
    public void calculateGross() {
        this.gross = (basics != null ? basics : BigDecimal.ZERO)
                .add(hra != null ? hra : BigDecimal.ZERO)
                .add(medicalAllowance != null ? medicalAllowance : BigDecimal.ZERO);
        
        // Set amount to gross (no deductions for now)
        this.amount = this.gross;
    }
}