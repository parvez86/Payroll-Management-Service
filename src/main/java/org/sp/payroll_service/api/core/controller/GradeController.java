package org.sp.payroll_service.api.core.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.sp.payroll_service.api.core.dto.GradeCreateRequest;
import org.sp.payroll_service.api.core.dto.GradeFilter;
import org.sp.payroll_service.api.core.dto.GradeResponse;
import org.sp.payroll_service.api.core.dto.GradeUpdateRequest;
import org.sp.payroll_service.api.payroll.dto.PageResponse;
import org.sp.payroll_service.domain.core.service.GradeService;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

/**
 * REST Controller for managing the hierarchical Grade entity (e.g., organizational structure).
 * Only ADMINs and EMPLOYERs should manage Grades.
 * This is a **synchronous** controller implementation.
 */
@Tag(name = "Grade Management", description = "CRUD and Search operations for the hierarchical Grade/Rank structure.")
@RestController
@RequestMapping("/api/v1/grades")
@RequiredArgsConstructor
@Slf4j
public class GradeController {

    private final GradeService gradeService;

    @Operation(summary = "Create a new Grade, optionally linking it to a parent Grade (for hierarchy).")
    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'EMPLOYER')")
    public ResponseEntity<GradeResponse> createGrade(
            @Valid @RequestBody GradeCreateRequest request) {
        log.info("Request to create new grade: {}", request.name());
        GradeResponse response = gradeService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @Operation(summary = "Get a Grade by its unique ID.")
    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<GradeResponse> getGradeById(
            @PathVariable UUID id) {
        log.debug("Request to fetch grade with ID: {}", id);
        return ResponseEntity.ok(gradeService.findById(id));
    }

    @Operation(summary = "Update an existing Grade (name or parent link).")
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'EMPLOYER')")
    public ResponseEntity<GradeResponse> updateGrade(
            @PathVariable UUID id,
            @Valid @RequestBody GradeUpdateRequest request) {
        log.info("Request to update grade with ID: {}", id);
        return ResponseEntity.ok(gradeService.update(id, request));
    }

    @Operation(summary = "Delete a Grade/Rank.")
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteGrade(@PathVariable UUID id) {
        log.warn("Request to delete grade with ID: {}", id);
        gradeService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Search and paginate Grades using various filters (keyword, parentId, minRank).")
    @GetMapping("/search")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<PageResponse<GradeResponse>> searchGrades(
            @ModelAttribute GradeFilter filter,
            @PageableDefault(sort = {"rank", "name"}) Pageable pageable) {
        log.debug("Request to search grades with filters: {}", filter);
        return ResponseEntity.ok(gradeService.search(filter, pageable));
    }

    @Operation(summary = "Get all Grades (non-paginated list, typically for dropdowns).")
    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<GradeResponse>> getAllGrades() {
        log.debug("Request to fetch all grades.");
        return ResponseEntity.ok(gradeService.findAll());
    }
}