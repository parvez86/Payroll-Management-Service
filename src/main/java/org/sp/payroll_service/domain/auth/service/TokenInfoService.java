package org.sp.payroll_service.domain.auth.service;

import org.sp.payroll_service.domain.auth.entity.TokenInfo;
import org.sp.payroll_service.domain.auth.entity.User;

import java.util.List;
import java.util.Optional;

/**
 * Service interface for managing JWT token lifecycle.
 * Handles token creation, validation, refresh, and revocation.
 */
public interface TokenInfoService {

    /**
     * Create and store new token pair for user
     * @param user The user for whom to create tokens
     * @param accessJti Access token JTI
     * @param refreshToken Refresh token string
     * @param deviceInfo Device information
     * @param ipAddress Client IP address
     * @return Created TokenInfo entity
     */
    TokenInfo createTokenInfo(User user, String accessJti, String refreshToken, String deviceInfo, String ipAddress);

    /**
     * Find token info by refresh token
     * @param refreshToken Refresh token string
     * @return Optional TokenInfo
     */
    Optional<TokenInfo> findByRefreshToken(String refreshToken);

    /**
     * Find token info by access JTI
     * @param accessJti Access token JTI
     * @return Optional TokenInfo
     */
    Optional<TokenInfo> findByAccessJti(String accessJti);

    /**
     * Validate refresh token and return token info if valid
     * @param refreshToken Refresh token string
     * @return Optional TokenInfo if token is valid
     */
    Optional<TokenInfo> validateRefreshToken(String refreshToken);

    /**
     * Revoke all tokens for a user (logout from all devices)
     * @param user The user whose tokens to revoke
     * @return Number of tokens revoked
     */
    int revokeAllTokensForUser(User user);

    /**
     * Revoke specific token by refresh token
     * @param refreshToken Refresh token to revoke
     * @return Number of tokens revoked (0 or 1)
     */
    int revokeTokenByRefreshToken(String refreshToken);

    /**
     * Revoke specific token by access JTI
     * @param accessJti Access JTI to revoke
     * @return Number of tokens revoked (0 or 1)
     */
    int revokeTokenByAccessJti(String accessJti);

    /**
     * Update token info with new access token details
     * @param tokenInfo Existing token info
     * @param newAccessJti New access token JTI
     * @return Updated TokenInfo
     */
    TokenInfo updateAccessToken(TokenInfo tokenInfo, String newAccessJti);

    /**
     * Get all active tokens for a user
     * @param user The user
     * @return List of active tokens
     */
    List<TokenInfo> getActiveTokensForUser(User user);

    /**
     * Clean up expired tokens (maintenance operation)
     * @return Number of tokens deleted
     */
    int cleanupExpiredTokens();

    /**
     * Check if user has too many active sessions
     * @param user The user
     * @param maxSessions Maximum allowed sessions
     * @return True if user has too many sessions
     */
    boolean hasExceededMaxSessions(User user, int maxSessions);

    Optional<TokenInfo> findValidRefreshToken(String refreshToken);

    boolean isRevoked(String jti);
}