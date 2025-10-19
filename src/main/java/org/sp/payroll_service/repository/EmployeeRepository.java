package org.sp.payroll_service.repository;

import org.sp.payroll_service.domain.common.repository.BaseRepository;
import org.sp.payroll_service.domain.payroll.Employee;
import org.springframework.data.jpa.repository.Query;
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
     * @param bizId 4-digit business ID
     * @return optional employee
     */
    @Query("SELECT e FROM Employee e JOIN FETCH e.grade JOIN FETCH e.account WHERE e.bizId = :bizId")
    Optional<Employee> findByBizIdWithDetails(String bizId);

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
     * @param bizId business identifier
     * @return true if exists
     */
    boolean existsByBizId(String bizId);
}