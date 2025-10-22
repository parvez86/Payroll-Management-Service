package org.sp.payroll_service.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.sp.payroll_service.domain.auth.entity.User;
import org.sp.payroll_service.domain.common.exception.InvalidTokenException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.Set;
import java.util.UUID;

/**
 * Utility class for generating, validating, and extracting data from JWTs.
 * Uses the io.jsonwebtoken (JJWT) library.
 */
@Component
@Slf4j
public class JwtTokenProvider {

    // Inject from application.properties/yml
    @Value("${app.jwt.secret}")
    private String jwtSecret;

    @Value("${app.jwt.expiration}")
    private long jwtExpirationInMs;

    @Value("${app.jwt.refresh-expiration:604800000}") // 7 days in milliseconds
    private long refreshTokenExpirationInMs;

    private SecretKey key;

    /**
     * Retrieves the signing key, creating it from the base64 secret if necessary.
     */
    @PostConstruct
    public void init() {
        this.key = Keys.hmacShaKeyFor(jwtSecret.getBytes());
    }

    public long getExpirationTime() {
        return jwtExpirationInMs;
    }

    public long getRefreshExpirationTime() {
        return refreshTokenExpirationInMs;
    }

    // --- Generation ---

    /**
     * Generates a JWT for a given user.
     * The subject is the user's ID (UUID), and the claims contain the username and roles.
     * @param user The user entity
     * @return The signed JWT string
     */
    public String generateToken(User user) {
        return generateAccessToken(user, UUID.randomUUID().toString());
    }

    /**
     * Generates an access token with a specific JTI for a user.
     * @param user The user entity
     * @param jti JWT ID for unique token identification
     * @return The signed JWT access token string
     */
    public String generateAccessToken(User user, String jti) {
        log.debug("JWT: Generating access token for user: {} with JTI: {}", user.getUsername(), jti);
        
        String userId = user.getId().toString();
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + jwtExpirationInMs);
        
        log.debug("JWT: Token details - UserId: {}, Expiry: {}, Roles: {}", 
                userId, expiryDate, Set.of(user.getRole().name()));

        String token = Jwts.builder()
                .subject(userId)
                .id(jti) // JWT ID for token tracking
                .claim("username", user.getUsername())
                .claim("roles", Set.of(user.getRole().name()))
                .claim("type", "access") // Token type
                .issuedAt(now)
                .expiration(expiryDate)
                .signWith(key)
                .compact();
                
        log.info("JWT: Access token generated successfully for user: {}, length: {}", 
                user.getUsername(), token.length());
        
