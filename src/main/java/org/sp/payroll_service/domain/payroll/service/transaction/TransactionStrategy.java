package org.sp.payroll_service.domain.payroll.service.transaction;

import org.sp.payroll_service.domain.payroll.entity.Transaction;
import org.sp.payroll_service.domain.wallet.entity.Account;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Strategy interface for different types of financial transactions.
 * Implements the Strategy pattern for transaction processing.
 */
public interface TransactionStrategy {
    
    /**
     * Validates if this strategy can handle the transaction.
     * @param debitAccount source account
     * @param creditAccount destination account
     * @param amount transaction amount
     * @return true if strategy can handle this transaction
     */
    boolean canHandle(Account debitAccount, Account creditAccount, BigDecimal amount);
    
    /**
     * Executes the transaction using this strategy.
     * @param debitAccount source account
     * @param creditAccount destination account
     * @param amount transaction amount
     * @param referenceId reference identifier
     * @param description transaction description
     * @return completed transaction
     */
    Transaction execute(Account debitAccount, Account creditAccount, BigDecimal amount, 
                       String referenceId, String description);
    
    /**
     * Gets the transaction category this strategy handles.
     * @return transaction category
     */
    org.sp.payroll_service.domain.common.enums.TransactionCategory getCategory();
    
    /**
     * Gets the transaction type this strategy handles.
     * @return transaction type
     */
    org.sp.payroll_service.domain.common.enums.TransactionType getType();
}