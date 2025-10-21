package org.sp.payroll_service.domain.auth.entity;

import jakarta.persistence.*;
import lombok.*;
import org.sp.payroll_service.domain.common.entity.BaseAuditingEntity;

import java.time.Instant;
import java.util.UUID;

/**
 * Entity representing JWT token information for managing token lifecycle.
 * Stores both access and refresh token details for proper token management.
 */
@Entity
@Table(name = "token_info")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(callSuper = false)
public class TokenInfo extends BaseAuditingEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "id")
    private UUID id;

    /**
     * Reference to the user who owns this token
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    /**
     * Refresh token string for generating new access tokens
     */
    @Column(name = "refresh_token", nullable = false, length = 500)
    private String refreshToken;

    /**
     * Access token JTI (JWT ID) for unique identification
     */
    @Column(name = "access_jti", nullable = false, length = 36)
    private String accessJti;

    /**
     * Access token expiration timestamp
     */
    @Column(name = "access_expires", nullable = false)
    private Instant accessExpires;

    /**
     * Refresh token expiration timestamp
     */
    @Column(name = "refresh_expires", nullable = false)
    private Instant refreshExpires;

    /**
     * Flag indicating if this token pair has been revoked
     */
    @Column(name = "is_revoked", nullable = false)
    @Builder.Default
    private Boolean isRevoked = false;

    /**
     * Device/client information for token tracking
     */
    @Column(name = "device_info", length = 255)
    private String deviceInfo;

    /**
     * IP address from which token was issued
     */
    @Column(name = "ip_address", length = 45)
    private String ipAddress;

    // --- Utility Methods ---

    /**
     * Checks if the access token is expired
     */
    public boolean isAccessTokenExpired() {
        return Instant.now().isAfter(accessExpires);
    }

    /**
     * Checks if the refresh token is expired
     */
    public boolean isRefreshTokenExpired() {
        return Instant.now().isAfter(refreshExpires);
    }

    /**
     * Checks if the token pair is valid (not revoked and not expired)
     */
    public boolean isValid() {
        return !isRevoked && !isRefreshTokenExpired();
    }

    /**
     * Revokes this token pair
     */
    public void revoke() {
        this.isRevoked = true;
    }
}