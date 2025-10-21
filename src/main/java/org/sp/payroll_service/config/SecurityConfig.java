package org.sp.payroll_service.config;

import org.sp.payroll_service.security.JwtAccessDeniedHandler;
import org.sp.payroll_service.security.JwtAuthenticationEntryPoint;
import org.sp.payroll_service.security.JwtAuthenticationFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.annotation.web.configurers.AuthorizeHttpRequestsConfigurer;
import org.springframework.security.config.annotation.web.configurers.ExceptionHandlingConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * Global security configuration for JWT authentication.
 * Enables method-level security (@PreAuthorize, @PostAuthorize).
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
public class SecurityConfig {

    private final JwtAuthenticationEntryPoint authEntryPoint;
    private final JwtAccessDeniedHandler accessDeniedHandler;
    private final JwtAuthenticationFilter jwtAuthenticationFilter; // Inject the filter

    // Constructor Injection
    public SecurityConfig(
            JwtAuthenticationEntryPoint authEntryPoint,
            JwtAccessDeniedHandler accessDeniedHandler,
            JwtAuthenticationFilter jwtAuthenticationFilter) {
        this.authEntryPoint = authEntryPoint;
        this.accessDeniedHandler = accessDeniedHandler;
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
    }

    /**
     * Configures HTTP security filter chain.
     * @param http HttpSecurity configuration
     * @return configured SecurityFilterChain
     */
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        return http
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(this::configureAuthorization)
                .exceptionHandling(this::configureExceptionHandling)
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
                .build();
    }

    // --- Helper Configuration Methods ---

    private void configureAuthorization(AuthorizeHttpRequestsConfigurer<HttpSecurity>.AuthorizationManagerRequestMatcherRegistry auth) {
        auth
                // Authentication endpoints (public access)
                .requestMatchers(HttpMethod.POST, "/pms/v1/api/auth/login").permitAll()
                .requestMatchers(HttpMethod.POST, "/pms/v1/api/auth/register").permitAll()
                
                // Actuator health checks for monitoring
                .requestMatchers("/pms/v1/api/actuator/**").permitAll()
                .requestMatchers("/actuator/**").permitAll()
                
                // Swagger UI and API documentation (public access)
                .requestMatchers("/pms/v1/api/v3/api-docs/**").permitAll()
                .requestMatchers("/pms/v1/api/swagger-ui/**").permitAll()
                .requestMatchers("/pms/v1/api/swagger-ui.html").permitAll()
                .requestMatchers("/pms/v1/api/swagger-resources/**").permitAll()
                .requestMatchers("/pms/v1/api/webjars/**").permitAll()
                
                // Alternative Swagger paths (without context path)
                .requestMatchers("/v3/api-docs/**").permitAll()
                .requestMatchers("/swagger-ui/**").permitAll()
                .requestMatchers("/swagger-ui.html").permitAll()
                .requestMatchers("/swagger-resources/**").permitAll()
                .requestMatchers("/webjars/**").permitAll()
                
                // Health check endpoints
                .requestMatchers("/health").permitAll()
                .requestMatchers("/pms/v1/api/health").permitAll()
                
                // All other requests require authentication
                .anyRequest().authenticated();
    }

    private void configureExceptionHandling(ExceptionHandlingConfigurer<HttpSecurity> exceptionHandling) {
        exceptionHandling
                // Handles unauthenticated attempts (missing/invalid token) -> 401 Unauthorized
                .authenticationEntryPoint(authEntryPoint)
                // Handles authenticated users attempting forbidden actions (lack of role) -> 403 Forbidden
                .accessDeniedHandler(accessDeniedHandler);
    }

    // --- Bean Definitions ---

    /**
     * Provides the global AuthenticationManager.
     * Required for manual authentication (e.g., in the Login Controller).
     */
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration) throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }

    /**
     * Defines the PasswordEncoder (e.g., BCrypt) used throughout the application.
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}