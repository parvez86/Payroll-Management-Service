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
 * Strategy for company account top-up transactions.
 * Handles adding funds to company accounts.
 * @deprecated This implementation is deprecated. Use transaction package instead.
 */
@Component("deprecatedCompanyTopUpStrategy")  // Explicit name to avoid conflicts
@RequiredArgsConstructor
@Slf4j
@Deprecated
public class CompanyTopUpStrategy implements TransactionStrategy {
    
    private final TransactionRepository transactionRepository;
    
    @Override
    @Transactional(isolation = Isolation.SERIALIZABLE)
    public CompletableFuture<Transaction> execute(
            Account debitAccount, 
            Account creditAccount, 
            BigDecimal amount, 
            String referenceId, 
            String description) {
        
        log.info("Executing company top-up: {} to {}", amount, creditAccount.getAccountNumber());
        
        // For top-up, we only credit the company account (debitAccount can be null or external)
        Transaction transaction = Transaction.builder()
                .debitAccount(debitAccount) // Can be null for external funding
                .creditAccount(creditAccount)
                .amount(amount)
                .type(TransactionType.COMPANY_TOPUP)
                .category(TransactionCategory.TOPUP)
                .referenceId(referenceId)
                .description(description)
                .transactionStatus(TransactionStatus.PENDING)
                .requestedAt(Instant.now())
                .build();
        
        try {
            // Add funds to company account
            creditAccount.setCurrentBalance(creditAccount.getCurrentBalance().add(amount));
            
            // Mark as processed
            transaction.markAsProcessed();
            transaction.setProcessedAt(Instant.now());
            
            // Save transaction
            Transaction savedTransaction = transactionRepository.save(transaction);
            
            log.info("Company top-up completed successfully: {}", savedTransaction.getId());
            return CompletableFuture.completedFuture(savedTransaction);
            
        } catch (Exception e) {
            log.error("Company top-up failed: {}", e.getMessage(), e);
            transaction.markAsFailed();
            transaction.setFailureReason(e.getMessage());
            transactionRepository.save(transaction);
            throw new RuntimeException("Company top-up failed", e);
        }
    }
    
    @Override
    public boolean validate(Account debitAccount, Account creditAccount, BigDecimal amount) {
        // Credit account must exist
        if (creditAccount == null || amount == null) {
            return false;
        }
        
        // Amount must be positive
        return amount.compareTo(BigDecimal.ZERO) > 0;
    }
    
    @Override
    public String getTransactionType() {
        return TransactionType.COMPANY_TOPUP.name();
    }
}