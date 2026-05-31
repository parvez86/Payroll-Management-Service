package org.sp.payroll_service.domain.common.enums;

/**
 * Enumeration for user preference scope.
 * Determines what companies a user sees in the UI.
 */
public enum PreferenceScope {
    /**
     * User sees all companies in the system.
     * Only ADMIN users can have GLOBAL scope.
     */
    GLOBAL,
    
    /**
     * User sees a single selected company.
     * EMPLOYER and EMPLOYEE users have COMPANY scope.
     */
    COMPANY
}
