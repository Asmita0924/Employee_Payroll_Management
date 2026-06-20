package com.payroll.config;

import com.payroll.service.UserDetailsServiceImpl;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
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

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthFilter;
    private final UserDetailsServiceImpl userDetailsService;

    public SecurityConfig(JwtAuthenticationFilter jwtAuthFilter, UserDetailsServiceImpl userDetailsService) {
        this.jwtAuthFilter = jwtAuthFilter;
        this.userDetailsService = userDetailsService;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(AbstractHttpConfigurer::disable)
            .authorizeHttpRequests(auth -> auth
                // Allow static frontend assets publicly
                .requestMatchers("/", "/index.html", "/favicon.ico", "/css/**", "/js/**", "/static/**").permitAll()
                // Allow public access to Auth endpoints
                .requestMatchers("/api/auth/**").permitAll()
                
                // Employee REST APIs security rules
                .requestMatchers(HttpMethod.GET, "/api/employees/**").hasAnyRole("ADMIN", "HR", "EMPLOYEE")
                .requestMatchers(HttpMethod.POST, "/api/employees/**").hasAnyRole("ADMIN", "HR")
                .requestMatchers(HttpMethod.PUT, "/api/employees/**").hasAnyRole("ADMIN", "HR")
                .requestMatchers(HttpMethod.DELETE, "/api/employees/**").hasRole("ADMIN")
                
                // Department REST APIs security rules
                .requestMatchers(HttpMethod.GET, "/api/departments/**").hasAnyRole("ADMIN", "HR", "EMPLOYEE")
                .requestMatchers(HttpMethod.POST, "/api/departments/**").hasRole("ADMIN")
                .requestMatchers(HttpMethod.PUT, "/api/departments/**").hasRole("ADMIN")
                .requestMatchers(HttpMethod.DELETE, "/api/departments/**").hasRole("ADMIN")

                // Attendance REST APIs security rules
                .requestMatchers(HttpMethod.GET, "/api/attendance/**").hasAnyRole("ADMIN", "HR", "EMPLOYEE")
                .requestMatchers(HttpMethod.POST, "/api/attendance/**").hasAnyRole("ADMIN", "HR", "EMPLOYEE")

                // Leave REST APIs security rules
                .requestMatchers(HttpMethod.GET, "/api/leave/**").hasAnyRole("ADMIN", "HR", "EMPLOYEE")
                .requestMatchers(HttpMethod.POST, "/api/leave/**").hasAnyRole("ADMIN", "HR", "EMPLOYEE")
                .requestMatchers(HttpMethod.PUT, "/api/leave/approve/**").hasAnyRole("ADMIN", "HR")

                // Payroll REST APIs security rules
                .requestMatchers(HttpMethod.GET, "/api/payroll/**").hasAnyRole("ADMIN", "HR", "EMPLOYEE")
                .requestMatchers(HttpMethod.POST, "/api/payroll/generate/**").hasAnyRole("ADMIN", "HR")
                
                // All other endpoints require authentication
                .anyRequest().authenticated()
            )
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authenticationProvider(authenticationProvider())
            .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(userDetailsService);
        authProvider.setPasswordEncoder(passwordEncoder());
        return authProvider;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }
}
