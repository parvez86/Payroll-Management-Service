package org.sp.payroll_service.domain.auth.entity;

import jakarta.persistence.*;
import lombok.*;
import org.sp.payroll_service.domain.common.entity.BaseEntity;
import org.sp.payroll_service.domain.common.enums.PreferenceScope;
import org.sp.payroll_service.domain.common.enums.Theme;
import org.sp.payroll_service.domain.common.exception.ErrorCodes;
import org.sp.payroll_service.domain.common.exception.ValidationException;
import org.sp.payroll_service.domain.core.entity.Company;

import java.util.UUID;

/**
 * User Preferences entity for UI customization and personalization.
 * 
 * One-to-one relationship with User entity.
 * Extends BaseEntity for standard ID and audit field management.
 */
@Entity
@Table(name = "user_preferences")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(callSuper = true)
public class UserPreferences extends BaseEntity {

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;  // OneToOne relationship (UNIQUE FK to User)
    
    // User preference fields
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PreferenceScope selectedScope;  // GLOBAL or COMPANY
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "selected_company_id")
    private Company selectedCompany;  // NULL if GLOBAL scope
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private Theme theme = Theme.LIGHT;  // light | dark | system
    
    @Column(nullable = false, length = 10)
    @Builder.Default
    private String language = "en";  // ISO 639-1 code

    /**
     * Validate that scope and company are consistent
     * (Database level constraint also enforces this)
     */
    @PrePersist
    @PreUpdate
    public void validateScopeCompanyConsistency() {
        if (selectedScope == PreferenceScope.GLOBAL && selectedCompany != null) {
            throw new ValidationException(
                "GLOBAL scope cannot have selected_company_id",
                ErrorCodes.VALIDATION_BUSINESS_RULE
            );
        }
        if (selectedScope == PreferenceScope.COMPANY && selectedCompany == null) {
            throw new ValidationException(
                "COMPANY scope requires selected_company_id",
                ErrorCodes.VALIDATION_BUSINESS_RULE
            );
        }
    }
}
