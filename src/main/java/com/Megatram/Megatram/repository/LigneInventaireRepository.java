package com.Megatram.Megatram.repository;

import com.Megatram.Megatram.Entity.LigneInventaire;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface LigneInventaireRepository extends JpaRepository<LigneInventaire,Long> {
    List<LigneInventaire> findByInventaireId(Long inventaireId);
}