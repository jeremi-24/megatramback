package com.Megatram.Megatram.repository;

import com.Megatram.Megatram.Entity.Organisation;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface OrganisationRepository extends JpaRepository<Organisation, Long> {

    Optional<Organisation> findFirstByOrderByIdAsc();

}