package org.sp.payroll_service.api.payroll.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.sp.payroll_service.api.payroll.dto.*;
import org.sp.payroll_service.domain.payroll.service.PayrollService;
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
 * REST Controller for payroll batch operations and salary processing.
 * Handles payroll batch lifecycle, salary calculations, and ACID-compliant payroll execution.
 */
@Tag(name = "Payroll Management", description = "Payroll batch operations, salary calculations, and payment processing")
@RestController
@RequestMapping("/api/v1/payroll")
@RequiredArgsConstructor
@Slf4j
public class PayrollController {

    private final PayrollService payrollService;

    // --- BATCH MANAGEMENT ---

    @Operation(summary = "Create a new payroll batch")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Payroll batch created successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid request data"),
        @ApiResponse(responseCode = "403", description = "Access denied")
    })
    @PostMapping("/batches")
    @PreAuthorize("hasRole('ADMIN')")
    public CompletableFuture<ResponseEntity<PayrollBatchResponse>> createBatch(
            @Valid @RequestBody CreatePayrollBatchRequest request) {
        log.info("Creating new payroll batch: {}", request.name());
        return payrollService.createPayrollBatch(request)
                .thenApply(batch -> ResponseEntity.status(HttpStatus.CREATED).body(batch));
    }

    @Operation(summary = "Get all payroll batches")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Payroll batches retrieved successfully"),
        @ApiResponse(responseCode = "403", description = "Access denied")
    })
    @GetMapping("/batches")
    @PreAuthorize("hasAnyRole('ADMIN', 'EMPLOYER')")
    public CompletableFuture<ResponseEntity<Page<PayrollBatchSummary>>> getAllBatches(
            @Parameter(description = "Filter criteria") @ModelAttribute PayrollBatchFilter filter,
            @PageableDefault(size = 20, sort = "createdAt") Pageable pageable) {
        log.debug("Retrieving payroll batches with filter: {}", filter);
        return payrollService.getAllBatches(filter, pageable)
                .thenApply(ResponseEntity::ok);
    }

    @Operation(summary = "Get payroll batch by ID")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Payroll batch found"),
        @ApiResponse(responseCode = "404", description = "Payroll batch not found"),
        @ApiResponse(responseCode = "403", description = "Access denied")
    })
    @GetMapping("/batches/{batchId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'EMPLOYER')")
    public CompletableFuture<ResponseEntity<PayrollBatchResponse>> getBatchById(
            @Parameter(description = "Payroll batch ID") @PathVariable UUID batchId) {
        log.debug("Retrieving payroll batch: {}", batchId);
        return payrollService.getBatchById(batchId)
                .thenApply(ResponseEntity::ok);
    }

    @Operation(summary = "Get payroll items for a batch")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Payroll items retrieved successfully"),
        @ApiResponse(responseCode = "404", description = "Payroll batch not found"),
        @ApiResponse(responseCode = "403", description = "Access denied")
    })
    @GetMapping("/batches/{batchId}/items")
    @PreAuthorize("hasAnyRole('ADMIN', 'EMPLOYER')")
    public CompletableFuture<ResponseEntity<Page<PayrollItemResponse>>> getBatchItems(
            @Parameter(description = "Payroll batch ID") @PathVariable UUID batchId,
            @PageableDefault(size = 50) Pageable pageable) {
        log.debug("Retrieving payroll items for batch: {}", batchId);
        return payrollService.getBatchItems(batchId, pageable)
                .thenApply(ResponseEntity::ok);
    }

    // --- SALARY CALCULATIONS ---

    @Operation(summary = "Calculate salaries for a batch (preview mode)")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Salary calculations completed"),
        @ApiResponse(responseCode = "404", description = "Payroll batch not found"),
        @ApiResponse(responseCode = "403", description = "Access denied")
    })
    @GetMapping("/batches/{batchId}/calculate")
    @PreAuthorize("hasAnyRole('ADMIN', 'EMPLOYER')")
    public CompletableFuture<ResponseEntity<List<SalaryCalculation>>> calculateSalaries(
            @Parameter(description = "Payroll batch ID") @PathVariable UUID batchId) {
        log.info("Calculating salaries for batch: {}", batchId);
        return payrollService.calculateSalaries(batchId)
                .thenApply(ResponseEntity::ok);
    }

    @Operation(summary = "Calculate salaries for all employees in a company")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Salary calculations completed"),
        @ApiResponse(responseCode = "404", description = "Company not found"),
        @ApiResponse(responseCode = "403", description = "Access denied")
    })
    @GetMapping("/companies/{companyId}/calculate")
    @PreAuthorize("hasAnyRole('ADMIN', 'EMPLOYER')")
    public CompletableFuture<ResponseEntity<List<SalaryCalculation>>> calculateSalariesForCompany(
            @Parameter(description = "Company ID") @PathVariable UUID companyId) {
        log.info("Calculating salaries for company: {}", companyId);
        return payrollService.calculateSalariesForCompany(companyId)
                .thenApply(ResponseEntity::ok);
    }

    // --- PAYROLL PROCESSING ---

    @Operation(summary = "Process payroll batch (execute actual payments)")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Payroll processed successfully"),
        @ApiResponse(responseCode = "400", description = "Insufficient funds or processing error"),
        @ApiResponse(responseCode = "404", description = "Payroll batch not found"),
        @ApiResponse(responseCode = "403", description = "Access denied")
    })
    @PostMapping("/batches/{batchId}/process")
    @PreAuthorize("hasRole('ADMIN')")
    public CompletableFuture<ResponseEntity<PayrollResult>> processPayroll(
            @Parameter(description = "Payroll batch ID") @PathVariable UUID batchId) {
        log.warn("Processing payroll batch with ACID transactions: {}", batchId);
        return payrollService.processPayroll(batchId)
                .thenApply(result -> {
                    if (result.success()) {
                        log.info("Payroll batch {} processed successfully. Processed: {}, Failed: {}", 
                                batchId, result.successfulPayments(), result.failedPayments());
                        return ResponseEntity.ok(result);
                    } else {
                        log.error("Payroll batch {} failed: {}", batchId, result.errorMessages());
                        return ResponseEntity.badRequest().body(result);
                    }
                });
    }

    // --- PAYROLL ITEM MANAGEMENT ---

    @Operation(summary = "Retry a failed payroll item")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Payroll item retried successfully"),
        @ApiResponse(responseCode = "404", description = "Payroll item not found"),
        @ApiResponse(responseCode = "403", description = "Access denied")
    })
    @PostMapping("/items/{payrollItemId}/retry")
    @PreAuthorize("hasRole('ADMIN')")
    public CompletableFuture<ResponseEntity<PayrollItemResponse>> retryPayrollItem(
            @Parameter(description = "Payroll item ID") @PathVariable UUID payrollItemId) {
        log.info("Retrying payroll item: {}", payrollItemId);
        return payrollService.retryPayrollItem(payrollItemId)
                .thenApply(ResponseEntity::ok);
    }

    // --- BATCH LIFECYCLE ---

    @Operation(summary = "Cancel a pending payroll batch")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Payroll batch cancelled successfully"),
        @ApiResponse(responseCode = "400", description = "Batch cannot be cancelled"),
        @ApiResponse(responseCode = "404", description = "Payroll batch not found"),
        @ApiResponse(responseCode = "403", description = "Access denied")
    })
    @PostMapping("/batches/{batchId}/cancel")
    @PreAuthorize("hasRole('ADMIN')")
    public CompletableFuture<ResponseEntity<PayrollBatchResponse>> cancelBatch(
            @Parameter(description = "Payroll batch ID") @PathVariable UUID batchId) {
        log.info("Cancelling payroll batch: {}", batchId);
        return payrollService.cancelBatch(batchId)
                .thenApply(ResponseEntity::ok);
    }
}