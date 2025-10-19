package org.sp.payroll_service.domain.common.enums;

public enum TransactionCategory {
    PAYROLL, // Standard monthly salary payment
    REVERSAL, // Compensation or correction (Saga rollback)
    TOPUP // Adding funds to the company's main account
}