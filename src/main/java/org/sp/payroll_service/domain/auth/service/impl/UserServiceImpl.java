package org.sp.payroll_service.domain.auth.service.impl;

import lombok.extern.slf4j.Slf4j;
import org.sp.payroll_service.api.auth.dto.*;
import org.sp.payroll_service.domain.auth.entity.User;
import org.sp.payroll_service.domain.auth.service.UserService;
import org.sp.payroll_service.domain.common.exception.DuplicateEntryException;
import org.sp.payroll_service.domain.common.exception.ErrorCodes;
import org.sp.payroll_service.domain.common.exception.ResourceNotFoundException;
import org.sp.payroll_service.domain.common.exception.ValidationException;
import org.sp.payroll_service.domain.common.service.AbstractCrudService;
import org.sp.payroll_service.repository.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.scheduling.annotation.Async;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;

/**
 * Concrete service implementation for managing {@code User} entities.
 * <p>
 * This class extends the generic {@code AbstractCrudService} to inherit boilerplate CRUD operations
 * (create, find, update, delete) and adds domain-specific business logic, such as:
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

    /**
     * Constructs the UserServiceImpl.
     * <p>
     * Initializes the base service with the repository and resource name ("User"),
     * and injects the necessary domain-specific dependencies.
     *
     * @param userRepository  The JPA repository for User entities.
     * @param passwordEncoder The Spring Security password encoder for hashing.
     */
    public UserServiceImpl(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        super(userRepository, "User");
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    // --- Overrides for Creation and Update with Business Logic ---

    /**
     * Creates a new user after performing uniqueness checks on the username and email.
     *
     * @param request The DTO containing the user creation data.
     * @return A {@code CompletableFuture} containing the {@code UserResponse} DTO of the newly created user.
     * @throws DuplicateEntryException if the username or email already exists.
     */
    @Override
    @Transactional
    public CompletableFuture<UserResponse> create(UserCreateRequest request) {
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
     * @return A {@code CompletableFuture} containing the {@code UserResponse} DTO of the updated user.
     * @throws ValidationException     if the current password check fails during a password change attempt.
     * @throws DuplicateEntryException if the new username or email is already taken by another user.
     */
    @Override
    @Transactional
    public CompletableFuture<UserResponse> update(UUID id, UserUpdateRequest request) {
        User existingUser = repository.findById(id).orElseThrow();

        checkUniquenessOnUpdate(id, request.username(), request.email());

        // Business Rule: Handle optional password change
        if (request.newPassword() != null && !request.newPassword().isEmpty()) {
            if (!passwordEncoder.matches(request.currentPassword(), existingUser.getPasswordHash())) {
                throw new ValidationException("Current password is required and incorrect to change password.", ErrorCodes.AUTH_INVALID_CREDENTIALS);
            }
            existingUser.setPasswordHash(passwordEncoder.encode(request.newPassword()));
        }

        return super.update(id, request);
    }

    // --- Custom Search Implementation (Overrides AbstractCrudService.search) ---

    /**
     * Executes a dynamic search for users based on the provided filter criteria and pagination settings.
     * Uses JPA Specifications to build the query chain.
     *
     * @param filter   The {@code UserFilter} DTO containing search criteria (keyword, role).
     * @param pageable The pagination and sorting information.
     * @return A {@code CompletableFuture} containing a {@code Page} of {@code UserResponse} DTOs.
     */
    @Override
    @Async("virtualThreadExecutor")
    @Transactional(readOnly = true)
    public CompletableFuture<Page<UserResponse>> search(UserFilter filter, Pageable pageable) {

        // Start with an empty specification (no where clause)
        Specification<User> spec = (root, query, cb) -> null;

        // Keyword filter
        if (filter.keyword() != null && !filter.keyword().isBlank()) {
            String pattern = "%" + filter.keyword().toLowerCase() + "%";

            Specification<User> keywordSpec = (root, query, cb) -> cb.or(
                    cb.like(cb.lower(root.get("username")), pattern),
                    cb.like(cb.lower(root.get("email")), pattern)
            );

            spec = spec.and(keywordSpec);
        }

        // Role filter
        if (filter.role() != null) {
            Specification<User> roleSpec = (root, query, cb) -> cb.equal(root.get("role"), filter.role());
            spec = spec.and(roleSpec);
        }

        // Query execution
        Page<User> entityPage = specExecutor.findAll(spec, pageable);
        Page<UserResponse> responsePage = entityPage.map(this::mapToResponse);

        return CompletableFuture.completedFuture(responsePage);
    }

    /**
     * Finds a user by their unique username.
     * * @param username The username to search for.
     * @return A {@code CompletableFuture} containing the found {@code UserResponse} DTO.
     * @throws ResourceNotFoundException if no user with the given username is found.
     */
    @Override
    @Async("virtualThreadExecutor")
    @Transactional(readOnly = true)
    public CompletableFuture<UserResponse> findByUsername(String username) {
        log.debug("Fetching User by username: {}", username);

        return userRepository.findByUsername(username)
                .map(this::mapToResponse)
                .map(CompletableFuture::completedFuture)
                .orElseThrow(() -> ResourceNotFoundException.forEntity("User", username));
    }


    // --- Abstract Mapping Implementations (Required by AbstractCrudService) ---

    /**
     * Maps a {@code UserCreationRequest} DTO to a new {@code User} entity.
     * Applies password hashing before the entity is saved.
     *
     * @param creationRequest The incoming creation DTO.
     * @return The new, transient {@code User} entity.
     */
    @Override
    protected User mapToEntity(UserCreateRequest creationRequest) {
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
}