package org.sp.payroll_service.api.auth.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
@Slf4j
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
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request, HttpServletRequest httpRequest) {
        log.info("========== LOGIN REQUEST START ==========");
        log.info("Login attempt for username: {}", request.username());
        log.info("Request IP: {}", getClientIpAddress(httpRequest));
        log.info("Request User-Agent: {}", httpRequest.getHeader("User-Agent"));
        log.info("Request Content-Type: {}", httpRequest.getContentType());
        log.debug("Full request details: {}", request);
        
        try {
            log.info("Calling AuthenticationService.authenticate()...");
            AuthResponse response = authService.authenticate(request);
            
            log.info("Authentication successful for user: {}", request.username());
            log.debug("Generated access token length: {}", response.accessToken() != null ? response.accessToken().length() : 0);
            log.debug("Generated refresh token length: {}", response.refreshToken() != null ? response.refreshToken().length() : 0);
            log.info("Token type: {}, expires in: {} seconds", response.tokenType(), response.expiresIn());
            log.info("========== LOGIN REQUEST SUCCESS ==========");
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("========== LOGIN REQUEST FAILED ==========");
            log.error("Authentication failed for username: {}", request.username());
            log.error("Error type: {}", e.getClass().getSimpleName());
            log.error("Error message: {}", e.getMessage());
            log.error("Full stack trace:", e);
            log.error("========== LOGIN REQUEST END ==========");
            throw e;
        }
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
        log.info("Token refresh request received");
        log.debug("Refresh token length: {}", request.refreshToken() != null ? request.refreshToken().length() : 0);
        
        try {
            AuthResponse response = authService.refreshToken(request);
            log.info("Token refresh successful");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Token refresh failed: {}", e.getMessage());
            throw e;
        }
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
        log.info("Logout request received");
        
        try {
            authService.logout(request);
            log.info("Logout successful");
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            log.error("Logout failed: {}", e.getMessage());
            throw e;
        }
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
        log.info("User registration request for username: {}", request.username());
        try {
            UserResponse response = authService.registerUser(request);
            log.info("User registration successful for: {}", request.username());
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (Exception e) {
            log.error("User registration failed for: {} - Error: {}", request.username(), e.getMessage());
            throw e;
        }
    }
    
    /**
     * Helper method to extract client IP address from request
     */
    private String getClientIpAddress(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0].trim();
        }
        
        String xRealIP = request.getHeader("X-Real-IP");
        if (xRealIP != null && !xRealIP.isEmpty()) {
            return xRealIP;
        }
        
        return request.getRemoteAddr();
    }
}