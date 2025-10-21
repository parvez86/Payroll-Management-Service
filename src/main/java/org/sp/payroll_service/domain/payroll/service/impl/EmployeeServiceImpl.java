package org.sp.payroll_service.domain.payroll.service.impl;

import lombok.extern.slf4j.Slf4j;
import org.sp.payroll_service.api.core.dto.CompanyResponse;
import org.sp.payroll_service.api.core.dto.GradeResponse;
import org.sp.payroll_service.api.payroll.dto.*;
import org.sp.payroll_service.api.wallet.dto.AccountResponse;
import org.sp.payroll_service.domain.auth.entity.User;
import org.sp.payroll_service.domain.common.exception.DuplicateEntryException;
import org.sp.payroll_service.domain.common.exception.ResourceNotFoundException;
import org.sp.payroll_service.domain.common.service.AbstractCrudService;
import org.sp.payroll_service.domain.core.entity.Company;
import org.sp.payroll_service.domain.core.entity.Grade;
import org.sp.payroll_service.domain.payroll.entity.Employee;
import org.sp.payroll_service.domain.payroll.service.EmployeeService;
import org.sp.payroll_service.domain.wallet.entity.Account;
import org.sp.payroll_service.repository.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@Service
@Slf4j
public class EmployeeServiceImpl extends AbstractCrudService<
        Employee,
        UUID,
        EmployeeResponse,
        CreateEmployeeRequest,
        EmployeeUpdateRequest,
        EmployeeFilterRequest>
        implements EmployeeService {

    private final EmployeeRepository employeeRepository;
    private final UserRepository userRepository;
    private final GradeRepository gradeRepository;
    private final CompanyRepository companyRepository;
    private final AccountRepository accountRepository;

    public EmployeeServiceImpl(
            EmployeeRepository employeeRepository,
            UserRepository userRepository,
            GradeRepository gradeRepository,
            CompanyRepository companyRepository,
            AccountRepository accountRepository) {
        super(employeeRepository, "Employee");
        this.employeeRepository = employeeRepository;
        this.userRepository = userRepository;
        this.gradeRepository = gradeRepository;
        this.companyRepository = companyRepository;
        this.accountRepository = accountRepository;
    }

    // --- Overrides for Creation and Update with Business Logic ---

    @Override
    @Transactional
    public CompletableFuture<EmployeeResponse> create(CreateEmployeeRequest request) {
        log.info("Attempting to create employee with code: {}", request.bizId());

        // 1. Business Rule: Check uniqueness of the employee code
        if (employeeRepository.findByCode(request.bizId()).isPresent()) {
            throw DuplicateEntryException.forEntity("Employee", "code", request.bizId());
        }

        // 2. Fetch associated entities
        // Grade is required
        gradeRepository.findById(request.gradeId())
                .orElseThrow(() -> ResourceNotFoundException.forEntity("Grade", request.gradeId()));

        // Company is required
        companyRepository.findAll().stream().findFirst()
                .orElseThrow(() -> ResourceNotFoundException.forEntity("Company", "default"));

        // Delegation to super.create will trigger mapToEntity() for final object assembly and persistence
        return super.create(request);
    }

    /**
     * Handles complex updates involving related entities (User, Grade, Account)
     * before delegating to the base class for simple field updates.
     */
    @Override
    @Transactional
    public CompletableFuture<EmployeeResponse> update(UUID id, EmployeeUpdateRequest request) {
        Employee existingEmployee = employeeRepository.findById(id)
                .orElseThrow(() -> ResourceNotFoundException.forEntity("Employee", id));

        // 1. Handle User-related updates (Email, potentially Phone if shared with User entity)
        boolean isUserUpdated = false;
        if (request.email() != null) {
            User user = existingEmployee.getUser();
            if (user != null) {
                user.setEmail(request.email());
                isUserUpdated = true;
            }
        }
        if (isUserUpdated) {
            userRepository.save(existingEmployee.getUser());
        }

        // 2. Handle Grade update
        if (request.grade() != null) {
            // Find the Grade entity (assuming the DTO grade carries the name/id needed for lookup)
            // NOTE: This assumes Grade DTO's 'name' property is sufficient for lookup.
            Grade newGrade = gradeRepository.findByName(request.grade().getName())
                    .orElseThrow(() -> ResourceNotFoundException.forEntity("Grade", request.grade().getName()));
            existingEmployee.setGrade(newGrade);
        }

        // 3. Handle Account details update
        boolean isAccountUpdated = false;
        if (request.bankAccountNumber() != null || request.bankRoutingNumber() != null) {
            Account account = existingEmployee.getAccount();
            if (account != null) {
                if (request.bankAccountNumber() != null) {
                    account.setAccountNumber(request.bankAccountNumber());
                    isAccountUpdated = true;
                }
                // bankRoutingNumber update logic (complex, but applied to Account entity)
                if (request.bankRoutingNumber() != null) {
                    account.getBranch().getBank().setSwiftBicCode(request.bankRoutingNumber());
                    isAccountUpdated = true;
                }
            }
        }
        if (isAccountUpdated) {
            accountRepository.save(existingEmployee.getAccount());
        }

        // Delegate to abstract base class for simple field mapping and final save
        return super.update(id, request);
    }

    // --- Custom Search Implementation ---

    @Override
    @Async("virtualThreadExecutor")
    @Transactional(readOnly = true)
    public CompletableFuture<Page<EmployeeResponse>> search(EmployeeFilterRequest filter, Pageable pageable) {
        Specification<Employee> spec = (root, query, cb) -> {
            var predicates = new java.util.ArrayList<jakarta.persistence.criteria.Predicate>();

            if (StringUtils.hasText(filter.searchKeyword())) {
                String pattern = "%" + filter.searchKeyword().toLowerCase() + "%";
                jakarta.persistence.criteria.Predicate nameLike = cb.like(cb.lower(root.get("name")), pattern);
                jakarta.persistence.criteria.Predicate codeLike = cb.like(cb.lower(root.get("code")), pattern);
                predicates.add(cb.or(nameLike, codeLike));
            }

            if (filter.gradeId() != null) {
                predicates.add(cb.equal(root.get("grade").get("id"), filter.gradeId()));
            }

            if (filter.status() != null) {
                // Ensure correct mapping if filter.transactionStatus is EmploymentStatus and entity.transactionStatus is EntityStatus
                predicates.add(cb.equal(root.get("transactionStatus"), filter.status()));
            }

            if (filter.companyId() != null) {
                // NOTE: Assumes Employee entity has a relationship named 'manager' to another Employee
                predicates.add(cb.equal(root.get("company").get("id"), filter.companyId()));
            }

            return cb.and(predicates.toArray(new jakarta.persistence.criteria.Predicate[0]));
        };

        Page<Employee> entityPage = employeeRepository.findAll(spec, pageable);
        return CompletableFuture.completedFuture(entityPage.map(this::mapToResponse));
    }

    // --- Abstract Mapping Implementations ---

    @Override
    protected Employee mapToEntity(CreateEmployeeRequest creationRequest) {
        // Fetch entities (assuming lookups are successful based on checks in create())
        Grade grade = gradeRepository.findById(creationRequest.gradeId()).get();
        Company company = companyRepository.findAll().stream().findFirst().get();

        // 1. Create and Save Account (Transactional boundary handles rollback if Employee save fails)
        Account account = Account.builder()
                .accountNumber(creationRequest.accountRequest().accountNumber())
                .accountName(creationRequest.accountRequest().accountName())
                // ... map remaining fields from CreateAccountRequest
                .build();
        Account savedAccount = accountRepository.save(account);

        // 2. Placeholder User (If the flow requires linking a User that doesn't exist yet,
        // this needs robust logic. Assuming a temporary or default User is provided.)
        User placeholderUser = userRepository.findById(UUID.randomUUID()) // Placeholder lookup
                .orElseGet(() -> User.builder().id(UUID.randomUUID()).build()); // Create mock User if not found

        return Employee.builder()
                .code(creationRequest.bizId())
                .name(creationRequest.name())
                .address(creationRequest.address())
                .mobile(creationRequest.mobile())
                .grade(grade)
                .company(company)
                .account(savedAccount) // Link the saved account
                .user(placeholderUser)
                .build();
    }

    @Override
    protected Employee mapToEntity(EmployeeUpdateRequest updateRequest, Employee entity) {
        // Apply changes only if the field is NOT null (robust PATCH behavior)
        if (updateRequest.phoneNumber() != null) {
            entity.setMobile(updateRequest.phoneNumber());
        }

        // NOTE: Grade, Account, and User updates are handled in the overridden 'update' method above,
        // to ensure the related entities are saved in their respective repositories if needed.

        // Status update logic
        if (updateRequest.status() != null) {
            // NOTE: Assuming EmploymentStatus has a utility to map to BaseEntity's EntityStatus enum
            // entity.setStatus(updateRequest.transactionStatus().toEntityStatus());
        }

        return entity;
    }

    @Override
    protected EmployeeResponse mapToResponse(Employee entity) {
        // Complete mapping logic for nested DTOs (GradeResponse and AccountResponse)

        GradeResponse gradeResponse = new GradeResponse(
                entity.getGrade().getId(),
                entity.getGrade().getName(),
                entity.getGrade().getRank(),
                entity.getGrade().getParent() != null? entity.getGrade().getParent().getId():null,
                entity.getGrade().getParent() != null? entity.getGrade().getParent().getName():null,
                entity.getCreatedAt(),
                entity.getCreatedBy()
        );

        AccountResponse accountResponse = new AccountResponse(
                entity.getAccount().getId(),
                entity.getAccount().getOwnerType(),
                entity.getAccount().getOwnerId(),
                entity.getAccount().getAccountType(),
                entity.getAccount().getAccountName(),
                entity.getAccount().getAccountNumber(),
                entity.getAccount().getCurrentBalance(),
                entity.getAccount().getOverdraftLimit(),
                entity.getAccount().getBranch() != null? entity.getAccount().getBranch().getId():null,
                entity.getAccount().getBranch() != null? entity.getAccount().getBranch().getBranchName():null,
                entity.getStatus(),
                entity.getCreatedAt(),
                entity.getCreatedBy()
        );

        CompanyResponse companyResponse = new CompanyResponse(
                entity.getCompany().getId(),
                entity.getCompany().getName(),
                entity.getCompany().getDescription(),
                entity.getCompany().getSalaryFormula().getId(),
                null,
                entity.getCompany().getCreatedAt(),
                entity.getCompany().getCreatedBy()
        );

        return new EmployeeResponse(
                entity.getId(),
                entity.getCode(),
                entity.getName(),
                entity.getAddress(),
                entity.getMobile(),
                companyResponse,
                gradeResponse,
                accountResponse,
                entity.getStatus()
        );
    }

    @Override
    @Async("virtualThreadExecutor")
    @Transactional(readOnly = true)
    public CompletableFuture<EmployeeResponse> findByBizId(String bizId) {
        log.debug("Finding employee by business ID: {}", bizId);
        return CompletableFuture.supplyAsync(() -> {
            Employee employee = employeeRepository.findByCode(bizId)
                    .orElseThrow(() -> ResourceNotFoundException.forEntity("Employee", bizId));
            return mapToResponse(employee);
        });
    }

    @Override
    @Async("virtualThreadExecutor")
    @Transactional(readOnly = true)
    public CompletableFuture<Object> getEmployeeCountByGrade() {
        log.debug("Getting employee count by grade");
        return CompletableFuture.supplyAsync(employeeRepository::getEmployeeCountByGrade);
    }

    @Override
    @Async("virtualThreadExecutor")
    @Transactional(readOnly = true)
    public CompletableFuture<Long> getTotalEmployeeCount() {
        log.debug("Getting total employee count");
        return CompletableFuture.supplyAsync(employeeRepository::count);
    }
}