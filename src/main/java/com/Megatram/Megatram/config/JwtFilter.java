package com.Megatram.Megatram.config;

import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.SignatureException;
import io.jsonwebtoken.UnsupportedJwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class JwtFilter extends OncePerRequestFilter {

    @Autowired
    private JwtUtil jwtUtil;
    
    // Il est recommandé d'utiliser un logger pour tracer les erreurs
    private static final Logger logger = LoggerFactory.getLogger(JwtFilter.class);

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {

        final String authHeader = request.getHeader("Authorization");

        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);

            try {
                // Le bloc try-catch englobe toute la logique de validation et de parsing du token
                if (jwtUtil.validateToken(token)) {
                    String email = jwtUtil.extractEmail(token);
                    String role = jwtUtil.extractRole(token);
                    List<String> permissions = jwtUtil.extractPermissions(token);
                    permissions = (permissions != null) ? permissions : List.of();

                    // Convertir permissions en authorities Spring Security
                    List<SimpleGrantedAuthority> authorities = permissions.stream()
                            .map(SimpleGrantedAuthority::new)
                            .collect(Collectors.toList());

                    // Ajouter aussi le rôle. Le préfixe "ROLE_" est une convention Spring Security.
                    if (role != null && !role.isEmpty()) {
                        authorities.add(new SimpleGrantedAuthority("ROLE_" + role));
                    }

                    // Créer l'objet d'authentification
                    UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                            email, null, authorities);
                    
                    // Attacher les détails de la requête à l'authentification
                    authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                    // Placer l'objet d'authentification dans le contexte de sécurité de Spring
                    // L'utilisateur est maintenant considéré comme authentifié pour cette requête.
                    SecurityContextHolder.getContext().setAuthentication(authentication);
                }
            } catch (ExpiredJwtException e) {
                logger.warn("Le token JWT a expiré : {}", e.getMessage());
            } catch (SignatureException e) {
                logger.error("La signature du token JWT est invalide : {}", e.getMessage());
            } catch (MalformedJwtException e) {
                logger.error("Le token JWT est malformé : {}", e.getMessage());
            } catch (UnsupportedJwtException e) {
                logger.error("Le token JWT n'est pas supporté : {}", e.getMessage());
            } catch (IllegalArgumentException e) {
                logger.error("La chaîne de claims du JWT est vide ou invalide : {}", e.getMessage());
            } catch (Exception e) {
                // Catch générique pour toute autre erreur inattendue lors du traitement du token
                logger.error("Erreur inattendue lors de la validation du token JWT : {}", e.getMessage());
            }
        }
        
        // IMPORTANT : Toujours continuer la chaîne de filtres, même si le token est invalide.
        // Si le SecurityContextHolder est vide, Spring Security refusera l'accès plus tard
        // si la ressource est protégée.
        chain.doFilter(request, response);
    }
}