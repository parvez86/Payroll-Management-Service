package org.sp.payroll_service.repository;

import org.sp.payroll_service.domain.common.enums.TransactionCategory;
import org.sp.payroll_service.domain.common.enums.TransactionStatus;
import org.sp.payroll_service.domain.common.repository.BaseRepository;
import org.sp.payroll_service.domain.payroll.Transaction;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * Transaction repository with financial query methods.
 */
@Repository
public interface TransactionRepository extends BaseRepository<Transaction, UUID> {
    
    /**
     * Finds transactions by account with pagination.
     * @param accountId account identifier
     * @param pageable pagination parameters
     * @return paginated transactions
     */
    @Query("SELECT t FROM Transaction t WHERE t.account.id = :accountId ORDER BY t.createdAt DESC")
    Page<Transaction> findByAccountIdOrderByCreatedAtDesc(UUID accountId, Pageable pageable);
    
    /**
     * Calculates pending debit amount for account.
     * @param accountId account identifier
     * @return pending debit sum
     */
    @Query("SELECT COALESCE(SUM(t.amount), 0) FROM Transaction t WHERE t.account.id = :accountId AND t.type = 'DEBIT' AND t.transactionStatus = 'PENDING'")
    BigDecimal sumPendingDebits(UUID accountId);
    
    /**
     * Calculates pending credit amount for account.
     * @param accountId account identifier
     * @return pending credit sum
     */
    @Query("SELECT COALESCE(SUM(t.amount), 0) FROM Transaction t WHERE t.account.id = :accountId AND t.type = 'CREDIT' AND t.transactionStatus = 'PENDING'")
    BigDecimal sumPendingCredits(UUID accountId);
    
    /**
     * Finds transactions by reference ID for tracking.
     * @param referenceId external reference
     * @return list of transactions
     */
    List<Transaction> findByReferenceIdOrderByCreatedAtAsc(String referenceId);
    
    /**
     * Finds failed transactions for compensation.
     * @param status transaction status
     * @param category transaction category
     * @return list of failed transactions
     */
    @Query("SELECT t FROM Transaction t WHERE t.transactionStatus = :status AND t.category = :category AND t.createdAt >= :since")
    List<Transaction> findFailedTransactions(TransactionStatus status, TransactionCategory category, Instant since);
}