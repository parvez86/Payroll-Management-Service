package org.sp.payroll_service.api.payroll.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.sp.payroll_service.api.payroll.dto.TransactionFilter;
import org.sp.payroll_service.api.payroll.dto.TransactionResponse;
import org.sp.payroll_service.api.payroll.dto.TransferRequest;
import org.sp.payroll_service.domain.common.dto.response.Money;
import org.sp.payroll_service.domain.payroll.service.TransactionService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

/**
 * REST Controller for financial transaction operations.
 * Handles money transfers, balance queries, and transaction history with ACID compliance.
 * <p>
 * NOTE: This is a **synchronous** controller implementation, calling the service directly.
 */
@Tag(name = "Transaction Management", description = "Financial transaction operations, transfers, and balance queries")
@RestController
@RequestMapping("/api/v1/transactions")
@RequiredArgsConstructor
@Slf4j
public class TransactionController {

    private final TransactionService transactionService;

    // --- MONEY TRANSFER OPERATIONS ---

    @Operation(summary = "Execute money transfer between accounts")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Transfer executed successfully"),
            @ApiResponse(responseCode = "400", description = "Insufficient funds or invalid request"),
            @ApiResponse(responseCode = "403", description = "Access denied")
    })
    @PostMapping("/transfer")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<TransactionResponse> executeTransfer(
            @Valid @RequestBody TransferRequest request) {
        log.info("Executing transfer: {} from {} to {}",
                request.amount(), request.debitAccountId(), request.creditAccountId());

        TransactionResponse transaction = transactionService.executeTransfer(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(transaction);
    }

    // --- BALANCE QUERIES ---

    @Operation(summary = "Get account balance")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Balance retrieved successfully"),
            @ApiResponse(responseCode = "404", description = "Account not found"),
            @ApiResponse(responseCode = "403", description = "Access denied")
    })
    @GetMapping("/accounts/{accountId}/balance")
    @PreAuthorize("hasAnyRole('ADMIN', 'EMPLOYER')")
    public ResponseEntity<Money> getAccountBalance(
            @Parameter(description = "Account ID") @PathVariable UUID accountId) {
        log.debug("Retrieving balance for account: {}", accountId);

        return ResponseEntity.ok(transactionService.getAccountBalance(accountId));
    }

    @Operation(summary = "Check if account has sufficient balance")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Balance check completed"),
            @ApiResponse(responseCode = "404", description = "Account not found"),
            @ApiResponse(responseCode = "403", description = "Access denied")
    })
    @PostMapping("/accounts/{accountId}/check-balance")
    @PreAuthorize("hasAnyRole('ADMIN', 'EMPLOYER')")
    public ResponseEntity<Boolean> checkSufficientBalance(
            @Parameter(description = "Account ID") @PathVariable UUID accountId,
            @Parameter(description = "Amount to check") @RequestBody Money amount) {
        log.debug("Checking sufficient balance for account: {} amount: {}", accountId, amount);

        return ResponseEntity.ok(transactionService.hasSufficientBalance(accountId, amount));
    }

    // --- TRANSACTION HISTORY ---

    @Operation(summary = "Get transaction history with filtering")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Transaction history retrieved successfully"),
            @ApiResponse(responseCode = "403", description = "Access denied")
    })
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'EMPLOYER')")
    public ResponseEntity<Page<TransactionResponse>> getTransactionHistory(
            @Parameter(description = "Filter criteria") @ModelAttribute TransactionFilter filter,
            @PageableDefault(size = 20, sort = "createdAt") Pageable pageable) {
        log.debug("Retrieving transaction history with filter: {}", filter);

        return ResponseEntity.ok(transactionService.getTransactionHistory(filter, pageable));
    }

    @Operation(summary = "Get transaction by ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Transaction found"),
            @ApiResponse(responseCode = "404", description = "Transaction not found"),
            @ApiResponse(responseCode = "403", description = "Access denied")
    })
    @GetMapping("/{transactionId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'EMPLOYER')")
    public ResponseEntity<TransactionResponse> getTransactionById(
            @Parameter(description = "Transaction ID") @PathVariable UUID transactionId) {
        log.debug("Retrieving transaction: {}", transactionId);

        return ResponseEntity.ok(transactionService.getTransactionById(transactionId));
    }

    @Operation(summary = "Get all transactions for a specific account")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Account transactions retrieved successfully"),
            @ApiResponse(responseCode = "404", description = "Account not found"),
            @ApiResponse(responseCode = "403", description = "Access denied")
    })
    @GetMapping("/accounts/{accountId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'EMPLOYER')")
    public ResponseEntity<Page<TransactionResponse>> getAccountTransactions(
            @Parameter(description = "Account ID") @PathVariable UUID accountId,
            @PageableDefault(size = 20, sort = "createdAt") Pageable pageable) {
        log.debug("Retrieving transactions for account: {}", accountId);

        return ResponseEntity.ok(transactionService.getAccountTransactions(accountId, pageable));
    }

    @Operation(summary = "Get all transactions for a payroll batch")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Batch transactions retrieved successfully"),
            @ApiResponse(responseCode = "404", description = "Payroll batch not found"),
            @ApiResponse(responseCode = "403", description = "Access denied")
    })
    @GetMapping("/batches/{batchId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'EMPLOYER')")
    public ResponseEntity<List<TransactionResponse>> getBatchTransactions(
            @Parameter(description = "Payroll batch ID") @PathVariable UUID batchId) {
        log.debug("Retrieving transactions for batch: {}", batchId);

        return ResponseEntity.ok(transactionService.getBatchTransactions(batchId));
    }

    // --- TRANSACTION MANAGEMENT ---

    @Operation(summary = "Reverse a transaction")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Transaction reversed successfully"),
            @ApiResponse(responseCode = "400", description = "Transaction cannot be reversed"),
            @ApiResponse(responseCode = "404", description = "Transaction not found"),
            @ApiResponse(responseCode = "403", description = "Access denied")
    })
    @PostMapping("/{transactionId}/reverse")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<TransactionResponse> reverseTransaction(
            @Parameter(description = "Transaction ID") @PathVariable UUID transactionId,
            @Parameter(description = "Reason for reversal") @RequestBody String reason) {
        log.warn("Reversing transaction: {} with reason: {}", transactionId, reason);

        TransactionResponse reversalTransaction = transactionService.reverseTransaction(transactionId, reason);
        return ResponseEntity.status(HttpStatus.CREATED).body(reversalTransaction);
    }
}
