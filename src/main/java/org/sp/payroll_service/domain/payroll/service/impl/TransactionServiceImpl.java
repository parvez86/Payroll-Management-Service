package org.sp.payroll_service.domain.payroll.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.sp.payroll_service.api.payroll.dto.TransactionFilter;
import org.sp.payroll_service.api.payroll.dto.TransactionResponse;
import org.sp.payroll_service.api.payroll.dto.TransferRequest;
import org.sp.payroll_service.api.payroll.mapper.TransactionMapper;
import org.sp.payroll_service.domain.common.dto.response.HeaderResponse;
import org.sp.payroll_service.domain.common.dto.response.Money;
import org.sp.payroll_service.domain.common.enums.*;
import org.sp.payroll_service.domain.common.exception.AccessDeniedException;
import org.sp.payroll_service.domain.common.exception.ResourceNotFoundException;
import org.sp.payroll_service.domain.payroll.entity.PayrollBatch;
import org.sp.payroll_service.domain.payroll.entity.PayrollItem;
import org.sp.payroll_service.domain.payroll.entity.Transaction;
import org.sp.payroll_service.domain.payroll.exception.InsufficientFundsException;
import org.sp.payroll_service.domain.payroll.exception.PayrollProcessingException;
import org.sp.payroll_service.domain.payroll.service.TransactionService;
import org.sp.payroll_service.domain.payroll.service.transaction.TransactionStrategyService;
import org.sp.payroll_service.domain.wallet.entity.Account;
import org.sp.payroll_service.repository.AccountRepository;
import org.sp.payroll_service.repository.CompanyUserRoleRepository;
import org.sp.payroll_service.repository.CompanyRepository;
import org.sp.payroll_service.repository.EmployeeRepository;
import org.sp.payroll_service.repository.PayrollBatchRepository;
import org.sp.payroll_service.repository.PayrollItemRepository;
import org.sp.payroll_service.repository.TransactionRepository;
import org.sp.payroll_service.utils.JwtUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Service implementation for financial transaction operations.
 * Provides ACID-compliant double-entry accounting with proper isolation.
 * Now includes role-based authorization for transaction access.
 * <p>
 * NOTE: All methods are now synchronous (blocking). Asynchronous execution using
 * Virtual Threads should be handled by the calling Controller or service layer.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class TransactionServiceImpl implements TransactionService {

    private final TransactionRepository transactionRepository;
    private final AccountRepository accountRepository;
    private final CompanyRepository companyRepository;
    private final EmployeeRepository employeeRepository;
    private final TransactionMapper transactionMapper;
    private final TransactionStrategyService transactionStrategyService;
    private final PayrollBatchRepository payrollBatchRepository;
    private final PayrollItemRepository payrollItemRepository;
    private final CompanyUserRoleRepository companyUserRoleRepository;

    @Override
    @Transactional(isolation = Isolation.SERIALIZABLE)
    public TransactionResponse executeTransfer(TransferRequest request, HeaderResponse principal) {
        log.info("Executing transfer: {} from {} to {} by {} ({})",
                request.amount(), request.debitAccountId(), request.creditAccountId(), principal.username(), principal.userId());

        // SECURITY: Get current user for audit trail
        UUID currentUserId = principal.userId();
        if (currentUserId == null) {
            throw new IllegalStateException("Cannot execute transfer: User not authenticated");
        }

        try {
            // 1. VALIDATION: Check for self-transfer
            if (request.debitAccountId().equals(request.creditAccountId())) {
                throw new IllegalArgumentException("Cannot transfer to the same account");
            }

            // 2. IDEMPOTENCY: Check for duplicate referenceId
            if (request.referenceId() != null && transactionRepository.existsByReferenceId(request.referenceId())) {
                log.warn("Duplicate transfer attempt with referenceId: {}", request.referenceId());
                throw new IllegalArgumentException("Transaction with this referenceId already exists");
            }

            // 3. PESSIMISTIC LOCKING: Prevent concurrent modifications (lock in consistent order to avoid deadlock)
            UUID firstLockId = request.debitAccountId().compareTo(request.creditAccountId()) < 0 
                ? request.debitAccountId() : request.creditAccountId();
            UUID secondLockId = firstLockId.equals(request.debitAccountId()) 
                ? request.creditAccountId() : request.debitAccountId();
            
            accountRepository.findByIdWithLock(firstLockId)
                    .orElseThrow(() -> ResourceNotFoundException.forEntity("Account", firstLockId));
            accountRepository.findByIdWithLock(secondLockId)
                    .orElseThrow(() -> ResourceNotFoundException.forEntity("Account", secondLockId));

            // Now fetch for use (already locked)
            Account debitAccount = accountRepository.findById(request.debitAccountId())
                    .orElseThrow(() -> ResourceNotFoundException.forEntity("Account", request.debitAccountId()));

            Account creditAccount = accountRepository.findById(request.creditAccountId())
                    .orElseThrow(() -> ResourceNotFoundException.forEntity("Account", request.creditAccountId()));

            // 4. AUTHORIZATION: Verify user can debit from this account (admin or account owner)
            if (!canUserAccessAccount(currentUserId, debitAccount)) {
                throw new AccessDeniedException (
                    "User not authorized to transfer from account: " + debitAccount.getAccountNumber());
            }

            // 5. EXECUTE: Use strategy pattern to execute transaction
            Transaction transaction = transactionStrategyService.executeTransaction(
                    debitAccount,
                    creditAccount,
                    request.amount(),
                    request.referenceId(),
                    request.description());

            // 6. PAYROLL LINKING: Populate payroll-related fields if provided
            if (request.payrollBatchId() != null) {
                PayrollBatch batch = payrollBatchRepository.findById(request.payrollBatchId())
                        .orElseThrow(() -> ResourceNotFoundException.forEntity("PayrollBatch", request.payrollBatchId()));
                transaction.setPayrollBatch(batch);
                // Set company from batch (for payroll transactions)
                if (batch.getCompany() != null) {
                    transaction.setCompany(batch.getCompany());
                }
            }
            
            if (request.payrollItemId() != null) {
                PayrollItem item = payrollItemRepository.findById(request.payrollItemId())
                    .orElseThrow(() -> ResourceNotFoundException.forEntity("PayrollItem", request.payrollItemId()));
                transaction.setSourceItem(item);
            }

            // 6.5. COMPANY ASSIGNMENT: Set company if not already set (critical for authorization)
            if (transaction.getCompany() == null) {
                // Try to get company from debit account if it's a company account
                if (debitAccount.getOwnerType() == OwnerType.COMPANY && debitAccount.getOwnerId() != null) {
                    org.sp.payroll_service.domain.core.entity.Company company = companyRepository.findById(debitAccount.getOwnerId())
                            .orElse(null);
                    if (company != null) {
                        transaction.setCompany(company);
                        log.debug("Set transaction company from debit account: {}", company.getId());
                    }
                }

                // Fallback: Try to get from credit account's employee's company
                if (transaction.getCompany() == null && creditAccount.getOwnerType() == OwnerType.EMPLOYEE) {
                    UUID creditAccountId = creditAccount.getId();
                    var employee = employeeRepository.findByAccount_IdAndStatus(creditAccountId, EntityStatus.ACTIVE).orElse(null);
                    if (employee != null && employee.getCompany() != null) {
                        transaction.setCompany(employee.getCompany());
                        log.debug("Set transaction company from credit account employee: {}", employee.getCompany().getId());
                    }
                }
            }

            // Validate company is set (CRITICAL for authorization filtering)
            if (transaction.getCompany() == null) {
                log.error("Transaction company is NULL - authorization will fail. Debit: {}, Credit: {}",
                        debitAccount.getId(), creditAccount.getId());
                throw new PayrollProcessingException("Cannot determine transaction company for authorization");
            }
            
            // Override type/category if explicitly specified
            if (request.transactionType() != null) {
                transaction.setType(request.transactionType());
            }

            if (request.transactionCategory() != null) {
                transaction.setCategory(request.transactionCategory());
            }

            // 7. AUDIT: Set who created this transaction
            transaction.setCreatedBy(currentUserId);

            // 8. PERSIST: Save transaction and updated account balances atomically
            accountRepository.save(debitAccount);
            accountRepository.save(creditAccount);
            Transaction savedTransaction = transactionRepository.save(transaction);

            log.info("Transfer completed successfully: {} - Transaction ID: {} by user: {}",
                    request.amount(), savedTransaction.getId(), currentUserId);

            return transactionMapper.toResponse(savedTransaction);

        } catch (ResourceNotFoundException | IllegalArgumentException |
                 InsufficientFundsException e) {
            log.warn("Transfer rejected: {} - {}", request.referenceId(), e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("Transfer failed: {} from {} to {} - {}",
                    request.amount(), request.debitAccountId(), request.creditAccountId(), e.getMessage(), e);
            throw new PayrollProcessingException(
                "Transfer execution failed: " + e.getMessage(), e);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public Money getAccountBalance(UUID accountId) {
        log.debug("Retrieving balance for account: {}", accountId);

        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> ResourceNotFoundException.forEntity("Account", accountId));

        return Money.of(account.getCurrentBalance());
    }

    @Override
    @Transactional(readOnly = true)
    public Page<TransactionResponse> getTransactionHistory(TransactionFilter filter, HeaderResponse principal, Pageable pageable) {
        log.debug("Retrieving transaction history with filter: {}, principal: {}", filter, principal);

        // Create spec based on role and company filter
        Specification<Transaction> spec = (root, query, cb) -> {
            var predicates = new ArrayList<jakarta.persistence.criteria.Predicate>();
            
            // Add company filter if not ADMIN
            if (filter.companyId() != null) {
                predicates.add(cb.equal(root.get("company").get("id"), filter.companyId()));
            }
            
            // Add other filters from filter object
            if (filter != null) {
                if (filter.status() != null) {
                    predicates.add(cb.equal(root.get("transactionStatus"), filter.status()));
                }
                if (filter.type() != null) {
                    predicates.add(cb.equal(root.get("type"), filter.type()));
                }
                if (filter.category() != null) {
                    predicates.add(cb.equal(root.get("category"), filter.category()));
                }
                if (filter.debitAccountId() != null) {
                    predicates.add(cb.equal(root.get("debitAccount").get("id"), filter.debitAccountId()));
                }
                if (filter.creditAccountId() != null) {
                    predicates.add(cb.equal(root.get("creditAccount").get("id"), filter.creditAccountId()));
                }
                if (filter.minAmount() != null) {
                    predicates.add(cb.ge(root.get("amount"), filter.minAmount()));
                }
                if (filter.maxAmount() != null) {
                    predicates.add(cb.le(root.get("amount"), filter.maxAmount()));
                }
            }
            
            if (predicates.isEmpty()) {
                return cb.conjunction();
            }
            return cb.and(predicates.toArray(new jakarta.persistence.criteria.Predicate[0]));
        };
        
        Page<Transaction> transactionPage = transactionRepository.findAll(spec, pageable);
        return transactionPage.map(transactionMapper::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public TransactionResponse getTransactionById(UUID transactionId) {
        log.debug("Retrieving transaction: {}", transactionId);

        Transaction transaction = transactionRepository.findById(transactionId)
                .orElseThrow(() -> ResourceNotFoundException.forEntity("Transaction", transactionId));

        return transactionMapper.toResponse(transaction);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<TransactionResponse> getAccountTransactions(UUID accountId, Pageable pageable) {
        log.debug("Retrieving transactions for account: {}", accountId);

        // Verify account exists
        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> ResourceNotFoundException.forEntity("Account", accountId));

        Page<Transaction> transactionPage = transactionRepository.findByAccountId(accountId, pageable);
        return transactionPage.map(transactionMapper::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public List<TransactionResponse> getBatchTransactions(UUID batchId) {
        log.debug("Retrieving transactions for batch: {}", batchId);

        PayrollBatch batch = payrollBatchRepository.findById(batchId)
                .orElseThrow(() -> ResourceNotFoundException.forEntity("PayrollBatch", batchId));

        List<Transaction> transactions = transactionRepository.findByPayrollBatchId(batchId);
        return transactions.stream()
                .map(transactionMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public Boolean hasSufficientBalance(UUID accountId, Money amount) {
        log.debug("Checking sufficient balance for account: {} amount: {}", accountId, amount);

        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> ResourceNotFoundException.forEntity("Account", accountId));

        return account.getCurrentBalance().compareTo(amount.amount()) >= 0;
    }

    @Override
    @Transactional(isolation = Isolation.SERIALIZABLE)
    public TransactionResponse reverseTransaction(UUID transactionId, String reason, HeaderResponse principal) {
        log.warn("Reversing transaction: {} with reason: {}", transactionId, reason);

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
                .createdBy(principal.userId())
                .build();

        // Execute reversal transfer (now synchronous)
        TransactionResponse reversalTransaction = executeTransfer(reversalRequest, principal);

        log.info("Transaction {} reversed successfully. Reversal transaction: {}",
                transactionId, reversalTransaction.id());

        return reversalTransaction;
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
            
            // Security: Filter by createdBy (admin can see all, others only their own)
            if (filter.createdBy() != null) {
                predicates.add(cb.equal(root.get("createdBy"), filter.createdBy()));
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

            // Validate date range
            if (filter.fromDate() != null && filter.toDate() != null) {
                if (filter.fromDate().isAfter(filter.toDate())) {
                    throw new IllegalArgumentException("fromDate cannot be after toDate");
                }
            }
            
            if (filter.searchText() != null && !filter.searchText().isBlank()) {
                // Sanitize search text to prevent SQL injection via LIKE
                String sanitized = filter.searchText()
                    .replace("%", "\\%")
                    .replace("_", "\\_")
                    .toLowerCase()
                    .trim();
                String pattern = "%" + sanitized + "%";
                jakarta.persistence.criteria.Predicate refIdLike = cb.like(cb.lower(root.get("referenceId")), pattern);
                jakarta.persistence.criteria.Predicate descLike = cb.like(cb.lower(root.get("description")), pattern);
                predicates.add(cb.or(refIdLike, descLike));
            }

            return cb.and(predicates.toArray(new jakarta.persistence.criteria.Predicate[0]));
        };
    }

    /**
     * Check if user has authorization to access/debit from an account.
     * Admin can access all accounts, others can only access their own.
     */
    private boolean canUserAccessAccount(UUID userId, Account account) {
        Role role = JwtUtils.getCurrentUserRole();

        // Admin has access to all accounts
        if (role == Role.ADMIN) {
            return true;
        }

        // User can access accounts they own (employee personal accounts)
        if (account.getOwnerId().equals(userId)) {
            return true;
        }

        // Employer can access company funding accounts
        if (role == Role.EMPLOYER && account.getOwnerType() == OwnerType.COMPANY) {
            // Check if user has EMPLOYER role on this company
            UUID companyId = account.getOwnerId();
            return companyUserRoleRepository.existsByUser_IdAndCompany_IdAndRoleOnCompanyAndActiveTrue(
                    userId, companyId, CompanyRoleType.EMPLOYER);
        }

        return false;
    }
}
