package org.sp.payroll_service.security.annotation;

import io.swagger.v3.oas.annotations.Parameter;
import org.sp.payroll_service.domain.common.annotation.HeaderPrincipal;
import java.lang.annotation.*;

/**
 * Custom meta-annotation to inject the current authenticated user's details 
 * (HeaderResponse) into a controller method parameter.
 * Best Practice: It includes 'hidden = true' to prevent this security detail
 * from appearing in Swagger/OpenAPI documentation.
 */
@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Parameter(hidden = true) // Hides the parameter from API documentation (Best Practice)
@HeaderPrincipal // Your existing custom annotation that triggers the ArgumentResolver
public @interface CurrentUser {}
