package org.sp.payroll_service.api.auth.dto;

import org.sp.payroll_service.api.wallet.dto.AccountResponse;

import java.util.List;
import java.util.Map;
import java.util.UUID;

public record UserDetailsResponse(
        UserResponse user,
        AccountResponse account,
        String fullName,
        String description,
        UUID companyId,        // Primary/default company (for backward compatibility)
        Map<UUID, String> companyIds, // All companies user has access to (for employers/admins)
        String bizId           // Employee code
) {
}