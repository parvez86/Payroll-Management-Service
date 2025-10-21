package org.sp.payroll_service.api.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

/**
 * Request DTO for logout operation.
 */
@Schema(description = "Request to logout and revoke tokens")
public record LogoutRequest(
    
    @Schema(description = "Refresh token to revoke (optional - if not provided, will revoke all user tokens)", 
            example = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...")
    String refreshToken,
    
    @Schema(description = "Whether to logout from all devices", example = "false")
    Boolean logoutFromAllDevices
) {
    
    public LogoutRequest {
        if (logoutFromAllDevices == null) {
            logoutFromAllDevices = false;
        }
    }
}