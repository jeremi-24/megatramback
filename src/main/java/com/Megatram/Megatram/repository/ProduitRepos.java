package com.Megatram.Megatram.repository;

import com.Megatram.Megatram.Entity.Categorie;
import com.Megatram.Megatram.Entity.Produit;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ProduitRepos extends JpaRepository<Produit, Long> {
    @Query("SELECT COUNT(p) FROM Produit p")
    long countAllProduits();

    Page<Produit> findAll(Pageable pageable);

    Optional<Produit> findByCodeBarre(String codeBarre);

    List<Produit> findByNom(String nom);

    boolean existsByLieuStockId(Long id);

    // CORRECTION : La condition "OR CAST(p.qte AS string) = :searchTerm" a été supprimée
    @Query("SELECT p FROM Produit p LEFT JOIN p.categorie c LEFT JOIN p.lieuStock l " +
            "WHERE LOWER(p.nom) LIKE LOWER(CONCAT('%', :searchTerm, '%')) " +
            "OR LOWER(p.ref) LIKE LOWER(CONCAT('%', :searchTerm, '%')) " +
            "OR LOWER(p.codeBarre) LIKE LOWER(CONCAT('%', :searchTerm, '%')) " +
            "OR LOWER(c.nom) LIKE LOWER(CONCAT('%', :searchTerm, '%')) " +
            "OR LOWER(l.nom) LIKE LOWER(CONCAT('%', :searchTerm, '%')) " +
            "OR CAST(p.qteMin AS string) = :searchTerm")
    List<Produit> searchProduits(@Param("searchTerm") String searchTerm);
}