package com.Megatram.Megatram.Dto;

public class LigneReapprovisionnementResponseDto {
    public Long id;
    public Long produitId;
    public String produitNom;
    public int qteAjoutee;
    public String lieuStockNom;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getProduitId() {
        return produitId;
    }

    public void setProduitId(Long produitId) {
        this.produitId = produitId;
    }

    public String getProduitNom() {
        return produitNom;
    }

    public void setProduitNom(String produitNom) {
        this.produitNom = produitNom;
    }

    public int getQteAjoutee() {
        return qteAjoutee;
    }

    public void setQteAjoutee(int qteAjoutee) {
        this.qteAjoutee = qteAjoutee;
    }

    public String getLieuStockNom() {
        return lieuStockNom;
    }

    public void setLieuStockNom(String lieuStockNom) {
        this.lieuStockNom = lieuStockNom;
    }
}
