package com.Megatram.Megatram.Entity;

import jakarta.persistence.*;
import com.Megatram.Megatram.Entity.Categorie;
import com.Megatram.Megatram.Entity.LieuStock;

import java.util.UUID;

@Entity
public class Produit {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String nom;
    private String ref;
    private double prix; // Ce sera le prix UNitaire
    private int qteMin;
    private String codeBarre;

    // Nouveau champ pour la quantité par carton
    private int qteParCarton;

    // Nouveau champ pour le prix par carton
    private double prixCarton;

    @ManyToOne
    @JoinColumn(name = "categorie_id")
    private Categorie categorie;

    @ManyToOne
    @JoinColumn(name = "lieu_stock_id")
    private LieuStock lieuStock;

    private String barcodeImagePath; // Champ pour stocker le chemin de l'image du code-barres

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

    public int getQteMin() {
        return qteMin;
    }

    public void setQteMin(int qteMin) {
        this.qteMin = qteMin;
    }

    public String getCodeBarre() {
        return codeBarre;
    }

    public void setCodeBarre(String codeBarre) {
        this.codeBarre = codeBarre;
    }

    public int getQteParCarton() {
        return qteParCarton;
    }

    public void setQteParCarton(int qteParCarton) {
        this.qteParCarton = qteParCarton;
    }

    // Getter et Setter pour le nouveau champ prixCarton
    public double getPrixCarton() {
        return prixCarton;
    }

    public void setPrixCarton(double prixCarton) {
        this.prixCarton = prixCarton;
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

    public String getBarcodeImagePath() {
        return barcodeImagePath;
    }

    public void setBarcodeImagePath(String barcodeImagePath) {
        this.barcodeImagePath = barcodeImagePath;
    }
}
