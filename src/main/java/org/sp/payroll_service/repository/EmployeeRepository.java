package org.sp.payroll_service.repository;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import org.sp.payroll_service.domain.common.repository.BaseRepository;
import org.sp.payroll_service.domain.payroll.entity.Employee;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Employee repository with custom query methods.
 */
@Repository
public interface EmployeeRepository extends BaseRepository<Employee, UUID> {
    /**
     * Finds employee by business identifier.
     * @param code 4-digit business ID
     * @return optional employee
     */
    Optional<Employee> findByCode(@NotBlank(message = "Business ID is required.") @Pattern(regexp = "\\d{4}", message = "Business ID must be 4 digits.") String code);

    /**
     * Finds employee by user ID.
     * @param userId user identifier
     * @return optional employee
     */
    @Query("SELECT e FROM Employee e " +
           "LEFT JOIN FETCH e.grade g " +
           "LEFT JOIN FETCH e.company c " +
           "LEFT JOIN FETCH c.salaryFormula " +
           "LEFT JOIN FETCH e.account a " +
           "LEFT JOIN FETCH a.branch " +
           "WHERE e.user.id = :userId")
    Optional<Employee> findByUserId(@Param("userId") UUID userId);

    /**
     * Find employee by ID with all related entities eagerly loaded.
     * @param id employee ID
     * @return optional employee with joined entities
     */
    @Query("SELECT e FROM Employee e " +
           "LEFT JOIN FETCH e.grade g " +
           "LEFT JOIN FETCH e.company c " +
           "LEFT JOIN FETCH c.salaryFormula " +
           "LEFT JOIN FETCH e.account a " +
           "LEFT JOIN FETCH a.branch " +
           "WHERE e.id = :id")
    Optional<Employee> findByIdWithJoins(@Param("id") UUID id);

    /**
     * Finds all employees ordered by grade ranking.
     * @return list of employees
     */
    @Query("SELECT e FROM Employee e JOIN FETCH e.grade g ORDER BY g.rank ASC, e.name ASC")
    List<Employee> findAllOrderedByGrade();
    
    /**
     * Counts employees by grade for validation.
     * @param gradeId grade identifier
     * @return employee count
     */
    long countByGradeId(UUID gradeId);
    
    /**
     * Checks if business ID exists.
     * @param code business identifier
     * @return true if exists
     */
    boolean existsByCode(String code);
    
    /**
     * Get employee count grouped by grade.
     * @return employee count statistics by grade
     */
    @Query("SELECT g.name, COUNT(e) FROM Employee e JOIN e.grade g GROUP BY g.id, g.name ORDER BY g.rank")
    List<Object[]> getEmployeeCountByGrade();

    /**
     * Executes the logic: MAX(INT(BizId)). This assumes the Employee entity
     * has a String field named 'bizId' which stores the 4-digit ID (e.g., "0123").
     * We cast it to an integer inside the query to ensure correct numerical sorting.
     */
    // We are now casting the String 'bizId' field to an Integer type for the MAX aggregation.
    @Query("SELECT COALESCE(MAX(CAST(e.code AS integer)),1) FROM Employee e")
    int findMaxIdNumber();
}