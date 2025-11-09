package org.sp.payroll_service.domain.core.service.impl;

import lombok.extern.slf4j.Slf4j;
import org.sp.payroll_service.api.core.dto.*;
import org.sp.payroll_service.api.payroll.dto.TransactionResponse;
import org.sp.payroll_service.api.wallet.dto.AccountResponse;
import org.sp.payroll_service.domain.common.dto.response.AuditInfo;
import org.sp.payroll_service.domain.common.dto.response.Money;
import org.sp.payroll_service.domain.common.enums.AccountType;
import org.sp.payroll_service.domain.common.enums.OwnerType;
import org.sp.payroll_service.domain.common.exception.DuplicateEntryException;
import org.sp.payroll_service.domain.common.exception.ResourceNotFoundException;
import org.sp.payroll_service.domain.common.service.AbstractCrudService;
import org.sp.payroll_service.domain.core.entity.Company;
import org.sp.payroll_service.domain.core.service.CompanyService;
import org.sp.payroll_service.domain.core.entity.Branch;
import org.sp.payroll_service.domain.payroll.entity.SalaryDistributionFormula;
import org.sp.payroll_service.domain.payroll.entity.Transaction;
import org.sp.payroll_service.domain.wallet.entity.Account;
import org.sp.payroll_service.repository.AccountRepository;
import org.sp.payroll_service.repository.BranchRepository;
import org.sp.payroll_service.repository.CompanyRepository;
import org.sp.payroll_service.repository.SalaryDistributionFormulaRepository;
import org.sp.payroll_service.repository.TransactionRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import jakarta.persistence.criteria.Predicate;
import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

/**
 * Concrete service implementation for managing {@code Company} entities.
 * Handles cascading creation of the Company's main payroll {@code Account} and links to {@code SalaryDistributionFormula}.
 * All public methods in this service are synchronous (blocking).
 */
