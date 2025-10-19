package org.sp.payroll_service.repository;

import org.sp.payroll_service.domain.core.Company;
import org.sp.payroll_service.domain.common.repository.BaseRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

/**
 * Company data access repository.
 * Focuses on uniqueness checks for the business name.
 */
@Repository
public interface CompanyRepository extends BaseRepository<Company, UUID> {
    
    /**
     * Finds a Company entity by its unique business name (excluding DELETED status).
     * @param name The unique name of the company
     * @return Optional Company entity
     */
    Optional<Company> findByName(String name);
    
    /**
     * Checks if a Company exists with the given name (excluding DELETED status).
     * @param name The unique name of the company
     * @return true if a company with that name exists
     */
    boolean existsByName(String name);
}