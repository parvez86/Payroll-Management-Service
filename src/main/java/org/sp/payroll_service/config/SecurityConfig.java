package org.sp.payroll_service.config;

import org.sp.payroll_service.security.AuthenticationFilter;
import org.sp.payroll_service.security.JwtAccessDeniedHandler;
import org.sp.payroll_service.security.JwtAuthenticationEntryPoint;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfigurationSource;

/**
 * ✅ CLEAN SECURITY CONFIGURATION
 * 
 * Simple, clear security setup with:
 * 1. Single authentication filter (AuthenticationFilter)
 * 2. Clear public endpoint rules  
 * 3. Proper exception handling
 * 4. No complex path matching
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
public class SecurityConfig {

    private final JwtAuthenticationEntryPoint authEntryPoint;
    private final JwtAccessDeniedHandler accessDeniedHandler;
    private final AuthenticationFilter authenticationFilter;
    private final CorsConfigurationSource corsConfigurationSource;

    public SecurityConfig(
            JwtAuthenticationEntryPoint authEntryPoint,
            JwtAccessDeniedHandler accessDeniedHandler,
            AuthenticationFilter authenticationFilter,
            CorsConfigurationSource corsConfigurationSource) {
        this.authEntryPoint = authEntryPoint;
        this.accessDeniedHandler = accessDeniedHandler;
        this.authenticationFilter = authenticationFilter;
        this.corsConfigurationSource = corsConfigurationSource;
    }

    /**
     * ✅ SIMPLE SECURITY FILTER CHAIN
     */
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        return http
                // ✅ Enable CORS with custom configuration
                .cors(cors -> cors.configurationSource(corsConfigurationSource))
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        // ✅ Auth endpoints (POST only)
                        .requestMatchers(HttpMethod.POST, "/api/v1/auth/**").permitAll()
                        
                        // ✅ Actuator endpoints (all methods)
                        .requestMatchers("/actuator/**").permitAll()
                        
                        // ✅ Swagger/API docs (all methods) - all variants
                        .requestMatchers("/swagger-ui/**", "/v1/api/swagger-ui/**", "/api/v1/swagger-ui/**").permitAll()
                        .requestMatchers("/v3/api-docs/**", "/v1/api/v3/api-docs/**", "/api/v1/v3/api-docs/**").permitAll()
                        .requestMatchers("/webjars/**", "/v1/api/webjars/**", "/api/v1/webjars/**").permitAll()
                        .requestMatchers("/swagger-resources/**", "/v1/api/swagger-resources/**", "/api/v1/swagger-resources/**").permitAll()
                        
                        // ✅ All other requests require authentication
                        .anyRequest().authenticated()
                )
                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint(authEntryPoint)  // 401 for missing/invalid token
                        .accessDeniedHandler(accessDeniedHandler)  // 403 for insufficient permissions
                )
                // ✅ Add authentication filter
                .addFilterBefore(authenticationFilter, UsernamePasswordAuthenticationFilter.class)
                .build();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration) throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(12);
    }
}