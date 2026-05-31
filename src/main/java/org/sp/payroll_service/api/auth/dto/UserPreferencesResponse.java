package org.sp.payroll_service.api.auth.dto;

import org.sp.payroll_service.domain.common.enums.PreferenceScope;
import org.sp.payroll_service.domain.common.enums.Theme;

import java.time.Instant;
import java.util.UUID;

/**
 * Response DTO for user preferences.
 * Returns user's UI customization settings.
 */
public record UserPreferencesResponse(
    UUID userId,
    PreferenceScope selectedScope,
    UUID selectedCompanyId,
    String selectedCompanyName,
    Theme theme,
    String language,
    Instant updatedAt
) {}
