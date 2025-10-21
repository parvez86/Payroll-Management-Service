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
import org.sp.payroll_service.domain.core.service.GradeService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

/**
 * REST Controller for managing the hierarchical Grade entity (e.g., organizational structure).
 * Only ADMINs and EMPLOYERs should manage Grades.
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
    public CompletableFuture<ResponseEntity<GradeResponse>> createGrade(
            @Valid @RequestBody GradeCreateRequest request) {
        log.info("Request to create new grade: {}", request.name());
        return gradeService.create(request)
                .thenApply(response -> ResponseEntity.status(HttpStatus.CREATED).body(response));
    }

    @Operation(summary = "Get a Grade by its unique ID.")
    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public CompletableFuture<ResponseEntity<GradeResponse>> getGradeById(
            @PathVariable UUID id) {
        log.debug("Request to fetch grade with ID: {}", id);
        return gradeService.findById(id)
                .thenApply(ResponseEntity::ok);
    }

    @Operation(summary = "Update an existing Grade (name or parent link).")
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'EMPLOYER')")
    public CompletableFuture<ResponseEntity<GradeResponse>> updateGrade(
            @PathVariable UUID id,
            @Valid @RequestBody GradeUpdateRequest request) {
        log.info("Request to update grade with ID: {}", id);
        return gradeService.update(id, request)
                .thenApply(ResponseEntity::ok);
    }

    @Operation(summary = "Delete a Grade/Rank.")
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasRole('ADMIN')")
    public CompletableFuture<ResponseEntity<Void>> deleteGrade(@PathVariable UUID id) {
        log.warn("Request to delete grade with ID: {}", id);
        return gradeService.delete(id)
                .thenApply(v -> ResponseEntity.noContent().build());
    }

    @Operation(summary = "Search and paginate Grades using various filters (keyword, parentId, minRank).")
    @GetMapping("/search")
    @PreAuthorize("isAuthenticated()")
    public CompletableFuture<ResponseEntity<Page<GradeResponse>>> searchGrades(
            // Uses @ModelAttribute to bind URL query parameters to the GradeFilter DTO
            @ModelAttribute GradeFilter filter, 
            @PageableDefault(sort = {"rank", "name"}) Pageable pageable) {
        log.debug("Request to search grades with filters: {}", filter);
        return gradeService.search(filter, pageable)
                .thenApply(ResponseEntity::ok);
    }
    
    @Operation(summary = "Get all Grades (non-paginated list, typically for dropdowns).")
    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public CompletableFuture<ResponseEntity<List<GradeResponse>>> getAllGrades() {
        log.debug("Request to fetch all grades.");
        // Assuming findAll returns all entities mapped to a List<ResponseDTO>
        return gradeService.findAll() 
                .thenApply(ResponseEntity::ok);
    }
}