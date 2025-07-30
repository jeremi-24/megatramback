package com.Megatram.Megatram.repository;

import com.Megatram.Megatram.Entity.Notification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {
    
    // Trouver toutes les notifications d’un utilisateur donné
    List<Notification> findByUserId(Long userId);

    // Trouver toutes les notifications non lues d’un utilisateur
    List<Notification> findByUserIdAndLuFalse(Long userId);
    
    // Supprimer toutes les notifications d’un utilisateur (optionnel)
    void deleteByUserId(Long userId);
}
