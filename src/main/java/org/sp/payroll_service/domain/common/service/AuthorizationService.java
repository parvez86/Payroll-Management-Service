package org.sp.payroll_service.domain.common.service;

import org.sp.payroll_service.domain.common.dto.response.HeaderResponse;
import org.sp.payroll_service.domain.payroll.entity.Employee;
import org.springframework.data.jpa.domain.Specification;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Centralized authorization service for role-based access control.
 * Handles complex hierarchical access logic for ADMIN, EMPLOYER, and EMPLOYEE roles.
 * Makes authorization decisions reusable across all domain services.
 */
public interface AuthorizationService {
    
    /**
     * Apply role-based access filtering for Employee queries.
     * - ADMIN: sees all employees
     * - EMPLOYER: sees employees in companies they manage
     * - EMPLOYEE: sees self + downstream subordinates
     * 
     * @param principal the authenticated user's principal
     * @return JPA specification for filtering employees
     */
    Specification<Employee> applyEmployeeAccessFilter(HeaderResponse principal);
    
    /**
     * Apply generic role-based access filtering for any entity with company relationship.
     * Filters based on accessible company IDs for the user's role.
     * 
     * @param principal the authenticated user's principal
     * @param <T> the entity type
     * @return JPA specification for filtering by company
     */
    <T> Specification<T> applyCompanyBasedAccessFilter(HeaderResponse principal);
    
    /**
     * Apply generic role-based access filtering for any entity with employee relationship.
     * Filters based on accessible employee IDs for the user's role.
     * 
     * @param principal the authenticated user's principal
     * @param <T> the entity type
     * @return JPA specification for filtering by employee
     */
    <T> Specification<T> applyEmployeeBasedAccessFilter(HeaderResponse principal);
    
    /**
     * Get all company IDs the user can access based on their role.
     * - ADMIN: all companies
     * - EMPLOYER: companies they have role assignments for
     * - EMPLOYEE: their employer's company
     * 
     * @param principal the authenticated user's principal
     * @return list of accessible company IDs (empty for no access)
     */
    List<UUID> getAccessibleCompanyIds(HeaderResponse principal);
    
    /**
     * Get all employee IDs the user can access based on their role.
     * - ADMIN: all employees
     * - EMPLOYER: employees in companies they manage
     * - EMPLOYEE: self + downstream subordinates
     * 
     * @param principal the authenticated user's principal
     * @return list of accessible employee IDs (empty for no access)
     */
    List<UUID> getAccessibleEmployeeIds(HeaderResponse principal);
    
    /**
     * Get downstream employee IDs (subordinates) for a given employee.
     * Uses grade-based hierarchy: employees with lower grades (higher rank numbers).
     * 
     * @param employeeId the employee ID
     * @return list of subordinate employee IDs
     */
    List<UUID> getDownstreamEmployeeIds(UUID employeeId);
    
    /**
     * Check if a user can access a specific employee.
     * 
     * @param principal the authenticated user's principal
     * @param employeeId the employee ID to check
     * @return true if user can access the employee
     */
    boolean canAccessEmployee(HeaderResponse principal, UUID employeeId);
    
    /**
     * Check if a user can access a specific company.
     * 
     * @param principal the authenticated user's principal
     * @param companyId the company ID to check
     * @return true if user can access the company
     */
    boolean canAccessCompany(HeaderResponse principal, UUID companyId);
    
    /**
     * Get the employee entity for the current user (if they are an employee).
     * 
     * @param userId the user ID
     * @return optional employee entity
     */
    Optional<Employee> getEmployeeForUser(UUID userId);
}
