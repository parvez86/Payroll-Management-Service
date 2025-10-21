package org.sp.payroll_service.api.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

/**
 * Request DTO for refreshing access tokens using refresh token.
 */
@Schema(description = "Request to refresh access token")
public record RefreshTokenRequest(
    
    @NotBlank(message = "Refresh token is required")
    @Schema(description = "Refresh token for generating new access token", example = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...")
    String refreshToken
) {
}