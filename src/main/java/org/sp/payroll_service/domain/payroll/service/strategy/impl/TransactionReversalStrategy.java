package org.sp.payroll_service.domain.payroll.service.strategy.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.sp.payroll_service.domain.common.enums.TransactionCategory;
import org.sp.payroll_service.domain.common.enums.TransactionStatus;
import org.sp.payroll_service.domain.common.enums.TransactionType;
import org.sp.payroll_service.domain.payroll.entity.Transaction;
import org.sp.payroll_service.domain.payroll.service.strategy.TransactionStrategy;
import org.sp.payroll_service.domain.wallet.entity.Account;
import org.sp.payroll_service.repository.TransactionRepository;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.concurrent.CompletableFuture;

/**
 * Strategy for transaction reversal operations.
 * Handles reversing failed or incorrect transactions.
 * @deprecated This implementation is deprecated. Use transaction package instead.
 */
@Component("deprecatedTransactionReversalStrategy")  // Explicit name to avoid conflicts
@RequiredArgsConstructor
@Slf4j
@Deprecated
public class TransactionReversalStrategy implements TransactionStrategy {
    
    private final TransactionRepository transactionRepository;
    
    @Override
    @Transactional(isolation = Isolation.SERIALIZABLE)
    public CompletableFuture<Transaction> execute(
            Account debitAccount, 
            Account creditAccount, 
            BigDecimal amount, 
            String referenceId, 
            String description) {
        
        log.info("Executing transaction reversal: {} from {} to {}", 
                amount, debitAccount.getAccountNumber(), creditAccount.getAccountNumber());
        
        if (!validate(debitAccount, creditAccount, amount)) {
            throw new IllegalArgumentException("Invalid reversal transaction parameters");
        }
        
        // Create reversal transaction record
        Transaction transaction = Transaction.builder()
                .debitAccount(debitAccount)
                .creditAccount(creditAccount)
                .amount(amount)
                .type(TransactionType.TRANSACTION_REVERSAL)
                .category(TransactionCategory.REVERSAL)
                .referenceId(referenceId)
                .description(description)
                .transactionStatus(TransactionStatus.PENDING)
                .requestedAt(Instant.now())
                .build();
        
        try {
            // Execute reversal (opposite of original transaction)
            debitAccount.setCurrentBalance(debitAccount.getCurrentBalance().subtract(amount));
            creditAccount.setCurrentBalance(creditAccount.getCurrentBalance().add(amount));
            
            // Mark as processed
            transaction.markAsProcessed();
            transaction.setProcessedAt(Instant.now());
            
            // Save transaction
            Transaction savedTransaction = transactionRepository.save(transaction);
            
            log.info("Transaction reversal completed successfully: {}", savedTransaction.getId());
            return CompletableFuture.completedFuture(savedTransaction);
            
        } catch (Exception e) {
            log.error("Transaction reversal failed: {}", e.getMessage(), e);
            transaction.markAsFailed();
            transaction.setFailureReason(e.getMessage());
            transactionRepository.save(transaction);
            throw new RuntimeException("Transaction reversal failed", e);
        }
    }
    
    @Override
    public boolean validate(Account debitAccount, Account creditAccount, BigDecimal amount) {
        // Basic validation
        if (debitAccount == null || creditAccount == null || amount == null) {
            return false;
        }
        
        // Amount must be positive
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            return false;
        }
        
        // Check sufficient funds in debit account for reversal
        if (debitAccount.getCurrentBalance().compareTo(amount) < 0) {
            return false;
        }
        
        // Ensure accounts are different
        return !debitAccount.getId().equals(creditAccount.getId());
    }
    
    @Override
    public String getTransactionType() {
        return TransactionType.TRANSACTION_REVERSAL.name();
    }
}