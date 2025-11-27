package org.sp.payroll_service.api.payroll.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.sp.payroll_service.api.payroll.dto.CreatePayrollBatchRequest;
import org.sp.payroll_service.api.payroll.dto.PayrollBatchFilter;
import org.sp.payroll_service.api.payroll.dto.PayrollBatchResponse;
import org.sp.payroll_service.api.payroll.dto.PayrollBatchSummary;
import org.sp.payroll_service.api.payroll.dto.PayrollItemResponse;
import org.sp.payroll_service.api.payroll.dto.PayrollResult;
import org.sp.payroll_service.api.payroll.dto.SalaryCalculation;
import org.sp.payroll_service.domain.common.dto.response.HeaderResponse;
import org.sp.payroll_service.domain.payroll.service.PayrollService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

/**
 * REST Controller for payroll batch operations and salary processing.
 * Handles payroll batch lifecycle, salary calculations, and ACID-compliant payroll execution.
 * <p>
 * This is a **synchronous** controller implementation, calling the service directly.
 */
@Tag(name = "Payroll Management", description = "Payroll batch operations, salary calculations, and payment processing")
@RestController
@RequestMapping("/api/v1/payroll")
@RequiredArgsConstructor
@Slf4j
public class PayrollController {

    private final PayrollService payrollService;

