package com.Megatram.Megatram.Dto;

import com.Megatram.Megatram.enums.TypeLieu;

public class LieuStockRequestDTO {

    private String nom;
    private TypeLieu type;
    private String localisation;

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
