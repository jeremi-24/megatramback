package com.Megatram.Megatram.Dto;

import com.Megatram.Megatram.Entity.Produit;

public class ProduitDto {

    private Long id;
    private String nom;
    private String ref;
    private int qte;
    private double prix;
    private String codeBarre;
    private Long categorieId;
    private String lieuStockNom;
    private int qteMin;

    private String categorieNom;
    private Long lieuStockId;


//    public ProduitDto() {
//        // constructeur vide requis
//    }

    public ProduitDto(Produit produit) {
        this.id = produit.getId();
        this.nom = produit.getNom();
        this.ref = produit.getRef();
        this.qte = produit.getQte();
        this.qteMin = produit.getQteMin();
        this.prix = produit.getPrix();
        this.codeBarre = produit.getCodeBarre();

        // Gestion des cas où la catégorie ou le lieu est nul
        if (produit.getCategorie() != null) {
            this.categorieNom = produit.getCategorie().getNom();
        }

        if (produit.getLieuStock() != null) {
            this.lieuStockNom = String.valueOf(produit.getLieuStock().getNom());
        }
    }

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

    public int getQte() {
        return qte;
    }

    public void setQte(int qte) {
        this.qte = qte;
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
}
