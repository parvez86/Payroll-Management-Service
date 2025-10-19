package org.sp.payroll_service.repository;

import org.sp.payroll_service.domain.payroll.PayrollItem;
import org.sp.payroll_service.domain.common.repository.BaseRepository;
import org.sp.payroll_service.domain.payroll.PayrollBatch;
import org.springframework.stereotype.Repository;

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
     * Finds the latest active Payroll Item for a specific Employee ID.
     * Assumes a composite key or a unique constraint on (Employee ID, Batch ID) might exist,
     * but this method specifically targets the most recent item for display.
     * @param employeeId The ID of the employee
     * @return Optional PayrollItem entity
     */
    Optional<PayrollItem> findFirstByEmployeeIdOrderByCreatedAtDesc(UUID employeeId);
}
