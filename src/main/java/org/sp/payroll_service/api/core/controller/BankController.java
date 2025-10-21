package org.sp.payroll_service.api.core.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.sp.payroll_service.api.core.dto.BankCreateRequest;
import org.sp.payroll_service.api.core.dto.BankFilter;
import org.sp.payroll_service.api.core.dto.BankResponse;
import org.sp.payroll_service.api.core.dto.BankUpdateRequest;
import org.sp.payroll_service.domain.core.service.BankService;
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
 * REST Controller for managing {@code Bank} entities.
 */
@Tag(name = "Bank Management", description = "CRUD and search operations for financial institutions.")
@RestController
@RequestMapping("/api/v1/banks")
@RequiredArgsConstructor
@Slf4j
public class BankController {

    private final BankService bankService;

    // --- CREATE ---
    @Operation(summary = "Create a new bank entry")
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')") // Only Admins should configure new banks
    public CompletableFuture<ResponseEntity<BankResponse>> createBank(
            @Valid @RequestBody BankCreateRequest request) {
        log.info("Request to create new bank: {}", request.name());
        return bankService.create(request)
                .thenApply(response -> ResponseEntity.status(HttpStatus.CREATED).body(response));
    }

    // --- READ BY ID ---
    @Operation(summary = "Get a bank by ID")
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'EMPLOYER')")
    public CompletableFuture<ResponseEntity<BankResponse>> getBankById(
            @PathVariable UUID id) {
        log.debug("Request to fetch bank with ID: {}", id);
        return bankService.findById(id)
                .thenApply(ResponseEntity::ok);
    }

    // --- UPDATE ---
    @Operation(summary = "Update an existing bank")
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')") // Only Admins should modify bank config
    public CompletableFuture<ResponseEntity<BankResponse>> updateBank(
            @PathVariable UUID id,
            @Valid @RequestBody BankUpdateRequest request) {
        log.info("Request to update bank with ID: {}", id);
        return bankService.update(id, request)
                .thenApply(ResponseEntity::ok);
    }

    // --- DELETE ---
    @Operation(summary = "Delete a bank by ID")
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasRole('ADMIN')")
    public CompletableFuture<ResponseEntity<Void>> deleteBank(@PathVariable UUID id) {
        log.warn("Request to delete bank with ID: {}", id);
        return bankService.delete(id)
                .thenApply(v -> ResponseEntity.noContent().build());
    }

    // --- SEARCH ---
    @Operation(summary = "Search banks using dynamic filters and pagination")
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'EMPLOYER')")
    public CompletableFuture<ResponseEntity<Page<BankResponse>>> searchBanks(
            @ModelAttribute BankFilter filter,
            @PageableDefault(size = 20, sort = "name") Pageable pageable) {
        log.debug("Request to search banks with filters: {}", filter);
        return bankService.search(filter, pageable)
                .thenApply(ResponseEntity::ok);
    }
}