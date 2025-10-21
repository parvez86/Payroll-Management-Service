package org.sp.payroll_service.domain.common.enums;

/**
 * Transaction types for payroll management system.
 * Simplified to essential operations only.
 */
public enum TransactionType {
    SALARY_DISBURSEMENT("Company to Employee salary payment"),
    PAYROLL_DISBURSEMENT("Company to Employee salary payment"), // Alias for compatibility
    COMPANY_TOPUP("Adding funds to company account"),
    TRANSACTION_REVERSAL("Reversing a failed/incorrect transaction"),
    TRANSFER("General account transfer"),
    ACCOUNT_TRANSFER("Account to account transfer");
    
    private final String description;
    
    TransactionType(String description) {
        this.description = description;
    }
    
    public String getDescription() {
        return description;
    }
}