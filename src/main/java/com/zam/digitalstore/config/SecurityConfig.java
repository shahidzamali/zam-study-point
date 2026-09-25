package com.zam.digitalstore.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {

    // BCrypt Password Encoder
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    // Authentication Manager
    @Bean
    public AuthenticationManager authenticationManager(
            AuthenticationConfiguration configuration) throws Exception {

        return configuration.getAuthenticationManager();
    }

    // Security Configuration
    @Bean
    public SecurityFilterChain securityFilterChain(
            HttpSecurity http) throws Exception {

        http
                .csrf(csrf -> csrf.disable())

                .cors(cors -> {
                })

                .authorizeHttpRequests(auth -> auth

                        .requestMatchers(
                                "/api/user/register",
                                "/api/user/login",
                                "/api/products",
                                "/api/products/**",
                                "/api/payment/create-order",
                                "/api/payment/verify",
                                "/api/admin/**")
                        .permitAll()

                        .requestMatchers(
                                "/",
                                "/index.html",
                                "/login.html",
                                "/register.html",
                                "/admin-login.html",
                                "/admin.html",
                                "/style.css",
                                "/script.js",
                                "/login.js",
                                "/admin-login.js",
                                "/favicon.ico")
                        .permitAll()

                        .anyRequest().permitAll());

        return http.build();
    }
}