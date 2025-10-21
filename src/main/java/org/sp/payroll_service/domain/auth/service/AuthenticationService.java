package org.sp.payroll_service.domain.auth.service;

import jakarta.validation.Valid;
import org.sp.payroll_service.api.auth.dto.AuthResponse;
import org.sp.payroll_service.api.auth.dto.LoginRequest;
import org.sp.payroll_service.api.auth.dto.LogoutRequest;
import org.sp.payroll_service.api.auth.dto.RefreshTokenRequest;
import org.sp.payroll_service.api.auth.dto.UserCreateRequest;
import org.sp.payroll_service.api.auth.dto.UserResponse;
import org.springframework.security.core.userdetails.UserDetails;

/**
 * Authentication service contract.
 */
public interface AuthenticationService {
    /**
     * Authenticates user credentials and generates JWT token pair.
     * @param request login credentials
     * @return authentication response with access and refresh tokens
     * @throws org.sp.payroll_service.domain.common.exception.AuthenticationException if credentials are invalid
     */
    AuthResponse authenticate(LoginRequest request);
    
    /**
     * Validates JWT token and extracts user information.
     * @param token JWT token string
     * @return user details if token is valid
     * @throws org.sp.payroll_service.domain.common.exception.InvalidTokenException if token is invalid or expired
     */
    UserDetails validateToken(String token);

    /**
     * Refreshes an access token using a valid refresh token.
     * @param request refresh token request
     * @return new token pair
     * @throws org.sp.payroll_service.domain.common.exception.AuthenticationException if refresh token is invalid
     */
    AuthResponse refreshToken(RefreshTokenRequest request);

    /**
     * Logs out a user by revoking their tokens.
     * @param request logout request with refresh token and options
     */
    void logout(LogoutRequest request);

    /**
     * Registers a new user.
     * @param request user registration details
     * @return user response with created user information
     */
    UserResponse registerUser(@Valid UserCreateRequest request);
}