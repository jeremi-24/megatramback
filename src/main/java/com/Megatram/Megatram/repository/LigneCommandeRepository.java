package com.Megatram.Megatram.repository;

import com.Megatram.Megatram.Entity.LigneCommande;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
    public interface LigneCommandeRepository extends JpaRepository<LigneCommande, Long> {

}
