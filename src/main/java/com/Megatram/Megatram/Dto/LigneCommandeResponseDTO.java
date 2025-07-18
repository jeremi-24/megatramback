package com.Megatram.Megatram.Dto;

import com.Megatram.Megatram.Entity.LigneCommande;
import com.Megatram.Megatram.Entity.Produit;

public class LigneCommandeResponseDTO {

    private Long id;              // L'ID de la ligne de commande elle-même
    private String produitNom;    // Le nom du produit pour l'affichage
    private String produitRef;    // La référence du produit peut aussi être utile
    private int qteVoulu;         // La quantité commandée
    private double produitPrix;   // Le prix unitaire au moment de la commande
    private double totalLigne;    // Le sous-total pour cette ligne
    public LigneCommandeResponseDTO() {
    }
    public LigneCommandeResponseDTO(LigneCommande ligne, Produit produit) {
        this.id = ligne.getId();
        this.qteVoulu = ligne.getQteVoulu();
        this.produitPrix = ligne.getProduitPrix();
        this.totalLigne = ligne.getTotalCommande(); // Le nom dans l'entité est 'totalCommande'

        if (produit != null) {
            this.produitNom = produit.getNom();
            this.produitRef = produit.getRef();
        }}

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

    public String getProduitRef() {
        return produitRef;
    }

    public void setProduitRef(String produitRef) {
        this.produitRef = produitRef;
    }

    public int getQteVoulu() {
        return qteVoulu;
    }

    public void setQteVoulu(int qteVoulu) {
        this.qteVoulu = qteVoulu;
    }

    public double getProduitPrix() {
        return produitPrix;
    }

    public void setProduitPrix(double produitPrix) {
        this.produitPrix = produitPrix;
    }

    public double getTotalLigne() {
        return totalLigne;
    }

    public void setTotalLigne(double totalLigne) {
        this.totalLigne = totalLigne;
    }
}
