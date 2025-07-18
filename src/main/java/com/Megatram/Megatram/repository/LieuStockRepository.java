package com.Megatram.Megatram.repository;

import com.Megatram.Megatram.Entity.LieuStock;
import com.Megatram.Megatram.enums.TypeLieu;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
    public interface LieuStockRepository extends JpaRepository<LieuStock, Long> {
        // Trouver tous les lieux d'un certain type (ex: tous les MAGASINS)
        List<LieuStock> findByType(TypeLieu type);
     Optional<LieuStock> findByNomIgnoreCase(String nom);

        // Trouver un lieu par son nom
        Optional<LieuStock> findByNom(String nom);

    boolean existsByNomIgnoreCase(String trim);


}
