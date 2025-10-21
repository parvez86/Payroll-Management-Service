package org.sp.payroll_service.domain.payroll.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.sp.payroll_service.domain.auth.entity.User;
import org.sp.payroll_service.domain.common.entity.BaseEntity;
import org.sp.payroll_service.domain.core.entity.Company;
import org.sp.payroll_service.domain.core.entity.Grade;
import org.sp.payroll_service.domain.wallet.entity.Account;

@Entity
@Table(name = "employees")
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@ToString(callSuper = true, exclude = {"user", "company", "grade", "account"})
public class Employee extends BaseEntity {

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", referencedColumnName = "id", unique = true, nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "company_id", nullable = false)
    private Company company;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "account_id", referencedColumnName = "id", unique = true, nullable = false)
    private Account account;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "grade_id", nullable = false)
    private Grade grade;

    @Column(name = "code", unique = true, length = 4, nullable = false)
    private String code; // The unique 4-char business key/bizId

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "address")
    private String address;

    @Column(name = "mobile", length = 20)
    private String mobile;
}
