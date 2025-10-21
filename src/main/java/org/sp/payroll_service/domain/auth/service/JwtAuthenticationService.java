package org.sp.payroll_service.domain.auth.service;

import lombok.extern.slf4j.Slf4j;
import org.sp.payroll_service.api.auth.dto.AuthResponse;
import org.sp.payroll_service.api.auth.dto.LoginRequest;
import org.sp.payroll_service.api.auth.dto.LogoutRequest;
import org.sp.payroll_service.api.auth.dto.RefreshTokenRequest;
import org.sp.payroll_service.api.auth.dto.UserCreateRequest;
import org.sp.payroll_service.api.auth.dto.UserResponse;
import org.sp.payroll_service.domain.auth.entity.TokenInfo;
import org.sp.payroll_service.domain.auth.entity.User;
import org.sp.payroll_service.domain.common.enums.Role;
import org.sp.payroll_service.domain.common.exception.AuthenticationException;
import org.sp.payroll_service.domain.common.exception.DuplicateEntryException;
import org.sp.payroll_service.domain.auth.entity.UserDetailsImpl;
import org.sp.payroll_service.domain.common.exception.ErrorCodes;
import org.sp.payroll_service.repository.UserRepository;
import org.sp.payroll_service.security.JwtTokenProvider;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;
import java.util.Optional;

/**
 * Service implementation for core user authentication and JWT token management.
 * Handles user login, registration, token generation, validation, and revocation.
 */
