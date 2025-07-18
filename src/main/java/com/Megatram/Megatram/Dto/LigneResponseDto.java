package com.Megatram.Megatram.Dto;

public class LigneResponseDto {
    public Long produitId;
    public String nomProduit;
    public int qteScanne;
    public int qteAvantScan;
    public Integer ecart;
    public String lieuStockNom;

    public Long getProduitId() {
        return produitId;
    }

    public void setProduitId(Long produitId) {
        this.produitId = produitId;
    }

    public String getNomProduit() {
        return nomProduit;
    }

    public void setNomProduit(String nomProduit) {
        this.nomProduit = nomProduit;
    }

    public int getQteScanne() {
        return qteScanne;
    }

    public void setQteScanne(int qteScanne) {
        this.qteScanne = qteScanne;
    }

    public int getQteAvantScan() {
        return qteAvantScan;
    }

    public void setQteAvantScan(int qteAvantScan) {
        this.qteAvantScan = qteAvantScan;
    }

    public Integer getEcart() {
        return ecart;
    }

    public void setEcart(Integer ecart) {
        this.ecart = ecart;
    }

    public String getLieuStockNom() {
        return lieuStockNom;
    }

    public void setLieuStockNom(String lieuStockNom) {
        this.lieuStockNom = lieuStockNom;
    }
}