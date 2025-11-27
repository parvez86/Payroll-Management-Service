package org.sp.payroll_service.utils;

import org.sp.payroll_service.domain.auth.entity.UserDetailsImpl;
import org.sp.payroll_service.domain.common.enums.Role;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.UUID;

/**
 * Utility for extracting JWT user information from SecurityContext.
 * Provides simple static methods to get current user ID, username, and role.
 */
public class JwtUtils {

    private JwtUtils() {
        // Utility class - prevent instantiation
    }

    /**
     * Get current authenticated user's ID from JWT.
     * @return User ID or null if not authenticated
     */
    public static UUID getCurrentUserId() {
        UserDetailsImpl user = getCurrentUser();
        return user != null ? user.getId() : null;
    }

    /**
     * Get current authenticated user's username from JWT.
     * @return Username or null if not authenticated
     */
    public static String getCurrentUsername() {
        UserDetailsImpl user = getCurrentUser();
        return user != null ? user.getUsername() : null;
    }

    /**
     * Get current authenticated user's role from JWT.
     * @return Role or null if not authenticated
     */
    public static Role getCurrentUserRole() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof org.sp.payroll_service.domain.common.dto.response.HeaderResponse) {
            return ((org.sp.payroll_service.domain.common.dto.response.HeaderResponse) authentication.getPrincipal()).role();
        }
        UserDetailsImpl user = getCurrentUser();
        return user != null ? user.getRole() : null;
    }

    /**
     * Check if current user has ADMIN role.
     * @return true if user is admin, false otherwise
     */
    public static boolean isAdmin() {
        return Role.ADMIN.equals(getCurrentUserRole());
    }

    /**
     * Extract UserDetailsImpl from SecurityContext.
     * @return UserDetailsImpl or null if not authenticated
     */
    private static UserDetailsImpl getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof UserDetailsImpl) {
            return (UserDetailsImpl) authentication.getPrincipal();
        }
        return null;
    }
}
