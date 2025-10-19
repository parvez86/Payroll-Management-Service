package org.sp.payroll_service.repository;

import org.sp.payroll_service.domain.auth.User;
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
}