package com.Megatram.Megatram.repository;

import com.Megatram.Megatram.Entity.Vente;
import org.springframework.data.jpa.repository.JpaRepository;

public interface VenteRepository extends JpaRepository<Vente, Long> {
    boolean existsByCommande_Id(Long id);
}
