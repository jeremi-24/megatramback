package com.Megatram.Megatram.Dto;

import com.Megatram.Megatram.Entity.Produit;

public class ProduitAdminDto {

    private Long id;
    private String nom;
    private String ref;
    private double prix; // Prix unitaire
    private String codeBarre;
    private Long categorieId;
    private String lieuStockNom;
    private int qteMin;

    private String categorieNom;
    private Long lieuStockId;

    // Nouveaux champs pour la gestion des cartons et prix par carton
    private int qteParCarton;
    private double prixCarton;

    // Nouvelle propriété pour la quantité totale globale
    private int quantiteTotaleGlobale;


    // Constructeur vide (utile pour la désérialisation)
    public ProduitAdminDto() {
    }

    // Constructeur pour créer un DTO à partir de l'entité Produit
    public ProduitAdminDto(Produit produit) {
        this.id = produit.getId();
        this.nom = produit.getNom();
        this.ref = produit.getRef();
        this.prix = produit.getPrix(); // Prix unitaire
        this.codeBarre = produit.getCodeBarre();
        this.qteMin = produit.getQteMin();

        // Mappage des nouveaux champs
        this.qteParCarton = produit.getQteParCarton();
        this.prixCarton = produit.getPrixCarton();

        // Mappage des relations si elles sont chargées
        if (produit.getCategorie() != null) {
            this.categorieId = produit.getCategorie().getId();
            this.categorieNom = produit.getCategorie().getNom();
        } else {
            this.categorieId = null;
            this.categorieNom = null;
        }

        if (produit.getLieuStock() != null) {
            this.lieuStockId = produit.getLieuStock().getId();
            this.lieuStockNom = produit.getLieuStock().getNom();
        } else {
            this.lieuStockId = null;
            this.lieuStockNom = null;
        }
    }


    // Getters and Setters

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

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

    public String getCodeBarre() {
        return codeBarre;
    }

    public void setCodeBarre(String codeBarre) {
        this.codeBarre = codeBarre;
    }

    public Long getCategorieId() {
        return categorieId;
    }

    public void setCategorieId(Long categorieId) {
        this.categorieId = categorieId;
    }

    public String getLieuStockNom() {
        return lieuStockNom;
    }

    public void setLieuStockNom(String lieuStockNom) {
        this.lieuStockNom = lieuStockNom;
    }

    public int getQteMin() {
        return qteMin;
    }

    public void setQteMin(int qteMin) {
        this.qteMin = qteMin;
    }

    public String getCategorieNom() {
        return categorieNom;
    }

    public void setCategorieNom(String categorieNom) {
        this.categorieNom = categorieNom;
    }

    public Long getLieuStockId() {
        return lieuStockId;
    }

    public void setLieuStockId(Long lieuStockId) {
        this.lieuStockId = lieuStockId;
    }

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

    public int getQuantiteTotaleGlobale() {
        return quantiteTotaleGlobale;
    }

    public void setQuantiteTotaleGlobale(int quantiteTotaleGlobale) {
        this.quantiteTotaleGlobale = quantiteTotaleGlobale;
    }
}
