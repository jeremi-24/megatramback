package com.Megatram.Megatram.Dto;

import com.Megatram.Megatram.Entity.LigneVente;
import com.Megatram.Megatram.Entity.Produit;

public class LigneVenteDto {
    private Long id;
    private Long produitId;
    private String produitNom;
    private int qteVendu;
    private double produitPrix;
    private double total;

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

    public int getQteVendu() {
        return qteVendu;
    }

    public void setQteVendu(int qteVendu) {
        this.qteVendu = qteVendu;
    }

    public double getProduitPrix() {
        return produitPrix;
    }

    public void setProduitPrix(double produitPrix) {
        this.produitPrix = produitPrix;
    }

    public double getTotal() {
        return total;
    }

    public void setTotal(double total) {
        this.total = total;
    }

    public LigneVenteDto(LigneVente ligne, Produit produit) {
        this.id = ligne.getId();
        this.qteVendu = ligne.getQteVendu();
        this.produitPrix = ligne.getProduitPrix();
        this.total = ligne.getTotal();
        if (produit != null) {
            this.produitNom = produit.getNom();
        }
    }

}