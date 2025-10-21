package org.sp.payroll_service.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.ContentCachingRequestWrapper;
import org.springframework.web.util.ContentCachingResponseWrapper;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Enumeration;

/**
 * Request/Response logging filter for debugging API calls.
 * Logs detailed information about incoming HTTP requests and outgoing responses.
 */
@Slf4j
@Component
public class RequestLoggingFilter extends OncePerRequestFilter {

    private static final int MAX_PAYLOAD_LENGTH = 1000;

    @Override
    protected void doFilterInternal(HttpServletRequest request, @NonNull  HttpServletResponse response,
                                  @NonNull FilterChain filterChain) throws ServletException, IOException {
        
        // Skip logging for actuator endpoints to reduce noise
        if (request.getRequestURI().contains("/actuator") || 
            request.getRequestURI().contains("/health") ||
            request.getRequestURI().contains("/metrics")) {
            filterChain.doFilter(request, response);
            return;
        }
        
        // Wrap request and response to cache content
        ContentCachingRequestWrapper wrappedRequest = new ContentCachingRequestWrapper(request);
        ContentCachingResponseWrapper wrappedResponse = new ContentCachingResponseWrapper(response);
        
        long startTime = System.currentTimeMillis();
        
        try {
            // Log incoming request
            logRequest(wrappedRequest);
            
            // Process the request
            filterChain.doFilter(wrappedRequest, wrappedResponse);
            
        } finally {
            long duration = System.currentTimeMillis() - startTime;
            
            // Log outgoing response
            logResponse(wrappedRequest, wrappedResponse, duration);
            
            // Copy response content back to the original response
            wrappedResponse.copyBodyToResponse();
        }
    }

    private void logRequest(ContentCachingRequestWrapper request) {
        log.info("========== INCOMING REQUEST ==========");
        log.info("Method: {} {}", request.getMethod(), request.getRequestURI());
        log.info("Query String: {}", request.getQueryString());
        log.info("Remote Address: {}", getClientIpAddress(request));
        log.info("User-Agent: {}", request.getHeader("User-Agent"));
        log.info("Content-Type: {}", request.getContentType());
        log.info("Content-Length: {}", request.getContentLength());
        
        // Log headers
        log.debug("Headers:");
        Enumeration<String> headerNames = request.getHeaderNames();
        while (headerNames.hasMoreElements()) {
            String headerName = headerNames.nextElement();
            String headerValue = request.getHeader(headerName);
            
            // Mask sensitive headers
            if (headerName.toLowerCase().contains("authorization") || 
                headerName.toLowerCase().contains("password")) {
                headerValue = "[MASKED]";
            }
            
            log.debug("  {}: {}", headerName, headerValue);
        }
        
        // Log request body for POST/PUT requests
        if ("POST".equals(request.getMethod()) || "PUT".equals(request.getMethod())) {
            byte[] content = request.getContentAsByteArray();
            if (content.length > 0) {
                String body = new String(content, StandardCharsets.UTF_8);
                
                // Mask password fields in JSON
                if (body.contains("password")) {
                    body = body.replaceAll("\"password\"\\s*:\\s*\"[^\"]*\"", "\"password\":\"[MASKED]\"");
                }
                
                if (body.length() > MAX_PAYLOAD_LENGTH) {
                    body = body.substring(0, MAX_PAYLOAD_LENGTH) + "... [TRUNCATED]";
                }
                
                log.info("Request Body: {}", body);
            }
        }
    }

    private void logResponse(ContentCachingRequestWrapper request, ContentCachingResponseWrapper response, long duration) {
        log.info("========== OUTGOING RESPONSE ==========");
        log.info("Status: {} {}", response.getStatus(), getStatusText(response.getStatus()));
        log.info("Duration: {} ms", duration);
        log.info("Content-Type: {}", response.getContentType());
        
        // Log response headers
        log.debug("Response Headers:");
        for (String headerName : response.getHeaderNames()) {
            log.debug("  {}: {}", headerName, response.getHeader(headerName));
        }
        
        // Log response body
        byte[] content = response.getContentAsByteArray();
        if (content.length > 0) {
            String body = new String(content, StandardCharsets.UTF_8);
            
            // Mask token fields in JSON
            if (body.contains("token")) {
                body = body.replaceAll("\"token\"\\s*:\\s*\"[^\"]*\"", "\"token\":\"[MASKED]\"");
                body = body.replaceAll("\"accessToken\"\\s*:\\s*\"[^\"]*\"", "\"accessToken\":\"[MASKED]\"");
                body = body.replaceAll("\"refreshToken\"\\s*:\\s*\"[^\"]*\"", "\"refreshToken\":\"[MASKED]\"");
            }
            
            if (body.length() > MAX_PAYLOAD_LENGTH) {
                body = body.substring(0, MAX_PAYLOAD_LENGTH) + "... [TRUNCATED]";
            }
            
            log.info("Response Body: {}", body);
        }
        
        log.info("========== REQUEST COMPLETED ==========");
    }

    private String getClientIpAddress(HttpServletRequest request) {
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
    
    private String getStatusText(int status) {
        return switch (status) {
            case 200 -> "OK";
            case 201 -> "Created";
            case 400 -> "Bad Request";
            case 401 -> "Unauthorized";
            case 403 -> "Forbidden";
            case 404 -> "Not Found";
            case 409 -> "Conflict";
            case 500 -> "Internal Server Error";
            default -> "Unknown";
        };
    }
}