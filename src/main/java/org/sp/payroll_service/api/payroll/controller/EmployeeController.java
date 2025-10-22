package org.sp.payroll_service.api.payroll.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.sp.payroll_service.api.payroll.dto.CreateEmployeeRequest;
import org.sp.payroll_service.api.payroll.dto.EmployeeFilterRequest;
import org.sp.payroll_service.api.payroll.dto.EmployeeResponse;
import org.sp.payroll_service.api.payroll.dto.EmployeeUpdateRequest;
import org.sp.payroll_service.domain.payroll.service.EmployeeService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

/**
 * REST Controller for employee management operations.
 * Handles employee CRUD operations with proper security and validation.
 *
 * This is a **synchronous** controller implementation, calling the service directly.
 */
@Tag(name = "Employee Management", description = "Employee CRUD operations, grade management, and account association")
@RestController
@RequestMapping("/api/v1/employees")
@RequiredArgsConstructor
@Slf4j
public class EmployeeController {

    private final EmployeeService employeeService;

    // --- CREATE ---

    @Operation(summary = "Create a new employee")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Employee created successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request data or business rule violation"),
            @ApiResponse(responseCode = "403", description = "Access denied")
    })
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<EmployeeResponse> createEmployee(
            @Valid @RequestBody CreateEmployeeRequest request) {
        log.info("Creating new employee with bizId: {}", request.bizId());
        EmployeeResponse employee = employeeService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(employee);
    }

    // --- READ ---

    @Operation(summary = "Get all employees")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Employees retrieved successfully"),
            @ApiResponse(responseCode = "403", description = "Access denied")
    })
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'EMPLOYER')")
    public ResponseEntity<Page<EmployeeResponse>> getAllEmployees(
            @Parameter(description = "Filter criteria") @ModelAttribute EmployeeFilterRequest filter,
            @PageableDefault(size = 20, sort = "grade.rank") Pageable pageable) {
        log.debug("Retrieving employees with filter: {}", filter);
        return ResponseEntity.ok(employeeService.search(filter, pageable));
    }

    @Operation(summary = "Get employee by ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Employee found"),
            @ApiResponse(responseCode = "404", description = "Employee not found"),
            @ApiResponse(responseCode = "403", description = "Access denied")
    })
    @GetMapping("/{employeeId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'EMPLOYER', 'EMPLOYEE')")
    public ResponseEntity<EmployeeResponse> getEmployeeById(
            @Parameter(description = "Employee ID") @PathVariable UUID employeeId) {
        log.debug("Retrieving employee: {}", employeeId);
        return ResponseEntity.ok(employeeService.findById(employeeId));
    }

    @Operation(summary = "Get employee by business ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Employee found"),
            @ApiResponse(responseCode = "404", description = "Employee not found"),
            @ApiResponse(responseCode = "403", description = "Access denied")
    })
    @GetMapping("/biz-id/{bizId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'EMPLOYER')")
    public ResponseEntity<EmployeeResponse> getEmployeeByBizId(
            @Parameter(description = "Employee business ID (4-digit)") @PathVariable String bizId) {
        log.debug("Retrieving employee by bizId: {}", bizId);
        return ResponseEntity.ok(employeeService.findByBizId(bizId));
    }

    @Operation(summary = "Get employees by grade")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Employees retrieved successfully"),
            @ApiResponse(responseCode = "404", description = "Grade not found"),
            @ApiResponse(responseCode = "403", description = "Access denied")
    })
    @GetMapping("/grade/{gradeId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'EMPLOYER')")
    public ResponseEntity<Page<EmployeeResponse>> getEmployeesByGrade(
            @Parameter(description = "Grade ID") @PathVariable UUID gradeId,
            @PageableDefault(size = 20, sort = "name") Pageable pageable) {
        log.debug("Retrieving employees for grade: {}", gradeId);
        EmployeeFilterRequest filter = new EmployeeFilterRequest(
                null, null, null, gradeId, null, null, null, null, null, null
        );
        return ResponseEntity.ok(employeeService.search(filter, pageable));
    }

    // --- UPDATE ---

    @Operation(summary = "Update an existing employee")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Employee updated successfully"),
            @ApiResponse(responseCode = "404", description = "Employee not found"),
            @ApiResponse(responseCode = "400", description = "Invalid request data"),
            @ApiResponse(responseCode = "403", description = "Access denied")
    })
    @PutMapping("/{employeeId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<EmployeeResponse> updateEmployee(
            @Parameter(description = "Employee ID") @PathVariable UUID employeeId,
            @Valid @RequestBody EmployeeUpdateRequest request) {
        log.info("Updating employee: {}", employeeId);
        return ResponseEntity.ok(employeeService.update(employeeId, request));
    }

    // --- DELETE ---

    @Operation(summary = "Delete an employee")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Employee deleted successfully"),
            @ApiResponse(responseCode = "404", description = "Employee not found"),
            @ApiResponse(responseCode = "400", description = "Employee cannot be deleted (e.g., has payroll history)"),
            @ApiResponse(responseCode = "403", description = "Access denied")
    })
    @DeleteMapping("/{employeeId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteEmployee(
            @Parameter(description = "Employee ID") @PathVariable UUID employeeId) {
        log.warn("Deleting employee: {}", employeeId);
        employeeService.delete(employeeId);
        return ResponseEntity.noContent().build();
    }

    // --- EMPLOYEE STATISTICS ---

    @Operation(summary = "Get employee count by grade")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Employee counts retrieved successfully"),
            @ApiResponse(responseCode = "403", description = "Access denied")
    })
    @GetMapping("/stats/count-by-grade")
    @PreAuthorize("hasAnyRole('ADMIN', 'EMPLOYER')")
    public ResponseEntity<Object> getEmployeeCountByGrade() {
        log.debug("Retrieving employee count by grade");
        return ResponseEntity.ok(employeeService.getEmployeeCountByGrade());
    }

    @Operation(summary = "Get total employee count")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Total count retrieved successfully"),
            @ApiResponse(responseCode = "403", description = "Access denied")
    })
    @GetMapping("/stats/total-count")
    @PreAuthorize("hasAnyRole('ADMIN', 'EMPLOYER')")
    public ResponseEntity<Long> getTotalEmployeeCount() {
        log.debug("Retrieving total employee count");
        return ResponseEntity.ok(employeeService.getTotalEmployeeCount());
    }
}
