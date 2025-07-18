package com.Megatram.Megatram.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

/**
 * Service centralisé pour l'envoi de notifications via WebSockets.
 * Il sert d'intermédiaire simple entre notre logique métier et le système de messagerie.
 */
@Service
public class NotificationService {

    /**
     * Le SimpMessagingTemplate est l'outil principal de Spring pour envoyer des messages
     * à des destinations (topics) WebSocket. Il est automatiquement configuré
     * lorsque vous activez @EnableWebSocketMessageBroker.
     */
    private final SimpMessagingTemplate messagingTemplate;

    /**
     * On injecte le SimpMessagingTemplate via le constructeur.
     * C'est une bonne pratique qui rend le service plus facile à tester.
     */
    @Autowired
    public NotificationService(SimpMessagingTemplate messagingTemplate) {
        this.messagingTemplate = messagingTemplate;
    }

    /**
     * Envoie un message de notification à une destination spécifique (un canal/topic).
     *
     * @param destination Le canal sur lequel envoyer le message.
     *                    Doit commencer par le préfixe défini dans WebSocketConfig (ex: "/topic/secretariat").
     * @param message     L'objet ou le texte à envoyer. Il sera automatiquement converti en JSON.
     *                    Pour notre cas simple, un simple String suffit.
     */
    public void envoyerNotification(String destination, String message) {
        // La méthode convertAndSend prend la destination et le "payload" (le contenu du message)
        // et les envoie à tous les clients qui sont abonnés à cette destination.
        messagingTemplate.convertAndSend(destination, message);

        // Pour le débogage, il peut être utile de logger l'envoi
        System.out.println("Notification envoyée à '" + destination + "': " + message);
    }
}