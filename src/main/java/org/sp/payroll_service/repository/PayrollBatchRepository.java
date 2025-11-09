package org.sp.payroll_service.repository;

import org.sp.payroll_service.domain.common.enums.PayrollStatus;
import org.sp.payroll_service.domain.payroll.entity.PayrollBatch;
import org.sp.payroll_service.domain.common.repository.BaseRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Payroll Batch data access repository.
 * Manages lookups based on batch date/period and transactionStatus.
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
     * Finds all active Payroll Batches with a specific transactionStatus, with pagination.
     * @param status The transactionStatus of the batch (e.g., PENDING, COMPLETED)
     * @param pageable Pagination information
     * @return Page of PayrollBatch entities
     */
    Page<PayrollBatch> findAllByStatus(PayrollStatus status, Pageable pageable);


    /**
     * Check if an active entity exists by ID.
     * @param payrollStatus payroll batch status
     */
    @Query("SELECT CASE WHEN COUNT(e) > 0 THEN true ELSE false END FROM PayrollBatch e WHERE e.payrollStatus = :payrollStatus AND e.status <> 'DELETED'")
    boolean existsByPayrollStatus(@Param("payrollStatus") PayrollStatus payrollStatus);


    /**
     * Finds the first active (not DELETED) PayrollBatch with the given status.
     * Orders by ID to ensure a consistent 'first' result.
     * @param payrollStatus the status to search for
     * @return an Optional containing the first matching entity, or empty if none found.
     */
    @Query(value = "SELECT e FROM PayrollBatch e " +
            "WHERE e.payrollStatus = :payrollStatus AND e.status <> 'DELETED' " +
            "ORDER BY e.createdAt ASC " +
            "FETCH FIRST 1 ROWS ONLY")
    Optional<PayrollBatch> findFirstActiveByPayrollStatus(
            @Param("payrollStatus") PayrollStatus payrollStatus
    );

    /**
     * Finds the first active (not DELETED) {@link PayrollBatch} belonging to a specific company
     * and having one of the specified statuses.
     * * The search is ordered by the creation date in ascending order, ensuring the oldest matching
     * batch is returned first.
     *
     * @param companyId The unique identifier (UUID) of the company the batch belongs to.
     * @param statuses A {@link List} of {@link PayrollStatus} enums to filter the results by.
     * @return An {@link Optional} containing the oldest matching {@link PayrollBatch},
     * or {@link Optional#empty()} if no matching batch is found.
     */
    @Query("SELECT pb FROM PayrollBatch pb WHERE pb.company.id = :companyId AND pb.payrollStatus IN :statuses ORDER BY pb.createdAt ASC")
    Optional<PayrollBatch> findFirstByCompanyIdAndPayrollStatusInOrderByCreatedAtAsc(@Param("companyId") UUID companyId, @Param("statuses") List<PayrollStatus> statuses);
}
