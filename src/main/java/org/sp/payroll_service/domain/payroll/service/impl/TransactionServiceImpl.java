package org.sp.payroll_service.domain.payroll.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.sp.payroll_service.api.payroll.dto.TransactionFilter;
import org.sp.payroll_service.api.payroll.dto.TransactionResponse;
import org.sp.payroll_service.api.payroll.dto.TransferRequest;
import org.sp.payroll_service.api.payroll.mapper.TransactionMapper;
import org.sp.payroll_service.domain.common.dto.response.Money;
import org.sp.payroll_service.domain.common.enums.TransactionCategory;
import org.sp.payroll_service.domain.common.enums.TransactionStatus;
import org.sp.payroll_service.domain.common.enums.TransactionType;
import org.sp.payroll_service.domain.common.exception.ResourceNotFoundException;
import org.sp.payroll_service.domain.payroll.entity.Transaction;
import org.sp.payroll_service.domain.payroll.exception.InsufficientFundsException;
import org.sp.payroll_service.domain.payroll.service.TransactionService;
import org.sp.payroll_service.domain.payroll.service.transaction.TransactionStrategyService;
import org.sp.payroll_service.domain.wallet.entity.Account;
import org.sp.payroll_service.repository.AccountRepository;
import org.sp.payroll_service.repository.TransactionRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

