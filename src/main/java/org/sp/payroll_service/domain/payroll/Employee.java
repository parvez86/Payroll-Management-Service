package org.sp.payroll_service.domain.payroll;

import jakarta.persistence.*;
import jakarta.validation.constraints.Size;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.sp.payroll_service.domain.auth.User;
import org.sp.payroll_service.domain.common.entity.BaseEntity;
import org.sp.payroll_service.domain.core.Grade;
import org.sp.payroll_service.domain.wallet.Account;

@Entity
@Table(name = "employees")
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@ToString(callSuper = true)
public class Employee extends BaseEntity {

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", referencedColumnName = "id", unique = true, nullable = false)
    private User user;

    @Column(name = "biz_id", unique = true, nullable = false)
    @Size(min = 4,  max = 4)
    private String bizId;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "account_id", referencedColumnName = "id", unique = true, nullable = false)
    private Account account; // Link to the employee's wallet/savings account

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "grade_id", nullable = false)
    private Grade grade; // Link to the grade for salary calculation

    @Column(name = "employee_code", unique = true, length = 4, nullable = false)
    private String employeeCode; // The unique 4-char business key

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "address")
    private String address;

    @Column(name = "mobile", length = 20)
    private String mobile;
}
