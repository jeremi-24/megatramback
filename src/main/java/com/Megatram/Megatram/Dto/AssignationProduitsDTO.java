package com.Megatram.Megatram.Dto;

import java.util.List;

public class AssignationProduitsDTO {
    private List<Long> produitIds;
    private Long categorieId;
    private Long entrepotId;

    // Getters et setters
    public List<Long> getProduitIds() {
        return produitIds;
    }

    public void setProduitIds(List<Long> produitIds) {
        this.produitIds = produitIds;
    }

    public Long getCategorieId() {
        return categorieId;
    }

    public void setCategorieId(Long categorieId) {
        this.categorieId = categorieId;
    }

    public Long getEntrepotId() {
        return entrepotId;
    }

    public void setEntrepotId(Long entrepotId) {
        this.entrepotId = entrepotId;
    }
}