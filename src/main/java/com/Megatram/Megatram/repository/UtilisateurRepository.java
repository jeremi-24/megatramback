package com.Megatram.Megatram.repository;

import com.Megatram.Megatram.Entity.Utilisateur;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;


public interface UtilisateurRepository extends JpaRepository<Utilisateur, Long> {

    Optional<Utilisateur> findByEmail(String email);

    Utilisateur findByEmailAndPassword(String email, String password);

    boolean existsByEmail(String email);

    boolean existsByRoleId(long id);

    boolean existsByLieuId(Long id);

    @Query("SELECT u FROM Utilisateur u " +
       "JOIN FETCH u.role r " +
       "LEFT JOIN FETCH r.permissions " +
       "WHERE u.email = :email")
Optional<Utilisateur> findByEmailWithPermissions(@Param("email") String email);

}

