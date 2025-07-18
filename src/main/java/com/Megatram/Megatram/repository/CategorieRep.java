package com.Megatram.Megatram.repository;

import com.Megatram.Megatram.Entity.Categorie;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface  CategorieRep extends JpaRepository<Categorie, Long> {
    Optional<Categorie> findByNom(String nom);

    Optional<Categorie> findByNomIgnoreCase(String nom);

}
