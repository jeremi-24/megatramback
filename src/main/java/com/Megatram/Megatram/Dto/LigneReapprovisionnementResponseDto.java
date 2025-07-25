package com.Megatram.Megatram.Dto;

import com.Megatram.Megatram.Entity.LigneReapprovisionnement;
import com.Megatram.Megatram.Entity.Produit;
import com.Megatram.Megatram.Entity.LieuStock;

public class LigneReapprovisionnementResponseDto {

    private Long id;
    private Long produitId;
    private String produitNom;
    private int qteAjouteeDansLigne;
    private String typeQuantite;
    private String lieuStockNom;

    private int qteAjouteeCartons;
    private int qteAjouteeUnites;
    private int qteAjouteeTotaleUnites;

    public LigneReapprovisionnementResponseDto() {
    }

    // Constructeur pour créer un DTO à partir d'une entité LigneReapprovisionnement
    public LigneReapprovisionnementResponseDto(LigneReapprovisionnement ligne) {
        this.id = ligne.getId();
        Produit produit = ligne.getProduit();
        if (produit != null) {
             this.produitId = produit.getId();
             this.produitNom = produit.getNom();
        } else {
            this.produitId = null;
            this.produitNom = "Produit Inconnu";
        }

        this.qteAjouteeDansLigne = ligne.getQteAjoutee();
        this.typeQuantite = ligne.getTypeQuantite();

        LieuStock lieuStock = ligne.getLieuStock();
        if (lieuStock != null) {
            this.lieuStockNom = lieuStock.getNom();
        } else {
            this.lieuStockNom = "Lieu Inconnu";
        }

        if (produit != null && produit.getQteParCarton() > 0) {
             if ("CARTON".equalsIgnoreCase(this.typeQuantite)) {
                this.qteAjouteeCartons = this.qteAjouteeDansLigne;
                this.qteAjouteeUnites = 0;
                this.qteAjouteeTotaleUnites = this.qteAjouteeDansLigne * produit.getQteParCarton();
             } else {
                this.qteAjouteeCartons = this.qteAjouteeDansLigne / produit.getQteParCarton();
                this.qteAjouteeUnites = this.qteAjouteeDansLigne % produit.getQteParCarton();
                this.qteAjouteeTotaleUnites = this.qteAjouteeDansLigne;
             }
        } else {
            this.qteAjouteeCartons = 0;
            this.qteAjouteeUnites = this.qteAjouteeDansLigne;
            this.qteAjouteeTotaleUnites = this.qteAjouteeDansLigne;
        }
    }


    // Getters and Setters

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

    public int getQteAjouteeDansLigne() {
        return qteAjouteeDansLigne;
    }

    public void setQteAjouteeDansLigne(int qteAjouteeDansLigne) {
        this.qteAjouteeDansLigne = qteAjouteeDansLigne;
    }

    public String getTypeQuantite() {
        return typeQuantite;
    }

    public void setTypeQuantite(String typeQuantite) {
        this.typeQuantite = typeQuantite;
    }

    public String getLieuStockNom() {
        return lieuStockNom;
    }

    public void setLieuStockNom(String lieuStockNom) {
        this.lieuStockNom = lieuStockNom;
    }

    public int getQteAjouteeCartons() {
        return qteAjouteeCartons;
    }

    public void setQteAjouteeCartons(int qteAjouteeCartons) {
        this.qteAjouteeCartons = qteAjouteeCartons;
    }

    public int getQteAjouteeUnites() {
        return qteAjouteeUnites;
    }

    public void setQteAjouteeUnites(int qteAjouteeUnites) {
        this.qteAjouteeUnites = qteAjouteeUnites;
    }

    public int getQteAjouteeTotaleUnites() {
        return qteAjouteeTotaleUnites;
    }

    public void setQteAjouteeTotaleUnites(int qteAjouteeTotaleUnites) {
        this.qteAjouteeTotaleUnites = qteAjouteeTotaleUnites;
    }
}
