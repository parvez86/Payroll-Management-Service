package org.sp.payroll_service.api.auth.dto;

import org.sp.payroll_service.domain.common.enums.Role;

/**
 * Filter object for searching User entities (used in the search method).
 */
public record UserFilter(
    String keyword, // Search across username and email
    Role role,
    Boolean enabled
) {}