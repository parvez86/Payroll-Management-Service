package org.sp.payroll_service.api.payroll.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import org.sp.payroll_service.api.wallet.dto.CreateAccountRequest;

import java.util.UUID;

/**
 * Employee creation request record.
 * @param bizId 4-digit business identifier
 * @param name employee full name
 * @param address residential address
 * @param mobile contact number
 * @param gradeId grade reference ID
 * @param accountRequest bank account details
 */
public record CreateEmployeeRequest(
        @NotBlank(message = "Business ID is required.")
        @Pattern(regexp = "\\d{4}", message = "Business ID must be 4 digits.")
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

        @NotNull(message = "Account details are required.")
        @Valid
        CreateAccountRequest accountRequest
) {}