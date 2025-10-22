package org.sp.payroll_service.domain.payroll.service;

import org.sp.payroll_service.api.payroll.dto.*;
import org.sp.payroll_service.domain.payroll.exception.InsufficientFundsException;
import org.sp.payroll_service.domain.payroll.exception.PayrollProcessingException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

/**
 * Service interface for payroll processing operations.
 * Handles batch creation, salary calculations, and payroll execution.
 */
public interface PayrollService {
    
    /**
     * Creates a new payroll batch for processing.
     * @param request batch creation data
     * @return created batch details
     */
    PayrollBatchResponse createPayrollBatch(CreatePayrollBatchRequest request);
    
    /**
     * Calculates salaries for all employees without executing transfers.
     * @param batchId batch identifier
     * @return salary calculations for all employees
     */
    List<SalaryCalculation> calculateSalaries(UUID batchId);
    
    /**
     * Calculates salaries for all employees without a specific batch.
     * @param companyId company identifier
     * @return salary calculations for all employees
     */
    List<SalaryCalculation> calculateSalariesForCompany(UUID companyId);
    
    /**
     * Processes payroll batch with ACID compliance.
     * Executes actual money transfers from company to employee accounts.
     * @param batchId batch identifier
     * @return processing result with detailed transactionStatus
     * @throws InsufficientFundsException if company balance insufficient
     * @throws PayrollProcessingException if processing fails
     */
    PayrollResult processPayroll(UUID batchId);
    
    /**
     * Retrieves all payroll batches with optional filtering.
     * @param filter filter criteria
     * @param pageable pagination parameters
     * @return paginated batch summaries
     */
    Page<PayrollBatchSummary> getAllBatches(PayrollBatchFilter filter, Pageable pageable);
    
    /**
     * Retrieves a specific payroll batch by ID.
     * @param batchId batch identifier
     * @return batch details
     */
    PayrollBatchResponse getBatchById(UUID batchId);
    
    /**
     * Retrieves payroll items for a specific batch.
     * @param batchId batch identifier
     * @param pageable pagination parameters
     * @return paginated payroll items
     */
    Page<PayrollItemResponse> getBatchItems(UUID batchId, Pageable pageable);
    
    /**
     * Retries a failed payroll item.
     * @param payrollItemId payroll item identifier
     * @return updated payroll item
     */
    PayrollItemResponse retryPayrollItem(UUID payrollItemId);
    
    /**
     * Cancels a pending payroll batch.
     * @param batchId batch identifier
     * @return updated batch response
     */
    PayrollBatchResponse cancelBatch(UUID batchId);
}