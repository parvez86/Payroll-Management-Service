package org.sp.payroll_service.api.auth.dto;

import org.sp.payroll_service.domain.common.enums.Role;

import java.util.UUID;
import java.time.Instant;

/**
 * Data Transfer Object representing the public profile of a User.
 * Uses a Java Record for immutability and conciseness.
 */
public record UserResponse(
    UUID id,
    String username,
    String email,
    Role role,
    Instant createdAt
) {}