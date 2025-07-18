package com.Megatram.Megatram.repository;

import com.Megatram.Megatram.Entity.Facture;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
    public interface FactureRepository extends JpaRepository<Facture, Long> {
        // Vous pourriez vouloir trouver une facture par sa commande associée
        // Optional<Facture> findByCommandeId(Long commandeId);
        boolean existsByCommandeId(Long commandeId);

}
