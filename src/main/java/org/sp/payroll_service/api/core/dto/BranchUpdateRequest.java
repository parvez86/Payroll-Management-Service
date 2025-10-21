package org.sp.payroll_service.api.core.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.UUID;

/**
 * DTO for updating an existing Bank Branch entity.
 */
public record BranchUpdateRequest(
        @NotNull(message = "Bank ID is required.")
        UUID bankId, // Link to the parent Bank

        @NotBlank(message = "Branch name is required.")
        @Size(max = 100, message = "Branch name must be less than 100 characters.")
        String branchName,

        @Size(max = 255, message = "Address must be less than 255 characters.")
        String address
) {
}