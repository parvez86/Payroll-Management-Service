package org.sp.payroll_service.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

/**
 * Application startup listener to display important URLs and information.
 */
@Component
@Slf4j
public class ApplicationStartupListener {

    @Value("${server.port:8080}")
    private int serverPort;
    
    @Value("${server.servlet.context-path:}")
    private String contextPath;
    
    @Value("${spring.application.name:payroll-service}")
    private String applicationName;

    @EventListener(ApplicationReadyEvent.class)
    public void onApplicationReady() {
        String baseUrl = "http://localhost:" + serverPort;
        String fullContextPath = contextPath.isEmpty() ? "" : contextPath;
        
        // Wait a moment to ensure all startup logs are complete
        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        
        log.info("🚀 ==========================================");
        log.info("🚀 {} Started Successfully!", applicationName);
        log.info("🚀 ==========================================");
        log.info("🌐 Application URL: {}{}", baseUrl, fullContextPath);
        log.info("📚 Swagger UI: {}{}/swagger-ui/index.html", baseUrl, fullContextPath);
        log.info("📋 API Docs: {}{}/v3/api-docs", baseUrl, fullContextPath);
        log.info("❤️  Health Check: {}{}/actuator/health", baseUrl, fullContextPath);
        log.info("🔐 Auth Login: {}{}/auth/login", baseUrl, fullContextPath);
        log.info("🚀 ==========================================");
        
        // Also print to System.out for Docker visibility with more spacing
        System.out.println("");
        System.out.println("");
        System.out.println("████████████████████████████████████████████████████████████");
        System.out.println("🚀 " + applicationName.toUpperCase() + " - READY TO USE!");
        System.out.println("████████████████████████████████████████████████████████████");
        System.out.println("🌐 Application URL: " + baseUrl + fullContextPath);
        System.out.println("📚 Swagger UI: " + baseUrl + fullContextPath + "/swagger-ui/index.html");
        System.out.println("📋 API Docs: " + baseUrl + fullContextPath + "/v3/api-docs");
        System.out.println("❤️  Health Check: " + baseUrl + fullContextPath + "/actuator/health");
        System.out.println("🔐 Auth Login: " + baseUrl + fullContextPath + "/auth/login");
        System.out.println("████████████████████████████████████████████████████████████");
        System.out.println("� TIP: Open Swagger UI in your browser to test the API!");
        System.out.println("████████████████████████████████████████████████████████████");
        System.out.println("");
        System.out.println("");
    }
}