package org.sp.payroll_service.repository;

import org.sp.payroll_service.domain.common.enums.PayrollStatus;
import org.sp.payroll_service.domain.payroll.PayrollBatch;
import org.sp.payroll_service.domain.common.repository.BaseRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * Payroll Batch data access repository.
 * Manages lookups based on batch date/period and status.
 */
@Repository
public interface PayrollBatchRepository extends BaseRepository<PayrollBatch, UUID> {

    /**
     * Finds all active batches that fall within a specific payment date range.
     * @param startDate The start date of the period (inclusive)
     * @param endDate The end date of the period (inclusive)
     * @return List of active PayrollBatch entities
     */
    List<PayrollBatch> findByPayrollMonthBetween(Instant startDate, Instant endDate);

    /**
     * Finds all active Payroll Batches with a specific status, with pagination.
     * @param status The status of the batch (e.g., PENDING, COMPLETED)
     * @param pageable Pagination information
     * @return Page of PayrollBatch entities
     */
    Page<PayrollBatch> findAllByStatus(PayrollStatus status, Pageable pageable);
}
