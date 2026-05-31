package org.sp.payroll_service.api.auth.dto;

import jakarta.validation.constraints.*;
import org.sp.payroll_service.domain.common.enums.PreferenceScope;
import org.sp.payroll_service.domain.common.enums.Theme;

/**
 * Request DTO for creating or updating user preferences.
 * Includes validation constraints for all fields.
 */
public record UserPreferencesRequest(
    @NotNull(message = "Scope is required")
    PreferenceScope selectedScope,
    
    @Pattern(
        regexp = "^[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}$|^$",
        message = "Invalid company UUID format or empty"
    )
    String selectedCompanyId,  // Required if scope is COMPANY
    
    @NotNull(message = "Theme is required")
    Theme theme,
    
    @NotNull(message = "Language is required")
    @Pattern(regexp = "^[a-z]{2}$", message = "Language must be ISO 639-1 code (e.g., en, bn)")
    String language
) {}