        return token;
    }

    /**
     * Generates a refresh token for a user.
     * @param user The user entity
     * @return The signed JWT refresh token string
     */
    public String generateRefreshToken(User user) {
        log.debug("JWT: Generating refresh token for user: {}", user.getUsername());
        
        String userId = user.getId().toString();
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + refreshTokenExpirationInMs);
        String refreshJti = UUID.randomUUID().toString();
        
        log.debug("JWT: Refresh token details - UserId: {}, Expiry: {}, JTI: {}", 
                userId, expiryDate, refreshJti);

        String token = Jwts.builder()
                .subject(userId)
                .id(refreshJti) // Unique JTI for refresh token
                .claim("username", user.getUsername())
                .claim("type", "refresh") // Token type
                .issuedAt(now)
                .expiration(expiryDate)
                .signWith(key)
                .compact();
                
        log.info("JWT: Refresh token generated successfully for user: {}, length: {}", 
                user.getUsername(), token.length());
        
        return token;
    }

    // --- Validation and Extraction ---

    /**
     * Extracts the user ID (UUID) from the token's subject claim.
     * @param token The JWT string
     * @return The user's UUID
     * @throws InvalidTokenException if the token is invalid or the subject is malformed.
     */
    public UUID getUserIdFromJWT(String token) {
        Claims claims = getClaims(token);
        String subject = claims.getSubject();
        log.info("subject: {}", subject);
        try {
            return UUID.fromString(subject);
        } catch (IllegalArgumentException e) {
            log.error("JWT Subject is not a valid UUID: {}", subject);
            throw new InvalidTokenException("Subject malformed (not a UUID)");
        }
    }
    
    /**
     * Validates the integrity and expiration of the JWT.
     * @param authToken The JWT string
     * @return true if the token is valid
     * @throws InvalidTokenException if validation fails
     */
    public boolean validateToken(String authToken) {
        try {
            Claims claims = getClaims(authToken);
            return true;
        } catch (ExpiredJwtException ex) {
            log.error("🔍 [JWT-VALIDATION-DEBUG] ❌ EXPIRED JWT TOKEN: {}", ex.getMessage());
            throw new InvalidTokenException("Expired");
        } catch (UnsupportedJwtException ex) {
            log.error("🔍 [JWT-VALIDATION-DEBUG] ❌ UNSUPPORTED JWT TOKEN: {}", ex.getMessage());
            throw new InvalidTokenException("Unsupported format");
        } catch (MalformedJwtException ex) {
            log.error("🔍 [JWT-VALIDATION-DEBUG] ❌ MALFORMED JWT TOKEN: {}", ex.getMessage());
            throw new InvalidTokenException("Malformed");
        } catch (Exception ex) {
            throw new InvalidTokenException("Validation failure during claims extraction", ex);
        }
    }

    /**
     * Extracts the JWT ID (JTI) from the token.
     * @param token The JWT string
     * @return JWT ID if present, null otherwise
     */
    public String getJtiFromToken(String token) {
        try {
            Claims claims = getClaims(token);
            return claims.getId();
        } catch (InvalidTokenException e) {
            log.warn("Failed to extract JTI from token: {}", e.getMessage());
            return null;
        }
    }

    /**
     * Extracts the token type from the JWT claims.
     * @param token The JWT string
     * @return Token type ("access" or "refresh") if present, null otherwise
     */
    public String getTokenType(String token) {
        try {
            Claims claims = getClaims(token);
            return (String) claims.get("type");
        } catch (InvalidTokenException e) {
            log.warn("Failed to extract token type: {}", e.getMessage());
            return null;
        }
    }

    /**
     * Validates if the token is of the expected type.
     * @param token The JWT string
     * @param expectedType The expected token type ("access" or "refresh")
     * @return true if token is valid and of expected type
     */
    public boolean validateTokenType(String token, String expectedType) {
        String tokenType = getTokenType(token);
        return expectedType.equals(tokenType);
    }

    /**
     * Safely retrieves the claims from the JWT, handling all parsing exceptions.
     */
    private Claims getClaims(String token) {
        log.error("🔍 [JWT-CLAIMS-DEBUG] === GET CLAIMS START ===");
        log.error("🔍 [JWT-CLAIMS-DEBUG] Secret key algorithm: {}", key != null ? key.getAlgorithm() : "null");

        if (token == null) {
            log.error("🔍 [JWT-CLAIMS-DEBUG] ❌ Token is null");
            throw new InvalidTokenException("Token is null");
        }

        // Defensive cleaning: trim, remove Bearer prefix and any internal whitespace/newlines
        String cleaned = token.trim();
        if (cleaned.toLowerCase().startsWith("bearer ")) {
            cleaned = cleaned.substring(7);
        }
        // Collapse/remove any whitespace characters that might have been introduced by transport/logging
        cleaned = cleaned.replaceAll("\\s+", "");

        log.error("🔍 [JWT-CLAIMS-DEBUG] Cleaned token first 50 chars: {}", cleaned != null ? cleaned.substring(0, Math.min(50, cleaned.length())) : "null");

        try {
            // Use the parser with verifyWith to parse the compact JWS and get Claims
            Claims claims = Jwts.parser()
                    .verifyWith(key)
                    .build()
                    .parseSignedClaims(cleaned)
                    .getPayload();

            log.error("🔍 [JWT-CLAIMS-DEBUG] ✅ CLAIMS EXTRACTED SUCCESSFULLY");
            return claims;
        } catch (JwtException | IllegalArgumentException ex) {
            log.error("🔍 [JWT-CLAIMS-DEBUG] ❌ CLAIMS EXTRACTION FAILED: {} - {}", ex.getClass().getSimpleName(), ex.getMessage());
            log.error("🔍 [JWT-CLAIMS-DEBUG] ❌ EXCEPTION DETAILS:", ex);
            // Re-throw as a business-specific exception
            throw new InvalidTokenException("Validation failure during claims extraction", ex);
        }
    }
}