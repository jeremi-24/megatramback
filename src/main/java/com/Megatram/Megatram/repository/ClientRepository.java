package com.Megatram.Megatram.repository;


import com.Megatram.Megatram.Entity.Client;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface ClientRepository extends JpaRepository<Client, Long> {
    Optional<Client> findByNom(String nom);
    boolean existsByNom(String nom);
    @Query("SELECT c FROM Client c WHERE c.nom NOT LIKE '%@%'")
    List<Client> findClientsExternes();

}