    @Operation(summary = "Get first pending or partial pending payroll batch for a company")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Payroll batch found"),
            @ApiResponse(responseCode = "404", description = "No pending batch found"),
            @ApiResponse(responseCode = "403", description = "Access denied")
    })
    @Deprecated
    @GetMapping("/companies/{companyId}/pending-batch")
    @PreAuthorize("hasAnyRole('ADMIN', 'EMPLOYER')")
    public ResponseEntity<PayrollBatchResponse> getFirstPendingOrPartialPendingBatch(
            @Parameter(description = "Company ID") @PathVariable("companyId") UUID companyId) {
        PayrollBatchResponse batch = payrollService.findFirstPendingOrPartialPendingBatch(companyId);
        if (batch != null) {
            return ResponseEntity.ok(batch);
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }

    @Operation(summary = "Get Last payroll batch for a company")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Payroll batch found"),
            @ApiResponse(responseCode = "404", description = "No pending batch found"),
            @ApiResponse(responseCode = "403", description = "Access denied")
    })
    @GetMapping("/companies/{companyId}/last-batch")
    @PreAuthorize("hasAnyRole('ADMIN', 'EMPLOYER')")
    public ResponseEntity<PayrollBatchResponse> getFirstPendingOrPartialPendingOrLastBatch(
            @Parameter(description = "Company ID") @PathVariable("companyId") UUID companyId) {
        PayrollBatchResponse batch = payrollService.getLastPayrollBatchByIdAndEmployeeId(companyId);
        if (batch != null) {
            return ResponseEntity.ok(batch);
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }

    @Operation(summary = "Create a new payroll batch")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Payroll batch created successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request data"),
            @ApiResponse(responseCode = "403", description = "Access denied")
    })
    @PostMapping("/batches")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<PayrollBatchResponse> createPayrollBatch(
            @Valid @RequestBody CreatePayrollBatchRequest request,
            @AuthenticationPrincipal HeaderResponse principal) {
        log.info("Creating payroll batch for company: {} by {} ({})", request.companyId(), principal.username(), principal.userId());
        PayrollBatchResponse response = payrollService.createPayrollBatch(request, principal);
        log.info("Payroll batch created for company {} by {}", request.companyId(), principal.userId());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @Operation(summary = "Get all payroll batches")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Payroll batches retrieved successfully"),
            @ApiResponse(responseCode = "403", description = "Access denied")
    })
    @GetMapping("/batches")
    @PreAuthorize("hasAnyRole('ADMIN', 'EMPLOYER')")
    public ResponseEntity<Page<PayrollBatchSummary>> getAllBatches(
            @Parameter(description = "Filter criteria") @ModelAttribute PayrollBatchFilter filter,
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        log.debug("Retrieving payroll batches with filter: {}", filter);
        return ResponseEntity.ok(payrollService.getAllBatches(filter, pageable));
    }

    @Operation(summary = "Get payroll batch by ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Payroll batch found"),
            @ApiResponse(responseCode = "404", description = "Payroll batch not found"),
            @ApiResponse(responseCode = "403", description = "Access denied")
    })
    @GetMapping("/batches/{batchId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'EMPLOYER')")
    public ResponseEntity<PayrollBatchResponse> getBatchById(
            @Parameter(description = "Payroll batch ID") @PathVariable UUID batchId) {
        log.debug("Retrieving payroll batch: {}", batchId);
        return ResponseEntity.ok(payrollService.getBatchById(batchId));
    }

    @Operation(summary = "Get payroll items for a batch")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Payroll items retrieved successfully"),
            @ApiResponse(responseCode = "404", description = "Payroll batch not found"),
            @ApiResponse(responseCode = "403", description = "Access denied")
    })
    @GetMapping("/batches/{batchId}/items")
    @PreAuthorize("hasAnyRole('ADMIN', 'EMPLOYER')")
    public ResponseEntity<Page<PayrollItemResponse>> getBatchItems(
            @Parameter(description = "Payroll batch ID") @PathVariable UUID batchId,
            @Parameter(description = "EmployeeId ID") @RequestParam(value = "employeeId", required = false) UUID employeeId,
            @PageableDefault(size = 20, sort = "basics", direction = Sort.Direction.DESC) Pageable pageable) {
        log.debug("Retrieving payroll items for batch: {}", batchId);
        return ResponseEntity.ok(payrollService.getBatchItems(batchId, employeeId, pageable));
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
    public ResponseEntity<List<SalaryCalculation>> calculateSalaries(
            @Parameter(description = "Payroll batch ID") @PathVariable UUID batchId) {
        log.info("Calculating salaries for batch: {}", batchId);
        return ResponseEntity.ok(payrollService.calculateSalaries(batchId));
    }

    @Operation(summary = "Calculate salaries for all employees in a company")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Salary calculations completed"),
            @ApiResponse(responseCode = "404", description = "Company not found"),
            @ApiResponse(responseCode = "403", description = "Access denied")
    })
    @GetMapping("/companies/{companyId}/calculate")
    @PreAuthorize("hasAnyRole('ADMIN', 'EMPLOYER')")
    @Deprecated
    public ResponseEntity<List<SalaryCalculation>> calculateSalariesForCompany(
            @Parameter(description = "Company ID") @PathVariable UUID companyId) {
        log.info("Calculating salaries for company: {}", companyId);
        return ResponseEntity.ok(payrollService.calculateSalariesForCompany(companyId));
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
    public ResponseEntity<PayrollResult> processPayroll(
            @Parameter(description = "Payroll batch ID") @PathVariable UUID batchId,
            @AuthenticationPrincipal HeaderResponse principal) {
        log.warn("Processing payroll batch with ACID transactions: {}, principal: {}", batchId, principal);
        PayrollResult result = payrollService.processPayroll(batchId, principal);

        if (result.success()) {
            log.info("Payroll batch {} processed successfully. Processed: {}, Failed: {}",
                    batchId, result.successfulPayments(), result.failedPayments());
            return ResponseEntity.ok(result);
        } else {
            log.error("Payroll batch {} failed: {}", batchId, result.errorMessages());
            return ResponseEntity.badRequest().body(result);
        }
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
    public ResponseEntity<PayrollItemResponse> retryPayrollItem(
            @Parameter(description = "Payroll item ID") @PathVariable UUID payrollItemId) {
        log.info("Retrying payroll item: {}", payrollItemId);
        return ResponseEntity.ok(payrollService.retryPayrollItem(payrollItemId));
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
    public ResponseEntity<PayrollBatchResponse> cancelBatch(
            @Parameter(description = "Payroll batch ID") @PathVariable UUID batchId) {
        log.info("Cancelling payroll batch: {}", batchId);
        return ResponseEntity.ok(payrollService.cancelBatch(batchId));
    }
}