@Service
@Slf4j
public class CompanyServiceImpl extends AbstractCrudService<
        Company,
        UUID,
        CompanyResponse,
        CompanyCreateRequest,
        CompanyUpdateRequest,
        CompanyFilter>
        implements CompanyService {

    private final CompanyRepository companyRepository;
    private final AccountRepository accountRepository;
    private final BranchRepository branchRepository;
    private final SalaryDistributionFormulaRepository formulaRepository;
    private final TransactionRepository transactionRepository;

    public CompanyServiceImpl(CompanyRepository companyRepository,
                              AccountRepository accountRepository,
                              BranchRepository branchRepository,
                              SalaryDistributionFormulaRepository formulaRepository,
                              TransactionRepository transactionRepository) {
        super(companyRepository, "Company");
        this.companyRepository = companyRepository;
        this.accountRepository = accountRepository;
        this.branchRepository = branchRepository;
        this.formulaRepository = formulaRepository;
        this.transactionRepository = transactionRepository;
    }

    // --- CORE CRUD IMPLEMENTATIONS ---

    @Override
    @Transactional
    // FIX: Changed return type from CompletableFuture<CompanyResponse> to CompanyResponse
    public CompanyResponse create(CompanyCreateRequest request) {
        // Business Rule: Company name must be unique
        if (companyRepository.existsByName(request.name())) {
            throw DuplicateEntryException.forEntity("Company", "name", request.name());
        }

        // We assume createMainAccountEntity handles Account uniqueness (accountNumber)
        return super.create(request);
    }

    // --- MAPPING LOGIC (No changes needed) ---

    @Override
    protected Company mapToEntity(CompanyCreateRequest creationRequest) {
        // 1. Fetch linked Formula entity
        SalaryDistributionFormula formula = getFormulaOrThrow(creationRequest.salaryFormulaId());

        // 2. Create the Company entity (ID not yet set)
        Company company = Company.builder()
                .name(creationRequest.name())
                .description(creationRequest.description())
                .salaryFormula(formula)
                .build();

        // 3. Create and set the Main Account entity (ID is transient until company save)
        Account mainAccount = createMainAccountEntity(creationRequest.mainAccountRequest(), company.getId());

        company.setAccount(mainAccount);

        return company;
    }

    @Override
    protected Company mapToEntity(CompanyUpdateRequest updateRequest, Company entity) {
        // Business Rule: Check uniqueness of name (excluding current entity)
        if (StringUtils.hasText(updateRequest.name())) {
            if (companyRepository.existsByNameAndIdNot(updateRequest.name(), entity.getId())) {
                throw DuplicateEntryException.forEntity("Company", "name", updateRequest.name());
            }
            entity.setName(updateRequest.name());
        }

        if (updateRequest.description() != null) {
            entity.setDescription(updateRequest.description());
        }

        if (updateRequest.salaryFormulaId() != null) {
            SalaryDistributionFormula formula = getFormulaOrThrow(updateRequest.salaryFormulaId());
            entity.setSalaryFormula(formula);
        }

        return entity;
    }

    @Override
    protected CompanyResponse mapToResponse(Company entity) {
        AccountResponse accountResponse = mapAccountToResponse(entity.getAccount());

        return new CompanyResponse(
                entity.getId(),
                entity.getName(),
                entity.getDescription(),
                entity.getSalaryFormula() != null ? entity.getSalaryFormula().getId() : null,
                accountResponse,
                entity.getCreatedAt(),
                entity.getCreatedBy()
        );
    }

    // --- Helper Methods (No changes needed) ---

    private SalaryDistributionFormula getFormulaOrThrow(UUID formulaId) {
        return formulaRepository.findById(formulaId)
                .orElseThrow(() -> ResourceNotFoundException.forEntity("SalaryDistributionFormula", formulaId));
    }


    private AccountResponse mapAccountToResponse(Account account) {
        if (account == null) return null;
        String branchName = account.getBranch() != null ? account.getBranch().getBranchName() : null;

        return new AccountResponse(
                account.getId(),
                account.getOwnerType(),
                account.getOwnerId(),
                account.getAccountType(),
                account.getAccountName(),
                account.getAccountNumber(),
                account.getCurrentBalance(),
                account.getOverdraftLimit(),
                account.getBranch().getId(),
                branchName,
                account.getStatus(),
                account.getCreatedAt(),
                account.getCreatedBy()
        );
    }

    private Account createMainAccountEntity(CompanyMainAccountRequest request, UUID companyId) {
        if (accountRepository.existsByAccountNumber(request.accountNumber())) {
            throw DuplicateEntryException.forEntity("Account", "accountNumber", request.accountNumber());
        }

        Branch branch = branchRepository.findById(request.branchId())
                .orElseThrow(() -> ResourceNotFoundException.forEntity("Branch", request.branchId()));

        return Account.builder()
                .ownerType(OwnerType.COMPANY)
                .ownerId(companyId)
                .accountType(AccountType.CURRENT)
                .accountName(request.accountName())
                .accountNumber(request.accountNumber())
                .currentBalance(request.initialBalance())
                .overdraftLimit(BigDecimal.ZERO)
                .branch(branch)
                .build();
    }

    @Override
    @Transactional
    // FIX: Changed return type from CompletableFuture<CompanyResponse> to CompanyResponse
    public CompanyResponse topUpAccount(UUID companyId, CompanyTopUpRequest request) {
        // FIX: Removed CompletableFuture.supplyAsync wrapper. Blocking code executes directly.

        // 1. Find the company
        Company company = companyRepository.findById(companyId)
                .orElseThrow(() -> ResourceNotFoundException.forEntity("Company", companyId));

        // 2. Get the company's main account
        Account companyAccount = company.getAccount();
        if (companyAccount == null) {
            throw new IllegalStateException("Company does not have a main account");
        }

        // 3. Add funds to the account
        BigDecimal currentBalance = companyAccount.getCurrentBalance();
        BigDecimal newBalance = currentBalance.add(request.amount());
        companyAccount.setCurrentBalance(newBalance);

        // 4. Save the updated account
        accountRepository.save(companyAccount);

        // 5. Log the top-up operation
        log.info("Company {} account topped up with {}. Previous balance: {}, New balance: {}",
                company.getName(), request.amount(), currentBalance, newBalance);

        // 6. Return updated company response
        return mapToResponse(company);
    }

    @Override
    @Transactional(readOnly = true)
    // FIX: Changed return type from CompletableFuture<AccountResponse> to AccountResponse
    public AccountResponse getCompanyAccount(UUID companyId) {
        // FIX: Removed CompletableFuture.supplyAsync wrapper. Blocking code executes directly.

        // 1. Find the company
        Company company = companyRepository.findById(companyId)
                .orElseThrow(() -> ResourceNotFoundException.forEntity("Company", companyId));

        // 2. Get the company's main account
        Account companyAccount = company.getAccount();
        if (companyAccount == null) {
            throw new IllegalStateException("Company does not have a main account");
        }

        // 3. Map to AccountResponse
        return AccountResponse.builder()
                .id(companyAccount.getId())
                .accountNumber(companyAccount.getAccountNumber())
                .accountType(companyAccount.getAccountType())
                .currentBalance(companyAccount.getCurrentBalance())
//                .availableBalance(companyAccount.getAvailableBalance())
                .status(companyAccount.getStatus())
//                .bankId(companyAccount.getBranch() != null ? companyAccount.getBranch().getBank().getId() : null)
                .branchId(companyAccount.getBranch() != null ? companyAccount.getBranch().getId() : null)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    // FIX: Changed return type from CompletableFuture<Page<TransactionResponse>> to Page<TransactionResponse>
    public Page<TransactionResponse> getCompanyTransactions(UUID companyId, Pageable pageable) {
        // FIX: Removed CompletableFuture.supplyAsync wrapper. Blocking code executes directly.

        // 1. Find the company
        Company company = companyRepository.findById(companyId)
                .orElseThrow(() -> ResourceNotFoundException.forEntity("Company", companyId));

        // 2. Get the company's main account
        Account companyAccount = company.getAccount();
        if (companyAccount == null) {
            throw new IllegalStateException("Company does not have a main account");
        }

        // 3. Find all transactions for this account (both from and to)
        Specification<Transaction> spec = (root, query, cb) -> {
            Predicate fromAccount = cb.equal(root.get("debitAccount").get("id"), companyAccount.getId());
            Predicate toAccount = cb.equal(root.get("creditAccount").get("id"), companyAccount.getId());
            return cb.or(fromAccount, toAccount);
        };

        // Blocking JPA call
        Page<Transaction> transactions = transactionRepository.findAll(spec, pageable);

        // 4. Map to TransactionResponse
        return transactions.map(transaction -> TransactionResponse.builder()
                .id(transaction.getId())
//                .transactionNumber(transaction.getTransactionNumber())
                .amount(Money.of(transaction.getAmount()))
                .type(transaction.getType())
                .status(transaction.getStatus())
                .description(transaction.getDescription())
                .debitAccountId(transaction.getDebitAccount() != null ? transaction.getDebitAccount().getId() : null)
                .creditAccountId(transaction.getCreditAccount() != null ? transaction.getCreditAccount().getId() : null)
                .referenceId(transaction.getReferenceId())
                .payrollBatchId(transaction.getPayrollBatch() != null ? transaction.getPayrollBatch().getId() : null)
                .requestedAt(transaction.getRequestedAt())
                .processedAt(transaction.getProcessedAt())
                .auditInfo(AuditInfo.builder()
                        .version(transaction.getVersion())
                        .createdAt(transaction.getCreatedAt())
                        .createdBy(transaction.getCreatedBy() != null?transaction.getCreatedBy().toString():null)
                        .lastModifiedAt(transaction.getUpdatedAt())
                        .lastModifiedBy(transaction.getUpdatedBy() != null?transaction.getUpdatedBy().toString():null)
                        .build())
                .build());
    }

    @Override
    protected Specification<Company> buildSpecificationFromFilter(CompanyFilter filter) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new java.util.ArrayList<>();

            if (StringUtils.hasText(filter.keyword())) {
                String pattern = "%" + filter.keyword().toLowerCase() + "%";
                Predicate nameMatch = cb.like(cb.lower(root.get("name")), pattern);
                Predicate descriptionMatch = cb.like(cb.lower(root.get("description")), pattern);
                predicates.add(cb.or(nameMatch, descriptionMatch));
            }

            if (filter.salaryFormulaId() != null) {
                predicates.add(cb.equal(root.get("salaryFormula").get("id"), filter.salaryFormulaId()));
            }

            if (filter.status() != null) {
                predicates.add(cb.equal(root.get("status"), filter.status()));
            }

            return cb.and(predicates.toArray(new jakarta.persistence.criteria.Predicate[0]));
        };
    }
}