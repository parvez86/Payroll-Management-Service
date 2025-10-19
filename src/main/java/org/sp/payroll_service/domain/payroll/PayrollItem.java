package org.sp.payroll_service.domain.payroll;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.sp.payroll_service.domain.common.entity.BaseEntity;
import org.sp.payroll_service.domain.common.enums.PayrollItemStatus;

import java.math.BigDecimal;

/**
 * Represents the detailed salary/payslip calculation for one employee in a batch.
 * Links to Transaction(s) that execute the payment.
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

    @Column(name = "net_amount", precision = 19, scale = 4, nullable = false)
    private BigDecimal netAmount;

    @Column(name = "gross_salary", precision = 19, scale = 4)
    private BigDecimal grossSalary;
    
    @Column(name = "basics", precision = 19, scale = 4)
    private BigDecimal basics;
    
    @Column(name = "hra", precision = 19, scale = 4)
    private BigDecimal hra;
    
    @Column(name = "medical_allowance", precision = 19, scale = 4)
    private BigDecimal medicalAllowance;

//    @Column(name = "tax_deduction", precision = 19, scale = 4)
//    private BigDecimal taxDeduction;

    @Enumerated(EnumType.STRING)
    @Column(name = "payroll_item_status", nullable = false)
    @Builder.Default
    private PayrollItemStatus payrollItemStatus = PayrollItemStatus.PENDING;

    @Column(name = "failure_reason")
    private String failureReason;
}