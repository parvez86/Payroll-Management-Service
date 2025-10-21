package org.sp.payroll_service.api.auth.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.sp.payroll_service.api.auth.dto.AuthResponse;
import org.sp.payroll_service.api.auth.dto.LogoutRequest;
import org.sp.payroll_service.api.auth.dto.RefreshTokenRequest;
import org.sp.payroll_service.api.auth.dto.UserCreateRequest;
import org.sp.payroll_service.api.auth.dto.LoginRequest;
import org.sp.payroll_service.api.auth.dto.UserResponse;
import org.sp.payroll_service.domain.auth.service.AuthenticationService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST controller for authentication operations.
 */
@RestController
@RequestMapping("/api/v1/auth")
@Validated
@RequiredArgsConstructor
@Tag(name = "Authentication", description = "Authentication and user management endpoints")
public class AuthController {
    
    private final AuthenticationService authService;
    
    /**
     * Authenticates user and returns JWT token.
     * @param request login credentials
     * @return JWT authentication response
     */
    @PostMapping("/login")
    @Operation(summary = "User Login", description = "Authenticates user credentials and returns JWT token")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Authentication successful"),
        @ApiResponse(responseCode = "401", description = "Invalid credentials"),
        @ApiResponse(responseCode = "400", description = "Invalid request format")
    })
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        AuthResponse response = authService.authenticate(request);
        return ResponseEntity.ok(response);
    }
    
    /**
     * Refreshes access token using refresh token.
     * @param request refresh token request
     * @return new token pair
     */
    @PostMapping("/refresh")
    @Operation(summary = "Refresh Token", description = "Generates new access token using refresh token")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Token refreshed successfully"),
        @ApiResponse(responseCode = "401", description = "Invalid or expired refresh token"),
        @ApiResponse(responseCode = "400", description = "Invalid request format")
    })
    public ResponseEntity<AuthResponse> refreshToken(@Valid @RequestBody RefreshTokenRequest request) {
        AuthResponse response = authService.refreshToken(request);
        return ResponseEntity.ok(response);
    }

    /**
     * Logs out user by revoking tokens.
     * @param request logout request with refresh token and options
     * @return success response
     */
    @PostMapping("/logout")
    @Operation(summary = "User Logout", description = "Revokes user tokens for logout")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Logout successful"),
        @ApiResponse(responseCode = "401", description = "Invalid refresh token"),
        @ApiResponse(responseCode = "400", description = "Invalid request format")
    })
    public ResponseEntity<Void> logout(@Valid @RequestBody LogoutRequest request) {
        authService.logout(request);
        return ResponseEntity.ok().build();
    }

    /**
     * Creates new user account.
     * @param request user creation data
     * @return created user information
     */
    @PostMapping("/register")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Register User", description = "Creates new user account (Admin only)")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "User created successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid request data"),
        @ApiResponse(responseCode = "403", description = "Access denied - Admin role required"),
        @ApiResponse(responseCode = "409", description = "Username or email already exists")
    })
    public ResponseEntity<UserResponse> register(@Valid @RequestBody UserCreateRequest request) {
        UserResponse response = authService.registerUser(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}