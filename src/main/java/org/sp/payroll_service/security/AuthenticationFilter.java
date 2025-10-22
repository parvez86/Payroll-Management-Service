package org.sp.payroll_service.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.sp.payroll_service.domain.auth.entity.User;
import org.sp.payroll_service.domain.auth.entity.UserDetailsImpl;
import org.sp.payroll_service.domain.common.exception.InvalidTokenException;
import org.sp.payroll_service.repository.UserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

/**
 * 🔐 CLEAR AUTHENTICATION FILTER FLOW
 * 
 * Simple, single-purpose filter that:
 * 1. Identifies public endpoints (skip JWT)
 * 2. Extracts and validates JWT for protected endpoints
 * 3. Sets authentication context
 * 
 * Context Path: /pms (handled by Spring, not visible to this filter)
 * This filter sees: /api/v1/auth/login, /v1/api/swagger-ui/index.html, etc.
 */
@Component
@Slf4j
public class AuthenticationFilter extends OncePerRequestFilter {

    private final List<AuthenticationDelegate> delegates;
    private final AntPathMatcher pathMatcher = new AntPathMatcher();

    // ✅ SINGLE SOURCE OF TRUTH (defined here for self-containment)
    private static final List<String> PUBLIC_ENDPOINTS = Arrays.asList(
            "/api/v1/auth/login",
            "/api/v1/auth/register",
            "/api/v1/auth/refresh",
            "/api/v1/actuator/**",
            "/api/v1/health",
            "/v3/api-docs/**",
            "/swagger-ui/**",
            "/swagger-resources/**",
            "/webjars/**"
    );

    /**
     * Constructor for dependency injection. Spring automatically collects all beans
     * that implement {@code AuthenticationDelegate} and injects them as a list.
     *
     * @param delegates A list of all authentication delegates (e.g., JwtAuthenticationDelegate).
     */
    public AuthenticationFilter(List<AuthenticationDelegate> delegates) {
        this.delegates = delegates;
    }

    /**
     * Standard method to bypass filter logic for public paths using robust Ant-style matching.
     *
     * @param request The current HTTP request.
     * @return {@code true} if the filter should be skipped (URI is public).
     */
    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String requestUri = request.getRequestURI();

        boolean shouldSkip = PUBLIC_ENDPOINTS.stream()
                .anyMatch(pattern -> pathMatcher.match(pattern, requestUri));

        if (shouldSkip) {
            log.debug("✅ AUTH FILTER SKIP: URI {} matches public pattern.", requestUri);
        }
        return shouldSkip;
    }

    /**
     * The core filter logic for protected endpoints. It iterates through all delegates
     * until one successfully authenticates the request.
     */
    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request,
                                    @NonNull HttpServletResponse response,
                                    @NonNull FilterChain filterChain) throws ServletException, IOException {

        log.error("🔍 [MAIN-FILTER-DEBUG] URI: {}, Method: {}", request.getRequestURI(), request.getMethod());

        // Check if authentication is already present (e.g., from a previous filter in the chain)
        if (SecurityContextHolder.getContext().getAuthentication() != null) {
            log.error("🔍 [MAIN-FILTER-DEBUG] ✅ AUTHENTICATION ALREADY SET - Skipping");
            filterChain.doFilter(request, response);
            return;
        }

        try {
            Authentication authentication = null;
            boolean authenticationAttempted = false;

            // 1. Iterate through delegates and attempt authentication
            for (AuthenticationDelegate delegate : delegates) {
                try {
                    log.error("🔍 [MAIN-FILTER-DEBUG] 🔄 Trying delegate: {}", delegate.getClass().getSimpleName());
                    authentication = delegate.attemptAuthentication(request);
                    
                    if (authentication != null) {
                        log.error("🔍 [MAIN-FILTER-DEBUG] ✅ DELEGATE SUCCESS: {} authenticated user", delegate.getClass().getSimpleName());
                        authenticationAttempted = true;
                        break;
                    } else {
                        log.error("🔍 [MAIN-FILTER-DEBUG] 🔄 DELEGATE RETURNED NULL: {}", delegate.getClass().getSimpleName());
                        // If JWT was present but returned null, consider this an authentication attempt
                        if (delegate.getClass().getSimpleName().contains("Jwt")) {
                            String authHeader = request.getHeader("Authorization");
                            if (authHeader != null && authHeader.startsWith("Bearer ")) {
                                authenticationAttempted = true;
                                log.error("🔍 [MAIN-FILTER-DEBUG] ❌ JWT DELEGATE FAILED WITH TOKEN PRESENT");
                            }
                        }
                    }
                } catch (Exception delegateEx) {
                    // Log the specific delegate that failed and continue to next delegate
                    log.error("� [MAIN-FILTER-DEBUG] ❌ DELEGATE EXCEPTION: {} failed - {}", 
                            delegate.getClass().getSimpleName(), delegateEx.getMessage());
                    authenticationAttempted = true;
                    // Don't break - let other delegates try, but mark as attempted
                }
            }

            // 2. Set context if authentication was successful
            if (authentication != null) {
                SecurityContextHolder.getContext().setAuthentication(authentication);
                log.error("� [MAIN-FILTER-DEBUG] ✅ AUTHENTICATION SET: User {} authenticated", authentication.getName());
            } else {
                log.error("🔍 [MAIN-FILTER-DEBUG] ❌ NO AUTHENTICATION: All delegates failed");
                
                // If authentication was attempted but failed (especially with JWT), don't continue
                if (authenticationAttempted) {
                    log.error("🔍 [MAIN-FILTER-DEBUG] ❌ AUTHENTICATION ATTEMPTED BUT FAILED - BLOCKING REQUEST");
                    response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                    response.getWriter().write("{\"error\":\"Authentication failed\",\"message\":\"Invalid or expired token\"}");
                    response.setContentType("application/json");
                    log.error("🔍 [MAIN-FILTER-DEBUG] === MAIN FILTER END (401 SENT) ===");
                    return;
                }
            }

        } catch (Exception ex) {
            // Log critical unexpected errors
            log.error("❌ AUTH FILTER CRITICAL ERROR: {} - {}", request.getRequestURI(), ex.getMessage(), ex);
        }

        log.error("🔍 [MAIN-FILTER-DEBUG] === MAIN FILTER END (CONTINUING TO NEXT FILTER) ===");
        filterChain.doFilter(request, response);
    }
}