package org.sp.payroll_service.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.sp.payroll_service.domain.auth.entity.User;
import org.sp.payroll_service.domain.auth.entity.UserDetailsImpl;
import org.sp.payroll_service.domain.common.exception.InvalidTokenException;
import org.sp.payroll_service.repository.UserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.UUID;

/**
 * Custom filter executed once per request to process the JWT from the Authorization header.
 * This filter directly uses JwtTokenProvider and UserRepository to avoid circular dependencies.
 */
@Component
@Slf4j
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtTokenProvider tokenProvider;
    private final UserRepository userRepository;

    @Value("${app.jwt.header-prefix:Bearer }")
    private String tokenPrefix;

    // Constructor injection - removed AuthenticationService to break circular dependency
    public JwtAuthenticationFilter(JwtTokenProvider tokenProvider, UserRepository userRepository) {
        this.tokenProvider = tokenProvider;
        this.userRepository = userRepository;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        
        // Skip JWT processing for public endpoints
        if (isPublicEndpoint(request)) {
            filterChain.doFilter(request, response);
            return;
        }
        
        try {
            String jwt = getJwtFromRequest(request);

            if (StringUtils.hasText(jwt)) {
                
                // 1. Validate token directly using JwtTokenProvider
                tokenProvider.validateToken(jwt);
                
                // 2. Extract user ID from token
                UUID userId = tokenProvider.getUserIdFromJWT(jwt);
                
                // 3. Load user from database and create UserDetails
                User user = userRepository.findById(userId)
                    .orElseThrow(() -> {
                        log.warn("User ID {} from token not found in the database.", userId);
                        return new UsernameNotFoundException("User associated with token not found.");
                    });
                
                UserDetails userDetails = UserDetailsImpl.create(user);
                
                // 4. Create the authentication object
                UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                    userDetails, 
                    null, // Null credentials since we are using token
                    userDetails.getAuthorities()
                );
                
                // 5. Attach request details and set the context
                authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(authentication);
            }
        } catch (InvalidTokenException ex) {
            // Log the token failure but DO NOT throw or handle here.
            // Let the chain continue so the AccessDeniedHandler/EntryPoint can catch it later.
            log.debug("JWT validation failed: {}", ex.getMessage());
        } catch (Exception ex) {
            // General failure (e.g., database lookup failed)
            log.error("Could not set user authentication in security context: {}", ex.getMessage(), ex);
        }

        filterChain.doFilter(request, response);
    }

    /**
     * Extracts the JWT from the Authorization header (e.g., "Bearer eyJ...").
     */
    private String getJwtFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith(tokenPrefix)) {
            return bearerToken.substring(tokenPrefix.length());
        }
        return null;
    }
    
    /**
     * Checks if the request is for a public endpoint that doesn't require authentication.
     */
    private boolean isPublicEndpoint(HttpServletRequest request) {
        String rawPath = request.getRequestURI();
        if (rawPath == null) return false;

        // Normalize path: lowercase and remove duplicate slashes
        String path = rawPath.toLowerCase().replaceAll("//+", "/");

        // Remove common API context prefixes so we can match both /pms/v1/api/swagger-ui and /swagger-ui
        String[] contextPrefixes = {"/pms/v1/api", "/api", ""};

        // Variants to match (includes common typos like 'swaggger')
        String[] swaggerVariants = {"swagger-ui", "swaggger-ui", "swaggerui", "swagger-ui.html"};

        // Public auth endpoints
        String[] authEndpoints = {"/auth/login", "/auth/register", "/auth/refresh"};

        // Other public paths
        String[] otherPublics = {"/v3/api-docs", "/swagger-resources", "/webjars", "/h2-console", "/actuator/health"};

        for (String prefix : contextPrefixes) {
            String base = prefix == null || prefix.isEmpty() ? "" : prefix;

            // Check auth endpoints
            for (String auth : authEndpoints) {
                String candidate = base + auth;
                if (path.equals(candidate) || path.startsWith(candidate + "/")) return true;
            }

            // Check swagger variants (contains because UI may be at /swagger-ui/index.html or /swagger-ui/)
            for (String sw : swaggerVariants) {
                String candidate = base + "/" + sw;
                if (path.equals(candidate) || path.startsWith(candidate) || path.contains("/" + sw + "/") || path.endsWith(sw)) return true;
            }

            // Other publics
            for (String other : otherPublics) {
                String candidate = base + other;
                if (path.equals(candidate) || path.startsWith(candidate + "/") || path.startsWith(candidate)) return true;
            }
        }

        return false;
    }
}