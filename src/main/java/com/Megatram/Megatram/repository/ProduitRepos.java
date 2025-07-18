package com.Megatram.Megatram.repository;

import com.Megatram.Megatram.Entity.Categorie;
import com.Megatram.Megatram.Entity.Produit;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface ProduitRepos extends JpaRepository<Produit, Long> {
    @Query("SELECT COUNT(p) FROM Produit p")
    long countAllProduits();
    Page<Produit> findAll(Pageable pageable);
    Produit findByNom(String nom);
    // Dans l'interface ProduitRepos.java
    Optional<Produit> findByCodeBarre(String codeBarre);

    boolean existsByLieuStockId(Long id);
}