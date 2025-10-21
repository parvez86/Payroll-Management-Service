package org.sp.payroll_service.api.core.dto;

import java.time.Instant;
import java.util.UUID;

/**
 * Data Transfer Object (DTO) representing the read-only, public details of a
 * Grade entity.
 * <p>
 * This DTO defines a specific pay level or band within the organization, including
 * its associated salary limits.
 * </p>
 *
 * @param id The unique identifier (UUID) for this grade record.
 * @param name The unique, short code or name of the grade (e.g., "G1", "Senior", "Executive").
 * @param rank A detailed explanation of the grade and the roles it applies to.
 * @param parentId The minimum allowable annual salary for employees classified under this grade.
 * @param parentName The maximum allowable annual salary for employees classified under this grade.
 * @param createdAt The timestamp indicating when this grade record was first created.
 */
public record GradeResponse(
    UUID id,
    String name,
    Integer rank,
    UUID parentId,
    String parentName,
    Instant createdAt,
    UUID createdBy
) {}