package org.sp.payroll_service.api.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.sp.payroll_service.domain.common.enums.Role;

/**
 * User creation request record.
 * @param username unique identifier
 * @param email user email
 * @param password raw password
 * @param role user role enum
 */
public record UserCreateRequest(
    @NotBlank @Size(min = 3, max = 50) String username,
    @Email String email,
    @NotBlank @Size(min = 6) String password,
    @NotNull Role role
) {}