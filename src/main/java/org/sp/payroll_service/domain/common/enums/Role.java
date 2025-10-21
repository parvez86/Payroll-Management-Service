package org.sp.payroll_service.domain.common.enums;

/**
 * User roles in the payroll system.
 */
public enum Role {
    ADMIN,
    EMPLOYER,
    EMPLOYEE;

    private static final String ROLE_PREFIX = "ROLE_";

    /**
     * Converts a string representation (e.g., "ADMIN", "ROLE_EMPLOYEE") into the corresponding Role enum constant.
     * The method is case-insensitive and handles the standard Spring Security "ROLE_" prefix.
     *
     * @param roleString The string representation of the role.
     * @return The matching Role enum constant.
     * @throws IllegalArgumentException if the string does not match any defined role.
     */
    public static Role fromString(String roleString) {
        if (roleString == null || roleString.trim().isEmpty()) {
            throw new IllegalArgumentException("Role string cannot be null or empty.");
        }

        String cleanedString = roleString.toUpperCase();

        // Remove the ROLE_ prefix if present
        if (cleanedString.startsWith(ROLE_PREFIX)) {
            cleanedString = cleanedString.substring(ROLE_PREFIX.length());
        }

        // Use valueOf to find the enum constant
        try {
            return Role.valueOf(cleanedString);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid role string: " + roleString, e);
        }
    }

    public static String getRoleNameWithPrefix(Role role) {
        return ROLE_PREFIX + role.name();
    }
}
