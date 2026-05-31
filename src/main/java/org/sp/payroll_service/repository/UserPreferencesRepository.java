package org.sp.payroll_service.repository;

import org.sp.payroll_service.domain.auth.entity.UserPreferences;
import org.sp.payroll_service.domain.common.enums.Theme;
import org.sp.payroll_service.domain.common.repository.BaseRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository for UserPreferences entity.
 * Provides data access methods for user preference queries with soft-delete support.
 */
@Repository
public interface UserPreferencesRepository extends BaseRepository<UserPreferences, UUID> {
    
    /**
     * Find preferences for a user by user ID.
     * Uses custom query to find by the user relationship.
     * @param userId the user's UUID ID
     * @return optional containing user preferences if found
     */
    @Query("SELECT up FROM UserPreferences up WHERE up.user.id = :userId")
    Optional<UserPreferences> findByUserId(@Param("userId") UUID userId);
    
    /**
     * Check if user has preferences for a specific company.
     * Uses custom query to find by user relationship and company.
     * @param userId the user's UUID ID
     * @param companyId the company's UUID ID
     * @return optional containing user preferences if found
     */
    @Query("SELECT up FROM UserPreferences up WHERE up.user.id = :userId AND up.selectedCompany.id = :companyId")
    Optional<UserPreferences> findByUserIdAndSelectedCompanyId(@Param("userId") UUID userId, @Param("companyId") UUID companyId);
    
    /**
     * Find all users who prefer a specific company.
     * Uses custom query for explicit entity relationship navigation.
     * @param companyId the company's UUID ID
     * @return list of user preferences for that company
     */
    @Query("SELECT up FROM UserPreferences up WHERE up.selectedCompany.id = :companyId")
    List<UserPreferences> findBySelectedCompanyId(@Param("companyId") UUID companyId);
    
    /**
     * Find all users with a specific language preference
     * @param language the ISO 639-1 language code (e.g., 'en', 'bn')
     * @return list of user preferences with that language
     */
    List<UserPreferences> findByLanguage(String language);
    
    /**
     * Find all users with a specific theme preference
     * @param theme the theme enum value
     * @return list of user preferences with that theme
     */
    List<UserPreferences> findByTheme(Theme theme);
}
