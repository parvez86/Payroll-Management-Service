package org.sp.payroll_service.domain.common.enums;

/**
 * Defines access scope for company user roles.
 * Used to restrict what areas of company operations a user can access.
 */
public enum AccessScope {
    /**
     * Full access to all company operations
     */
    FULL,
    
    /**
     * Access limited to financial operations only
     */
    FINANCE_ONLY,
    
    /**
     * Access limited to payroll operations only
     */
    PAYROLL_ONLY,
    
    /**
     * Access limited to HR/employee management only
     */
    HR_ONLY,
    
    /**
     * Read-only access across all areas
     */
    READ_ONLY
}
