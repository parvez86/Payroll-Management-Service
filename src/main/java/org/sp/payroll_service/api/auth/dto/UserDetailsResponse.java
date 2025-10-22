package org.sp.payroll_service.api.auth.dto;

import org.sp.payroll_service.api.wallet.dto.AccountResponse;

import java.util.UUID;

public record UserDetailsResponse(
    UserResponse user,
    AccountResponse account,
    String fullName,
    String description,
    UUID companyId,
    String bizId // code (employee)
) {
}