package com.Megatram.Megatram.repository;

import com.Megatram.Megatram.Entity.Commande;
import com.Megatram.Megatram.enums.StatutCommande;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
    public interface CommandeRepository extends JpaRepository<Commande, Long> {
        // Trouver toutes les commandes pour un client spécifique
        List<Commande> findByClientId(Long clientId);

        // Trouver toutes les commandes avec un certain statut
        List<Commande> findByStatut(StatutCommande statut);



    @Query("SELECT c FROM Commande c " +
            "LEFT JOIN c.client cl " +
            "LEFT JOIN c.lieuStock ls " +
            "WHERE LOWER(CAST(c.statut AS string)) LIKE LOWER(CONCAT('%', :term, '%')) " +
            "OR LOWER(cl.nom) LIKE LOWER(CONCAT('%', :term, '%')) " +
            "OR LOWER(cl.tel) LIKE LOWER(CONCAT('%', :term, '%')) " +  // remplacé email par tel
            "OR LOWER(ls.nom) LIKE LOWER(CONCAT('%', :term, '%'))")
    List<Commande> searchCommandes(@Param("term") String term);


}
