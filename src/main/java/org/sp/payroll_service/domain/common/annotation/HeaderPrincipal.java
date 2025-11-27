package org.sp.payroll_service.domain.common.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Custom annotation to retrieve the authenticated user's principal data (HeaderResponse DTO)
 * directly from the Spring Security Context and inject it into a controller method argument.
 */
@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
public @interface HeaderPrincipal {
    // Value is true if the principal is required, meaning an unauthenticated call will result in an error.
    boolean required() default true;
}