package com.Megatram.Megatram.repository;

import com.Megatram.Megatram.Entity.LieuStock;
import com.Megatram.Megatram.Entity.Produit;
import com.Megatram.Megatram.Entity.Stock;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Date;
import java.util.List;
import java.util.Optional;

public interface StockRepository extends JpaRepository<Stock, Long> {

    Optional<Stock> findByProduitAndLieuStock(Produit produit, LieuStock lieuStock);

    // Nouvelle méthode pour trouver tous les stocks par produit
    List<Stock> findByProduit(Produit produit);

    // Nouvelle méthode pour trouver tous les stocks par lieu
    List<Stock> findByLieuStock(LieuStock lieuStock);

    @Query("SELECT s FROM Stock s WHERE s.lieuStock.nom = :lieuStockNom")
    List<Stock> findByLieuStockNom(@Param("lieuStockNom") String lieuStockNom);
}