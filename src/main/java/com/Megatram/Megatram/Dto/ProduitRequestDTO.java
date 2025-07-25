package com.Megatram.Megatram.Dto;

public class ProduitRequestDTO {

    private String nom;
    private String ref;
    private double prix; // Prix unitaire
    private int qteMin;
    private Long categorieId;
    private Long lieuStockId;

    // Nouveaux champs pour la gestion des cartons et prix par carton
    private int qteParCarton;
    private double prixCarton;


    // Getters and Setters

    public String getNom() {
        return nom;
    }

    public void setNom(String nom) {
        this.nom = nom;
    }

    public String getRef() {
        return ref;
    }

    public void setRef(String ref) {
        this.ref = ref;
    }

    public double getPrix() {
        return prix;
    }

    public void setPrix(double prix) {
        this.prix = prix;
    }

    public int getQteMin() {
        return qteMin;
    }

    public void setQteMin(int qteMin) {
        this.qteMin = qteMin;
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

    // Getters et Setters pour les nouveaux champs
    public int getQteParCarton() {
        return qteParCarton;
    }

    public void setQteParCarton(int qteParCarton) {
        this.qteParCarton = qteParCarton;
    }

    public double getPrixCarton() {
        return prixCarton;
    }

    public void setPrixCarton(double prixCarton) {
        this.prixCarton = prixCarton;
    }
}
