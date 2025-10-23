package org.sp.payroll_service.api.core.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.sp.payroll_service.api.core.dto.BranchCreateRequest;
import org.sp.payroll_service.api.core.dto.BranchFilter;
import org.sp.payroll_service.api.core.dto.BranchResponse;
import org.sp.payroll_service.api.core.dto.BranchUpdateRequest;
import org.sp.payroll_service.api.payroll.dto.PageResponse;
import org.sp.payroll_service.domain.core.service.BranchService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

/**
 * REST Controller for managing {@code Branch} entities (Bank Branches).
 * <p>
 * This controller provides standard CRUD operations and a search endpoint for bank branch data.
 * All operations are now **blocking and synchronous**.
 * </p>
 *
 * @author Gemini Assistant
 * @version 1.0
 * @see BranchService
 */
@Tag(name = "Bank Branch Management", description = "CRUD and search operations for specific bank branches.")
@RestController
@RequestMapping("/api/v1/branches")
@RequiredArgsConstructor
@Slf4j
public class BranchController {

    private final BranchService branchService;

    /**
     * Creates a new bank branch entry based on the provided request body.
     *
     * <p>Security: Requires role 'ADMIN'.</p>
     *
     * @param request The DTO containing the details for the new branch.
     * @return A {@link ResponseEntity} with status 201 (Created) and the created {@link BranchResponse} body.
     */
    @Operation(summary = "Create a new bank branch entry")
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<BranchResponse> createBranch(
            @Valid @RequestBody BranchCreateRequest request) {
        log.info("Request to create new branch: {} for bank {}", request.branchName(), request.bankId());
        // Synchronous call
        BranchResponse response = branchService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Retrieves a specific bank branch by its unique identifier.
     *
     * <p>Security: Requires role 'ADMIN', 'EMPLOYER', or 'ACCOUNTANT'.</p>
     *
     * @param id The UUID of the branch to retrieve.
     * @return A {@link ResponseEntity} with status 200 (OK) and the {@link BranchResponse} body.
     */
    @Operation(summary = "Get a branch by ID")
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'EMPLOYER', 'ACCOUNTANT')")
    public ResponseEntity<BranchResponse> getBranchById(
            @PathVariable UUID id) {
        log.debug("Request to fetch branch with ID: {}", id);
        return ResponseEntity.ok(branchService.findById(id));
    }

    /**
     * Updates an existing bank branch identified by the given ID.
     *
     * <p>Security: Requires role 'ADMIN'.</p>
     *
     * @param id The UUID of the branch to update.
     * @param request The DTO containing the updated branch details.
     * @return A {@link ResponseEntity} with status 200 (OK) and the updated {@link BranchResponse} body.
     */
    @Operation(summary = "Update an existing bank branch")
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<BranchResponse> updateBranch(
            @PathVariable UUID id,
            @Valid @RequestBody BranchUpdateRequest request) {
        log.info("Request to update branch with ID: {}", id);
        return ResponseEntity.ok(branchService.update(id, request));
    }

    /**
     * Deletes a bank branch identified by the given ID.
     *
     * <p>Security: Requires role 'ADMIN'.</p>
     *
     * @param id The UUID of the branch to delete.
     * @return A {@link ResponseEntity} with status 204 (No Content) upon successful deletion.
     */
    @Operation(summary = "Delete a branch by ID")
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteBranch(@PathVariable UUID id) {
        log.warn("Request to delete branch with ID: {}", id);
        branchService.delete(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * Searches and paginates bank branches based on the provided filter criteria.
     *
     * <p>The search supports filtering by keywords and bank ID, along with standard pagination and sorting.</p>
     * <p>Security: Requires role 'ADMIN', 'EMPLOYER', or 'ACCOUNTANT'.</p>
     *
     * @param filter The {@link BranchFilter} DTO containing criteria for the search (e.g., keyword, bankId).
     * @param pageable The pagination and sorting information (default size is 20, sorted by branchName).
     * @return A {@link ResponseEntity} with status 200 (OK) and a {@link Page} of {@link BranchResponse} objects.
     */
    @Operation(summary = "Search branches using dynamic filters and pagination")
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'EMPLOYER', 'ACCOUNTANT')")
    public ResponseEntity<PageResponse<BranchResponse>> searchBranches(
            @ModelAttribute BranchFilter filter,
            @PageableDefault(size = 20, sort = "branchName") Pageable pageable) {
        log.debug("Request to search branches with filters: {}", filter);
        return ResponseEntity.ok(branchService.search(filter, pageable));
    }
}
