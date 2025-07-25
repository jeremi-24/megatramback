package com.Megatram.Megatram.Dto;

import com.Megatram.Megatram.Entity.LigneReapprovisionnement;
import com.Megatram.Megatram.Entity.Produit;
import com.Megatram.Megatram.Entity.LieuStock;

public class LigneReapprovisionnementDto {

    // CORRIGÉ : Les champs sont maintenant privés
    private Long produitId;
    private String lieuStockNom;
    private int qteAjoutee;
    private String typeQuantite;
    private Long id;
    private String produitNom;

    public LigneReapprovisionnementDto() {
    }

    // AJOUTÉ : Un constructeur pratique pour convertir l'entité en DTO
    public LigneReapprovisionnementDto(LigneReapprovisionnement ligne) {
        this.id = ligne.getId();
        this.qteAjoutee = ligne.getQteAjoutee();
        this.typeQuantite = ligne.getTypeQuantite();

        Produit produit = ligne.getProduit();
        if (produit != null) {
            this.produitId = produit.getId();
            this.produitNom = produit.getNom();
        }

        LieuStock lieuStock = ligne.getLieuStock();
        if (lieuStock != null) {
            this.lieuStockNom = lieuStock.getNom();
        }
    }

    // --- Getters and Setters ---

    public Long getProduitId() {
        return produitId;
    }

    public void setProduitId(Long produitId) {
        this.produitId = produitId;
    }

    public String getLieuStockNom() {
        return lieuStockNom;
    }

    public void setLieuStockNom(String lieuStockNom) {
        this.lieuStockNom = lieuStockNom;
    }

    public int getQteAjoutee() {
        return qteAjoutee;
    }

    public void setQteAjoutee(int qteAjoutee) {
        this.qteAjoutee = qteAjoutee;
    }

    public String getTypeQuantite() {
        return typeQuantite;
    }

    public void setTypeQuantite(String typeQuantite) {
        this.typeQuantite = typeQuantite;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getProduitNom() {
        return produitNom;
    }

    public void setProduitNom(String produitNom) {
        this.produitNom = produitNom;
    }
}