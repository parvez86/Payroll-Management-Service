package org.sp.payroll_service.domain.auth.service.impl;

import lombok.extern.slf4j.Slf4j;
import org.sp.payroll_service.api.auth.dto.*;
import org.sp.payroll_service.api.wallet.dto.AccountResponse;
import org.sp.payroll_service.domain.auth.entity.User;
import org.sp.payroll_service.domain.auth.service.UserService;
import org.sp.payroll_service.domain.common.exception.DuplicateEntryException;
import org.sp.payroll_service.domain.common.exception.ErrorCodes;
import org.sp.payroll_service.domain.common.exception.ResourceNotFoundException;
import org.sp.payroll_service.domain.common.exception.ValidationException;
import org.sp.payroll_service.domain.common.service.AbstractCrudService;
import org.sp.payroll_service.domain.core.entity.Company;
import org.sp.payroll_service.domain.wallet.entity.Account;
import org.sp.payroll_service.repository.CompanyRepository;
import org.sp.payroll_service.repository.EmployeeRepository;
import org.sp.payroll_service.repository.UserRepository;
import org.sp.payroll_service.security.JwtTokenProvider;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.scheduling.annotation.Async;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.UUID;

/**
 * Concrete service implementation for managing {@code User} entities.
 * <p>
 * This class extends the generic {@code AbstractCrudService} to inherit boilerplate CRUD operations
 * and adds domain-specific business logic. **CRUD operations are synchronous** to maintain the
 * Spring Security context and transactional integrity, with heavy lifting (like searching)
 * being offloaded to dedicated asynchronous executors.
 * <ul>
 * <li>Password hashing during creation and update.</li>
 * <li>Unique constraint checks for username and email.</li>
 * <li>Custom search implementation using JPA Specifications.</li>
 * </ul>
 */
