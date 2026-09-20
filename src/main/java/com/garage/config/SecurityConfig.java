package com.garage.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {
    
    private final JwtAuthenticationFilter jwtAuthFilter;
    private final AuthenticationProvider authenticationProvider;
    
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(AbstractHttpConfigurer::disable)
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/api/auth/**").permitAll()
                .requestMatchers("/swagger-ui/**", "/api-docs/**", "/swagger-ui.html").permitAll()
                .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                // Admin only endpoints
                .requestMatchers("/api/employees/**").hasRole("ADMIN")
                // Back Office endpoints
                .requestMatchers(HttpMethod.POST, "/api/parts/**").hasAnyRole("ADMIN", "BACK_OFFICE")
                .requestMatchers(HttpMethod.PUT, "/api/parts/**").hasAnyRole("ADMIN", "BACK_OFFICE")
                .requestMatchers(HttpMethod.DELETE, "/api/parts/**").hasAnyRole("ADMIN", "BACK_OFFICE")
                .requestMatchers(HttpMethod.POST, "/api/repair-actions/**").hasAnyRole("ADMIN", "BACK_OFFICE")
                .requestMatchers(HttpMethod.PUT, "/api/repair-actions/**").hasAnyRole("ADMIN", "BACK_OFFICE")
                .requestMatchers(HttpMethod.DELETE, "/api/repair-actions/**").hasAnyRole("ADMIN", "BACK_OFFICE")
                // Document management - Back Office can upload and delete
                .requestMatchers(HttpMethod.POST, "/api/documents/**").hasAnyRole("ADMIN", "BACK_OFFICE")
                .requestMatchers(HttpMethod.DELETE, "/api/documents/**").hasAnyRole("ADMIN", "BACK_OFFICE")
                // Cashier endpoints
                .requestMatchers("/api/receipts/**").hasAnyRole("CASHIER", "ADMIN", "BACK_OFFICE")
                .requestMatchers("/api/payments/**").hasAnyRole("CASHIER", "ADMIN", "BACK_OFFICE")
                // All authenticated users
                .anyRequest().authenticated()
            )
            .sessionManagement(session -> session
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            )
            .authenticationProvider(authenticationProvider)
            .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);
        
        return http.build();
    }
}