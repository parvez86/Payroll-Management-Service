package org.sp.payroll_service.api.payroll.dto;

import jakarta.validation.constraints.*;
import org.sp.payroll_service.domain.core.entity.Grade;
import org.sp.payroll_service.domain.common.enums.EmploymentStatus;

import java.math.BigDecimal;
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
 * @param gradeId The updated payroll grade level (e.g., Grade.G5).
 * @param status The updated employment transactionStatus (e.g., EmploymentStatus.TERMINATED).
 * @param jobTitle The employee's new job title or designation. Must be less than 50 characters if provided.
 * @param bankAccountNumber The updated primary bank account number for payroll disbursement. Must be less than 50 characters if provided.
 * @param bankRoutingNumber The updated bank routing number (e.g., Swift/ABA code). Must be less than 50 characters if provided.
 */
public record EmployeeUpdateRequest(

    @Email
    String email,

    @NotBlank @Size(min = 6) String password,

    // Example of enforcing validity using a regex pattern
    @Pattern(regexp = "^(\\([0-9]{3}\\)|[0-9]{3})[-.\\s]?[0-9]{3}[-.\\s]?[0-9]{4}$", message = "Phone number format is invalid.")
    String phoneNumber,

    // Employment Details
    UUID gradeId,

    @Size(max = 50)
    String jobTitle,

    // Financial Details
    @Size(max = 50)
    String bankAccountNumber,

    @Size(max = 50)
    String bankRoutingNumber,

    EmploymentStatus status,

    @NotBlank(message = "Name is required.")
    @Size(max = 100)
    String name,

    @Size(max = 255, message = "Address cannot exceed 255 characters.")
    String address,

    @Pattern(regexp = "\\+?[0-9]{10,15}", message = "Invalid mobile number format.")
    String mobile,

    @NotBlank(message = "Account name (Holder name) is required.")
    @Size(max = 100, message = "Account name must be less than 100 characters.")
    String accountName,

    @NotNull(message = "Overdraft limit is required.")
    @DecimalMin(value = "0.00", inclusive = true, message = "Overdraft limit cannot be negative.")
    BigDecimal overdraftLimit,

    @NotNull(message = "Branch ID is required for bank branch linkage.")
    UUID branchId
) {}
