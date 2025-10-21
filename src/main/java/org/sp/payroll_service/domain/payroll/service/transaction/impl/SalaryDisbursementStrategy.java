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
 * Strategy for salary disbursement transactions.
 * Handles company account to employee account transfers for payroll.
 */
@Component("activeSalaryDisbursementStrategy")  // Explicit name to avoid conflicts
@RequiredArgsConstructor
@Slf4j
public class SalaryDisbursementStrategy implements TransactionStrategy {
    
    @Override
    public boolean canHandle(Account debitAccount, Account creditAccount, BigDecimal amount) {
        // Company account (CURRENT) paying to Employee account (SAVINGS/CURRENT)
        return debitAccount.getAccountType() == AccountType.CURRENT && 
               debitAccount.getOwnerType() == OwnerType.COMPANY &&
               (creditAccount.getAccountType() == AccountType.SAVINGS || 
                creditAccount.getAccountType() == AccountType.CURRENT) &&
               creditAccount.getOwnerType() == OwnerType.EMPLOYEE &&
               amount.compareTo(BigDecimal.ZERO) > 0;
    }
    
    @Override
    public Transaction execute(Account debitAccount, Account creditAccount, BigDecimal amount, 
                              String referenceId, String description) {
        log.info("Executing salary disbursement: {} from {} to {}", 
                amount, debitAccount.getAccountNumber(), creditAccount.getAccountNumber());
        
        // Validate sufficient balance
        if (debitAccount.getCurrentBalance().compareTo(amount) < 0) {
            throw new InsufficientFundsException(
                String.format("Insufficient funds in company account %s. Required: %s, Available: %s",
                    debitAccount.getAccountNumber(), amount, debitAccount.getCurrentBalance())
            );
        }
        
        // Create transaction record
        Transaction transaction = Transaction.builder()
            .debitAccount(debitAccount)
            .creditAccount(creditAccount)
            .amount(amount)
            .type(TransactionType.SALARY_DISBURSEMENT)
            .category(TransactionCategory.PAYROLL)
            .transactionStatus(TransactionStatus.PENDING)
            .referenceId(referenceId)
            .description(description != null ? description : "Salary payment")
            .requestedAt(Instant.now())
            .build();
        
        try {
            // Execute double-entry accounting
            executeDoubleEntry(debitAccount, creditAccount, amount);
            
            // Mark transaction as successful
            transaction.markAsProcessed();
            
            log.info("Salary disbursement completed successfully: {}", referenceId);
            return transaction;
            
        } catch (Exception e) {
            transaction.markAsFailed();
            log.error("Salary disbursement failed for {}: {}", referenceId, e.getMessage());
            throw e;
        }
    }
    
    @Override
    public TransactionCategory getCategory() {
        return TransactionCategory.PAYROLL;
    }
    
    @Override
    public TransactionType getType() {
        return TransactionType.SALARY_DISBURSEMENT;
    }
    
    /**
     * Executes double-entry accounting for the transaction.
     */
    private void executeDoubleEntry(Account debitAccount, Account creditAccount, BigDecimal amount) {
        // Debit company account (decrease balance)
        BigDecimal newDebitBalance = debitAccount.getCurrentBalance().subtract(amount);
        debitAccount.setCurrentBalance(newDebitBalance);
        
        // Credit employee account (increase balance)
        BigDecimal newCreditBalance = creditAccount.getCurrentBalance().add(amount);
        creditAccount.setCurrentBalance(newCreditBalance);
        
        log.debug("Double-entry completed - Debit: {} -> {}, Credit: {} -> {}", 
                debitAccount.getAccountNumber(), newDebitBalance,
                creditAccount.getAccountNumber(), newCreditBalance);
    }
}