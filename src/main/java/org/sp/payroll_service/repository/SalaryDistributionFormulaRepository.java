package org.sp.payroll_service.repository;

import org.sp.payroll_service.domain.common.repository.BaseRepository;
import org.sp.payroll_service.domain.payroll.entity.SalaryDistributionFormula;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

/**
 * Repository for managing salary distribution formulas.
 */
@Repository
public interface SalaryDistributionFormulaRepository extends BaseRepository<SalaryDistributionFormula, UUID> {
    
    /**
     * Find the default salary formula (assuming there's typically one active formula).
     */
    @Query("SELECT sdf FROM SalaryDistributionFormula sdf ORDER BY sdf.createdAt DESC LIMIT 1")
    Optional<SalaryDistributionFormula> findDefaultFormula();

    /**
     * Checks if a formula with the given name exists.
     * Used during creation for uniqueness validation.
     *
     * @param name The name of the salary formula.
     * @return true if a formula with the name exists, false otherwise.
     */
    boolean existsByName(String name);

    /**
     * Finds a formula by its unique name.
     * Used during update for uniqueness validation.
     *
     * @param name The name of the salary formula.
     * @return An Optional containing the found formula, or empty if not found.
     */
    Optional<SalaryDistributionFormula> findByName(String name);
    
    /**
     * Check if any formula exists.
     */
    @Query("SELECT COUNT(sdf) > 0 FROM SalaryDistributionFormula sdf")
    boolean existsAny();
}