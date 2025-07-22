package com.Megatram.Megatram.Dto;

import java.util.List;

public class AssignationProduitsDTO {
    private List<Long> produitIds;
    private Long categorieId;
    private Long lieuStockId;

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

    public Long getLieuStockId() {
        return lieuStockId;
    }

    public void setLieuStockId(Long lieuStockId) {
        this.lieuStockId = lieuStockId;
    }
}