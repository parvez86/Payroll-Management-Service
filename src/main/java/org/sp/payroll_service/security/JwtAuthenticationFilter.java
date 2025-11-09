package org.sp.payroll_service.security;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.sp.payroll_service.domain.auth.entity.User;
import org.sp.payroll_service.domain.auth.entity.UserDetailsImpl;
import org.sp.payroll_service.domain.common.enums.EntityStatus;
import org.sp.payroll_service.domain.common.exception.InvalidTokenException;
import org.sp.payroll_service.repository.TokenInfoRepository;
import org.sp.payroll_service.repository.UserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import java.util.UUID;

/**
 * Custom filter executed once per request to process the JWT from the Authorization header.
 * This filter directly uses JwtTokenProvider and UserRepository to avoid circular dependencies.
 */
@Component
@Slf4j
public class JwtAuthenticationFilter implements AuthenticationDelegate {

    private final JwtTokenProvider tokenProvider;
    private final UserRepository userRepository;
    private final TokenInfoRepository tokenInfoRepository;

    @Value("${app.jwt.header-prefix:Bearer }")
    private String tokenPrefix;

    /**
     * Constructor for dependency injection.
     * @param tokenProvider The service for JWT processing.
     * @param userRepository The repository for fetching user data.
     */
    public JwtAuthenticationFilter(JwtTokenProvider tokenProvider, UserRepository userRepository, TokenInfoRepository tokenInfoRepository) {
        this.tokenProvider = tokenProvider;
        this.userRepository = userRepository;
        this.tokenInfoRepository = tokenInfoRepository;
    }

    /**
     * Attempts authentication by looking for a JWT in the Authorization header.
     *
     * @param request The current HTTP request.
     * @return A valid {@code Authentication} token if the JWT is present and valid, null otherwise.
     */
    @Override
    public Authentication attemptAuthentication(HttpServletRequest request) throws Exception {
        String requestURI = request.getRequestURI();
        String method = request.getMethod();
        
        log.error("🔍 [JWT-DEBUG] === AUTHENTICATION ATTEMPT START ===");
        log.error("🔍 [JWT-DEBUG] Method: {} URI: {}", method, requestURI);
        log.error("🔍 [JWT-DEBUG] Headers: Authorization = {}", request.getHeader("Authorization"));
        
        String jwt = extractJwt(request);
        if (!StringUtils.hasText(jwt)) {
            // No JWT found, this delegate cannot authenticate. Return null to let others try.
            return null;
        }

        log.debug("🎫 JWT DELEGATE: Token found. Validating and processing.");

        try {
            // 1. Validate token (will throw InvalidTokenException on failure)
            if (!tokenProvider.validateTokenType(jwt, "access")) {
                throw new InvalidTokenException("Invalid access token type");
            }

            String jti = tokenProvider.getJtiFromToken(jwt);

            if (!StringUtils.hasText(jti)) {
                log.error("🔍 [JWT-DEBUG] ❌ TOKEN REJECTED: JTI claim is missing.");
                throw new InvalidTokenException("Token is missing JTI claim; cannot be tracked.");
            }

            if (tokenInfoRepository.existsByAccessJtiAndIsRevokedTrue(jti)) {
                throw new InvalidTokenException("Token is revoked");
            }
            log.error("🔍 [JWT-DEBUG] ✅ TOKEN VALIDATION SUCCESSFUL");

            log.error("🔍 [JWT-DEBUG] 🆔 Extracting user ID from token...");
            // 2. Extract user ID and load user from database
            UUID userId = tokenProvider.getUserIdFromJWT(jwt);
            log.error("🔍 [JWT-DEBUG] ✅ USER ID EXTRACTED: {}", userId);
            
            log.error("🔍 [JWT-DEBUG] 🔍 Looking up user in database...");
            User user = userRepository.findByIdAndStatus(userId, EntityStatus.ACTIVE)
                    .orElseThrow(() -> {
                        log.error("🔍 [JWT-DEBUG] ❌ USER NOT FOUND: User ID {} from token not found in database", userId);
                        return new UsernameNotFoundException("User not found: " + userId);
                    });

            log.error("🔍 [JWT-DEBUG] ✅ USER FOUND: {} with role: {}", user.getUsername(), user.getRole());

            // 3. Create UserDetails and Authentication object
            UserDetails userDetails = UserDetailsImpl.create(user);
            UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                    userDetails, null, userDetails.getAuthorities());

            authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

            log.error("� [JWT-DEBUG] ✅ AUTHENTICATION SUCCESSFUL for user: {}", user.getUsername());
            return authentication;
            
        } catch (InvalidTokenException ex) {
            // Invalid/expired token - log and return null (let other delegates try or fail at Spring Security level)
            log.error("� [JWT-DEBUG] ❌ TOKEN VALIDATION FAILED: {}", ex.getMessage());
            return null;
        } catch (UsernameNotFoundException ex) {
            // User not found - this is a more serious issue, rethrow
            log.error("🔍 [JWT-DEBUG] ❌ USER NOT FOUND: {}", ex.getMessage());
            throw ex;
        }
    }

    /**
     * Extracts the JWT from the Authorization header (e.g., "Bearer eyJ...").
     */
    private String extractJwt(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith(tokenPrefix)) {
            return bearerToken.substring(tokenPrefix.length());
        }
        return null;
    }
}