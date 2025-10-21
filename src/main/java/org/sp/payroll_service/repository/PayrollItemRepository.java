package org.sp.payroll_service.repository;

import org.sp.payroll_service.domain.common.enums.PayrollItemStatus;
import org.sp.payroll_service.domain.payroll.entity.PayrollItem;
import org.sp.payroll_service.domain.common.repository.BaseRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Payroll Item data access repository.
 * Focuses on retrieving payslips by batch and by employee.
 */
@Repository
public interface PayrollItemRepository extends BaseRepository<PayrollItem, UUID> {

    /**
     * Finds all active Payroll Items belonging to a specific Payroll Batch ID.
     * @param payrollBatchId The ID of the parent batch
     * @return List of active PayrollItem entities
     */
    List<PayrollItem> findAllByPayrollBatchId(UUID payrollBatchId);

    /**
     * Find paginated payroll items for a specific batch.
     */
    Page<PayrollItem> findByPayrollBatchId(UUID batchId, Pageable pageable);

    /**
     * Find all payroll items for a batch with a specific transactionStatus.
     */
    List<PayrollItem> findByPayrollBatchIdAndPayrollItemStatus(UUID batchId, PayrollItemStatus status);

    /**
     * Finds the latest active Payroll Item for a specific Employee ID.
     * Assumes a composite key or a unique constraint on (Employee ID, Batch ID) might exist,
     * but this method specifically targets the most recent item for display.
     * @param employeeId The ID of the employee
     * @return Optional PayrollItem entity
     */
    Optional<PayrollItem> findFirstByEmployeeIdOrderByCreatedAtDesc(UUID employeeId);

    /**
     * Find all payroll items for a specific employee.
     */
    List<PayrollItem> findByEmployeeId(UUID employeeId);

    /**
     * Count payroll items by transactionStatus for a specific batch.
     */
    @Query("SELECT COUNT(pi) FROM PayrollItem pi WHERE pi.payrollBatch.id = :batchId AND pi.payrollItemStatus = :status")
    Long countByBatchIdAndStatus(UUID batchId, PayrollItemStatus status);

    /**
     * Get sum of amounts for successful payments in a batch.
     */
    @Query("SELECT COALESCE(SUM(pi.amount), 0) FROM PayrollItem pi WHERE pi.payrollBatch.id = :batchId AND pi.payrollItemStatus = 'PAID'")
    BigDecimal getTotalPaidAmountForBatch(UUID batchId);

    /**
     * Get sum of amounts for failed payments in a batch.
     */
    @Query("SELECT COALESCE(SUM(pi.amount), 0) FROM PayrollItem pi WHERE pi.payrollBatch.id = :batchId AND pi.payrollItemStatus = 'FAILED'")
    BigDecimal getTotalFailedAmountForBatch(UUID batchId);

    /**
     * Get total amount for all items in a batch.
     */
    @Query("SELECT COALESCE(SUM(pi.amount), 0) FROM PayrollItem pi WHERE pi.payrollBatch.id = :batchId")
    BigDecimal getTotalAmountForBatch(UUID batchId);
    
    /**
     * Count payroll items by batch ID.
     */
    long countByPayrollBatchId(UUID batchId);
}
