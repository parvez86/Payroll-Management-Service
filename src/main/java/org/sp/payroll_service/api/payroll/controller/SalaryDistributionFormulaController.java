package org.sp.payroll_service.api.payroll.controller;

import io.swagger.v3.oas.annotations.Operation;
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
import java.util.concurrent.CompletableFuture;

/**
 * REST Controller for managing {@code SalaryDistributionFormula} entities.
 * <p>
 * All methods are asynchronous, leveraging {@code CompletableFuture} to ensure
 * non-blocking execution and efficient resource utilization.
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
     * @return A {@code CompletableFuture} containing the newly created formula response with HTTP 201 Created.
     */
    @Operation(summary = "Create a new salary formula")
    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'EMPLOYER')")
    public CompletableFuture<ResponseEntity<SalaryDistributionFormulaResponse>> createFormula(
            @Valid @RequestBody SalaryDistributionFormulaCreateRequest request) {
        log.info("Request to create new salary formula: {}", request.name());
        return formulaService.create(request)
                .thenApply(response -> ResponseEntity.status(HttpStatus.CREATED).body(response));
    }

    /**
     * Retrieves a salary distribution formula by its ID.
     *
     * @param id The ID of the formula to retrieve.
     * @return A {@code CompletableFuture} containing the formula response with HTTP 200 OK.
     */
    @Operation(summary = "Get a salary formula by ID")
    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public CompletableFuture<ResponseEntity<SalaryDistributionFormulaResponse>> getFormulaById(
            @PathVariable UUID id) {
        log.debug("Request to fetch formula with ID: {}", id);
        return formulaService.findById(id)
                .thenApply(ResponseEntity::ok);
    }

    /**
     * Updates an existing salary distribution formula.
     *
     * @param id The ID of the formula to update.
     * @param request The DTO containing the updated formula data.
     * @return A {@code CompletableFuture} containing the updated formula response with HTTP 200 OK.
     */
    @Operation(summary = "Update an existing salary formula")
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'EMPLOYER')")
    public CompletableFuture<ResponseEntity<SalaryDistributionFormulaResponse>> updateFormula(
            @PathVariable UUID id,
            @Valid @RequestBody SalaryDistributionFormulaUpdateRequest request) {
        log.info("Request to update formula with ID: {}", id);
        return formulaService.update(id, request)
                .thenApply(ResponseEntity::ok);
    }

    /**
     * Deletes a salary distribution formula by its ID.
     *
     * @param id The ID of the formula to delete.
     * @return A {@code CompletableFuture} with HTTP 204 No Content.
     */
    @Operation(summary = "Delete a salary formula by ID")
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasAnyRole('ADMIN')")
    public CompletableFuture<ResponseEntity<Void>> deleteFormula(@PathVariable UUID id) {
        log.warn("Request to delete formula with ID: {}", id);
        return formulaService.delete(id)
                .thenApply(v -> ResponseEntity.noContent().build());
    }

    /**
     * Searches for salary formulas based on filter criteria and pagination settings.
     *
     * @param filter The DTO containing the search filters.
     * @param pageable The pagination and sorting information (default size 20).
     * @return A {@code CompletableFuture} containing a paginated result of formula responses with HTTP 200 OK.
     */
    @Operation(summary = "Search salary formulas using dynamic filters and pagination")
    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public CompletableFuture<ResponseEntity<Page<SalaryDistributionFormulaResponse>>> searchFormulas(
            @ModelAttribute SalaryDistributionFormulaFilter filter,
            @PageableDefault(size = 20) Pageable pageable) {
        log.debug("Request to search formulas with filters: {} and pageable: {}", filter, pageable);
        return formulaService.search(filter, pageable)
                .thenApply(ResponseEntity::ok);
    }
}
