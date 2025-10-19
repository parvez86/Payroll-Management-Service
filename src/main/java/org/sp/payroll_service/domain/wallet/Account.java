package org.sp.payroll_service.domain.wallet;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.DynamicUpdate;
import org.sp.payroll_service.domain.common.enums.AccountType;
import org.sp.payroll_service.domain.common.entity.BaseEntity;
import org.sp.payroll_service.domain.common.enums.OwnerType;
import org.sp.payroll_service.domain.core.Branch;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * The centralized account/wallet for both the Company and Employees.
 * Inherits id (UUID), version, createdAt, and updatedAt from BaseEntity.
 */
@Entity
@Table(name = "accounts")
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
@DynamicUpdate
@SuperBuilder
public class Account extends BaseEntity {

    @Enumerated(EnumType.STRING)
    @Column(name = "owner_type", nullable = false)
    private OwnerType ownerType; // COMPANY or EMPLOYEE

    @Column(name = "owner_id", nullable = false)
    private UUID ownerId; // ID of the Company or Employee

    @Enumerated(EnumType.STRING)
    @Column(name = "account_type", nullable = false)
    private AccountType accountType;

    @Column(name = "account_name")
    private String accountName;

    @Column(name = "account_number", unique = true, nullable = false)
    private String accountNumber;

    @Builder.Default
    @Column(name = "current_balance", precision = 19, scale = 4, nullable = false)
    private BigDecimal currentBalance = BigDecimal.ZERO;

    @Builder.Default
    @Column(name = "overdraft_limit", precision = 19, scale = 4, nullable = false)
    private BigDecimal overdraftLimit = BigDecimal.ZERO;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "branch_id", nullable = false)
    private Branch branch;
}
