package org.sp.payroll_service.domain.auth.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.sp.payroll_service.domain.auth.entity.TokenInfo;
import org.sp.payroll_service.domain.auth.entity.User;
import org.sp.payroll_service.domain.auth.service.TokenInfoService;
import org.sp.payroll_service.repository.TokenInfoRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Implementation of TokenInfoService for managing JWT token lifecycle.
 */
@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class TokenInfoServiceImpl implements TokenInfoService {

    private final TokenInfoRepository tokenInfoRepository;

    @Value("${app.jwt.expiration:86400000}") // 24 hours in milliseconds
    private long accessTokenValidityMs;

    @Value("${app.jwt.refresh-expiration:604800000}") // 7 days in milliseconds  
    private long refreshTokenValidityMs;

    @Override
    public TokenInfo createTokenInfo(User user, String accessJti, String refreshToken, String deviceInfo, String ipAddress) {
        log.debug("Creating token info for user: {}", user.getUsername());

        Instant now = Instant.now();
        Instant accessExpires = now.plusSeconds(accessTokenValidityMs / 1000);
        Instant refreshExpires = now.plusSeconds(refreshTokenValidityMs / 1000);

        TokenInfo tokenInfo = TokenInfo.builder()
                .user(user)
                .accessJti(accessJti)
                .refreshToken(refreshToken)
                .accessExpires(accessExpires)
                .refreshExpires(refreshExpires)
                .isRevoked(false)
                .deviceInfo(deviceInfo)
                .ipAddress(ipAddress)
                .build();

        TokenInfo saved = tokenInfoRepository.save(tokenInfo);
        log.debug("Token info created with ID: {}", saved.getId());
        return saved;
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<TokenInfo> findByRefreshToken(String refreshToken) {
        return tokenInfoRepository.findByRefreshToken(refreshToken);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<TokenInfo> findByAccessJti(String accessJti) {
        return tokenInfoRepository.findByAccessJti(accessJti);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<TokenInfo> validateRefreshToken(String refreshToken) {
        return tokenInfoRepository.findValidRefreshToken(refreshToken, Instant.now());
    }

    @Override
    public int revokeAllTokensForUser(User user) {
        log.info("Revoking all tokens for user: {}", user.getUsername());
        return tokenInfoRepository.revokeAllTokensForUser(user);
    }

    @Override
    public int revokeTokenByRefreshToken(String refreshToken) {
        log.debug("Revoking token by refresh token");
        return tokenInfoRepository.revokeTokenByRefreshToken(refreshToken);
    }

    @Override
    public int revokeTokenByAccessJti(String accessJti) {
        log.debug("Revoking token by access JTI: {}", accessJti);
        return tokenInfoRepository.revokeTokenByAccessJti(accessJti);
    }

    @Override
    public TokenInfo updateAccessToken(TokenInfo tokenInfo, String newAccessJti) {
        log.debug("Updating access token for token info: {}", tokenInfo.getId());

        Instant now = Instant.now();
        Instant newAccessExpires = now.plusSeconds(accessTokenValidityMs / 1000);
        
        tokenInfo.setAccessJti(newAccessJti);
        tokenInfo.setAccessExpires(newAccessExpires);
        
        return tokenInfoRepository.save(tokenInfo);
    }

    @Override
    @Transactional(readOnly = true)
    public List<TokenInfo> getActiveTokensForUser(User user) {
        return tokenInfoRepository.findActiveTokensByUser(user);
    }

    @Override
    public int cleanupExpiredTokens() {
        log.info("Cleaning up expired tokens");
        int deletedCount = tokenInfoRepository.deleteExpiredTokens(Instant.now());
        log.info("Deleted {} expired tokens", deletedCount);
        return deletedCount;
    }

    @Override
    @Transactional(readOnly = true)
    public boolean hasExceededMaxSessions(User user, int maxSessions) {
        long activeTokenCount = tokenInfoRepository.countActiveTokensByUser(user, Instant.now());
        return activeTokenCount >= maxSessions;
    }

    @Override
    public Optional<TokenInfo> findValidRefreshToken(String refreshToken) {
        return tokenInfoRepository.findValidRefreshToken(refreshToken, Instant.now());
    }

    /**
     * Checks if a token (identified by its JTI) has been explicitly revoked or has expired
     * based on the database record. Used for access token validation.
     * * @param jti The JWT ID of the access token.
     * @return True if the token is revoked or expired; false otherwise.
     */
    @Transactional(readOnly = true)
    public boolean isRevoked(String jti) {
        return tokenInfoRepository.findByAccessJtiAndIsRevokedFalseAndAccessExpiresAfter(jti, Instant.now())
                .isEmpty();
    }
}