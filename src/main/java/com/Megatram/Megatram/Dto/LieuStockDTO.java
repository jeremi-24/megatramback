package com.Megatram.Megatram.Dto;

import com.Megatram.Megatram.Entity.LieuStock;
import com.Megatram.Megatram.enums.TypeLieu;

public class LieuStockDTO {
    private Long id;
    private String nom;
    private TypeLieu type;
    private String localisation;

    public LieuStockDTO() {
    }
    public LieuStockDTO(LieuStock lieuStock) {
        this.id = lieuStock.getId();
        this.nom = lieuStock.getNom();
        this.type = lieuStock.getType();
        this.localisation = lieuStock.getLocalisation();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNom() {
        return nom;
    }

    public void setNom(String nom) {
        this.nom = nom;
    }

    public TypeLieu getType() {
        return type;
    }

    public void setType(TypeLieu type) {
        this.type = type;
    }

    public String getLocalisation() {
        return localisation;
    }

    public void setLocalisation(String localisation) {
        this.localisation = localisation;
    }
}
