package org.sp.payroll_service.domain.payroll.service.transaction;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.sp.payroll_service.domain.payroll.entity.Transaction;
import org.sp.payroll_service.domain.payroll.exception.PayrollProcessingException;
import org.sp.payroll_service.domain.wallet.entity.Account;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

/**
 * Service that manages transaction strategies and executes transactions.
 * Uses Strategy pattern to handle different types of financial transactions.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class TransactionStrategyService {
    
    private final List<TransactionStrategy> strategies;
    
    /**
     * Executes a transaction using the appropriate strategy.
     * @param debitAccount source account
     * @param creditAccount destination account
     * @param amount transaction amount
     * @param referenceId reference identifier
     * @param description transaction description
     * @return completed transaction
     */
    public Transaction executeTransaction(Account debitAccount, Account creditAccount, 
                                        BigDecimal amount, String referenceId, String description) {
        
        log.info("Finding strategy for transaction: {} from {} to {}", 
                amount, debitAccount.getAccountNumber(), creditAccount.getAccountNumber());
        
        // Find appropriate strategy
        TransactionStrategy strategy = strategies.stream()
            .filter(s -> s.canHandle(debitAccount, creditAccount, amount))
            .findFirst()
            .orElseThrow(() -> new PayrollProcessingException(
                "No suitable transaction strategy found for the given accounts and amount"));
        
        log.info("Using strategy: {} for transaction", strategy.getClass().getSimpleName());
        
        // Execute transaction using the selected strategy
        return strategy.execute(debitAccount, creditAccount, amount, referenceId, description);
    }
    
    /**
     * Gets all available transaction strategies.
     * @return list of available strategies
     */
    public List<TransactionStrategy> getAvailableStrategies() {
        return strategies;
    }
}