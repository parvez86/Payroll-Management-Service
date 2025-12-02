package org.sp.payroll_service.domain.core.service;

import org.sp.payroll_service.domain.common.enums.CompanyRoleType;
import org.sp.payroll_service.domain.core.entity.CompanyUserRole;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Service interface for managing company-user role associations.
 * Provides centralized access control for company-level operations.
 * All methods respect temporal validity (active flag + valid_from/valid_to).
 */
public interface CompanyUserRoleService{
    /**
     * Get all company IDs a user has access to (any role).
     * Only returns currently valid (active + time-valid) assignments.
     *
     * @param userId the user ID
     * @return list of accessible company IDs (empty if none)
     */
    List<UUID> getUserCompanyIds(UUID userId);

    /**
     * Get all company IDs a user has access to with specific roles.
     * Only returns currently valid (active + time-valid) assignments.
     * 
     * @param userId the user ID
     * @param roles the required roles (varargs)
     * @return list of accessible company IDs (empty if none)
     */
    List<UUID> getUserCompanyIds(UUID userId, CompanyRoleType... roles);
    
    /**
     * Check if a user has access to a specific company (any role).
     * 
     * @param userId the user ID
     * @param companyId the company ID
     * @return true if user has access
     */
    boolean hasAccessToCompany(UUID userId, UUID companyId);
    
    /**
     * Check if a user has a specific role on a company.
     * 
     * @param userId the user ID
     * @param companyId the company ID
     * @param role the required role
     * @return true if user has the role
     */
    boolean hasRole(UUID userId, UUID companyId, CompanyRoleType role);
    
    /**
     * Check if a user has any of the specified roles on a company.
     * 
     * @param userId the user ID
     * @param companyId the company ID
     * @param roles the acceptable roles
     * @return true if user has any of the roles
     */
    boolean hasAnyRole(UUID userId, UUID companyId, CompanyRoleType... roles);
    
    /**
     * Get all active role assignments for a user.
     * 
     * @param userId the user ID
     * @return list of active roles
     */
    List<CompanyUserRole> getActiveRolesForUser(UUID userId);
    
    /**
     * Get all active role assignments for a company.
     * 
     * @param companyId the company ID
     * @return list of active roles
     */
    List<CompanyUserRole> getActiveRolesForCompany(UUID companyId);
    
    /**
     * Get a specific role assignment.
     * 
     * @param userId the user ID
     * @param companyId the company ID
     * @param role the role type
     * @return optional role assignment
     */
    Optional<CompanyUserRole> getRole(UUID userId, UUID companyId, CompanyRoleType role);
    
    /**
     * Assign a role to a user for a company.
     * Creates a new active role assignment with full access scope by default.
     * Idempotent: returns existing assignment if already exists.
     * 
     * @param userId the user ID
     * @param companyId the company ID
     * @param role the role to assign
     * @param createdBy who is creating this assignment (for audit)
     * @return the created or existing role assignment
     * @throws org.sp.payroll_service.domain.common.exception.ResourceNotFoundException if user or company not found
     */
    CompanyUserRole assignRole(UUID userId, UUID companyId, CompanyRoleType role, UUID createdBy);
    
    /**
     * Revoke a role assignment (soft delete - sets active = false).
     * Updates the updatedBy field for audit trail.
     * 
     * @param roleId the role assignment ID
     * @param revokedBy who is revoking this assignment (for audit)
     * @throws org.sp.payroll_service.domain.common.exception.ResourceNotFoundException if role not found
     */
    void revokeRole(UUID roleId, UUID revokedBy);
    
    /**
     * Check if a user has any company manager role (EMPLOYER or COMPANY_ADMIN).
     * Useful for determining if user can perform company-level administrative actions.
     * 
     * @param userId the user ID
     * @return true if user manages at least one company
     */
    boolean isCompanyManager(UUID userId);
}
