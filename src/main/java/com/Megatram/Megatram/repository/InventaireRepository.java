package com.Megatram.Megatram.repository;
import java.util.List;

import com.Megatram.Megatram.Entity.Inventaire;
import org.springframework.data.jpa.repository.JpaRepository;

public interface InventaireRepository extends JpaRepository<Inventaire,Long> {


    List<Inventaire> findByLieuStockId(Long lieuStockId);
}