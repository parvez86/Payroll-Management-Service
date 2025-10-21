package org.sp.payroll_service.security;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.sp.payroll_service.domain.auth.entity.User;
import org.sp.payroll_service.domain.auth.entity.UserDetailsImpl;
import org.sp.payroll_service.domain.common.exception.InvalidTokenException;
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

    @Value("${app.jwt.header-prefix:Bearer }")
    private String tokenPrefix;

    /**
     * Constructor for dependency injection.
     * @param tokenProvider The service for JWT processing.
     * @param userRepository The repository for fetching user data.
     */
    public JwtAuthenticationFilter(JwtTokenProvider tokenProvider, UserRepository userRepository) {
        this.tokenProvider = tokenProvider;
        this.userRepository = userRepository;
    }

    /**
     * Attempts authentication by looking for a JWT in the Authorization header.
     *
     * @param request The current HTTP request.
     * @return A valid {@code Authentication} token if the JWT is present and valid.
     * @throws InvalidTokenException if the token is present but invalid/expired.
     * @throws UsernameNotFoundException if the user in the token is not found.
     */
    @Override
    public Authentication attemptAuthentication(HttpServletRequest request) throws Exception {
        String jwt = extractJwt(request);

        if (!StringUtils.hasText(jwt)) {
            // No JWT found, this delegate cannot authenticate. Return null to let others try.
            return null;
        }

        log.debug("🎫 JWT DELEGATE: Token found. Validating and processing.");

        // 1. Validate token (will throw InvalidTokenException on failure)
        tokenProvider.validateToken(jwt);

        // 2. Extract user ID and load user from database
        UUID userId = tokenProvider.getUserIdFromJWT(jwt);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> {
                    log.warn("❌ USER NOT FOUND: User ID {} from token not found in database", userId);
                    return new UsernameNotFoundException("User not found: " + userId);
                });

        // 3. Create UserDetails and Authentication object
        UserDetails userDetails = UserDetailsImpl.create(user);
        UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                userDetails, null, userDetails.getAuthorities());

        authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

        log.debug("🔐 JWT AUTH SUCCESS: User {} authenticated via JWT", user.getUsername());
        return authentication;
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