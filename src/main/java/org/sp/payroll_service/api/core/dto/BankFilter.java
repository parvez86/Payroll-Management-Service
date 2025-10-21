package org.sp.payroll_service.api.core.dto;

/**
 * DTO for searching/filtering Bank entities.
 */
public record BankFilter(
    String keyword, // Search by name or swift code
    String countryCode
) {}