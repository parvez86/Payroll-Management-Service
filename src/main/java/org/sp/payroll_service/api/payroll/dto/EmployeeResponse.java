package org.sp.payroll_service.api.payroll.dto;

import org.sp.payroll_service.api.core.dto.CompanyResponse;
import org.sp.payroll_service.api.core.dto.GradeResponse;
import org.sp.payroll_service.api.wallet.dto.AccountResponse;
import org.sp.payroll_service.domain.common.enums.EntityStatus;

import java.util.UUID;

/**
 * Employee response record for API consumption.
 * @param id unique identifier
 * @param code business identifier
 * @param name employee name
 * @param grade grade information
 * @param account account details
 * @param status employee transactionStatus
 */
public record EmployeeResponse(
        UUID id,
        String code,
        String name,
        String address,
        String mobile,
        CompanyResponse company,
        GradeResponse grade,
        AccountResponse account,
        EntityStatus status
) {}