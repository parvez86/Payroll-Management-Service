package org.sp.payroll_service.domain.core.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.sp.payroll_service.domain.common.entity.BaseEntity;
import org.sp.payroll_service.domain.payroll.entity.SalaryDistributionFormula;
import org.sp.payroll_service.domain.wallet.entity.Account;

/**
 * Company entity representing an organization in the payroll system.
 */
@Entity
@Table(name = "companies")
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@ToString(callSuper = true)
public class Company extends BaseEntity {

    @Column(name = "name", unique = true, nullable = false)
    private String name;

    @Column(name = "description")
    private String description;

    /**
     * Reference to the salary calculation formula used by this company.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "salary_formula_id")
    private SalaryDistributionFormula salaryFormula;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "main_account_id", referencedColumnName = "id", nullable = false)
    private Account account;
}
