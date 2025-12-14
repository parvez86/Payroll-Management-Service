package org.sp.payroll_service.security.annotation;

import org.springframework.security.access.prepost.PreAuthorize;

import java.lang.annotation.*;

/**
 * Requires the user to be authenticated. Simple alias for @PreAuthorize("isAuthenticated()").
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Inherited
@Documented
@PreAuthorize("isAuthenticated()")
public @interface IsAuthenticated {
}