package org.sp.payroll_service.api.payroll.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import org.sp.payroll_service.domain.common.enums.PayrollStatus;

import java.time.LocalDate;
import java.util.UUID;

/**
 * Filter criteria for payroll batch queries.
 */
@Builder
@Schema(description = "Filter criteria for payroll batch searches")
public record PayrollBatchFilter(
    
    @Schema(description = "Filter by payroll transactionStatus")
    PayrollStatus status,
    
    @Schema(description = "Filter by company ID")
    UUID companyId,
    
    @Schema(description = "Filter by payroll month (start of range)")
    LocalDate fromMonth,
    
    @Schema(description = "Filter by payroll month (end of range)")
    LocalDate toMonth,
    
    @Schema(description = "Search by batch name (partial match)")
    String nameContains
) {}