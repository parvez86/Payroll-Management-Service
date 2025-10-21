package org.sp.payroll_service.domain.common.enums;

public enum TransactionStatus {
    PENDING,        // Initial state
    AUTHORIZED,     // Funds checked, ready for debit/credit
    COMPLETED,      // Funds successfully transferred
    SUCCESS,        // Alias for COMPLETED
    FAILED,         // Transfer failed (e.g., system error)
    REVERSED        // Compensation applied
}