/**
 * Service implementation for financial transaction operations.
 * Provides ACID-compliant double-entry accounting with proper isolation.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class TransactionServiceImpl implements TransactionService {

    private final TransactionRepository transactionRepository;
    private final AccountRepository accountRepository;
    private final TransactionMapper transactionMapper;
    private final TransactionStrategyService transactionStrategyService;

    @Override
    @Transactional(isolation = Isolation.SERIALIZABLE)
    public CompletableFuture<TransactionResponse> executeTransfer(TransferRequest request) {
        log.info("Executing transfer: {} from {} to {}", 
                request.amount(), request.debitAccountId(), request.creditAccountId());

        return CompletableFuture.supplyAsync(() -> {
            try {
                // Validate accounts exist
                Account debitAccount = accountRepository.findById(request.debitAccountId())
                        .orElseThrow(() -> ResourceNotFoundException.forEntity("Account", request.debitAccountId()));
                
                Account creditAccount = accountRepository.findById(request.creditAccountId())
                        .orElseThrow(() -> ResourceNotFoundException.forEntity("Account", request.creditAccountId()));

                // Use strategy pattern to execute transaction
                Transaction transaction = transactionStrategyService.executeTransaction(
                        debitAccount, 
                        creditAccount, 
                        request.amount(), 
                        request.referenceId(), 
                        request.description());

                // Save transaction and updated account balances
                accountRepository.save(debitAccount);
                accountRepository.save(creditAccount);
                Transaction savedTransaction = transactionRepository.save(transaction);

                log.info("Transfer completed successfully: {} - Transaction ID: {}", 
                        request.amount(), savedTransaction.getId());

                return transactionMapper.toResponse(savedTransaction);

            } catch (Exception e) {
                log.error("Transfer failed: {} from {} to {} - {}", 
                        request.amount(), request.debitAccountId(), request.creditAccountId(), e.getMessage());
                throw new RuntimeException("Transfer failed: " + e.getMessage(), e);
            }
        });
    }

    @Override
    @Async("virtualThreadExecutor")
    @Transactional(readOnly = true)
    public CompletableFuture<Money> getAccountBalance(UUID accountId) {
        log.debug("Retrieving balance for account: {}", accountId);
        
        return CompletableFuture.supplyAsync(() -> {
            Account account = accountRepository.findById(accountId)
                    .orElseThrow(() -> ResourceNotFoundException.forEntity("Account", accountId));
            
            return Money.of(account.getCurrentBalance());
        });
    }

    @Override
    @Async("virtualThreadExecutor")
    @Transactional(readOnly = true)
    public CompletableFuture<Page<TransactionResponse>> getTransactionHistory(TransactionFilter filter, Pageable pageable) {
        log.debug("Retrieving transaction history with filter: {}", filter);
        
        return CompletableFuture.supplyAsync(() -> {
            Specification<Transaction> spec = createSpecification(filter);
            Page<Transaction> transactionPage = transactionRepository.findAll(spec, pageable);
            
            return transactionPage.map(transactionMapper::toResponse);
        });
    }

    @Override
    @Async("virtualThreadExecutor")
    @Transactional(readOnly = true)
    public CompletableFuture<TransactionResponse> getTransactionById(UUID transactionId) {
        log.debug("Retrieving transaction: {}", transactionId);
        
        return CompletableFuture.supplyAsync(() -> {
            Transaction transaction = transactionRepository.findById(transactionId)
                    .orElseThrow(() -> ResourceNotFoundException.forEntity("Transaction", transactionId));
            
            return transactionMapper.toResponse(transaction);
        });
    }

    @Override
    @Async("virtualThreadExecutor")
    @Transactional(readOnly = true)
    public CompletableFuture<Page<TransactionResponse>> getAccountTransactions(UUID accountId, Pageable pageable) {
        log.debug("Retrieving transactions for account: {}", accountId);
        
        return CompletableFuture.supplyAsync(() -> {
            // Verify account exists
            accountRepository.findById(accountId)
                    .orElseThrow(() -> ResourceNotFoundException.forEntity("Account", accountId));

            Page<Transaction> transactionPage = transactionRepository.findByAccountId(accountId, pageable);
            return transactionPage.map(transactionMapper::toResponse);
        });
    }

    @Override
    @Async("virtualThreadExecutor")
    @Transactional(readOnly = true)
    public CompletableFuture<List<TransactionResponse>> getBatchTransactions(UUID batchId) {
        log.debug("Retrieving transactions for batch: {}", batchId);
        
        return CompletableFuture.supplyAsync(() -> {
            List<Transaction> transactions = transactionRepository.findByPayrollBatchId(batchId);
            return transactions.stream()
                    .map(transactionMapper::toResponse)
                    .collect(Collectors.toList());
        });
    }

    @Override
    @Async("virtualThreadExecutor")
    @Transactional(readOnly = true)
    public CompletableFuture<Boolean> hasSufficientBalance(UUID accountId, Money amount) {
        log.debug("Checking sufficient balance for account: {} amount: {}", accountId, amount);
        
        return CompletableFuture.supplyAsync(() -> {
            Account account = accountRepository.findById(accountId)
                    .orElseThrow(() -> ResourceNotFoundException.forEntity("Account", accountId));
            
            return account.getCurrentBalance().compareTo(amount.amount()) >= 0;
        });
    }

    @Override
    @Transactional(isolation = Isolation.SERIALIZABLE)
    public CompletableFuture<TransactionResponse> reverseTransaction(UUID transactionId, String reason) {
        log.warn("Reversing transaction: {} with reason: {}", transactionId, reason);
        
        return CompletableFuture.supplyAsync(() -> {
            Transaction originalTransaction = transactionRepository.findById(transactionId)
                    .orElseThrow(() -> ResourceNotFoundException.forEntity("Transaction", transactionId));

            if (originalTransaction.getTransactionStatus() != TransactionStatus.SUCCESS) {
                throw new IllegalStateException("Cannot reverse transaction that is not successful: " + originalTransaction.getStatus());
            }

            // Create reversal transfer request
            TransferRequest reversalRequest = TransferRequest.builder()
                    .debitAccountId(originalTransaction.getCreditAccount().getId())
                    .creditAccountId(originalTransaction.getDebitAccount().getId())
                    .amount(originalTransaction.getAmount())
                    .referenceId("REV-" + originalTransaction.getReferenceId())
                    .description("REVERSAL: " + reason + " | Original: " + originalTransaction.getDescription())
                    .build();

            // Execute reversal transfer
            TransactionResponse reversalTransaction = executeTransfer(reversalRequest).join();

            log.info("Transaction {} reversed successfully. Reversal transaction: {}", 
                    transactionId, reversalTransaction.id());

            return reversalTransaction;
        });
    }

    // --- Helper Methods ---

    private Specification<Transaction> createSpecification(TransactionFilter filter) {
        return (root, query, cb) -> {
            var predicates = new ArrayList<jakarta.persistence.criteria.Predicate>();

            if (filter.type() != null) {
                predicates.add(cb.equal(root.get("type"), filter.type()));
            }

            if (filter.category() != null) {
                predicates.add(cb.equal(root.get("category"), filter.category()));
            }

            if (filter.status() != null) {
                predicates.add(cb.equal(root.get("transactionStatus"), filter.status()));
            }

            if (filter.debitAccountId() != null) {
                predicates.add(cb.equal(root.get("debitAccount").get("id"), filter.debitAccountId()));
            }

            if (filter.creditAccountId() != null) {
                predicates.add(cb.equal(root.get("creditAccount").get("id"), filter.creditAccountId()));
            }

            if (filter.payrollBatchId() != null) {
                predicates.add(cb.equal(root.get("payrollBatch").get("id"), filter.payrollBatchId()));
            }

            if (filter.minAmount() != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("amount"), filter.minAmount()));
            }

            if (filter.maxAmount() != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("amount"), filter.maxAmount()));
            }

            if (filter.fromDate() != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("requestedAt"), filter.fromDate()));
            }

            if (filter.toDate() != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("requestedAt"), filter.toDate()));
            }

            if (filter.searchText() != null) {
                String pattern = "%" + filter.searchText().toLowerCase() + "%";
                jakarta.persistence.criteria.Predicate refIdLike = cb.like(cb.lower(root.get("referenceId")), pattern);
                jakarta.persistence.criteria.Predicate descLike = cb.like(cb.lower(root.get("description")), pattern);
                predicates.add(cb.or(refIdLike, descLike));
            }

            return cb.and(predicates.toArray(new jakarta.persistence.criteria.Predicate[0]));
        };
    }
}