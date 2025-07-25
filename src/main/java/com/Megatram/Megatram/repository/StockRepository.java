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

    // CORRIGÉ : Cette méthode est supprimée car le champ 'date' n'existe plus dans l'entité Stock.
    // List<Stock> findByLieuStock_IdAndDateBefore(Long lieuStockId, Date date);

    // CORRIGÉ : Cette méthode est également supprimée car elle dépend du champ 'date'.
    // List<Stock> findByProduit_IdAndLieuStock_IdAndDateBefore(Long produitId, Long lieuStockId, Date date);

    // Cette méthode est correcte et utilisée par votre StockService, on la garde.
    Optional<Stock> findByProduitAndLieuStock(Produit produit, LieuStock lieuStock);
}