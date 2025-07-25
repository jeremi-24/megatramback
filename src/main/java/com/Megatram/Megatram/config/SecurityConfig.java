package com.Megatram.Megatram.config;

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

@EnableMethodSecurity
@Configuration
public class SecurityConfig {

    @Autowired
    private JwtFilter jwtFilter;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                // Désactivation de la protection CSRF (comme vous l'aviez déjà fait)
                .csrf(csrf -> csrf.disable())

                // ==================== DÉBUT DE LA CORRECTION ====================
                // Autorise le rendu de la console H2, qui utilise des frames HTML
                .headers(headers -> headers
                        .frameOptions(frameOptions -> frameOptions.disable())
                )
                // ===================== FIN DE LA CORRECTION =====================

                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(
                                "/api/auth/login",
                                "/h2-console/**",            // Permet l'accès public à l'URL de la console
                                "/api/users/**",
                                "/api/roles/**",
                                "/v3/api-docs/**",
                                "/swagger-ui/**",
                                "/api/users/login",
                                "/swagger-ui.html",
                                "/ws-notifications/**",
                                "/swagger-resources/**",
                                "/api/livraisons",
                                "/api/livraisons/**",
                                "/webjars/**",
                                "/error"
                        ).permitAll()

                        // Vos règles de sécurité spécifiques
                        .requestMatchers("/api/auth/save").hasRole("ADMIN")
                        .requestMatchers("/api/admin/**").hasRole("ADMIN")
                        .requestMatchers("/api/user/**").hasAnyRole("ADMIN", "USER")
                        // La ligne ci-dessous est redondante car déjà couverte par permitAll, mais ne cause pas d'erreur.
                        .requestMatchers("/api/livraisons", "/api/livraisons/**").permitAll()

                        // Exige une authentification pour toutes les autres requêtes
                        .anyRequest().authenticated()
                )
                // Ajoute votre filtre JWT pour valider les tokens pour les requêtes authentifiées
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