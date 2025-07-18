package com.Megatram.Megatram.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

/**
 * Configure la communication en temps réel via WebSockets en utilisant le protocole STOMP.
 * STOMP est une surcouche de WebSocket qui simplifie la gestion des messages (abonnement, envoi, etc.).
 */
@Configuration
@EnableWebSocketMessageBroker // Active le serveur WebSocket et le broker de messages dans Spring
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    /**
     * Cette méthode enregistre l'endpoint de connexion WebSocket.
     * C'est l'URL que le client frontend utilisera pour établir la connexion initiale.
     */
    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        // L'endpoint "/ws-notifications" est l'adresse de connexion pour le client.
        // Exemple: http://localhost:8080/ws-notifications
        registry.addEndpoint("/ws-notifications")
                // On autorise les connexions depuis votre application frontend (React, Angular, etc.)
                .setAllowedOrigins("http://localhost:3000")
                // withSockJS() fournit une solution de repli (fallback) pour les anciens navigateurs
                // qui ne supportent pas les WebSockets natifs. C'est une bonne pratique.
                .withSockJS();
    }

    /**
     * Cette méthode configure le "message broker", qui est responsable de router
     * les messages entre les clients et le serveur.
     */
    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        // Définit le préfixe pour les destinations sur lesquelles les clients peuvent s'abonner.
        // Tous nos canaux de notification commenceront par "/topic".
        // Exemples : "/topic/secretariat", "/topic/magasin/5"
        registry.enableSimpleBroker("/topic");

        // Définit le préfixe pour les messages envoyés DEPUIS le client VERS le serveur.
        // Nous n'en avons pas besoin pour notre système de notification simple (le serveur pousse
        // les données), mais c'est une bonne pratique de le définir.
        // Exemple d'utilisation : un chat où le client envoie un message à "/app/chat".
        registry.setApplicationDestinationPrefixes("/app");
    }
}