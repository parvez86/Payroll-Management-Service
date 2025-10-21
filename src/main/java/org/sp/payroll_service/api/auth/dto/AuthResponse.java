package org.sp.payroll_service.api.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * JWT authentication response record with both access and refresh tokens.
 * @param accessToken JWT access token for API access
 * @param refreshToken JWT refresh token for generating new access tokens
 * @param tokenType token type (Bearer)
 * @param expiresIn access token expiration in seconds
 * @param refreshExpiresIn refresh token expiration in seconds
 */
@Schema(description = "Authentication response with access and refresh tokens")
public record AuthResponse(
    
    @Schema(description = "JWT access token", example = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...")
    String accessToken,
    
    @Schema(description = "JWT refresh token", example = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...")
    String refreshToken,
    
    @Schema(description = "Token type", example = "Bearer")
    String tokenType,
    
    @Schema(description = "Access token expiration time in seconds", example = "3600")
    long expiresIn,
    
    @Schema(description = "Refresh token expiration time in seconds", example = "604800")
    long refreshExpiresIn
) {
    
    /**
     * Convenience constructor with default token type
     */
    public AuthResponse(String accessToken, String refreshToken, long expiresIn, long refreshExpiresIn) {
        this(accessToken, refreshToken, "Bearer", expiresIn, refreshExpiresIn);
    }
    
    /**
     * Legacy constructor for backward compatibility
     */
    public static AuthResponse withAccessTokenOnly(String accessToken, long expiresIn) {
        return new AuthResponse(accessToken, null, "Bearer", expiresIn, 0);
    }
}