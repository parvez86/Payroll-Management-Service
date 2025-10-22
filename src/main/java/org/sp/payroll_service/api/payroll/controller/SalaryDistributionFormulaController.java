package org.sp.payroll_service.api.payroll.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.sp.payroll_service.api.payroll.dto.SalaryDistributionFormulaCreateRequest;
import org.sp.payroll_service.api.payroll.dto.SalaryDistributionFormulaFilter;
import org.sp.payroll_service.api.payroll.dto.SalaryDistributionFormulaResponse;
import org.sp.payroll_service.api.payroll.dto.SalaryDistributionFormulaUpdateRequest;
import org.sp.payroll_service.domain.payroll.service.SalaryDistributionFormulaService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

/**
 * REST Controller for managing {@code SalaryDistributionFormula} entities.
 * <p>
 * This is a **synchronous** controller implementation, calling the service directly.
 */
@Tag(name = "Salary Distribution Formula Management", description = "CRUD and search operations for salary distribution formulas.")
@RestController
@RequestMapping("/api/v1/salary-distribution-formulas")
@RequiredArgsConstructor
@Slf4j
public class SalaryDistributionFormulaController {

    private final SalaryDistributionFormulaService formulaService;

    /**
     * Creates a new salary distribution formula.
     *
     * @param request The DTO containing the formula data.
     * @return The newly created formula response with HTTP 201 Created.
     */
    @Operation(summary = "Create a new salary formula")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Formula created successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request data"),
            @ApiResponse(responseCode = "403", description = "Access denied")
    })
    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'EMPLOYER')")
    public ResponseEntity<SalaryDistributionFormulaResponse> createFormula(
            @Valid @RequestBody SalaryDistributionFormulaCreateRequest request) {
        log.info("Request to create new salary formula: {}", request.name());
        SalaryDistributionFormulaResponse response = formulaService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Retrieves a salary distribution formula by its ID.
     *
     * @param id The ID of the formula to retrieve.
     * @return The formula response with HTTP 200 OK.
     */
    @Operation(summary = "Get a salary formula by ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Formula found"),
            @ApiResponse(responseCode = "404", description = "Formula not found"),
            @ApiResponse(responseCode = "403", description = "Access denied")
    })
    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<SalaryDistributionFormulaResponse> getFormulaById(
            @Parameter(description = "Formula ID") @PathVariable UUID id) {
        log.debug("Request to fetch formula with ID: {}", id);
        return ResponseEntity.ok(formulaService.findById(id));
    }

    /**
     * Updates an existing salary distribution formula.
     *
     * @param id The ID of the formula to update.
     * @param request The DTO containing the updated formula data.
     * @return The updated formula response with HTTP 200 OK.
     */
    @Operation(summary = "Update an existing salary formula")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Formula updated successfully"),
            @ApiResponse(responseCode = "404", description = "Formula not found"),
            @ApiResponse(responseCode = "400", description = "Invalid request data"),
            @ApiResponse(responseCode = "403", description = "Access denied")
    })
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'EMPLOYER')")
    public ResponseEntity<SalaryDistributionFormulaResponse> updateFormula(
            @Parameter(description = "Formula ID") @PathVariable UUID id,
            @Valid @RequestBody SalaryDistributionFormulaUpdateRequest request) {
        log.info("Request to update formula with ID: {}", id);
        return ResponseEntity.ok(formulaService.update(id, request));
    }

    /**
     * Deletes a salary distribution formula by its ID.
     *
     * @param id The ID of the formula to delete.
     * @return {@code ResponseEntity<Void>} with HTTP 204 No Content.
     */
    @Operation(summary = "Delete a salary formula by ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Formula deleted successfully"),
            @ApiResponse(responseCode = "404", description = "Formula not found"),
            @ApiResponse(responseCode = "403", description = "Access denied")
    })
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasAnyRole('ADMIN')")
    public ResponseEntity<Void> deleteFormula(@Parameter(description = "Formula ID") @PathVariable UUID id) {
        log.warn("Request to delete formula with ID: {}", id);
        formulaService.delete(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * Searches for salary formulas based on filter criteria and pagination settings.
     *
     * @param filter The DTO containing the search filters.
     * @param pageable The pagination and sorting information (default size 20).
     * @return A paginated result of formula responses with HTTP 200 OK.
     */
    @Operation(summary = "Search salary formulas using dynamic filters and pagination")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Formulas retrieved successfully"),
            @ApiResponse(responseCode = "403", description = "Access denied")
    })
    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Page<SalaryDistributionFormulaResponse>> searchFormulas(
            @Parameter(description = "Filter criteria") @ModelAttribute SalaryDistributionFormulaFilter filter,
            @PageableDefault(size = 20) Pageable pageable) {
        log.debug("Request to search formulas with filters: {} and pageable: {}", filter, pageable);
        return ResponseEntity.ok(formulaService.search(filter, pageable));
    }
}
