package org.sp.payroll_service.repository;

import org.sp.payroll_service.domain.core.entity.Bank;
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

    /**
     * Checks if a bank with the given name exists, excluding the provided ID.
     */
    /**
     * Checks if an active Bank exists with the given SWIFT/BIC code.
     * @param name The bank's name
     * @param id The bank's id
     * @return true if a bank with that name exists excluding id
     */
    boolean existsByNameAndIdNot(String name, UUID id);

    /**
     * Checks if a bank with the given SWIFT/BIC code exists, excluding the provided ID.
     */
    /**
     * Checks if an active Bank exists with the given SWIFT/BIC code.
     * @param swiftBicCode The bank's SWIFT/BIC code
     * @param id The bank's id
     * @return true if a bank with that SWIFT/BIC code exists excluding id
     */
    boolean existsBySwiftBicCodeAndIdNot(String swiftBicCode, UUID id);
}
