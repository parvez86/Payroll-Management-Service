package org.sp.payroll_service.security.annotation;

import org.springframework.security.access.prepost.PreAuthorize;
import java.lang.annotation.*;

/**
 * Requires the user to have the ADMIN role.
 * Assumes roles are prefixed (e.g., 'ROLE_ADMIN') in the Spring Security context,
 * or that your expression resolver handles the role names directly.
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Inherited
@Documented
@PreAuthorize("hasRole('ADMIN')")
public @interface IsAdmin {}
