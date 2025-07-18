package com.Megatram.Megatram.repository;

import com.Megatram.Megatram.Entity.Commande;
import com.Megatram.Megatram.enums.StatutCommande;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
    public interface CommandeRepository extends JpaRepository<Commande, Long> {
        // Trouver toutes les commandes pour un client spécifique
        List<Commande> findByClientId(Long clientId);

        // Trouver toutes les commandes avec un certain statut
        List<Commande> findByStatut(StatutCommande statut);

}
