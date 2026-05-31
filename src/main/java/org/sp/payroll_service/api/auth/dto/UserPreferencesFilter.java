package org.sp.payroll_service.api.auth.dto;

import org.sp.payroll_service.domain.common.enums.PreferenceScope;
import org.sp.payroll_service.domain.common.enums.Theme;

import java.util.UUID;

/**
 * Filter DTO for searching and filtering user preferences.
 * Used for advanced search queries on the UserPreferences resource.
 */
public record UserPreferencesFilter(
        PreferenceScope selectedScope,
        UUID selectedCompanyId,
        Theme theme,
        String language
) {
}
