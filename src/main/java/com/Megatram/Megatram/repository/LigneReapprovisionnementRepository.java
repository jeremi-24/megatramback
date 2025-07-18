package com.Megatram.Megatram.repository;

import com.Megatram.Megatram.Entity.LigneReapprovisionnement;
import com.Megatram.Megatram.Entity.Reapprovisionnement;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface LigneReapprovisionnementRepository extends JpaRepository<LigneReapprovisionnement, Long> {
    List<LigneReapprovisionnement> findByReapprovisionnement(Reapprovisionnement r);

}
