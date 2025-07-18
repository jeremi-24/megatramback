package com.Megatram.Megatram.repository;

import com.Megatram.Megatram.Entity.BonLivraison;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;


@Repository
public interface BonLivraisonRepository extends JpaRepository<BonLivraison, Long> {
    // Par exemple, trouver un bon de livraison par sa commande
    // Optional<BonLivraison> findByCommandeId(Long commandeId);

    List<BonLivraison> findByCommande_LieuStock_Id(Long lieuId);
    boolean existsByCommandeId(Long commandeId);
}
