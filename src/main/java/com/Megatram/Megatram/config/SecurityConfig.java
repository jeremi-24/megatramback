package com.Megatram.Megatram.config;

import static org.springframework.security.config.Customizer.withDefaults;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.header.writers.ContentSecurityPolicyHeaderWriter; // <-- Importer la classe

@EnableMethodSecurity
@Configuration
public class SecurityConfig {

    @Autowired
    private JwtFilter jwtFilter;

    // L'URL de votre frontend qui doit pouvoir intégrer cette application
    private static final String FRONTEND_URL = "https://3000-firebase-studio-1750809039432.cluster-l6vkdperq5ebaqo3qy4ksvoqom.cloudworkstations.dev";

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .cors(withDefaults())
            .csrf(csrf -> csrf.disable())
            // DÉBUT DE LA CORRECTION CSP
            .headers(headers -> headers
                .addHeaderWriter(new ContentSecurityPolicyHeaderWriter(
                    "frame-ancestors 'self' " + FRONTEND_URL
                ))
            )
            // FIN DE LA CORRECTION CSP
            .authorizeHttpRequests(auth -> auth
                .requestMatchers(
                    "/api/auth/login",
                    "/api/users/login",
                    "/api/users/**",
                    "/api/roles/**",
                    "/api/livraisons",
                    "/api/livraisons/**",
                    "/ws-notifications/**", // Important pour WebSocket
                    "/api/ws-notifications/**",
                    "/api/ws-notifications/info",
                    "/v3/api-docs/**",
                    "/swagger-ui/**",
                    "/swagger-ui.html",
                    "/swagger-resources/**",
                    "/webjars/**",
                    "/h2-console/**",
                    "/error"
                ).permitAll()
                .requestMatchers("/api/auth/save").hasRole("ADMIN")
                .requestMatchers("/api/admin/**").hasRole("ADMIN")
                .requestMatchers("/api/user/**").hasAnyRole("ADMIN", "USER")
                .anyRequest().authenticated()
            )
            .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
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