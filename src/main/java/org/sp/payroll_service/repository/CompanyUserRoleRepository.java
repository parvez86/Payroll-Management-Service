package org.sp.payroll_service.repository;

import org.sp.payroll_service.domain.common.enums.CompanyRoleType;
import org.sp.payroll_service.domain.common.repository.BaseRepository;
import org.sp.payroll_service.domain.core.entity.CompanyUserRole;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository for CompanyUserRole entity.
 * Provides methods to query company-user role associations for authorization.
 */
@Repository
public interface CompanyUserRoleRepository extends BaseRepository<CompanyUserRole, UUID> {
    
    /**
     * Find all active role assignments for a user
     */
    List<CompanyUserRole> findByUserIdAndActiveTrue(UUID userId);
    
    /**
     * Find all active role assignments for a company
     */
    List<CompanyUserRole> findByCompanyIdAndActiveTrue(UUID companyId);
    
    /**
     * Check if user has any of the specified roles for a company
     */
    @Query("SELECT CASE WHEN COUNT(cur) > 0 THEN true ELSE false END " +
           "FROM CompanyUserRole cur " +
           "WHERE cur.user.id = :userId " +
           "AND cur.company.id = :companyId " +
           "AND cur.roleOnCompany IN :roles " +
           "AND cur.active = true " +
           "AND (cur.validFrom IS NULL OR cur.validFrom <= :now) " +
           "AND (cur.validTo IS NULL OR cur.validTo >= :now)")
    boolean existsByUserIdAndCompanyIdAndRoleIn(
        @Param("userId") UUID userId,
        @Param("companyId") UUID companyId,
        @Param("roles") List<CompanyRoleType> roles,
        @Param("now") Instant now
    );
    
    /**
     * Get all company IDs a user has access to (with any role)
     */
    @Query("SELECT DISTINCT cur.company.id " +
           "FROM CompanyUserRole cur " +
           "WHERE cur.user.id = :userId " +
           "AND cur.active = true " +
           "AND (cur.validFrom IS NULL OR cur.validFrom <= :now) " +
           "AND (cur.validTo IS NULL OR cur.validTo >= :now)")
    List<UUID> findCompanyIdsByUserId(@Param("userId") UUID userId, @Param("now") Instant now);

    /**
     * Get all company  a user has access to (with any role)
     */
    @Query("SELECT DISTINCT cur.company.id, cur.company.name " +
            "FROM CompanyUserRole cur " +
            "WHERE cur.user.id = :userId " +
            "AND cur.active = true " +
            "AND (cur.validFrom IS NULL OR cur.validFrom <= :now) " +
            "AND (cur.validTo IS NULL OR cur.validTo >= :now)")
    Map<UUID, String> findCompanyInfosByUserId(@Param("userId") UUID userId, @Param("now") Instant now);


    /**
     * Get all company IDs a user has access to with specific roles
     */
    @Query("SELECT DISTINCT cur.company.id " +
           "FROM CompanyUserRole cur " +
           "WHERE cur.user.id = :userId " +
           "AND cur.roleOnCompany IN :roles " +
           "AND cur.active = true " +
           "AND (cur.validFrom IS NULL OR cur.validFrom <= :now) " +
           "AND (cur.validTo IS NULL OR cur.validTo >= :now)")
    List<UUID> findCompanyIdsByUserIdAndRoles(
        @Param("userId") UUID userId,
        @Param("roles") List<CompanyRoleType> roles,
        @Param("now") Instant now
    );
    
    /**
     * Find a specific active role assignment
     */
    @Query("SELECT cur FROM CompanyUserRole cur " +
           "WHERE cur.user.id = :userId " +
           "AND cur.company.id = :companyId " +
           "AND cur.roleOnCompany = :role " +
           "AND cur.active = true")
    Optional<CompanyUserRole> findByUserIdAndCompanyIdAndRoleAndActiveTrue(
        @Param("userId") UUID userId,
        @Param("companyId") UUID companyId,
        @Param("role") CompanyRoleType role
    );
    
    /**
     * Check if a user has any active role in any company
     */
    @Query("SELECT CASE WHEN COUNT(cur) > 0 THEN true ELSE false END " +
           "FROM CompanyUserRole cur " +
           "WHERE cur.user.id = :userId " +
           "AND cur.active = true " +
           "AND (cur.validFrom IS NULL OR cur.validFrom <= :now) " +
           "AND (cur.validTo IS NULL OR cur.validTo >= :now)")
    boolean existsByUserIdAndActiveTrue(@Param("userId") UUID userId, @Param("now") Instant now);

    /**
     * Checks for the existence of a user record based on multiple criteria,
     * specifically verifying a user's active association and specific role
     * within a given company.
     *
     * <p>This method translates into a SQL query equivalent to:</p>
     * <code>
     * SELECT CASE WHEN COUNT(u) > 0 THEN TRUE ELSE FALSE END
     * FROM User u
     * WHERE u.userId = :userId
     * AND u.companyId = :companyId
     * AND u.roleOnCompany = :companyRoleType
     * AND u.active = TRUE
     * </code>
     *
     * @param userId The unique identifier (UUID) of the user to check.
     * @param companyId The unique identifier (UUID) of the company for which the role is being verified.
     * @param companyRoleType The specific role type (e.g., ADMIN, EMPLOYER, EMPLOYEE)
     * that the user must possess on the company.
     * @return {@code true} if an active user record exists with the specified
     * ID, company, and role; {@code false} otherwise.
     */
    boolean existsByUser_IdAndCompany_IdAndRoleOnCompanyAndActiveTrue(
            UUID userId,
            UUID companyId,
            CompanyRoleType companyRoleType
    );
}
