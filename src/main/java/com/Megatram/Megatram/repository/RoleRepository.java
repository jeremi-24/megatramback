package com.Megatram.Megatram.repository;

import com.Megatram.Megatram.Entity.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RoleRepository extends JpaRepository<Role, Long> {
    // Très important pour assigner un rôle à un utilisateur
    Optional<Role> findByNom(String nom);
}