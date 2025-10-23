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
import org.sp.payroll_service.api.payroll.dto.PageResponse;
import org.sp.payroll_service.domain.core.service.BankService;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

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
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<BankResponse> createBank(
            @Valid @RequestBody BankCreateRequest request) {
        log.info("Request to create new bank: {}", request.name());
        BankResponse response = bankService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    // --- READ BY ID ---
    @Operation(summary = "Get a bank by ID")
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'EMPLOYER')")
    public ResponseEntity<BankResponse> getBankById(
            @PathVariable UUID id) {
        log.debug("Request to fetch bank with ID: {}", id);
        return ResponseEntity.ok(bankService.findById(id));
    }

    // --- UPDATE ---
    @Operation(summary = "Update an existing bank")
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<BankResponse> updateBank(
            @PathVariable UUID id,
            @Valid @RequestBody BankUpdateRequest request) {
        log.info("Request to update bank with ID: {}", id);
        return ResponseEntity.ok(bankService.update(id, request));
    }

    // --- DELETE ---
    @Operation(summary = "Delete a bank by ID")
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteBank(@PathVariable UUID id) {
        log.warn("Request to delete bank with ID: {}", id);
        bankService.delete(id);
        return ResponseEntity.noContent().build();
    }

    // --- SEARCH ---
    @Operation(summary = "Search banks using dynamic filters and pagination")
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'EMPLOYER')")
    public ResponseEntity<PageResponse<BankResponse>> searchBanks(
            @ModelAttribute BankFilter filter,
            @PageableDefault(size = 20, sort = "name") Pageable pageable) {
        log.debug("Request to search banks with filters: {}", filter);
        return ResponseEntity.ok(bankService.search(filter, pageable));
    }
}