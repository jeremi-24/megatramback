package com.Megatram.Megatram.repository;


import com.Megatram.Megatram.Entity.Organisation;
import com.Megatram.Megatram.Entity.Produit;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FactureRepo extends JpaRepository<Organisation,Long> {


}
