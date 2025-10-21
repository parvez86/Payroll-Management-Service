package org.sp.payroll_service.repository;

import org.sp.payroll_service.domain.auth.entity.TokenInfo;
import org.sp.payroll_service.domain.auth.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository interface for TokenInfo entity operations.
 * Provides methods for token lifecycle management and cleanup.
 */
@Repository
public interface TokenInfoRepository extends JpaRepository<TokenInfo, UUID> {

    /**
     * Find token info by refresh token
     */
    Optional<TokenInfo> findByRefreshToken(String refreshToken);

    /**
     * Find token info by access JTI
     */
    Optional<TokenInfo> findByAccessJti(String accessJti);

    /**
     * Find all active tokens for a user
     */
    @Query("SELECT t FROM TokenInfo t WHERE t.user = :user AND t.isRevoked = false")
    List<TokenInfo> findActiveTokensByUser(@Param("user") User user);

    /**
     * Find all tokens for a user (active and revoked)
     */
    List<TokenInfo> findByUser(User user);

    /**
     * Check if refresh token exists and is valid
     */
    @Query("SELECT t FROM TokenInfo t WHERE t.refreshToken = :refreshToken AND t.isRevoked = false AND t.refreshExpires > :now")
    Optional<TokenInfo> findValidRefreshToken(@Param("refreshToken") String refreshToken, @Param("now") Instant now);

    /**
     * Revoke all tokens for a user
     */
    @Modifying
    @Query("UPDATE TokenInfo t SET t.isRevoked = true WHERE t.user = :user")
    int revokeAllTokensForUser(@Param("user") User user);

    /**
     * Revoke specific token by refresh token
     */
    @Modifying
    @Query("UPDATE TokenInfo t SET t.isRevoked = true WHERE t.refreshToken = :refreshToken")
    int revokeTokenByRefreshToken(@Param("refreshToken") String refreshToken);

    /**
     * Revoke specific token by access JTI
     */
    @Modifying
    @Query("UPDATE TokenInfo t SET t.isRevoked = true WHERE t.accessJti = :accessJti")
    int revokeTokenByAccessJti(@Param("accessJti") String accessJti);

    /**
     * Clean up expired tokens
     */
    @Modifying
    @Query("DELETE FROM TokenInfo t WHERE t.refreshExpires < :now")
    int deleteExpiredTokens(@Param("now") Instant now);

    /**
     * Count active tokens for a user
     */
    @Query("SELECT COUNT(t) FROM TokenInfo t WHERE t.user = :user AND t.isRevoked = false AND t.refreshExpires > :now")
    long countActiveTokensByUser(@Param("user") User user, @Param("now") Instant now);

    /**
     * Find tokens expiring soon for cleanup
     */
    @Query("SELECT t FROM TokenInfo t WHERE t.refreshExpires BETWEEN :now AND :threshold AND t.isRevoked = false")
    List<TokenInfo> findTokensExpiringSoon(@Param("now") Instant now, @Param("threshold") Instant threshold);

    Optional<TokenInfo> findByAccessJtiAndIsRevokedFalseAndAccessExpiresAfter(String jti, Instant now);

}