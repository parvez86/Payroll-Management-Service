package org.sp.payroll_service.api.auth.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.sp.payroll_service.api.auth.dto.UserPreferencesRequest;
import org.sp.payroll_service.api.auth.dto.UserPreferencesResponse;
import org.sp.payroll_service.domain.auth.service.UserPreferenceService;
import org.sp.payroll_service.domain.common.dto.response.HeaderResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * REST Controller for user preferences endpoints.
 * Handles retrieval and updates of user UI preferences (theme, language, scope, company).
 */
@RestController
@RequestMapping("/api/v1/auth/user/preferences")
@RequiredArgsConstructor
@Slf4j
@Validated
public class UserPreferencesController {
    
    private final UserPreferenceService userPreferenceService;
    
    /**
     * Get current user's preferences.
     * 
     * @param principal the authenticated user context
     * @return user preferences response
     */
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'EMPLOYER', 'EMPLOYEE')")
    public ResponseEntity<UserPreferencesResponse> getPreferences(
            @AuthenticationPrincipal HeaderResponse principal) {
        log.debug("Fetching preferences for user: {}", principal.userId());
        UserPreferencesResponse prefs = userPreferenceService.findById(principal.userId());
        return ResponseEntity.ok(prefs);
    }
    
    /**
     * Update current user's preferences.
     * Allows updating scope, company, theme, and language.
     * 
     * @param request the preferences update request containing new values
     * @param principal the authenticated user context
     * @return updated user preferences response
     */
    @PutMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'EMPLOYER', 'EMPLOYEE')")
    public ResponseEntity<UserPreferencesResponse> updatePreferences(
            @Valid @RequestBody UserPreferencesRequest request,
            @AuthenticationPrincipal HeaderResponse principal) {
        log.info("Updating preferences for user: {}", principal.userId());
        UserPreferencesResponse updated = userPreferenceService.update(
            principal.userId(), 
            request, 
            principal
        );
        return ResponseEntity.ok(updated);
    }
}
