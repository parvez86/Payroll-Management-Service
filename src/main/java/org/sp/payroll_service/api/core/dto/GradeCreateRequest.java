package org.sp.payroll_service.api.core.dto;

import jakarta.validation.constraints.*;
import java.util.UUID;


/**
 * DTO for creating a new Grade entity.
 */
public record GradeCreateRequest(
    @NotBlank(message = "Grade name is required.")
    @Size(max = 50, message = "Grade name must be less than 50 characters.")
    String name,
    
    // Optional: The ID of the parent Grade (for hierarchical structure)
    UUID parentId, 
    
    @NotNull(message = "Rank is required.")
    @Min(value = 1, message = "Rank must be 1 or greater.")
    Integer rank
) {}