@Service
@Slf4j
public class UserServiceImpl extends AbstractCrudService<User, UUID, UserResponse, UserCreateRequest, UserUpdateRequest, UserFilter>
        implements UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmployeeRepository employeeRepository;
    private final CompanyRepository companyRepository;
    private final JwtTokenProvider jwtTokenProvider;

    /**
     * Constructs the UserServiceImpl.
     * <p>
     * Initializes the base service with the repository and resource name ("User"),
     * and injects the necessary domain-specific dependencies.
     *
     * @param userRepository  The JPA repository for User entities.
     * @param passwordEncoder The Spring Security password encoder for hashing.
     */
    public UserServiceImpl(UserRepository userRepository, PasswordEncoder passwordEncoder, EmployeeRepository employeeRepository, CompanyRepository companyRepository, JwtTokenProvider jwtTokenProvider) {
        super(userRepository, "User");
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.employeeRepository = employeeRepository;
        this.companyRepository = companyRepository;
        this.jwtTokenProvider = jwtTokenProvider;
    }

    // --- Overrides for Creation and Update with Business Logic ---

    /**
     * Creates a new user after performing uniqueness checks on the username and email.
     *
     * @param request The DTO containing the user creation data.
     * @return The {@code UserResponse} DTO of the newly created user.
     * @throws DuplicateEntryException if the username or email already exists.
     */
    @Override
    @Transactional
    public UserResponse create(UserCreateRequest request) {
        // Business Rule: Check uniqueness before persisting
        if (userRepository.existsByUsername(request.username())) {
            throw DuplicateEntryException.forEntity("User", "username", request.username());
        }
        if (userRepository.existsByEmail(request.email())) {
            throw DuplicateEntryException.forEntity("User", "email", request.email());
        }
        return super.create(request); // Delegates to abstract base class logic
    }

    /**
     * Updates an existing user, performing uniqueness checks and handling optional password changes.
     *
     * @param id      The ID of the user to update.
     * @param request The DTO containing the update data.
     * @return The {@code UserResponse} DTO of the updated user.
     * @throws ValidationException     if the current password check fails during a password change attempt.
     * @throws DuplicateEntryException if the new username or email is already taken by another user.
     * @throws ResourceNotFoundException if the user with the given ID is not found.
     */
    @Override
    @Transactional
    public UserResponse update(UUID id, UserUpdateRequest request) {
        User existingUser = repository.findById(id)
                .orElseThrow(() -> ResourceNotFoundException.forEntity("User", id)); // Added proper exception

        checkUniquenessOnUpdate(id, request.username(), request.email());

        // Business Rule: Handle optional password change
        if (request.newPassword() != null && !request.newPassword().isEmpty()) {
            if (request.currentPassword() == null || !passwordEncoder.matches(request.currentPassword(), existingUser.getPasswordHash())) {
                throw new ValidationException("Current password is required and incorrect to change password.", ErrorCodes.AUTH_INVALID_CREDENTIALS);
            }
            existingUser.setPasswordHash(passwordEncoder.encode(request.newPassword()));
        }

        return super.update(id, request);
    }

    /**
     * Finds a user by their unique username.
     * This operation runs **asynchronously** on a virtual thread executor.
     * * @param username The username to search for.
     * @return A {@code CompletableFuture} containing the found {@code UserResponse} DTO.
     * @throws ResourceNotFoundException if no user with the given username is found.
     */
    @Override
    @Async("virtualThreadExecutor")
    @Transactional(readOnly = true)
    public UserResponse findByUsername(String username) {
        // ... (implementation code) ...
        return userRepository.findByUsername(username)
                .map(this::mapToResponse)
                .orElseThrow(() -> ResourceNotFoundException.forEntity("User", username));
    }

    @Override
    @Transactional(readOnly = true)
    public UserDetailsResponse me(String accessToken) {
        log.debug("Getting user details from access token");
        
        try {
            // 1. Validate and extract user ID from token
            UUID userId = jwtTokenProvider.getUserIdFromJWT(accessToken);
            log.debug("Extracted user ID from token: {}", userId);
            
            // 2. Find user by ID
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> ResourceNotFoundException.forEntity("User", userId));
            log.debug("Found user: {}", user.getUsername());
            
            // 3. Map user to UserResponse
            UserResponse userResponse = mapToResponse(user);
            
            // 4. Initialize other fields
            AccountResponse accountResponse = null;
            String fullName = null;
            String description = null;
            UUID companyId = null;
            String bizId = null;
            
            // 5. Check if user is an employee to get additional details
            var employeeOpt = employeeRepository.findByUserId(userId);
            if (employeeOpt.isPresent()) {
                var employee = employeeOpt.get();
                log.debug("User is an employee with code: {}", employee.getCode());
                
                // Get employee details
                fullName = employee.getName();
                bizId = employee.getCode();
                companyId = employee.getCompany() != null ? employee.getCompany().getId() : null;
                description = "Employee - " + (employee.getGrade() != null ? employee.getGrade().getName() : "No Grade");
                
                // Get account details if available
                if (employee.getAccount() != null) {
                    var account = employee.getAccount();
                    accountResponse = mapToAccountResponse(account);
                }
            } else {
                // User is not an employee (might be admin/employer)
                fullName = user.getUsername();
                description = "System User - " + user.getRole().name();
                
                // For admin/employer, try to get company info
                var companies = companyRepository.findAll();
                if (!companies.isEmpty()) {
                    Company company = companies.getFirst(); // Default company
                    companyId = company.getId();
                    accountResponse = mapToAccountResponse(company.getAccount());
                }
            }
            
            log.debug("Successfully retrieved user details for: {}", user.getUsername());
            
            return new UserDetailsResponse(
                    userResponse,
                    accountResponse,
                    fullName,
                    description,
                    companyId,
                    bizId
            );
            
        } catch (Exception e) {
            log.error("Failed to get user details from token: {}", e.getMessage(), e);
            throw e;
        }
    }

    private static AccountResponse mapToAccountResponse(Account account) {
        return new AccountResponse(
                account.getId(),
                account.getOwnerType(),
                account.getOwnerId(),
                account.getAccountType(),
                account.getAccountName(),
                account.getAccountNumber(),
                account.getCurrentBalance(),
                account.getOverdraftLimit(),
                account.getBranch() != null ? account.getBranch().getId() : null,
                account.getBranch() != null ? account.getBranch().getBranchName() : null,
                account.getStatus(),
                account.getCreatedAt(),
                account.getCreatedBy()
        );
    }


    // --- Abstract Mapping Implementations (No changes needed) ---

    /**
     * Maps a {@code UserCreationRequest} DTO to a new {@code User} entity.
     * Applies password hashing before the entity is saved.
     *
     * @param creationRequest The incoming creation DTO.
     * @return The new, transient {@code User} entity.
     */
    @Override
    protected User mapToEntity(UserCreateRequest creationRequest) {
        // ... (implementation code) ...
        return User.builder()
                .username(creationRequest.username())
                .email(creationRequest.email())
                .passwordHash(passwordEncoder.encode(creationRequest.password()))
                .role(creationRequest.role())
                .build();
    }

    /**
     * Applies changes from a {@code UserUpdateRequest} DTO to an existing {@code User} entity.
     * Note: Password updates are handled separately in the {@code update()} method.
     *
     * @param updateRequest The incoming update DTO.
     * @param entity        The existing {@code User} entity.
     * @return The updated, detached {@code User} entity.
     */
    @Override
    protected User mapToEntity(UserUpdateRequest updateRequest, User entity) {
        // ... (implementation code) ...
        entity.setUsername(updateRequest.username());
        entity.setEmail(updateRequest.email());
        if (updateRequest.role() != null) {
            entity.setRole(updateRequest.role());
        }
        return entity;
    }

    /**
     * Maps a persistent {@code User} entity to its public-facing {@code UserResponse} DTO.
     *
     * @param entity The persistent {@code User} entity.
     * @return The {@code UserResponse} DTO.
     */
    @Override
    protected UserResponse mapToResponse(User entity) {
        // ... (implementation code) ...
        return new UserResponse(
                entity.getId(),
                entity.getUsername(),
                entity.getEmail(),
                entity.getRole(),
                entity.getCreatedAt()
        );
    }

    // --- Private Helper: Best Industry Practice Uniqueness Check ---

    /**
     * Performs uniqueness validation for username and email during an update operation.
     * Ensures that the new values are not already taken by *another* user.
     *
     * @param currentId   The ID of the user being updated (to exclude from the search).
     * @param newUsername The requested new username.
     * @param newEmail    The requested new email.
     * @throws DuplicateEntryException if the username or email is already in use by a different user.
     */
    private void checkUniquenessOnUpdate(UUID currentId, String newUsername, String newEmail) {
        // ... (implementation code) ...
        // 1. Check Username
        userRepository.findByUsername(newUsername).ifPresent(user -> {
            if (!user.getId().equals(currentId)) {
                throw DuplicateEntryException.forEntity("User", "username", newUsername);
            }
        });

        // 2. Check Email (Requires UserRepository method: boolean existsByEmailAndIdNot(String email, UUID id);)
        if (userRepository.existsByEmailAndIdNot(newEmail, currentId)) {
            throw DuplicateEntryException.forEntity("User", "email", newEmail);
        }
    }

    @Override
    protected Specification<User> buildSpecificationFromFilter(UserFilter filter) {
        return (root, query, cb) -> {
            List<jakarta.persistence.criteria.Predicate> predicates = new java.util.ArrayList<>();

            if (filter.keyword() != null && !filter.keyword().isBlank()) {
                String pattern = "%" + filter.keyword().toLowerCase() + "%";
                jakarta.persistence.criteria.Predicate usernameLike = cb.like(cb.lower(root.get("username")), pattern);
                jakarta.persistence.criteria.Predicate emailLike = cb.like(cb.lower(root.get("email")), pattern);
                predicates.add(cb.or(usernameLike, emailLike));
            }

            if (filter.role() != null) {
                predicates.add(cb.equal(root.get("role"), filter.role()));
            }

            if (predicates.isEmpty()) {
                return cb.conjunction();
            } else {
                return cb.and(predicates.toArray(new jakarta.persistence.criteria.Predicate[0]));
            }
        };
    }
}