package com.Megatram.Megatram.Entity;

import jakarta.persistence.*;

@Entity
public class LigneInventaire {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    private Inventaire inventaire;

    @ManyToOne
    private Produit produit;

    // Ces champs stockeront les quantités en UNITÉS TOTALES
    private int qteScanne;
    private int qteAvantScan;
    private Integer ecart;

    // Nouveau champ pour stocker le type de quantité scannée ("CARTON" ou "UNITE")
    private String typeQuantiteScanne;

    @ManyToOne
    private LieuStock lieuStock;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Inventaire getInventaire() {
        return inventaire;
    }

    public void setInventaire(Inventaire inventaire) {
        this.inventaire = inventaire;
    }

    public Produit getProduit() {
        return produit;
    }

    public void setProduit(Produit produit) {
        this.produit = produit;
    }

    public int getQteScanne() {
        return qteScanne;
    }

    public void setQteScanne(int qteScanne) {
        this.qteScanne = qteScanne;
    }

    public int getQteAvantScan() {
        return qteAvantScan;
    }

    public void setQteAvantScan(int qteAvantScan) {
        this.qteAvantScan = qteAvantScan;
    }

    public Integer getEcart() {
        return ecart;
    }

    public void setEcart(Integer ecart) {
        this.ecart = ecart;
    }

    public LieuStock getLieuStock() {
        return lieuStock;
    }

    public void setLieuStock(LieuStock lieuStock) {
        this.lieuStock = lieuStock;
    }

    // Getter et Setter pour le nouveau champ typeQuantiteScanne
    public String getTypeQuantiteScanne() {
        return typeQuantiteScanne;
    }

    public void setTypeQuantiteScanne(String typeQuantiteScanne) {
        this.typeQuantiteScanne = typeQuantiteScanne;
    }
}
