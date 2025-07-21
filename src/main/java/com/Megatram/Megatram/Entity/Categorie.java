package com.Megatram.Megatram.Entity;

import jakarta.persistence.*;

import java.util.List;

@Entity
public class Categorie {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String refCategorie;

    private String nom;

    @OneToMany(mappedBy = "categorie")
    private List<Produit> produits;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }


    public Categorie() {} // constructeur vide requis par JPA

    public Categorie(String nom) {
        this.nom = nom;
    }

    public String getRefCategorie() {
        return refCategorie;
    }

    public void setRefCategorie(String refCategorie) {
        this.refCategorie = refCategorie;
    }

    public String getNom() {
        return nom;
    }

    public void setNom(String nom) {
        this.nom = nom;
    }

    public List<Produit> getProduits() {
        return produits;
    }

    public void setProduits(List<Produit> produits) {
        this.produits = produits;
    }
}
