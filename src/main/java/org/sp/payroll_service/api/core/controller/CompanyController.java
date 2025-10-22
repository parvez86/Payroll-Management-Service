package org.sp.payroll_service.api.core.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.sp.payroll_service.api.core.dto.CompanyCreateRequest;
import org.sp.payroll_service.api.core.dto.CompanyFilter;
import org.sp.payroll_service.api.core.dto.CompanyResponse;
import org.sp.payroll_service.api.core.dto.CompanyTopUpRequest;
import org.sp.payroll_service.api.core.dto.CompanyUpdateRequest;
import org.sp.payroll_service.api.payroll.dto.TransactionResponse;
import org.sp.payroll_service.api.wallet.dto.AccountResponse;
import org.sp.payroll_service.domain.core.service.CompanyService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

/**
 * REST Controller for managing the {@code Company} entity (The Organization).
 * All operations are secured as only high-level roles should configure the company's financial structure.
 * This is a **synchronous** controller implementation.
 */
@Tag(name = "Company Management", description = "CRUD and Search operations for the core organizational entity and its main payroll account.")
@RestController
@RequestMapping("/api/v1/companies")
@RequiredArgsConstructor
@Slf4j
public class CompanyController {

    private final CompanyService companyService;

    /**
     * Creates a new Company entity and sets up its mandatory main payroll account.
     */
    @Operation(summary = "Create a new Company and its main payroll account.")
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<CompanyResponse> createCompany(
            @Valid @RequestBody CompanyCreateRequest request) {
        log.info("Request to create new company: {}", request.name());
        CompanyResponse response = companyService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Retrieves a specific Company by ID.
     */
    @Operation(summary = "Get a Company by ID.")
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'EMPLOYER')")
    public ResponseEntity<CompanyResponse> getCompanyById(
            @PathVariable UUID id) {
        log.debug("Request to fetch company with ID: {}", id);
        return ResponseEntity.ok(companyService.findById(id));
    }

    /**
     * Updates an existing Company's metadata (e.g., name).
     */
    @Operation(summary = "Update Company metadata (e.g., name).")
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'EMPLOYER')")
    public ResponseEntity<CompanyResponse> updateCompany(
            @PathVariable UUID id,
            @Valid @RequestBody CompanyUpdateRequest request) {
        log.info("Request to update company with ID: {}", id);
        return ResponseEntity.ok(companyService.update(id, request));
    }

    /**
     * Deletes a Company entity. This is a high-privilege operation.
     */
    @Operation(summary = "Delete a Company by ID.")
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteCompany(@PathVariable UUID id) {
        log.warn("Request to delete company with ID: {}", id);
        companyService.delete(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * Searches and paginates Companies based on filter criteria.
     */
    @Operation(summary = "Search and paginate Company records.")
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'EMPLOYER')")
    public ResponseEntity<Page<CompanyResponse>> searchCompanies(
            @ModelAttribute CompanyFilter filter,
            @PageableDefault(size = 20, sort = "name") Pageable pageable) {
        log.debug("Request to search companies with filters: {}", filter);
        return ResponseEntity.ok(companyService.search(filter, pageable));
    }

    /**
     * Top-up company account with funds.
     */
    @Operation(summary = "Top-up company account balance")
    @PostMapping("/{companyId}/topup")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<CompanyResponse> topUpCompanyAccount(
            @PathVariable UUID companyId,
            @RequestBody CompanyTopUpRequest request) {
        log.info("Request to top-up company {} with amount: {}", companyId, request.amount());
        return ResponseEntity.ok(companyService.topUpAccount(companyId, request));
    }

    @Operation(summary = "Get company account balance")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Company account balance retrieved successfully"),
            @ApiResponse(responseCode = "404", description = "Company not found"),
            @ApiResponse(responseCode = "403", description = "Access denied")
    })
    @GetMapping("/{companyId}/account")
    @PreAuthorize("hasAnyRole('ADMIN', 'EMPLOYER')")
    public ResponseEntity<AccountResponse> getCompanyAccount(
            @Parameter(description = "Company ID") @PathVariable UUID companyId) {
        log.debug("Request to get company account for company: {}", companyId);
        return ResponseEntity.ok(companyService.getCompanyAccount(companyId));
    }

    @Operation(summary = "Get company transaction history")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Company transactions retrieved successfully"),
            @ApiResponse(responseCode = "404", description = "Company not found"),
            @ApiResponse(responseCode = "403", description = "Access denied")
    })
    @GetMapping("/{companyId}/transactions")
    @PreAuthorize("hasAnyRole('ADMIN', 'EMPLOYER')")
    public ResponseEntity<Page<TransactionResponse>> getCompanyTransactions(
            @Parameter(description = "Company ID") @PathVariable UUID companyId,
            @PageableDefault(size = 20, sort = "createdAt") Pageable pageable) {
        log.debug("Request to get company transactions for company: {}", companyId);
        return ResponseEntity.ok(companyService.getCompanyTransactions(companyId, pageable));
    }
}
