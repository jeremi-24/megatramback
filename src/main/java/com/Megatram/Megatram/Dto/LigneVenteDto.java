package com.Megatram.Megatram.Dto;

import com.Megatram.Megatram.Entity.LigneVente;
import com.Megatram.Megatram.Entity.Produit;

public class LigneVenteDto {

    private Long id;
    private Long produitId;
    private String produitNom;
    private double produitPrix;
    private int qteVendueDansLigne;
    private String typeQuantite;
    private int qteVendueCartons;
    private int qteVendueUnites;
    private int qteVendueTotaleUnites;
    private double total;
    private String codeProduit;
    private Long lieuStockId;

    public LigneVenteDto() {
    }

    public LigneVenteDto(LigneVente ligneVente) {
        this.id = ligneVente.getId();
        this.qteVendueDansLigne = ligneVente.getQteVendu();
        this.typeQuantite = ligneVente.getTypeQuantite();
        this.produitPrix = ligneVente.getProduitPrix();
        this.total = ligneVente.getTotal();

        if (ligneVente.getLieuStock() != null) {
            this.lieuStockId = ligneVente.getLieuStock().getId();
        }

        Produit produit = ligneVente.getProduit();
        if (produit != null) {
            this.produitId = produit.getId();
            this.produitNom = produit.getNom();
            this.codeProduit = produit.getCodeBarre();
            if (produit.getQteParCarton() > 0) {
                if ("CARTON".equalsIgnoreCase(this.typeQuantite)) {
                    this.qteVendueCartons = this.qteVendueDansLigne;
                    this.qteVendueUnites = 0;
                    this.qteVendueTotaleUnites = this.qteVendueDansLigne * produit.getQteParCarton();
                } else {
                    this.qteVendueCartons = this.qteVendueDansLigne / produit.getQteParCarton();
                    this.qteVendueUnites = this.qteVendueDansLigne % produit.getQteParCarton();
                    this.qteVendueTotaleUnites = this.qteVendueDansLigne;
                }
            } else {
                this.qteVendueCartons = 0;
                this.qteVendueUnites = this.qteVendueDansLigne;
                this.qteVendueTotaleUnites = this.qteVendueDansLigne;
            }
        }
    }

    // --- Getters and Setters ---

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

    public double getProduitPrix() {
        return produitPrix;
    }

    public void setProduitPrix(double produitPrix) {
        this.produitPrix = produitPrix;
    }

    public int getQteVendueDansLigne() {
        return qteVendueDansLigne;
    }

    public void setQteVendueDansLigne(int qteVendueDansLigne) {
        this.qteVendueDansLigne = qteVendueDansLigne;
    }

    public String getTypeQuantite() {
        return typeQuantite;
    }

    public void setTypeQuantite(String typeQuantite) {
        this.typeQuantite = typeQuantite;
    }

    public int getQteVendueCartons() {
        return qteVendueCartons;
    }

    public void setQteVendueCartons(int qteVendueCartons) {
        this.qteVendueCartons = qteVendueCartons;
    }

    public int getQteVendueUnites() {
        return qteVendueUnites;
    }

    public void setQteVendueUnites(int qteVendueUnites) {
        this.qteVendueUnites = qteVendueUnites;
    }

    public int getQteVendueTotaleUnites() {
        return qteVendueTotaleUnites;
    }

    public void setQteVendueTotaleUnites(int qteVendueTotaleUnites) {
        this.qteVendueTotaleUnites = qteVendueTotaleUnites;
    }

    public double getTotal() {
        return total;
    }

    public void setTotal(double total) {
        this.total = total;
    }

    public String getCodeProduit() {
        return codeProduit;
    }

    public void setCodeProduit(String codeProduit) {
        this.codeProduit = codeProduit;
    }

    public Long getLieuStockId() {
        return lieuStockId;
    }

    public void setLieuStockId(Long lieuStockId) {
        this.lieuStockId = lieuStockId;
    }
}