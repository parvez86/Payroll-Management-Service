package org.sp.payroll_service.repository;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.sp.payroll_service.domain.auth.entity.User;
import org.sp.payroll_service.domain.common.repository.BaseRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

/**
 * User data access repository with custom query methods.
 */
@Repository
public interface UserRepository extends BaseRepository<User, UUID> {
    
    /**
     * Finds user by username for authentication.
     * @param username unique identifier
     * @return optional user entity
     */
    Optional<User> findByUsername(String username);
    
    /**
     * Finds user by email for validation.
     * @param email user email
     * @return optional user entity
     */
    Optional<User> findByEmail(String email);
    
    /**
     * Checks if username exists for validation.
     * @param username unique identifier
     * @return true if username exists
     */
    boolean existsByUsername(String username);

    /**
     * Checks if username exists for validation (case-insensitive).
     * @param username unique identifier
     * @return true if username exists
     */
    boolean existsByUsernameIgnoreCase(@NotBlank @Size(min = 3, max = 50) String username);

    /**
     * Checks if email exists for validation.
     * @param email unique identifier
     * @return true if email exists
     */
    boolean existsByEmail(String email);

    /**
     * Checks if email exists for validation (case-insensitive).
     * @param email unique identifier
     * @return true if email exists
     */
    boolean existsByEmailIgnoreCase(@Email String email);

    /**
     * Checks if email exists for validation.
     * @param newEmail unique identifier
     * @param currentId unique identifier
     * @return true if a email exists
     */
    boolean existsByEmailAndIdNot(String newEmail, UUID currentId);
}