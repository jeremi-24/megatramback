package com.Megatram.Megatram.repository;

import com.Megatram.Megatram.Entity.LigneLivraison;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
    public interface LigneLivraisonRepository extends JpaRepository<LigneLivraison, Long> {

}
