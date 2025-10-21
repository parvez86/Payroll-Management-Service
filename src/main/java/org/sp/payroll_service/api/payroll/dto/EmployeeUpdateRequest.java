package org.sp.payroll_service.api.payroll.dto;

import org.sp.payroll_service.domain.core.entity.Grade;
import org.sp.payroll_service.domain.common.enums.EmploymentStatus;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;
import java.util.UUID;

/**
 * Data Transfer Object (DTO) used for updating an existing Employee entity.
 * <p>
 * **Optionality vs. Validity:** All fields in this DTO are optional (may be null) 
 * to allow partial updates. However, if a value IS provided (i.e., non-null), 
 * it MUST strictly conform to any associated validation constraints 
 * (e.g., {@code @Size}, {@code @Email}, {@code @Pattern}).
 * </p>
 *
 * @param phoneNumber The employee's updated contact phone number. Must match the standard US phone pattern if provided.
 * @param grade The updated payroll grade level (e.g., Grade.G5).
 * @param status The updated employment transactionStatus (e.g., EmploymentStatus.TERMINATED).
 * @param jobTitle The employee's new job title or designation. Must be less than 50 characters if provided.
 * @param bankAccountNumber The updated primary bank account number for payroll disbursement. Must be less than 50 characters if provided.
 * @param bankRoutingNumber The updated bank routing number (e.g., Swift/ABA code). Must be less than 50 characters if provided.
 */
public record EmployeeUpdateRequest(

    @Email
    String email,

    // Example of enforcing validity using a regex pattern
    @Pattern(regexp = "^(\\([0-9]{3}\\)|[0-9]{3})[-.\\s]?[0-9]{3}[-.\\s]?[0-9]{4}$", message = "Phone number format is invalid.")
    String phoneNumber,

    // Employment Details
    Grade grade,

    @Size(max = 50)
    String jobTitle,

    // Financial Details
    @Size(max = 50)
    String bankAccountNumber,

    @Size(max = 50)
    String bankRoutingNumber,

    EmploymentStatus status
) {}
