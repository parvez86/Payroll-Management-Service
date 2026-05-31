package org.sp.payroll_service.domain.common.enums;

/**
 * Enumeration for UI theme preferences.
 */
public enum Theme {
    /**
     * Light mode theme
     */
    LIGHT("light", "Light Mode"),
    
    /**
     * Dark mode theme
     */
    DARK("dark", "Dark Mode"),
    
    /**
     * System default theme (respects OS preferences)
     */
    SYSTEM("system", "System Default");
    
    private final String value;
    private final String label;
    
    Theme(String value, String label) {
        this.value = value;
        this.label = label;
    }
    
    public String getValue() {
        return value;
    }
    
    public String getLabel() {
        return label;
    }
}
