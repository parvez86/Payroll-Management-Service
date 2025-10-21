package org.sp.payroll_service.domain.payroll.service.transaction.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.sp.payroll_service.domain.common.enums.AccountType;
import org.sp.payroll_service.domain.common.enums.OwnerType;
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
 * Strategy for company account top-up transactions.
 * Handles external funding to company accounts.
 */
@Component("activeCompanyTopUpStrategy")  // Explicit name to avoid conflicts
@RequiredArgsConstructor
@Slf4j
public class CompanyTopUpStrategy implements TransactionStrategy {
    
    @Override
    public boolean canHandle(Account debitAccount, Account creditAccount, BigDecimal amount) {
        // External account (null debit) to Company account (CURRENT)
        return debitAccount == null && 
               creditAccount != null &&
               creditAccount.getAccountType() == AccountType.CURRENT && 
               creditAccount.getOwnerType() == OwnerType.COMPANY &&
               amount.compareTo(BigDecimal.ZERO) > 0;
    }
    
    @Override
    public Transaction execute(Account debitAccount, Account creditAccount, BigDecimal amount, 
                              String referenceId, String description) {
        log.info("Executing company top-up: {} to company account {}", 
                amount, creditAccount.getAccountNumber());
        
        // Create transaction record (debit account is null for external funding)
        Transaction transaction = Transaction.builder()
            .debitAccount(null) // External funding
            .creditAccount(creditAccount)
            .amount(amount)
            .type(TransactionType.COMPANY_TOPUP)
            .category(TransactionCategory.OPERATIONAL)
            .transactionStatus(TransactionStatus.PENDING)
            .referenceId(referenceId)
            .description(description != null ? description : "Company account top-up")
            .requestedAt(Instant.now())
            .build();
        
        try {
            // Credit company account (increase balance)
            BigDecimal newBalance = creditAccount.getCurrentBalance().add(amount);
            creditAccount.setCurrentBalance(newBalance);
            
            // Mark transaction as successful
            transaction.markAsProcessed();
            
            log.info("Company top-up completed successfully: {} - New balance: {}", referenceId, newBalance);
            return transaction;
            
        } catch (Exception e) {
            transaction.markAsFailed();
            log.error("Company top-up failed for {}: {}", referenceId, e.getMessage());
            throw e;
        }
    }
    
    @Override
    public TransactionCategory getCategory() {
        return TransactionCategory.OPERATIONAL;
    }
    
    @Override
    public TransactionType getType() {
        return TransactionType.COMPANY_TOPUP;
    }
}