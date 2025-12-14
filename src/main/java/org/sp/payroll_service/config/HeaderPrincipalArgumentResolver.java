package org.sp.payroll_service.config;

import org.sp.payroll_service.domain.common.annotation.HeaderPrincipal;
import org.sp.payroll_service.domain.common.dto.response.HeaderResponse;
import org.springframework.core.MethodParameter;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;
import org.sp.payroll_service.domain.auth.entity.UserDetailsImpl;
import org.sp.payroll_service.domain.common.enums.Role;

/**
 * Central utility class that resolves the HeaderResponse DTO for arguments 
 * annotated with @HeaderPrincipal.
 * * It extracts the DTO directly from the Spring Security Context's Authentication principal.
 */
@Component
public class HeaderPrincipalArgumentResolver implements HandlerMethodArgumentResolver {

    /**
     * Determines if this resolver supports the given method parameter.
     * We support parameters that are annotated with @HeaderPrincipal 
     * and are of type HeaderResponse.
     */
    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        // Support any annotation that is or is meta-annotated with @HeaderPrincipal
        boolean hasHeaderPrincipal = org.springframework.core.annotation.AnnotatedElementUtils.hasAnnotation(
            parameter.getParameter(), HeaderPrincipal.class);
        return hasHeaderPrincipal && parameter.getParameterType().equals(HeaderResponse.class);
    }

    /**
     * Resolves the method argument by fetching the principal object from the security context.
     *
     * IMPORTANT ASSUMPTION: The JWT filter/authentication process must have
     * placed the HeaderResponse DTO (or an object that can be cast to it)
     * as the principal in the Authentication object.
     */
    @Override
    public Object resolveArgument(MethodParameter parameter, ModelAndViewContainer mavContainer, 
                                 NativeWebRequest webRequest, WebDataBinderFactory binderFactory) throws Exception {
        
        HeaderPrincipal annotation = parameter.getParameterAnnotation(HeaderPrincipal.class);
        boolean isRequired = annotation != null && annotation.required();

        // 1. Get the current authentication object
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated() || authentication.getPrincipal() == null) {
            if (isRequired) {
                throw new IllegalStateException("Authentication principal is required but not found in the security context.");
            }
            // If not required, return null
            return null;
        }

        // 2. Extract the principal and cast it to the expected DTO type
        Object principal = authentication.getPrincipal();

        if (principal instanceof HeaderResponse headerResponse) {
            return headerResponse;
        } else {
            // Fallback: If principal is UserDetailsImpl, adapt it to HeaderResponse (JTI unavailable here)
            if (principal instanceof UserDetailsImpl userDetails) {
                Role role = null;
                if (userDetails.getAuthorities() != null && !userDetails.getAuthorities().isEmpty()) {
                    GrantedAuthority auth = userDetails.getAuthorities().iterator().next();
                    try {
                        role = Role.fromString(auth.getAuthority());
                    } catch (IllegalArgumentException ignored) {
                        role = null;
                    }
                }
                return new HeaderResponse(
                        userDetails.getId(),
                        userDetails.getUsername(),
                        role,
                        null
                );
            }

            // This happens if the authentication principal is not the expected or adaptable type
            String principalType = principal.getClass().getSimpleName();
            throw new IllegalStateException(
                String.format("Expected principal type HeaderResponse, but found %s. Check JWT filter configuration.", principalType)
            );
        }
    }
}