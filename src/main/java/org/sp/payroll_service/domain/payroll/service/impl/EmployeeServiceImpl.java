package org.sp.payroll_service.domain.payroll.service.impl;

import jakarta.persistence.criteria.Predicate;
import lombok.extern.slf4j.Slf4j;
import org.sp.payroll_service.api.core.dto.CompanyResponse;
import org.sp.payroll_service.api.core.dto.GradeResponse;
import org.sp.payroll_service.api.payroll.dto.*;
import org.sp.payroll_service.api.wallet.dto.AccountResponse;
import org.sp.payroll_service.domain.auth.entity.User;
import org.sp.payroll_service.domain.common.enums.AccountType;
import org.sp.payroll_service.domain.common.enums.EntityStatus;
import org.sp.payroll_service.domain.common.enums.OwnerType;
import org.sp.payroll_service.domain.common.enums.Role;
import org.sp.payroll_service.domain.common.exception.DuplicateEntryException;
import org.sp.payroll_service.domain.common.exception.ResourceNotFoundException;
import org.sp.payroll_service.domain.common.service.AbstractCrudService;
import org.sp.payroll_service.domain.core.entity.Branch;
import org.sp.payroll_service.domain.core.entity.Company;
import org.sp.payroll_service.domain.core.entity.Grade;
import org.sp.payroll_service.domain.payroll.entity.Employee;
import org.sp.payroll_service.domain.payroll.service.EmployeeService;
import org.sp.payroll_service.domain.wallet.entity.Account;
import org.sp.payroll_service.repository.*;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.springframework.http.HttpStatus.NOT_FOUND;

