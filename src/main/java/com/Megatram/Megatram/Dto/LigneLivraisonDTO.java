package com.Megatram.Megatram.Dto;

import com.Megatram.Megatram.Entity.LigneLivraison;
import com.Megatram.Megatram.Entity.Produit;

public class LigneLivraisonDTO {

    private Long id; // C'est bien d'avoir l'ID de la ligne
    private String produitNom;
    private int qteLivre;
    private double produitPrix;
    private double totalLivraison;

    // Constructeur vide, toujours utile
    public LigneLivraisonDTO() {
    }

    // LE CONSTRUCTEUR INTELLIGENT QUI FAIT LE MAPPING
    // Il prend l'entité de la base de données et la transforme en DTO pour l'affichage.
    public LigneLivraisonDTO(LigneLivraison ligne, Produit produit) {
        this.id = ligne.getId();
        this.qteLivre = ligne.getQteLivre();
        this.produitPrix = ligne.getProduitPrix();

        // Calculer le total de la ligne
        this.totalLivraison = ligne.getQteLivre() * ligne.getProduitPrix();

        // Récupérer le nom du produit pour un affichage clair
        if (produit != null) {
            this.produitNom = produit.getNom();
        } else {
            this.produitNom = "Produit Inconnu (ID: " + ligne.getProduitId() + ")";
        }
    }

    // --- Getters et Setters ---

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

    public int getQteLivre() {
        return qteLivre;
    }

    public void setQteLivre(int qteLivre) {
        this.qteLivre = qteLivre;
    }

    public double getProduitPrix() {
        return produitPrix;
    }

    public void setProduitPrix(double produitPrix) {
        this.produitPrix = produitPrix;
    }

    public double getTotalLivraison() {
        return totalLivraison;
    }

    public void setTotalLivraison(double totalLivraison) {
        this.totalLivraison = totalLivraison;
    }
}