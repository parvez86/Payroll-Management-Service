package org.sp.payroll_service.domain.core.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.sp.payroll_service.domain.auth.entity.User;
import org.sp.payroll_service.domain.common.entity.BaseEntity;
import org.sp.payroll_service.domain.common.enums.AccessScope;
import org.sp.payroll_service.domain.common.enums.CompanyRoleType;

import java.time.Instant;

/**
 * Association entity linking Companies to Users with role-based authorization.
 * Supports multiple users per company, delegation, time-bound access, and auditing.
 */
@Entity
@Table(name = "company_user_roles", indexes = {
    @Index(name = "idx_company_user_active", columnList = "company_id, user_id, active"),
    @Index(name = "idx_user_active", columnList = "user_id, active"),
    @Index(name = "idx_company_role", columnList = "company_id, role_on_company")
})
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@ToString(callSuper = true)
public class CompanyUserRole extends BaseEntity {
    
    /**
     * The company this role applies to
     */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "company_id", nullable = false)
    private Company company;
    
    /**
     * The user who has this role
     */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
    
    /**
     * The role this user has within the company
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "role_on_company", nullable = false, length = 50)
    private CompanyRoleType roleOnCompany;
    
    /**
     * Optional access scope to restrict what operations this user can perform
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "access_scope", length = 50)
    @Builder.Default
    private AccessScope accessScope = AccessScope.FULL;
    
    /**
     * Whether this role assignment is active
     */
    @Column(name = "active", nullable = false)
    @Builder.Default
    private Boolean active = true;
    
    /**
     * Optional start date for time-bound delegations
     */
    @Column(name = "valid_from")
    private Instant validFrom;
    
    /**
     * Optional end date for time-bound delegations
     */
    @Column(name = "valid_to")
    private Instant validTo;


    /**
     * Check if this role is currently valid (active and within validity period if specified)
     */
    public boolean isCurrentlyValid() {
        if (!Boolean.TRUE.equals(active)) {
            return false;
        }
        
        Instant now = Instant.now();
        
        if (validFrom != null && now.isBefore(validFrom)) {
            return false;
        }
        
        if (validTo != null && now.isAfter(validTo)) {
            return false;
        }
        
        return true;
    }
}
