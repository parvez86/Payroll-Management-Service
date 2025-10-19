package org.sp.payroll_service.repository;

import org.sp.payroll_service.domain.core.Bank;
import org.sp.payroll_service.domain.common.repository.BaseRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

/**
 * Bank data access repository.
 * Focuses on unique lookups for financial identifiers (name, SWIFT code).
 */
@Repository
public interface BankRepository extends BaseRepository<Bank, UUID> {

    /**
     * Finds an active Bank entity by its official name.
     * Used for retrieving details or checking name uniqueness.
     * @param name The official name of the bank
     * @return Optional Bank entity
     */
    Optional<Bank> findByName(String name);

    /**
     * Checks if an active Bank exists with the given name.
     * @param name The official name of the bank
     * @return true if a bank with that name exists
     */
    boolean existsByName(String name);

    /**
     * Finds an active Bank entity by its unique SWIFT/BIC code.
     * CRITICAL for payment routing and validation.
     * @param swiftBicCode The bank's SWIFT/BIC code
     * @return Optional Bank entity
     */
    Optional<Bank> findBySwiftBicCode(String swiftBicCode);

    /**
     * Checks if an active Bank exists with the given SWIFT/BIC code.
     * @param swiftBicCode The bank's SWIFT/BIC code
     * @return true if a bank with that SWIFT/BIC code exists
     */
    boolean existsBySwiftBicCode(String swiftBicCode);
}
