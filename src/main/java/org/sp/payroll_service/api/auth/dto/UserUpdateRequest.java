package org.sp.payroll_service.api.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import org.sp.payroll_service.domain.common.enums.Role;

/**
 * Request DTO for updating an existing User.
 */
public record UserUpdateRequest(
    @NotBlank String username,
    @NotBlank @Email String email,
    String currentPassword, // Required only if newPassword is provided
    String newPassword,
    Role role
) {}
