package org.sp.payroll_service.domain.auth;


import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.sp.payroll_service.api.auth.dto.LoginRequest;

@Slf4j
public class HeaderUtils {
    public static String getClientIpAddress(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0].trim();
        }

        String xRealIP = request.getHeader("X-Real-IP");
        if (xRealIP != null && !xRealIP.isEmpty()) {
            return xRealIP;
        }

        return request.getRemoteAddr();
    }

    public static void extractRequestInfo(LoginRequest request, HttpServletRequest httpRequest) {
        log.info("Request IP: {}", getClientIpAddress(httpRequest));
        log.info("Request User-Agent: {}", httpRequest.getHeader("User-Agent"));
        log.info("Request Content-Type: {}", httpRequest.getContentType());
        log.debug("Full request details: {}", request);
    }
}
