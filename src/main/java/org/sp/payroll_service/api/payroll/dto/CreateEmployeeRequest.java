package org.sp.payroll_service.api.payroll.dto;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.util.UUID;

/**
 * Employee creation request record.
 * @param bizId 4-digit business identifier
 * @param name employee full name
 * @param address residential address
 * @param mobile contact number
 * @param gradeId grade reference ID
 * @param companyId ID of the company the employee belongs to
 * @param username unique username for login
 * @param email employee email address
 * @param password initial password for login
 * @param accountName bank account holder name
 * @param accountNumber bank account number
 * @param overdraftLimit configured overdraft limit
 * @param branchId bank branch reference ID
 */
public record CreateEmployeeRequest(
//        @NotBlank(message = "Business ID is required.")
        @Pattern(regexp = "\\d{4}", message = "Business ID must be 4 digits. (optional)")
        String bizId,

        @NotBlank(message = "Name is required.")
        @Size(max = 100)
        String name,

        @Size(max = 255, message = "Address cannot exceed 255 characters.")
        String address,

        @Pattern(regexp = "\\+?[0-9]{10,15}", message = "Invalid mobile number format.")
        String mobile,

        @NotNull(message = "Grade ID is required.")
        UUID gradeId,

        @NotNull(message = "Company ID is required.")
        UUID companyId,

        @NotBlank @Size(min = 3, max = 50) String username,

        // CORRECTION: Email must be mandatory and validated
        @NotBlank(message = "Email is required.")
        @Email(message = "Invalid email format.")
        String email,

        @NotBlank @Size(min = 6) String password,

        @NotBlank(message = "Account name (Holder name) is required.")
        @Size(max = 100, message = "Account name must be less than 100 characters.")
        String accountName,

        // --- Account Number (Validation Refinement) ---
        @NotBlank(message = "Account number is required and unique.")
        @Pattern(regexp = "[0-9]+", message = "Account number must contain only digits.") // Added pattern for basic numeric check
        @Size(min = 10, max = 38, message = "Account number length is invalid.")
        String accountNumber,

        @NotNull(message = "Overdraft limit is required.")
        @DecimalMin(value = "0.00", inclusive = true, message = "Overdraft limit cannot be negative.")
        BigDecimal overdraftLimit,

        @NotNull(message = "Branch ID is required for bank branch linkage.")
        UUID branchId
) {}