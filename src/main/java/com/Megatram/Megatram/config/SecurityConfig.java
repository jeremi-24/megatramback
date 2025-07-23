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
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(
                                "/api/auth/login",            // Login autorisé à tous
                                "/api/users/**",
                                "/api/roles/**",   // Autoriser les autres endpoints d'auth (ex: roles)
                                "/v3/api-docs/**",
                                "/swagger-ui/**",
                                "/api/users/login",
                                "/swagger-ui.html",
                                "/ws-notifications/**",
                                "/swagger-resources/**",
                                "/api/livraisons",
                                "/api/livraisons/**" ,// 👈 ajouté ici

                                "/webjars/**",
                                "/error"
                        ).permitAll()

                        .requestMatchers("/api/auth/save").hasRole("ADMIN")   // Seul ADMIN peut créer utilisateurs
                        .requestMatchers("/api/admin/**").hasRole("ADMIN")    // ADMIN uniquement
                        .requestMatchers("/api/user/**").hasAnyRole("ADMIN", "USER") // ADMIN et USER
                        .requestMatchers("/api/livraisons", "/api/livraisons/**").permitAll()

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
