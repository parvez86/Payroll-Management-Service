package org.sp.payroll_service.repository;

import org.sp.payroll_service.domain.core.Grade;
import org.sp.payroll_service.domain.common.repository.BaseRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

/**
 * Grade data access repository.
 * Focuses on uniqueness checks for grade name and level.
 */
@Repository
public interface GradeRepository extends BaseRepository<Grade, UUID> {
    
    /**
     * Finds an active Grade entity by its unique name.
     * @param name The name of the grade (e.g., "Senior Engineer")
     * @return Optional Grade entity
     */
    Optional<Grade> findByName(String name);
    
    /**
     * Checks if an active Grade exists with the given name.
     * @param name The name of the grade
     * @return true if a grade with that name exists
     */
    boolean existsByName(String name);
}
