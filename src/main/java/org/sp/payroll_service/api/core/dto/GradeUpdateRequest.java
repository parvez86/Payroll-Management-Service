package org.sp.payroll_service.api.core.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.util.UUID;

/**
 * DTO for updating an existing Grade entity.
 */
public record GradeUpdateRequest(
    @NotBlank(message = "Grade name is required.")
    @Size(max = 50, message = "Grade name must be less than 50 characters.")
    String name,
    
    // Optional: The ID of the new parent Grade
    UUID parentId
) {}
