package com.Megatram.Megatram.Entity;


import jakarta.persistence.*;

import java.util.UUID;

@Entity
public class Produit {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String nom;
    private String ref;

    private int qte;
    private int qteMin;
    private double prix;

    @Column(unique = true)
    private String codeBarre; // Généré automatiquement

    @ManyToOne
    @JoinColumn(name = "categorie_id")
    private Categorie categorie;

    @ManyToOne
    @JoinColumn(name = "lieu_id")
    private LieuStock lieuStock;


    // Getters et Setters

    @PrePersist
    public void genererCodeBarre() {
        if (this.codeBarre == null || this.codeBarre.isEmpty()) {
            String suffix = UUID.randomUUID().toString().substring(0, 8).toUpperCase(); // ex: "A1B2C3D4"
            this.codeBarre = "PROD-" + suffix;
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

    public int getQteMin() {
        return qteMin;
    }

    public void setQteMin(int qteMin) {
        this.qteMin = qteMin;
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

    public Categorie getCategorie() {
        return categorie;
    }

    public void setCategorie(Categorie categorie) {
        this.categorie = categorie;
    }

    public LieuStock getLieuStock() {
        return lieuStock;
    }

    public void setLieuStock(LieuStock lieuStock) {
        this.lieuStock = lieuStock;
    }
}