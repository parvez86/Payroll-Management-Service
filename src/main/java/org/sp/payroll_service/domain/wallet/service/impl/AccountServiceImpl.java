package org.sp.payroll_service.domain.wallet.service.impl;

import lombok.extern.slf4j.Slf4j;
import org.sp.payroll_service.api.wallet.dto.AccountFilter;
import org.sp.payroll_service.api.wallet.dto.AccountResponse;
import org.sp.payroll_service.api.wallet.dto.CreateAccountRequest;
import org.sp.payroll_service.api.wallet.dto.UpdateAccountRequest;
import org.sp.payroll_service.domain.common.enums.OwnerType;
import org.sp.payroll_service.domain.common.exception.DuplicateEntryException;
import org.sp.payroll_service.domain.common.exception.ResourceNotFoundException;
import org.sp.payroll_service.domain.common.service.AbstractCrudService;
import org.sp.payroll_service.domain.core.entity.Branch;
import org.sp.payroll_service.domain.wallet.entity.Account;
import org.sp.payroll_service.domain.wallet.service.AccountService;
import org.sp.payroll_service.repository.AccountRepository;
import org.sp.payroll_service.repository.BranchRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
// REMOVED: import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import jakarta.persistence.criteria.Predicate;

import java.util.ArrayList;
import java.util.UUID;
// REMOVED: import java.util.concurrent.CompletableFuture;

/**
 * Concrete service implementation for managing {@code Account} entities (wallets).
 * Focuses on initial setup and delegation of transactional logic.
 * All public methods in this service are synchronous (blocking).
 */
@Service
@Slf4j
public class AccountServiceImpl extends AbstractCrudService<
        Account,
        UUID,
        AccountResponse,
        CreateAccountRequest,
        UpdateAccountRequest,
        AccountFilter>
        implements AccountService {

    private final AccountRepository accountRepository;
    private final BranchRepository branchRepository;

    public AccountServiceImpl(AccountRepository accountRepository, BranchRepository branchRepository) {
        super(accountRepository, "Account");
        this.accountRepository = accountRepository;
        this.branchRepository = branchRepository;
    }

    // --- Core CRUD Overrides ---

    @Override
    @Transactional
    // FIX: Changed return type from CompletableFuture<AccountResponse> to AccountResponse
    public AccountResponse create(CreateAccountRequest request) {
        // Fintech Rule 1: Account numbers must be globally unique
        if (accountRepository.existsByAccountNumber(request.accountNumber())) {
            throw DuplicateEntryException.forEntity("Account", "accountNumber", request.accountNumber());
        }

        // Fintech Rule 2: Branch existence validated in mapToEntity via getBranchOrThrow

        return super.create(request);
    }

    @Override
    @Transactional
    // FIX: Changed return type from CompletableFuture<AccountResponse> to AccountResponse
    public AccountResponse update(UUID id, UpdateAccountRequest request) {
        // Fintech Rule 1: Account number cannot be changed here (separate process)
        // Fintech Rule 2: Check update uniqueness (only for account number if it were changing)

        return super.update(id, request);
    }

    // --- Mapping Implementations (No changes needed) ---

    @Override
    protected Account mapToEntity(CreateAccountRequest creationRequest) {
        Branch branch = getBranchOrThrow(creationRequest.branchId());

        return Account.builder()
                .ownerType(creationRequest.ownerType())
                .ownerId(creationRequest.ownerId())
                .accountType(creationRequest.accountType())
                .accountName(creationRequest.accountName())
                .accountNumber(creationRequest.accountNumber())
                .overdraftLimit(creationRequest.overdraftLimit())
                .branch(branch)
                .build();
    }

    @Override
    protected Account mapToEntity(UpdateAccountRequest updateRequest, Account entity) {
        entity.setAccountName(updateRequest.accountName());
        entity.setOverdraftLimit(updateRequest.overdraftLimit());
        return entity;
    }

    @Override
    protected AccountResponse mapToResponse(Account entity) {
        String branchName = entity.getBranch() != null ? entity.getBranch().getBranchName() : null;

        return new AccountResponse(
                entity.getId(),
                entity.getOwnerType(),
                entity.getOwnerId(),
                entity.getAccountType(),
                entity.getAccountName(),
                entity.getAccountNumber(),
                entity.getCurrentBalance(),
                entity.getOverdraftLimit(),
                entity.getBranch().getId(),
                branchName,
                entity.getStatus(),
                entity.getCreatedAt(),
                entity.getCreatedBy()
        );
    }

    // --- Custom Search Implementation ---

    @Override
    // FIX: Removed @Async("virtualThreadExecutor")
    @Transactional(readOnly = true)
    // FIX: Changed return type from CompletableFuture<Page<AccountResponse>> to Page<AccountResponse>
    public Page<AccountResponse> search(AccountFilter filter, Pageable pageable) {
        Specification<Account> spec = (root, query, cb) -> {
            var predicates = new ArrayList<Predicate>();

            if (StringUtils.hasText(filter.keyword())) {
                String pattern = "%" + filter.keyword().toLowerCase() + "%";
                Predicate keywordMatch = cb.or(
                        cb.like(cb.lower(root.get("accountName")), pattern),
                        cb.like(cb.lower(root.get("accountNumber")), pattern)
                );
                predicates.add(keywordMatch);
            }

            if (filter.ownerType() != null) {
                predicates.add(cb.equal(root.get("ownerType"), filter.ownerType()));
            }

            if (filter.ownerId() != null) {
                predicates.add(cb.equal(root.get("ownerId"), filter.ownerId()));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };

        // Blocking JPA call
        Page<Account> entityPage = specExecutor.findAll(spec, pageable);
        // FIX: Return the direct Page object
        return entityPage.map(this::mapToResponse);
    }

    // --- Private Helpers (No changes needed) ---

    private Branch getBranchOrThrow(UUID branchId) {
        return branchRepository.findById(branchId)
                .orElseThrow(() -> ResourceNotFoundException.forEntity("Branch", branchId));
    }

    // --- Fintech Operations (Exposed for Transactional Service) ---

    /**
     * Finds an account by its owner ID. This is a read-only, synchronous operation.
     *
     * @param ownerId The ID of the employee or company that owns the account.
     * @param ownerType The type of owner (Employee or Company).
     * @return The AccountResponse DTO.
     * @throws ResourceNotFoundException if no account is found for the given owner ID.
     */
    @Transactional(readOnly = true)
    // FIX: Changed return type from CompletableFuture<AccountResponse> to AccountResponse
    public AccountResponse findByOwnerId(UUID ownerId, OwnerType ownerType) {
        // FIX: Removed CompletableFuture.supplyAsync wrapper. Blocking code executes directly.
        return accountRepository.findByOwnerIdAndOwnerType(ownerId, ownerType)
                .map(this::mapToResponse)
                .orElseThrow(() -> ResourceNotFoundException.forEntity("Account for OwnerId ", ownerId.toString()));
    }
}