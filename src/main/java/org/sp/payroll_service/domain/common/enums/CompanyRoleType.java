package org.sp.payroll_service.domain.common.enums;

/**
 * Defines roles a user can have within a company.
 * Used for fine-grained authorization on company-level operations.
 */
public enum CompanyRoleType {
    /**
     * Full company administrator - can manage all aspects
     */
    EMPLOYER,
    
    /**
     * Company admin - can manage most aspects
     */
    COMPANY_ADMIN,
    
    /**
     * Finance/accounting role - can access financial operations
     */
    COMPANY_ACCOUNTANT,
    
    /**
     * Approval role - can approve payroll and transactions
     */
    COMPANY_APPROVER,
    
    /**
     * Read-only viewer - can view company data
     */
    COMPANY_VIEWER
}
