package org.sp.payroll_service.domain.payroll.service.transaction.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.sp.payroll_service.domain.common.enums.AccountType;
import org.sp.payroll_service.domain.common.enums.OwnerType;
import org.sp.payroll_service.domain.common.enums.TransactionCategory;
import org.sp.payroll_service.domain.common.enums.TransactionStatus;
import org.sp.payroll_service.domain.common.enums.TransactionType;
import org.sp.payroll_service.domain.payroll.entity.Transaction;
import org.sp.payroll_service.domain.payroll.exception.InsufficientFundsException;
import org.sp.payroll_service.domain.payroll.service.transaction.TransactionStrategy;
import org.sp.payroll_service.domain.wallet.entity.Account;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.Instant;

/**
 * Strategy for general money transfers between accounts.
 * Handles basic account-to-account transfers with validation.
 */
@Component("activeGeneralTransferStrategy")  // Explicit name to avoid conflicts
@RequiredArgsConstructor
@Slf4j
public class GeneralTransferStrategy implements TransactionStrategy {
    
    @Override
    public boolean canHandle(Account debitAccount, Account creditAccount, BigDecimal amount) {
        // Basic validation for any account-to-account transfer
        return debitAccount != null && 
               creditAccount != null && 
               !debitAccount.getId().equals(creditAccount.getId()) &&
               amount != null && 
               amount.compareTo(BigDecimal.ZERO) > 0;
    }
    
    @Override
    public Transaction execute(Account debitAccount, Account creditAccount, BigDecimal amount, 
                              String referenceId, String description) {
        log.info("Executing general transfer: {} from {} to {}", 
                amount, debitAccount.getAccountNumber(), creditAccount.getAccountNumber());
        
        // Validate sufficient balance
        if (debitAccount.getCurrentBalance().compareTo(amount) < 0) {
            throw new InsufficientFundsException(
                String.format("Insufficient funds in account %s. Required: %s, Available: %s",
                    debitAccount.getAccountNumber(), amount, debitAccount.getCurrentBalance())
            );
        }
        
        // Determine transaction type based on account types
        TransactionType type = determineTransactionType(debitAccount, creditAccount);
        TransactionCategory category = determineTransactionCategory(debitAccount, creditAccount);
        
        // Create transaction record
        Transaction transaction = Transaction.builder()
            .debitAccount(debitAccount)
            .creditAccount(creditAccount)
            .amount(amount)
            .type(type)
            .category(category)
            .transactionStatus(TransactionStatus.PENDING)
            .referenceId(referenceId)
            .description(description != null ? description : "Account transfer")
            .requestedAt(Instant.now())
            .build();
        
        try {
            // Execute double-entry accounting
            executeDoubleEntry(debitAccount, creditAccount, amount);
            
            // Mark transaction as successful
            transaction.markAsProcessed();
            
            log.info("General transfer completed successfully: {}", referenceId);
            return transaction;
            
        } catch (Exception e) {
            transaction.markAsFailed();
            log.error("General transfer failed for {}: {}", referenceId, e.getMessage());
            throw e;
        }
    }
    
    @Override
    public TransactionCategory getCategory() {
        return TransactionCategory.TRANSFER;
    }
    
    @Override
    public TransactionType getType() {
        return TransactionType.ACCOUNT_TRANSFER;
    }
    
    /**
     * Determines transaction type based on account owners.
     */
    private TransactionType determineTransactionType(Account debitAccount, Account creditAccount) {
        if (debitAccount.getOwnerType() == OwnerType.COMPANY && 
            creditAccount.getOwnerType() == OwnerType.EMPLOYEE) {
            return TransactionType.SALARY_DISBURSEMENT;
        } else {
            return TransactionType.ACCOUNT_TRANSFER;
        }
    }
    
    /**
     * Determines transaction category based on context.
     */
    private TransactionCategory determineTransactionCategory(Account debitAccount, Account creditAccount) {
        if (debitAccount.getOwnerType() == OwnerType.COMPANY && 
            creditAccount.getOwnerType() == OwnerType.EMPLOYEE) {
            return TransactionCategory.PAYROLL;
        } else {
            return TransactionCategory.TRANSFER;
        }
    }
    
    /**
     * Executes double-entry accounting for the transaction.
     */
    private void executeDoubleEntry(Account debitAccount, Account creditAccount, BigDecimal amount) {
        // Debit source account (decrease balance)
        BigDecimal newDebitBalance = debitAccount.getCurrentBalance().subtract(amount);
        debitAccount.setCurrentBalance(newDebitBalance);
        
        // Credit destination account (increase balance)
        BigDecimal newCreditBalance = creditAccount.getCurrentBalance().add(amount);
        creditAccount.setCurrentBalance(newCreditBalance);
        
        log.debug("Double-entry completed - Debit: {} -> {}, Credit: {} -> {}", 
                debitAccount.getAccountNumber(), newDebitBalance,
                creditAccount.getAccountNumber(), newCreditBalance);
    }
}