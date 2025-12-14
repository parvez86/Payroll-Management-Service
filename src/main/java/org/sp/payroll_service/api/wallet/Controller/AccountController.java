package org.sp.payroll_service.api.wallet.Controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.sp.payroll_service.api.payroll.dto.PageResponse;
import org.sp.payroll_service.api.wallet.dto.*;
import org.sp.payroll_service.domain.common.dto.response.HeaderResponse;
import org.sp.payroll_service.domain.common.enums.OwnerType;
import org.sp.payroll_service.domain.wallet.service.AccountService;
import org.sp.payroll_service.security.annotation.CurrentUser;
import org.sp.payroll_service.security.annotation.IsAuthenticated;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

/**
 * REST Controller for managing {@code Account} entities (Financial Wallets).
 * This is a **synchronous** controller implementation, calling the service directly.
 */
@Tag(name = "Account/Wallet Management", description = "CRUD and search operations for company/employee financial accounts.")
@RestController
@RequestMapping("/api/v1/accounts")
@RequiredArgsConstructor
@Slf4j
public class AccountController {

    private final AccountService accountService;

    // --- CREATE (Setup) ➕ ---
    @Operation(summary = "Create a new financial account (Company or Employee wallet)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Account created successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request data"),
            @ApiResponse(responseCode = "403", description = "Access denied")
    })
    @PostMapping
    // Restrict creation to administrative or payroll setup roles
    @PreAuthorize("hasAnyRole('ADMIN', 'EMPLOYER')")
    public ResponseEntity<AccountResponse> createAccount(
            @Valid @RequestBody CreateAccountRequest request,
            @CurrentUser HeaderResponse principal) {
        log.info("Request to create new account for owner: {} by {} ({})", request.ownerId(), principal.username(), principal.userId());

        AccountResponse response = accountService.create(request, principal);
        log.info("Account {} created by {}", response.id(), principal.userId());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @Operation(summary = "Get an account by its unique Account ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Account found"),
            @ApiResponse(responseCode = "404", description = "Account not found"),
            @ApiResponse(responseCode = "403", description = "Access denied")
    })
    @GetMapping("/{id}")
    @IsAuthenticated
    public ResponseEntity<AccountResponse> getAccountById(
            @Parameter(description = "Account ID") @PathVariable UUID id) {
        log.debug("Request to fetch account with ID: {}", id);

        return ResponseEntity.ok(accountService.findById(id));
    }

    @Operation(summary = "Get an account by the Owner ID (e.g., Employee ID)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Account found"),
            @ApiResponse(responseCode = "404", description = "Account not found"),
            @ApiResponse(responseCode = "403", description = "Access denied")
    })
    @GetMapping("/owner/{ownerId}/{ownerType}")
    // An employee (self) should be able to view their own account balance/details.
    @PreAuthorize("hasAnyRole('ADMIN', 'EMPLOYER') or #ownerId.toString() == authentication.name")
    public ResponseEntity<AccountResponse> getAccountByOwnerId(
            @Parameter(description = "Owner ID (e.g., Employee ID)") @PathVariable UUID ownerId,
            @Parameter(description = "Owner Type (e.g., EMPLOYEE, COMPANY)") @PathVariable OwnerType ownerType) {
        log.debug("Request to fetch account for Owner ID: {}", ownerId);

        // Synchronous call
        return ResponseEntity.ok(accountService.findByOwnerId(ownerId, ownerType));
    }

    // --- UPDATE (Metadata Only)  ---
    @Operation(summary = "Update account metadata (name, overdraft limit). Balance is updated via transactions.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Account updated successfully"),
            @ApiResponse(responseCode = "404", description = "Account not found"),
            @ApiResponse(responseCode = "403", description = "Access denied")
    })
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'EMPLOYER')")
    public ResponseEntity<AccountResponse> updateAccount(
            @Parameter(description = "Account ID") @PathVariable UUID id,
            @Valid @RequestBody UpdateAccountRequest request,
            @org.springframework.security.core.annotation.AuthenticationPrincipal org.sp.payroll_service.domain.common.dto.response.HeaderResponse principal) {
        log.info("Request to update account metadata with ID: {} by {} ({})", id, principal.username(), principal.userId());

        ResponseEntity<AccountResponse> response = ResponseEntity.ok(accountService.update(id, request, principal));
        log.info("Account metadata updated for ID: {} by {}", id, principal.userId());
        return response;
    }

    // --- DELETE (Cleanup) 🗑️ ---
    @Operation(summary = "Delete an account by ID (high privilege operation)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Account deleted successfully"),
            @ApiResponse(responseCode = "404", description = "Account not found"),
            @ApiResponse(responseCode = "403", description = "Access denied")
    })
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteAccount(
            @Parameter(description = "Account ID") @PathVariable UUID id,
            @org.springframework.security.core.annotation.AuthenticationPrincipal org.sp.payroll_service.domain.common.dto.response.HeaderResponse principal) {
        log.warn("Request to delete account with ID: {} by {} ({})", id, principal.username(), principal.userId());

        accountService.delete(id, principal);
        log.info("Account deleted with ID: {} by {}", id, principal.userId());
        return ResponseEntity.noContent().build();
    }

    // --- SEARCH/PAGINATE 🔍 ---
    @Operation(summary = "Search accounts using dynamic filters and pagination")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Accounts retrieved successfully"),
            @ApiResponse(responseCode = "403", description = "Access denied")
    })
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'EMPLOYER')")
    public ResponseEntity<PageResponse<AccountResponse>> searchAccounts(
            @Parameter(description = "Filter criteria") @ModelAttribute AccountFilter filter,
            @PageableDefault(size = 20, sort = "accountNumber") Pageable pageable,
            @AuthenticationPrincipal HeaderResponse headerResponse) {
        log.debug("Request to search accounts with filters: {}, principal: {}", filter, headerResponse);

        return ResponseEntity.ok(accountService.search(filter, pageable, headerResponse));
    }

    // --- TRANSACTIONAL ENDPOINTS (Placeholder for a separate TransactionService) ---

//    @Operation(summary = "Deposit funds into the account")
//    @ApiResponses(value = {
//            @ApiResponse(responseCode = "503", description = "Not yet implemented")
//    })
//    @PostMapping("/{id}/deposit")
//    @PreAuthorize("hasAnyRole('ADMIN', 'EMPLOYER')")
//    public ResponseEntity<AccountResponse> deposit(
//            @Parameter(description = "Account ID") @PathVariable UUID id,
//            @Valid @RequestBody AccountTransactionRequest request) {
//        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).build();
//    }
//
//    @Operation(summary = "Withdraw funds from the account")
//    @ApiResponses(value = {
//            @ApiResponse(responseCode = "503", description = "Not yet implemented")
//    })
//    @PostMapping("/{id}/withdraw")
//    @PreAuthorize("hasAnyRole('ADMIN', 'EMPLOYER')")
//    public ResponseEntity<AccountResponse> withdraw(
//            @Parameter(description = "Account ID") @PathVariable UUID id,
//            @Valid @RequestBody AccountTransactionRequest request) {
//        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).build();
//    }
}