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


//    @Query("SELECT c FROM Commande c JOIN c.client cl JOIN c.lieuLivraison l WHERE LOWER(cl.nom) LIKE LOWER(CONCAT('%', :kw, '%')) OR LOWER(cl.prenom) LIKE LOWER(CONCAT('%', :kw, '%')) OR LOWER(l.nom) LIKE LOWER(CONCAT('%', :kw, '%')) OR LOWER(c.statut) LIKE LOWER(CONCAT('%', :kw, '%')) OR STR(c.id) LIKE CONCAT('%', :kw, '%') OR STR(c.totalCommande) LIKE CONCAT('%', :kw, '%') OR STR(c.date) LIKE CONCAT('%', :kw, '%')")
//    List<Commande> searchCommandes(@Param("kw") String keyword);




//
//    @Query("SELECT c FROM Commande c " +
//            "JOIN c.client cl " +
//            "JOIN c.lieuLivraison l " +
//            "WHERE LOWER(cl.nom) LIKE LOWER(CONCAT('%', :kw, '%')) " +
//            "OR LOWER(cl.prenom) LIKE LOWER(CONCAT('%', :kw, '%')) " +
//            "OR LOWER(l.nom) LIKE LOWER(CONCAT('%', :kw, '%')) " +
////            "OR LOWER(c.statut) LIKE LOWER(CONCAT('%', :kw, '%')) " +
////            "OR STR(c.id) LIKE CONCAT('%', :kw, '%') " +
////            "OR STR(c.totalCommande) LIKE CONCAT('%', :kw, '%') " +
//            "OR STR(c.date) LIKE CONCAT('%', :kw, '%')")
//    List<Commande> searchCommandes(@Param("kw") String kw);
}
