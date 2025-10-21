package org.sp.payroll_service.api.payroll.dto;

import java.util.UUID;

/**
 * Employee summary record for list operations.
 * @param id unique identifier
 * @param bizId business identifier
 * @param name employee name
 * @param gradeName grade name
 * @param accountNumber account number
 */
public record EmployeeSummary(
    UUID id,
    String bizId,
    String name,
    String gradeName,
    String accountNumber
) {}