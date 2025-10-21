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

        // Check if authentication is already present (e.g., from a previous filter in the chain)
        if (SecurityContextHolder.getContext().getAuthentication() != null) {
            log.debug("AUTH ALREADY SET: Skipping delegate check for {}", request.getRequestURI());
            filterChain.doFilter(request, response);
            return;
        }

        try {
            Authentication authentication = null;

            // 1. Iterate through delegates and attempt authentication
            for (AuthenticationDelegate delegate : delegates) {
                authentication = delegate.attemptAuthentication(request);
                if (authentication != null) {
                    log.debug("✅ DELEGATE AUTH SUCCESS: Authentication set by {}", delegate.getClass().getSimpleName());
                    break;
                }
            }

            // 2. Set context if authentication was successful
            if (authentication != null) {
                SecurityContextHolder.getContext().setAuthentication(authentication);
            }
            // 3. If authentication is still null, Spring Security will catch the failure later

        } catch (Exception ex) {
            // Log critical errors (like InvalidTokenException, UserNotFound, or database errors)
            // The exception itself will eventually be caught by the EntryPoint/AccessDeniedHandler.
            log.warn("❌ AUTH DELEGATION FAILED: {} - {}", request.getRequestURI(), ex.getMessage());
        }

        filterChain.doFilter(request, response);
    }
}