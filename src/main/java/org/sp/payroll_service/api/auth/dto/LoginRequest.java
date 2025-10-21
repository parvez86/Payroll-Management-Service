package org.sp.payroll_service.api.auth.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * Authentication request data transfer record.
 * @param username user identifier
 * @param password raw password for authentication
 */
public record LoginRequest(
    @NotBlank String username,
    @NotBlank String password
) {}