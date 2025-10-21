package org.sp.payroll_service.api.payroll.dto;

import lombok.Builder;
import org.sp.payroll_service.domain.core.entity.Grade;
import org.sp.payroll_service.domain.common.enums.EmploymentStatus;

import java.time.Instant;
import java.util.UUID;

/**
 * Data Transfer Object (DTO) used to define criteria for filtering and searching
 * a collection of Employee entities.
 * <p>
 * All fields are optional. A {@code null} value for any field means the filter is
 * **not applied** for that criterion. Only employees matching all provided (non-null)
 * criteria will be returned in the search results.
 * </p>
 *
 * @param searchKeyword A general keyword to search across common fields like
 * {@code firstName}, {@code lastName}, or {@code jobTitle}.
 * Results should include any employee where this keyword is a substring.
 * If {@code null}, employees from all departments are included.
 * @param companyId Filters employees who directly report to the Manager with this ID.
 * If {@code null}, the manager relationship is ignored.
 * @param gradeId Filters employees matching this specific payroll {@link Grade} level.
 * @param status Filters employees matching this specific {@link EmploymentStatus}.
 * @param hiredAfter Filters employees who were hired on or after this date.
 * Must be a valid {@code LocalDate}.
 * @param page The requested page number for pagination (1-indexed). Defaults to 1.
 * @param size The number of results to include per page. Defaults to a system maximum (e.g., 20).
 * @param sortBy The field name used to sort the results (e.g., "lastName", "hiredDate").
 * @param sortDirection The direction of the sort: "asc" for ascending, "desc" for descending.
 */
@Builder
public record EmployeeFilterRequest(
    String searchKeyword,

    UUID ownerId,

    UUID companyId,

    UUID gradeId,

    EmploymentStatus status,

    Instant hiredAfter,

    Integer page,

    Integer size,

    String sortBy,

    String sortDirection
) {}
