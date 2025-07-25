package com.Megatram.Megatram.Dto;

import com.Megatram.Megatram.Entity.LigneLivraison;
import com.Megatram.Megatram.Entity.Produit;

public class LigneLivraisonDTO {

    private Long id;
    private Long produitId; // Maintenir pour référence si nécessaire
    private String produitNom;
    private double produitPrix; // Prix appliqué (unitaire ou par carton)
    private int qteLivreeDansLigne; // Quantité stockée dans l'entité (cartons ou unités)
    private String typeQuantite; // "CARTON" ou "UNITE"

    // Nouveaux champs pour l'affichage des quantités converties
    private int qteLivreeCartons;
    private int qteLivreeUnites;
    private int qteLivreeTotaleUnites; // Utile pour certains affichages ou calculs

    private double totalLivraison; // Total de la ligne


    // Constructeur vide, toujours utile
    public LigneLivraisonDTO() {
    }

    // Constructeur pour créer un DTO à partir d'une entité LigneLivraison
    public LigneLivraisonDTO(LigneLivraison ligne) { // Simplified constructor
        this.id = ligne.getId();
        // Accéder au produit via la relation dans l'entité
        Produit produit = ligne.getProduit();
        if (produit != null) {
             this.produitId = produit.getId(); // Définir produitId à partir de l'objet Produit
             this.produitNom = produit.getNom();
        } else {
            this.produitId = null;
            this.produitNom = "Produit Inconnu";
        }

        this.qteLivreeDansLigne = ligne.getQteLivre(); // Quantité stockée (cartons ou unités)
        this.typeQuantite = ligne.getTypeQuantite();
        this.produitPrix = ligne.getProduitPrix(); // Prix appliqué (carton ou unitaire)
        this.totalLivraison = ligne.getTotalLivraison();


        // Calculer les quantités en cartons et unités pour l'affichage
        if (produit != null && produit.getQteParCarton() > 0) {
             if ("CARTON".equalsIgnoreCase(this.typeQuantite)) {
                this.qteLivreeCartons = this.qteLivreeDansLigne; // qteLivreeDansLigne est le nombre de cartons
                this.qteLivreeUnites = 0; // Pas d'unités restantes si livré en cartons complets
                this.qteLivreeTotaleUnites = this.qteLivreeDansLigne * produit.getQteParCarton();
             } else { // UNITE (ou type inconnu)
                this.qteLivreeCartons = this.qteLivreeDansLigne / produit.getQteParCarton(); // Calculer les cartons à partir des unités
                this.qteLivreeUnites = this.qteLivreeDansLigne % produit.getQteParCarton(); // Calculer les unités restantes
                this.qteLivreeTotaleUnites = this.qteLivreeDansLigne; // qteLivreeDansLigne est le nombre d'unités totales
             }
        } else {
             // Si qteParCarton est invalide ou produit est null, on affiche simplement la quantité stockée comme des unités totales
            this.qteLivreeCartons = 0;
            this.qteLivreeUnites = this.qteLivreeDansLigne;
            this.qteLivreeTotaleUnites = this.qteLivreeDansLigne;
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

    public double getProduitPrix() {
        return produitPrix;
    }

    public void setProduitPrix(double produitPrix) {
        this.produitPrix = produitPrix;
    }

    public int getQteLivreeDansLigne() {
        return qteLivreeDansLigne;
    }

    public void setQteLivreeDansLigne(int qteLivreeDansLigne) {
        this.qteLivreeDansLigne = qteLivreeDansLigne;
    }

    public String getTypeQuantite() {
        return typeQuantite;
    }

    public void setTypeQuantite(String typeQuantite) {
        this.typeQuantite = typeQuantite;
    }

    public int getQteLivreeCartons() {
        return qteLivreeCartons;
    }

    public void setQteLivreeCartons(int qteLivreeCartons) {
        this.qteLivreeCartons = qteLivreeCartons;
    }

    public int getQteLivreeUnites() {
        return qteLivreeUnites;
    }

    public void setQteLivreeUnites(int qteLivreeUnites) {
        this.qteLivreeUnites = qteLivreeUnites;
    }

    public int getQteLivreeTotaleUnites() {
        return qteLivreeTotaleUnites;
    }

    public void setQteLivreeTotaleUnites(int qteLivreeTotaleUnites) {
        this.qteLivreeTotaleUnites = qteLivreeTotaleUnites;
    }

    public double getTotalLivraison() {
        return totalLivraison;
    }

    public void setTotalLivraison(double totalLivraison) {
        this.totalLivraison = totalLivraison;
    }
}
