package org.sp.payroll_service.domain.payroll.service.transaction.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.sp.payroll_service.domain.common.enums.TransactionCategory;
import org.sp.payroll_service.domain.common.enums.TransactionStatus;
import org.sp.payroll_service.domain.common.enums.TransactionType;
import org.sp.payroll_service.domain.payroll.entity.Transaction;
import org.sp.payroll_service.domain.payroll.service.transaction.TransactionStrategy;
import org.sp.payroll_service.domain.wallet.entity.Account;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.Instant;

/**
 * Strategy for transaction reversal operations.
 * Handles reversing failed or incorrect transactions.
 */
@Component("transactionReversalStrategy")  // Explicit name to avoid conflicts
@RequiredArgsConstructor
@Slf4j
public class TransactionReversalStrategy implements TransactionStrategy {
    
    @Override
    public boolean canHandle(Account debitAccount, Account creditAccount, BigDecimal amount) {
        // This strategy handles reversal transactions where we need to reverse money flow
        return debitAccount != null && 
               creditAccount != null &&
               amount != null &&
               amount.compareTo(BigDecimal.ZERO) > 0 &&
               !debitAccount.getId().equals(creditAccount.getId()) &&
               debitAccount.getCurrentBalance().compareTo(amount) >= 0; // Sufficient funds for reversal
    }
    
    @Override
    public Transaction execute(Account debitAccount, Account creditAccount, BigDecimal amount, 
                              String referenceId, String description) {
        log.info("Executing transaction reversal: {} from {} to {}", 
                amount, debitAccount.getAccountNumber(), creditAccount.getAccountNumber());
        
        // Create reversal transaction record
        Transaction transaction = Transaction.builder()
            .debitAccount(debitAccount)
            .creditAccount(creditAccount)
            .amount(amount)
            .type(TransactionType.TRANSACTION_REVERSAL)
            .category(TransactionCategory.REVERSAL)
            .transactionStatus(TransactionStatus.PENDING)
            .referenceId(referenceId)
            .description(description != null ? description : "Transaction reversal")
            .requestedAt(Instant.now())
            .build();
        
        try {
            // Execute reversal: debit from source, credit to destination
            BigDecimal newDebitBalance = debitAccount.getCurrentBalance().subtract(amount);
            BigDecimal newCreditBalance = creditAccount.getCurrentBalance().add(amount);
            
            debitAccount.setCurrentBalance(newDebitBalance);
            creditAccount.setCurrentBalance(newCreditBalance);
            
            // Mark transaction as successful
            transaction.markAsProcessed();
            
            log.info("Transaction reversal completed successfully: {} - Debit balance: {}, Credit balance: {}", 
                    referenceId, newDebitBalance, newCreditBalance);
            return transaction;
            
        } catch (Exception e) {
            transaction.markAsFailed();
            log.error("Transaction reversal failed for {}: {}", referenceId, e.getMessage());
            throw e;
        }
    }
    
    @Override
    public TransactionCategory getCategory() {
        return TransactionCategory.REVERSAL;
    }
    
    @Override
    public TransactionType getType() {
        return TransactionType.TRANSACTION_REVERSAL;
    }
}