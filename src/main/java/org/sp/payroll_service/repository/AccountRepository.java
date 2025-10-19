package org.sp.payroll_service.repository;

import org.sp.payroll_service.domain.wallet.Account;
import org.sp.payroll_service.domain.common.repository.BaseRepository;
import org.sp.payroll_service.domain.common.enums.OwnerType;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Account data access repository.
 * Central for financial lookups by account number and owner.
 */
@Repository
public interface AccountRepository extends BaseRepository<Account, UUID> {
    
    /**
     * Finds an active Account by its unique account number.
     * @param accountNumber The unique account number
     * @return Optional Account entity
     */
    Optional<Account> findByAccountNumber(String accountNumber);
    
    /**
     * Finds all active Accounts belonging to a specific owner ID and type.
     * Crucial for retrieving the main company funding account or employee accounts.
     * @param ownerId The UUID of the owner (Employee or Company)
     * @param ownerType The type of the owner (Employee or Company)
     * @return List of active Account entities
     */
    List<Account> findAllByOwnerIdAndOwnerType(UUID ownerId, OwnerType ownerType);
    
    /**
     * Checks if an active Account exists with the given account number.
     */
    boolean existsByAccountNumber(String accountNumber);
}