/**
 * Service implementation for Employee management operations.
 * All methods are synchronous to maintain Spring Security context.
 * Virtual thread performance is achieved at the controller level.
 */
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
    private final BranchRepository branchRepository;
    private final PasswordEncoder passwordEncoder;

    public EmployeeServiceImpl(
            EmployeeRepository employeeRepository,
            UserRepository userRepository,
            GradeRepository gradeRepository,
            CompanyRepository companyRepository,
            AccountRepository accountRepository,
            BranchRepository branchRepository,
            PasswordEncoder passwordEncoder) {
        super(employeeRepository, "Employee");
        this.employeeRepository = employeeRepository;
        this.userRepository = userRepository;
        this.gradeRepository = gradeRepository;
        this.companyRepository = companyRepository;
        this.accountRepository = accountRepository;
        this.branchRepository = branchRepository;
        this.passwordEncoder = passwordEncoder;
    }

    // --- Overrides for Creation and Update with Business Logic ---

    @Override
    @Transactional(readOnly = true)
    public EmployeeResponse findById(UUID id) {
        log.debug("🔍 [EMPLOYEE-SERVICE] Finding employee by ID: {}", id);

        Optional<Employee> employeeOpt = employeeRepository.findByIdWithJoins(id);
        log.debug("🔍 [EMPLOYEE-SERVICE] Employee found: {}", employeeOpt.isPresent());

        if (employeeOpt.isPresent()) {
            Employee employee = employeeOpt.get();
            log.debug("🔍 [EMPLOYEE-SERVICE] Employee name: {}", employee.getName());
            log.debug("🔍 [EMPLOYEE-SERVICE] Grade null: {}", employee.getGrade() == null);
            log.debug("🔍 [EMPLOYEE-SERVICE] Company null: {}", employee.getCompany() == null);
            log.debug("🔍 [EMPLOYEE-SERVICE] Account null: {}", employee.getAccount() == null);

            EmployeeResponse response = mapToResponse(employee);
            log.debug("🔍 [EMPLOYEE-SERVICE] Response mapped successfully");
            return response;
        } else {
            log.warn("🔍 [EMPLOYEE-SERVICE] ❌ Employee not found with ID: {}", id);
            throw new ResponseStatusException(NOT_FOUND, "Employee not found with ID: " + id);
        }
    }

    @Override
    @Transactional
    public EmployeeResponse create(CreateEmployeeRequest request) {
        log.info("Attempting to create employee with code: {}", request.bizId());

        // 1. Business Rule: Check uniqueness of the employee code
        if (userRepository.findByUsername(request.username()).isPresent()) {
            throw DuplicateEntryException.forEntity("Username", "username", request.username());
        }

        if (userRepository.findByEmail(request.email()).isPresent()) {
            throw DuplicateEntryException.forEntity("Email", "email", request.email());
        }

        if (employeeRepository.findByCode(request.bizId()).isPresent()) {
            throw DuplicateEntryException.forEntity("Employee", "code", request.bizId());
        }


        // 2. Fetch associated entities
        // Grade is required
        gradeRepository.findById(request.gradeId())
                .orElseThrow(() -> ResourceNotFoundException.forEntity("Grade", request.gradeId()));

        // Company is required
        companyRepository.findByIdAndStatus(request.companyId(), EntityStatus.ACTIVE)
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
    public EmployeeResponse update(UUID id, EmployeeUpdateRequest request) {
        Employee existingEmployee = employeeRepository.findById(id)
                .orElseThrow(() -> ResourceNotFoundException.forEntity("Employee", id));

        // 1. Handle Grade update (validate grade exists)
        if (request.gradeId() != null) {
            Grade newGrade = gradeRepository.findById(request.gradeId())
                    .orElseThrow(() -> ResourceNotFoundException.forEntity("Grade", request.gradeId()));
            existingEmployee.setGrade(newGrade);
        }

        // 2. Handle User-related updates
        if (existingEmployee.getUser() != null) {
            boolean userUpdated = false;
            
            if (request.email() != null) {
                // Check email uniqueness
                userRepository.findByEmail(request.email()).ifPresent(user -> {
                    if (!user.getId().equals(existingEmployee.getUser().getId())) {
                        throw DuplicateEntryException.forEntity("User", "email", request.email());
                    }
                });
                existingEmployee.getUser().setEmail(request.email());
                userUpdated = true;
            }
            
            if (request.password() != null) {
                existingEmployee.getUser().setPasswordHash(passwordEncoder.encode(request.password()));
                userUpdated = true;
            }
            
//            if (userUpdated) {
//                userRepository.save(existingEmployee.getUser());
//            }
        }

        // 3. Handle Account updates
        if (existingEmployee.getAccount() != null) {
            boolean accountUpdated = false;
            
            if (request.accountName() != null) {
                existingEmployee.getAccount().setAccountName(request.accountName());
                accountUpdated = true;
            }
            
            if (request.bankAccountNumber() != null) {
                existingEmployee.getAccount().setAccountNumber(request.bankAccountNumber());
                accountUpdated = true;
            }
            
            if (request.overdraftLimit() != null) {
                existingEmployee.getAccount().setOverdraftLimit(request.overdraftLimit());
                accountUpdated = true;
            }
            
            if (request.branchId() != null) {
                Branch newBranch = branchRepository.findById(request.branchId())
                        .orElseThrow(() -> ResourceNotFoundException.forEntity("Branch", request.branchId()));
                existingEmployee.getAccount().setBranch(newBranch);
                accountUpdated = true;
            }
            
//            if (accountUpdated) {
//                accountRepository.save(existingEmployee.getAccount());
//            }
        }

        // 4. Delegate to abstract base class for simple field mapping and final save
        return super.update(id, request);
    }

    // --- Abstract Mapping Implementations ---

    @Override
    protected Employee mapToEntity(CreateEmployeeRequest creationRequest) {
        // Fetch entities (assuming lookups are successful based on checks in create())
        Grade grade = gradeRepository.findById(creationRequest.gradeId())
                .orElseThrow(() -> ResourceNotFoundException.forEntity("Grade", creationRequest.gradeId()));

        Company company = companyRepository.findByIdAndStatus(creationRequest.companyId(), EntityStatus.ACTIVE)
                .orElseThrow(() -> ResourceNotFoundException.forEntity("Company", creationRequest.companyId()));

        // 1. Create and save User first
        User user = User
                .builder()
                .username(creationRequest.username())
                .role(Role.EMPLOYEE)
                .email(creationRequest.email())
                .passwordHash(passwordEncoder.encode(creationRequest.password()))
                .build();
        user = userRepository.save(user);
        
        log.debug("Created user with ID: {}", user.getId());

        // 2. Get branch entity for account creation
        Branch branch = branchRepository.findById(creationRequest.branchId())
                .orElseThrow(() -> ResourceNotFoundException.forEntity("Branch", creationRequest.branchId()));
                
        // 3. Create and Save Account with proper owner information
        Account account = Account.builder()
                .ownerType(OwnerType.EMPLOYEE) // Set owner type
                .ownerId(user.getId()) // Set owner ID to the saved user's ID
                .accountType(AccountType.SAVINGS)
                .accountNumber(creationRequest.accountNumber())
                .accountName(creationRequest.accountName())
                .currentBalance(BigDecimal.ZERO) // Initialize with zero balance
                .overdraftLimit(creationRequest.overdraftLimit())
                .branch(branch) // Set the branch entity
                .build();
        Account savedAccount = accountRepository.save(account);
        
        log.debug("Created account with ID: {} for user: {}", savedAccount.getId(), user.getId());
        
        // 4. Generate employee code
        String code = creationRequest.bizId() != null ? creationRequest.bizId() : getBizId();
        
        // 5. Create employee entity
        return Employee.builder()
                .code(code)
                .name(creationRequest.name())
                .address(creationRequest.address())
                .mobile(creationRequest.mobile())
                .grade(grade)
                .company(company)
                .account(savedAccount) // Link the saved account
                .user(user) // Link the saved user
                .build();
    }

    private String getBizId() {
        int maxId = employeeRepository.findMaxIdNumber();
        // Get the next ID (increment by 1)
        int nextId = maxId + 1;
        return String.format("%04d", nextId);
    }

    @Override
    protected Employee mapToEntity(EmployeeUpdateRequest updateRequest, Employee entity) {
        // Apply changes only if the field is NOT null (robust PATCH behavior)
        
        // Basic employee fields
        if (updateRequest.name() != null) {
            entity.setName(updateRequest.name());
        }
        
        if (updateRequest.address() != null) {
            entity.setAddress(updateRequest.address());
        }
        
        if (updateRequest.mobile() != null) {
            entity.setMobile(updateRequest.mobile());
        }
        
        // Legacy phoneNumber field mapping (if still needed)
        if (updateRequest.phoneNumber() != null) {
            entity.setMobile(updateRequest.phoneNumber());
        }

        // User-related updates
        if (entity.getUser() != null) {
            if (updateRequest.email() != null) {
                entity.getUser().setEmail(updateRequest.email());
            }
            
            // Password update
            if (updateRequest.password() != null) {
                entity.getUser().setPasswordHash(passwordEncoder.encode(updateRequest.password()));
            }
        }

        // Grade update - handle in the overridden update() method for proper validation
        
        // Account updates
        if (entity.getAccount() != null) {
            if (updateRequest.accountName() != null) {
                entity.getAccount().setAccountName(updateRequest.accountName());
            }
            
            if (updateRequest.bankAccountNumber() != null) {
                entity.getAccount().setAccountNumber(updateRequest.bankAccountNumber());
            }
            
            if (updateRequest.overdraftLimit() != null) {
                entity.getAccount().setOverdraftLimit(updateRequest.overdraftLimit());
            }
            
            // Branch update - need to fetch new branch entity
            if (updateRequest.branchId() != null) {
                Branch newBranch = branchRepository.findById(updateRequest.branchId())
                        .orElseThrow(() -> ResourceNotFoundException.forEntity("Branch", updateRequest.branchId()));
                entity.getAccount().setBranch(newBranch);
            }
        }

        // Status update logic
        if (updateRequest.status() != null) {
            // Map EmploymentStatus to EntityStatus if needed
            // entity.setStatus(updateRequest.status().toEntityStatus());
        }

        return entity;
    }

    @Override
    protected EmployeeResponse mapToResponse(Employee entity) {
        log.error("🔍 [EMPLOYEE-MAPPING-DEBUG] Employee ID: {}", entity != null ? entity.getId() : "NULL");
        
        if (entity == null) {
            log.error("🔍 [EMPLOYEE-MAPPING-DEBUG] ❌ Employee entity is NULL");
            return null;
        }
        
        try {
            // Complete mapping logic for nested DTOs (GradeResponse and AccountResponse)
            GradeResponse gradeResponse = null;
            if (entity.getGrade() != null) {
                gradeResponse = new GradeResponse(
                        entity.getGrade().getId(),
                        entity.getGrade().getName(),
                        entity.getGrade().getRank(),
                        entity.getGrade().getParent() != null? entity.getGrade().getParent().getId():null,
                        entity.getGrade().getParent() != null? entity.getGrade().getParent().getName():null,
                        entity.getCreatedAt(),
                        entity.getCreatedBy()
                );
                log.error("🔍 [EMPLOYEE-MAPPING-DEBUG] ✅ Grade mapped: {}", gradeResponse.name());
            } else {
                log.error("🔍 [EMPLOYEE-MAPPING-DEBUG] ❌ Grade is NULL");
            }

            log.error("🔍 [EMPLOYEE-MAPPING-DEBUG] Checking account...");
            AccountResponse accountResponse = null;
            if (entity.getAccount() != null) {
                accountResponse = new AccountResponse(
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
                log.error("🔍 [EMPLOYEE-MAPPING-DEBUG] ✅ Account mapped: {}", accountResponse.accountNumber());
            } else {
                log.error("🔍 [EMPLOYEE-MAPPING-DEBUG] ❌ Account is NULL");
            }

            log.error("🔍 [EMPLOYEE-MAPPING-DEBUG] Checking company...");
            CompanyResponse companyResponse = null;
            if (entity.getCompany() != null) {
                companyResponse = new CompanyResponse(
                        entity.getCompany().getId(),
                        entity.getCompany().getName(),
                        entity.getCompany().getDescription(),
                        entity.getCompany().getSalaryFormula() != null ? entity.getCompany().getSalaryFormula().getId() : null,
                        null,
                        entity.getCompany().getCreatedAt(),
                        entity.getCompany().getCreatedBy()
                );
                log.error("🔍 [EMPLOYEE-MAPPING-DEBUG] ✅ Company mapped: {}", companyResponse.name());
            } else {
                log.error("🔍 [EMPLOYEE-MAPPING-DEBUG] ❌ Company is NULL");
            }

            EmployeeResponse response = new EmployeeResponse(
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
            
            log.error("🔍 [EMPLOYEE-MAPPING-DEBUG] ✅ Response created: {}", response.id());
            return response;
            
        } catch (Exception e) {
            log.error("🔍 [EMPLOYEE-MAPPING-DEBUG] ❌ MAPPING FAILED: {}", e.getMessage(), e);
            throw e;
        }
    }

    @Override
    @Transactional(readOnly = true)
    public EmployeeResponse findByBizId(String bizId) {
        log.debug("Finding employee by business ID: {}", bizId);
        Employee employee = employeeRepository.findByCode(bizId)
                .orElseThrow(() -> ResourceNotFoundException.forEntity("Employee", bizId));
        return mapToResponse(employee);
    }

    @Override
    @Transactional(readOnly = true)
    public Object getEmployeeCountByGrade() {
        log.debug("Getting employee count by grade");
        return employeeRepository.getEmployeeCountByGrade();
    }

    @Override
    @Transactional(readOnly = true)
    public Long getTotalEmployeeCount() {
        log.debug("Getting total employee count");
        return employeeRepository.count();
    }


    /**
     * Override buildSpecificationFromFilter to provide custom filtering logic.
     * This is called by the generic search() method in AbstractCrudService.
     * Applies industry-grade practices:
     * - Robust filter handling (null-safe, trimmed, case-insensitive)
     * - Complete pagination metadata preservation
     */
    @Override
    protected Specification<Employee> buildSpecificationFromFilter(EmployeeFilterRequest filter) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new java.util.ArrayList<>();

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
                // If filter.status() maps to the same enum/type as entity.transactionStatus, this is fine.
                predicates.add(cb.equal(root.get("status"), filter.status()));
            }

            if (filter.companyId() != null) {
                // Adjust path if your relationship is truly 'company'
                predicates.add(cb.equal(root.get("company").get("id"), filter.companyId()));
            }

            // If no predicates were added, return a true/conjunction predicate (no filtering)
            if (predicates.isEmpty()) {
                return cb.conjunction();
            }

            return cb.and(predicates.toArray(new jakarta.persistence.criteria.Predicate[0]));
        };
    }
}