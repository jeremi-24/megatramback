package com.Megatram.Megatram.repository;

import com.Megatram.Megatram.Entity.Stock;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Date;
import java.util.List;

public interface StockRepository extends JpaRepository<Stock, Long> {
    List<Stock> findByLieuStock_IdAndDateBefore(Long lieuStockId, Date date);
    List<Stock> findByProduit_IdAndLieuStock_IdAndDateBefore(Long produitId, Long lieuStockId, Date date);
}