@Service
@Transactional
@Slf4j
public class JwtAuthenticationService implements AuthenticationService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider tokenProvider;
    private final TokenInfoService tokenInfoService;

    public JwtAuthenticationService(UserRepository userRepository,
                                    PasswordEncoder passwordEncoder,
                                    JwtTokenProvider tokenProvider,
                                    TokenInfoService tokenInfoService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.tokenProvider = tokenProvider;
        this.tokenInfoService = tokenInfoService;
    }

    /**
     * Authenticates a user, generates a new JWT pair, and persists the refresh token information.
     * @param request The login credentials.
     * @return The AuthResponse containing the new access and refresh tokens and their expiry times in milliseconds.
     * @throws AuthenticationException if credentials are invalid.
     */
    @Override
    public AuthResponse authenticate(LoginRequest request) {
        // Find user
        User user = userRepository.findByUsername(request.username())
                .orElseThrow(() -> new AuthenticationException(
                        "Invalid username or password.",
                        ErrorCodes.AUTH_INVALID_CREDENTIALS)
                );

        // Check password match
        if (!passwordEncoder.matches(request.password(), user.getPasswordHash())) {
            throw new AuthenticationException(
                    "Invalid username or password.",
                    ErrorCodes.AUTH_INVALID_CREDENTIALS
            );
        }

        // Generate token pair
        String jti = UUID.randomUUID().toString();
        String accessToken = tokenProvider.generateAccessToken(user, jti);
        String refreshToken = tokenProvider.generateRefreshToken(user);

        // Store token information. The tokenInfoService calculates expiry based on the tokens/provider config.
        TokenInfo tokenInfo = tokenInfoService.createTokenInfo(user, jti, refreshToken, null, null);

        return new AuthResponse(
                accessToken,
                refreshToken,
                tokenInfo.getAccessExpires().toEpochMilli(),
                tokenInfo.getRefreshExpires().toEpochMilli()
        );
    }

    /**
     * Validates a JWT and loads the corresponding UserDetails object.
     * This method is typically called by the security filter chain.
     * * @param token The JWT string.
     * @return The UserDetails object for the authenticated user.
     * @throws AuthenticationException if the token is invalid, expired, or revoked.
     * @throws UsernameNotFoundException if the user ID from the token is not found.
     */
    @Override
    @Transactional(readOnly = true)
    public UserDetails validateToken(String token) {
        tokenProvider.validateToken(token);

        // Additional validation for access tokens - check if token is revoked via JTI
        if (tokenProvider.validateTokenType(token, "access")) {
            String jti = tokenProvider.getJtiFromToken(token);
            if (jti != null && tokenInfoService.isRevoked(jti)) {
                // If a token's JTI is found as revoked in the database, reject it.
                throw new AuthenticationException("Token has been revoked", ErrorCodes.AUTH_TOKEN_REVOKED);
            }
        }

        UUID userId = tokenProvider.getUserIdFromJWT(token);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> {
                    log.warn("User ID {} from token not found in the database.", userId);
                    return new UsernameNotFoundException("User associated with token not found.");
                });

        return UserDetailsImpl.create(user);
    }

    /**
     * Refreshes an access token using a valid refresh token (implementing token rotation).
     * The old refresh token is revoked, and a new token pair is issued.
     * * @param request The RefreshTokenRequest containing the old refresh token.
     * @return The AuthResponse containing the new access and refresh tokens.
     * @throws AuthenticationException if the refresh token is invalid, expired, or already revoked.
     */
    @Override
    public AuthResponse refreshToken(RefreshTokenRequest request) {
        String oldRefreshToken = request.refreshToken();

        // 1. Validate refresh token type
        if (!tokenProvider.validateTokenType(oldRefreshToken, "refresh")) {
            throw new AuthenticationException("Invalid refresh token type", ErrorCodes.AUTH_INVALID_TOKEN);
        }

        // 2. Validate refresh token in database (checks expiry, revocation, and transactionStatus)
        Optional<TokenInfo> tokenInfoOptional = tokenInfoService.validateRefreshToken(oldRefreshToken);
        TokenInfo oldTokenInfo = tokenInfoOptional
                .orElseThrow(() -> new AuthenticationException("Invalid, expired, or revoked refresh token", ErrorCodes.AUTH_INVALID_TOKEN));

        User user = oldTokenInfo.getUser();

        tokenInfoService.revokeTokenByRefreshToken(oldTokenInfo.getRefreshToken());

        // 4. Generate new token pair
        String jti = UUID.randomUUID().toString();
        String newAccessToken = tokenProvider.generateAccessToken(user, jti);
        String newRefreshToken = tokenProvider.generateRefreshToken(user);

        // 5. Create new TokenInfo record
        TokenInfo newTokenInfo = tokenInfoService.createTokenInfo(user, jti, newRefreshToken, null, null);

        return new AuthResponse(
                newAccessToken,
                newRefreshToken,
                newTokenInfo.getAccessExpires().toEpochMilli(),
                newTokenInfo.getRefreshExpires().toEpochMilli()
        );
    }

    /**
     * Logs out a user by revoking their refresh tokens.
     * @param request The LogoutRequest, specifying the token to revoke or all tokens for the user.
     * @throws AuthenticationException if the provided refresh token is not found or is already invalid.
     */
    @Override
    public void logout(LogoutRequest request) {
        String refreshToken = request.refreshToken();

        Optional<TokenInfo> tokenInfoOptional = tokenInfoService.findByRefreshToken(refreshToken);

        TokenInfo tokenInfo = tokenInfoOptional
                .orElseThrow(() -> new AuthenticationException("Invalid refresh token provided for logout.", ErrorCodes.AUTH_INVALID_TOKEN));

        if (request.logoutFromAllDevices()) {
            // Revoke all tokens for the user, regardless of expiry transactionStatus
            tokenInfoService.revokeAllTokensForUser(tokenInfo.getUser());
        } else {
            // Revoke only this specific token
            tokenInfoService.revokeTokenByRefreshToken(tokenInfo.getRefreshToken());
        }
    }

    /**
     * Registers a new user with a default role of EMPLOYER.
     * @param request The details for the new user.
     * @return The UserResponse DTO of the newly created user.
     * @throws DuplicateEntryException if the username or email already exists.
     */
    @Override
    @Transactional
    public UserResponse registerUser(UserCreateRequest request) {
        if (userRepository.existsByUsernameIgnoreCase(request.username())) {
            throw DuplicateEntryException.forEntity("User", "username", request.username());
        }
        if (userRepository.existsByEmailIgnoreCase(request.email())) {
            throw DuplicateEntryException.forEntity("User", "email", request.email());
        }

        User user = new User();
        user.setUsername(request.username());
        user.setEmail(request.email());
        user.setPasswordHash(passwordEncoder.encode(request.password()));
        // Set default role if none is provided
        user.setRole(request.role() != null ? request.role() : Role.EMPLOYER);

        user = userRepository.save(user);

        return new UserResponse(
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                user.getRole(),
                user.getCreatedAt()
        );
    }
}
