package org.sp.payroll_service.domain.payroll.service.strategy;

import org.sp.payroll_service.domain.payroll.entity.Transaction;
import org.sp.payroll_service.domain.wallet.entity.Account;

import java.math.BigDecimal;
import java.util.concurrent.CompletableFuture;

/**
 * Strategy pattern for different types of financial transactions.
 * Each strategy handles a specific transaction type with its own business rules.
 */
public interface TransactionStrategy {
    
    /**
     * Executes the transaction according to the strategy's business rules.
     * @param debitAccount account to debit from
     * @param creditAccount account to credit to
     * @param amount transaction amount
     * @param referenceId reference for tracking
     * @param description transaction description
     * @return completed transaction
     */
    CompletableFuture<Transaction> execute(
        Account debitAccount, 
        Account creditAccount, 
        BigDecimal amount, 
        String referenceId, 
        String description
    );
    
    /**
     * Validates if the transaction can be executed.
     * @param debitAccount account to debit from
     * @param creditAccount account to credit to
     * @param amount transaction amount
     * @return true if transaction is valid
     */
    boolean validate(Account debitAccount, Account creditAccount, BigDecimal amount);
    
    /**
     * Returns the transaction type this strategy handles.
     * @return transaction type
     */
    String getTransactionType();
}