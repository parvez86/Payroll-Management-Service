package org.sp.payroll_service.domain.auth.service;

import org.sp.payroll_service.api.auth.dto.UserPreferencesFilter;
import org.sp.payroll_service.api.auth.dto.UserPreferencesRequest;
import org.sp.payroll_service.api.auth.dto.UserPreferencesResponse;
import org.sp.payroll_service.domain.auth.entity.UserPreferences;
import org.sp.payroll_service.domain.common.enums.PreferenceScope;
import org.sp.payroll_service.domain.common.enums.Role;
import org.sp.payroll_service.domain.common.service.BaseCrudService;

import java.util.Optional;
import java.util.UUID;

/**
 * Service interface for user preference operations.
 * Extends the generic CRUD contract and adds preference-specific operations.
 */
public interface UserPreferenceService extends BaseCrudService<
        UUID,                       // ID type
        UserPreferencesResponse,    // Response DTO
        UserPreferencesRequest,     // Create Request DTO
        UserPreferencesRequest,     // Update Request DTO
        UserPreferencesFilter       // Filter DTO
        > {
    
    /**
     * Create default preferences for a newly created user.
     * Called during user registration to initialize default preferences based on role.
     * 
     * @param userId the new user's ID
     * @param role the user's role (ADMIN, EMPLOYER, EMPLOYEE)
     * @return the created UserPreferencesResponse dto
     */
    UserPreferencesResponse createDefaultPreferences(UUID userId, Role role);
    
    /**
     * Get preferred company ID if user selected COMPANY scope.
     * Returns empty if user selected GLOBAL scope or company doesn't exist.
     * 
     * @param userId the user's ID
     * @return Optional containing company ID, or empty if GLOBAL scope
     */
    Optional<UUID> getPreferredCompanyId(UUID userId);

    /**
     * Retrieves the user's preferences and current context.
     *
     * @param userId the user's ID
     * @return the user's preferences including selected scope,
     *         selected company, theme, and language
     * @throws org.sp.payroll_service.domain.common.exception.ResourceNotFoundException if no preferences exist for the user
     */
    Optional<UserPreferencesResponse> getUserPreferenceByUserId(UUID userId);
    
    /**
     * Get user's preferred scope (GLOBAL or COMPANY).
     * Returns COMPANY as default if preferences don't exist.
     * 
     * @param userId the user's ID
     * @return the PreferenceScope (GLOBAL or COMPANY)
     */
    PreferenceScope getPreferredScope(UUID userId);
}
