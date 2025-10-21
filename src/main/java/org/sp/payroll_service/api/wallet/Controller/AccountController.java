package org.sp.payroll_service.api.wallet.Controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.sp.payroll_service.api.wallet.dto.*;
import org.sp.payroll_service.domain.common.enums.OwnerType;
import org.sp.payroll_service.domain.wallet.service.AccountService;
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
 * REST Controller for managing {@code Account} entities (Financial Wallets).
 * Operations are asynchronous and secured following financial data best practices.
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
    @PostMapping
    // Restrict creation to administrative or payroll setup roles
    @PreAuthorize("hasAnyRole('ADMIN', 'EMPLOYER')") 
    public CompletableFuture<ResponseEntity<AccountResponse>> createAccount(
            @Valid @RequestBody CreateAccountRequest request) {
        log.info("Request to create new account for owner: {}", request.ownerId());
        return accountService.create(request)
                .thenApply(response -> ResponseEntity.status(HttpStatus.CREATED).body(response));
    }
    
    @Operation(summary = "Get an account by its unique Account ID")
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'EMPLOYER')")
    public CompletableFuture<ResponseEntity<AccountResponse>> getAccountById(
            @PathVariable UUID id) {
        log.debug("Request to fetch account with ID: {}", id);
        return accountService.findById(id)
                .thenApply(ResponseEntity::ok);
    }
    
    @Operation(summary = "Get an account by the Owner ID (e.g., Employee ID)")
    @GetMapping("/owner/{ownerId}/{ownerType}")
    // An employee (self) should be able to view their own account balance/details.
    @PreAuthorize("hasAnyRole('ADMIN', 'EMPLOYER') or #ownerId.toString() == authentication.name")
    public CompletableFuture<ResponseEntity<AccountResponse>> getAccountByOwnerId(
            @PathVariable UUID ownerId,
            @PathVariable OwnerType ownerType) {
        log.debug("Request to fetch account for Owner ID: {}", ownerId);
        return accountService.findByOwnerId(ownerId, ownerType)
                .thenApply(ResponseEntity::ok);
    }

    // --- UPDATE (Metadata Only)  ---
    @Operation(summary = "Update account metadata (name, overdraft limit). Balance is updated via transactions.")
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'EMPLOYER')") 
    public CompletableFuture<ResponseEntity<AccountResponse>> updateAccount(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateAccountRequest request) {
        log.info("Request to update account metadata with ID: {}", id);
        return accountService.update(id, request)
                .thenApply(ResponseEntity::ok);
    }
    
    // --- DELETE (Cleanup) 🗑️ ---
    @Operation(summary = "Delete an account by ID (high privilege operation)")
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasRole('ADMIN')")
    public CompletableFuture<ResponseEntity<Void>> deleteAccount(@PathVariable UUID id) {
        log.warn("Request to delete account with ID: {}", id);
        return accountService.delete(id)
                .thenApply(v -> ResponseEntity.noContent().build());
    }

    // --- SEARCH/PAGINATE 🔍 ---
    @Operation(summary = "Search accounts using dynamic filters and pagination")
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'EMPLOYER')")
    public CompletableFuture<ResponseEntity<Page<AccountResponse>>> searchAccounts(
            @ModelAttribute AccountFilter filter,
            @PageableDefault(size = 20, sort = "accountNumber") Pageable pageable) {
        log.debug("Request to search accounts with filters: {}", filter);
        return accountService.search(filter, pageable)
                .thenApply(ResponseEntity::ok);
    }
    
    // --- TRANSACTIONAL ENDPOINTS (Placeholder for a separate TransactionService) ---

    @Operation(summary = "Deposit funds into the account")
    @PostMapping("/{id}/deposit")
    @PreAuthorize("hasAnyRole('ADMIN', 'EMPLOYER')")
    public CompletableFuture<ResponseEntity<AccountResponse>> deposit(
            @PathVariable UUID id,
            @Valid @RequestBody AccountTransactionRequest request) {
        // Calls a specialized transaction service method:
        // return transactionService.deposit(id, request.amount(), request.description())
        //         .thenApply(ResponseEntity::ok);
        return CompletableFuture.completedFuture(ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).build());
    }

    @Operation(summary = "Withdraw funds from the account")
    @PostMapping("/{id}/withdraw")
    @PreAuthorize("hasAnyRole('ADMIN', 'EMPLOYER')")
    public CompletableFuture<ResponseEntity<AccountResponse>> withdraw(
            @PathVariable UUID id,
            @Valid @RequestBody AccountTransactionRequest request) {
        // Calls a specialized transaction service method:
        // return transactionService.withdraw(id, request.amount(), request.description())
        //         .thenApply(ResponseEntity::ok);
        return CompletableFuture.completedFuture(ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).build());
    }
}