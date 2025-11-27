package org.sp.payroll_service.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.List;

/**
 * Global Web MVC configuration to handle CORS policy.
 */
@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    private final HeaderPrincipalArgumentResolver headerPrincipalArgumentResolver;

    public WebMvcConfig(HeaderPrincipalArgumentResolver headerPrincipalArgumentResolver) {
        this.headerPrincipalArgumentResolver = headerPrincipalArgumentResolver;
    }

    /**
     * Adds the custom resolver to the list of argument resolvers used by Spring MVC.
     */
    @Override
    public void addArgumentResolvers(List<HandlerMethodArgumentResolver> resolvers) {
        resolvers.add(headerPrincipalArgumentResolver);
    }

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        // Allow requests from your React development server
        registry.addMapping("/**") // Apply to all endpoints
                .allowedOrigins("http://localhost:3000", "http://localhost:5173") // Add your React dev port(s)
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                .allowedHeaders("*") // Allow all headers
                .allowCredentials(true); // Allow cookies/session info (if needed)
    }
}
