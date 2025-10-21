package org.sp.payroll_service.security;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.security.core.Authentication;

/**
 * Defines the contract for an authentication scheme (e.g., JWT, OAuth2, API Key).
 * A filter will delegate the actual authentication attempt to one or more implementations
 * of this interface.
 */
public interface AuthenticationDelegate {

    /**
     * Attempts to extract credentials from the request and authenticate the user.
     *
     * @param request The current HTTP request, containing headers or parameters.
     * @return A valid {@code Authentication} object if successful, or {@code null} if this
     * delegate does not handle the request or fails authentication in a non-critical way
     * (allowing other delegates to try).
     * @throws Exception if a critical, unrecoverable error occurs during authentication (e.g., invalid token).
     */
    Authentication attemptAuthentication(HttpServletRequest request) throws Exception;
}
