package com.Megatram.Megatram.controller;

import com.Megatram.Megatram.Dto.NotificationDto;
import com.Megatram.Megatram.Entity.Notification;
import com.Megatram.Megatram.repository.NotificationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/notifications")
public class NotificationController {

    private final NotificationRepository notificationRepository;

    @Autowired
    public NotificationController(NotificationRepository notificationRepository) {
        this.notificationRepository = notificationRepository;
    }

    // Récupérer toutes les notifications d’un user
    @GetMapping("/user/{userId}")
    public List<NotificationDto> getNotificationsByUser(@PathVariable Long userId) {
        List<Notification> notifications = notificationRepository.findByUserId(userId);
        return notifications.stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    // Méthode de mapping Entity -> DTO
    private NotificationDto toDto(Notification entity) {
        NotificationDto dto = new NotificationDto();
        dto.setId(entity.getId());
        dto.setUserId(entity.getUserId());
        dto.setType(entity.getType());
        dto.setMessage(entity.getMessage());
        dto.setInfoId(entity.getInfoId());
        dto.setInfoStatus(entity.getInfoStatus());
        dto.setDate(entity.getDate());
        dto.setLu(entity.isLu());
        return dto;
    }
}
