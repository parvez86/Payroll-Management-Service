package org.sp.payroll_service.api.core.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * DTO for updating an existing Bank entity.
 */
public record BankUpdateRequest(
    @NotBlank(message = "Bank name is required.")
    @Size(max = 100, message = "Name must be less than 100 characters.")
    String name,
    
    @NotBlank(message = "SWIFT/BIC code is required.")
    @Size(min = 8, max = 11, message = "SWIFT/BIC must be 8 or 11 characters.")
    String swiftCode,
    
    @Size(max = 20, message = "Country code must be less than 20 characters.")
    String countryCode
) {}