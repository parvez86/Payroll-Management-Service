package org.sp.payroll_service.repository;

import org.sp.payroll_service.domain.core.entity.Branch;
import org.sp.payroll_service.domain.common.repository.BaseRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Branch data access repository.
 * Allows querying branches based on the associated Bank.
 */
@Repository
public interface BranchRepository extends BaseRepository<Branch, UUID> {

    /**
     * Finds all active branches belonging to a specific Bank.
     * @param bankId The parent Bank entity id
     * @return List of active Branch entities
     */
    List<Branch> findAllByBank_Id(UUID bankId);

    /**
     * Finds a paginated list of active branches belonging to a specific Bank.
     * @param bankId The parent Bank entity id
     * @param pageable Pagination information
     * @return Page of active Branch entities
     */
    Page<Branch> findAllByBank_Id(UUID bankId, Pageable pageable);

    /**
     * Finds a branch by its name and the parent bank (ensuring uniqueness within a bank).
     * @param branchName The name of the branch
     * @param bankId The parent Bank entity id
     * @return Optional Branch entity
     */
    Optional<Branch> findByBranchNameAndBank_Id(String branchName, UUID bankId);

    /**
     * Checks if a branch with the given name exists under a specific bank.
     * @param branchName The name of the branch
     * @param bankId The parent Bank entity id
     * @return true if a branch exists under the bank
     */
    boolean existsByBranchNameAndBank_Id(String branchName, UUID bankId);

    /**
     * Checks for unique branch name under a specific bank, excluding the current ID.
     * @param branchName The name of the branch
     * @param bankId The parent Bank entity id
     * @param currentId The current entity id
     * @return true if a branch exists with branch name and under bank id, excluding current id
     */
    boolean existsByBranchNameAndBankIdAndIdNot(String branchName, UUID bankId, UUID currentId);
}