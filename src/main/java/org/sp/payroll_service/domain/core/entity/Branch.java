package org.sp.payroll_service.domain.core.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import lombok.EqualsAndHashCode;
import org.sp.payroll_service.domain.common.entity.BaseEntity;

/**
 * Reference entity for a specific bank branch.
 */
@Entity
@Table(name = "branch")
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class Branch extends BaseEntity {

    @Column(name = "branch_name", nullable = false)
    private String branchName;
    
    @Column(name = "address")
    private String address;

    // Foreign Key back to the parent Bank
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "bank_id", nullable = false)
    private Bank bank;
}
