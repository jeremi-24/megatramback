package com.Megatram.Megatram.repository;

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

    Optional<Produit> findByRef(String ref);

    // ====================== CORRECTION APPLIQUÉE ICI ======================
    // La jointure "LEFT JOIN p.lieuStock l" et la condition de recherche sur "l.nom" ont été supprimées
    // car le champ "lieuStock" n'existe pas dans votre entité "Produit".
    @Query("SELECT p FROM Produit p LEFT JOIN p.categorie c " +
            "WHERE LOWER(p.nom) LIKE LOWER(CONCAT('%', :searchTerm, '%')) " +
            "OR LOWER(p.ref) LIKE LOWER(CONCAT('%', :searchTerm, '%')) " +
            "OR LOWER(p.codeBarre) LIKE LOWER(CONCAT('%', :searchTerm, '%')) " +
            "OR LOWER(c.nom) LIKE LOWER(CONCAT('%', :searchTerm, '%')) " +
            "OR CAST(p.qteMin AS string) = :searchTerm")
    List<Produit> searchProduits(@Param("searchTerm") String searchTerm);
    // ======================================================================
}