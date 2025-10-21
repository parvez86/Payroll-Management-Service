package org.sp.payroll_service.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * Swagger/OpenAPI 3.0 configuration for API documentation.
 */
@Configuration
public class SwaggerConfig {

    @Value("${server.servlet.context-path:/pms/v1/api}")
    private String contextPath;

    @Value("${server.port:20001}")
    private String serverPort;

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(buildApiInfo())
                .servers(buildServers())
                .addSecurityItem(new SecurityRequirement().addList("Bearer Authentication"))
                .components(buildComponents());
    }

    private Info buildApiInfo() {
        return new Info()
                .title("Payroll Management System API")
                .description("""
                        A comprehensive payroll management system built with Spring Boot 3.5.6 and Java 24.
                        
                        **Key Features:**
                        - JWT-based authentication and authorization
                        - Employee management with grade-based hierarchy
                        - ACID-compliant salary calculations and transfers
                        - Real-time financial transaction processing
                        - Comprehensive audit trail and reporting
                        
                        **Business Rules:**
                        - Exactly 10 employees across 6 grades (1,1,2,2,2,2)
                        - 4-digit unique employee IDs
                        - Salary Formula: Basic + HRA (20%) + Medical (15%)
                        - Grade Calculation: Grade6Base + (6 - GradeNumber) × 5000
                        """)
                .version("1.0.0")
                .contact(buildContact())
                .license(buildLicense());
    }

    private Contact buildContact() {
        return new Contact()
                .name("Payroll System Development Team")
                .email("dev@payrollsystem.com")
                .url("https://github.com/payroll-system");
    }

    private License buildLicense() {
        return new License()
                .name("MIT License")
                .url("https://opensource.org/licenses/MIT");
    }

    private List<Server> buildServers() {
        return List.of(
                new Server()
                        .url("http://localhost:" + serverPort + contextPath)
                        .description("Local Development Server"),
                new Server()
                        .url("https://api.payrollsystem.com" + contextPath)
                        .description("Production Server")
        );
    }

    private Components buildComponents() {
        return new Components()
                .addSecuritySchemes("Bearer Authentication", buildSecurityScheme());
    }

    private SecurityScheme buildSecurityScheme() {
        return new SecurityScheme()
                .name("Bearer Authentication")
                .type(SecurityScheme.Type.HTTP)
                .scheme("bearer")
                .bearerFormat("JWT")
                .in(SecurityScheme.In.HEADER)
                .description("Enter JWT Bearer token in the format: Bearer {token}");
    }
}