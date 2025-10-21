package org.sp.payroll_service.domain.payroll.service;

import org.sp.payroll_service.api.payroll.dto.TransactionFilter;
import org.sp.payroll_service.api.payroll.dto.TransactionResponse;
import org.sp.payroll_service.api.payroll.dto.TransferRequest;
import org.sp.payroll_service.domain.common.dto.response.Money;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

/**
 * Service interface for financial transaction operations.
 * Handles money transfers, balance queries, and transaction history.
 */
public interface TransactionService {
    
    /**
     * Executes a money transfer between two accounts.
     * Uses ACID-compliant double-entry accounting.
     * @param request transfer details
     * @return transaction result
     */
    CompletableFuture<TransactionResponse> executeTransfer(TransferRequest request);
    
    /**
     * Gets current balance for an account.
     * @param accountId account identifier
     * @return current balance
     */
    CompletableFuture<Money> getAccountBalance(UUID accountId);
    
    /**
     * Retrieves transaction history with optional filtering.
     * @param filter filter criteria
     * @param pageable pagination parameters
     * @return paginated transactions
     */
    CompletableFuture<Page<TransactionResponse>> getTransactionHistory(TransactionFilter filter, Pageable pageable);
    
    /**
     * Retrieves a specific transaction by ID.
     * @param transactionId transaction identifier
     * @return transaction details
     */
    CompletableFuture<TransactionResponse> getTransactionById(UUID transactionId);
    
    /**
     * Retrieves all transactions for a specific account.
     * @param accountId account identifier
     * @param pageable pagination parameters
     * @return paginated transactions for the account
     */
    CompletableFuture<Page<TransactionResponse>> getAccountTransactions(UUID accountId, Pageable pageable);
    
    /**
     * Retrieves all transactions for a specific payroll batch.
     * @param batchId payroll batch identifier
     * @return list of transactions for the batch
     */
    CompletableFuture<List<TransactionResponse>> getBatchTransactions(UUID batchId);
    
    /**
     * Validates if an account has sufficient balance for a transfer.
     * @param accountId account identifier
     * @param amount amount to check
     * @return true if sufficient balance exists
     */
    CompletableFuture<Boolean> hasSufficientBalance(UUID accountId, Money amount);
    
    /**
     * Reverses a transaction (if supported).
     * @param transactionId transaction to reverse
     * @param reason reason for reversal
     * @return reversal transaction
     */
    CompletableFuture<TransactionResponse> reverseTransaction(UUID transactionId, String reason);
}