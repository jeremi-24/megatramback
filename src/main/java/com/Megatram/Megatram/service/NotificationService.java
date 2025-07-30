package com.Megatram.Megatram.service;

import com.Megatram.Megatram.Entity.Notification;
import com.Megatram.Megatram.repository.NotificationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.HashMap;

@Service
public class NotificationService {

    private final SimpMessagingTemplate messagingTemplate;
    private final NotificationRepository notificationRepository;

    @Autowired
    public NotificationService(SimpMessagingTemplate messagingTemplate, NotificationRepository notificationRepository) {
        this.messagingTemplate = messagingTemplate;
        this.notificationRepository = notificationRepository;
    }

    public void envoyerNotification(String destination, Long userId, String message) {
        Notification notification = new Notification();
        notification.setMessage(message);
        notification.setDate(LocalDateTime.now());
        notification.setLu(false);
        notification.setUserId(userId); // <-- on ajoute le userId ici
        notification.setType(null);
        notification.setInfoId(null);
        notification.setInfoStatus(null);
    
        Notification savedNotif = notificationRepository.save(notification);
    
        Map<String, Object> payload = new HashMap<>();
        payload.put("id", savedNotif.getId());
        payload.put("userId", savedNotif.getUserId()); // Peut être null, accepté
        payload.put("type", savedNotif.getType());
        payload.put("message", savedNotif.getMessage());
        payload.put("info", null);
        payload.put("date", savedNotif.getDate().toString());
        payload.put("lu", savedNotif.isLu());
        
    
        messagingTemplate.convertAndSend(destination, payload);
        System.out.println("Notification envoyée à '" + destination + "' : " + payload);
    }
    
    public void envoyerNotificationGenerale(String message) {
        Notification notification = new Notification();
        notification.setMessage(message);
        notification.setDate(LocalDateTime.now());
        notification.setLu(false);
        notification.setUserId(null); // Pas d'utilisateur ciblé
        notification.setType("ALERTE_GENERALE");
        notification.setInfoId(null);
        notification.setInfoStatus(null);
    
        Notification savedNotif = notificationRepository.save(notification);
    
        Map<String, Object> payload = new HashMap<>();
        payload.put("id", savedNotif.getId());
        payload.put("userId", null); // null accepté ici
        payload.put("type", savedNotif.getType());
        payload.put("message", savedNotif.getMessage());
        payload.put("info", null);
        payload.put("date", savedNotif.getDate().toString());
        payload.put("lu", savedNotif.isLu());
        
    
        // Destination publique
        messagingTemplate.convertAndSend("/topic/app", payload);
        System.out.println("Notification générale envoyée à /topic/app : " + payload);
    }
    
    
    

    public void envoyerNotificationAuClient(Long userId, String type, String message, Long idCommande, String statut) {
        Notification notification = new Notification();
        notification.setUserId(userId);
        notification.setType(type);
        notification.setMessage(message);
        notification.setInfoId(idCommande);
        notification.setInfoStatus(statut);
        notification.setDate(LocalDateTime.now());
        notification.setLu(false);
    
        Notification savedNotif = notificationRepository.save(notification);
    
        Map<String, Object> info = new HashMap<>();
        info.put("id", savedNotif.getInfoId());
        info.put("status", savedNotif.getInfoStatus());
        
        Map<String, Object> payload = new HashMap<>();
        payload.put("id", savedNotif.getId());
        payload.put("userId", savedNotif.getUserId());
        payload.put("type", savedNotif.getType());
        payload.put("message", savedNotif.getMessage());
        payload.put("info", info); // Sous-map
        payload.put("date", savedNotif.getDate().toString());
        payload.put("lu", savedNotif.isLu());
        
    
        messagingTemplate.convertAndSend("/topic/client/" + userId, payload);
        System.out.println("Notification envoyée à user " + userId + " : " + payload);
    }
    
}
