package com.Megatram.Megatram.Entity;

import jakarta.persistence.*;

import java.util.UUID;

@Entity
public class LigneCommande {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long produitId;

    private double totalCommande;
    private double produitPrix;


    @ManyToOne
    @JoinColumn(name = "commande_id")
    private Commande commande;

    private int qteVoulu;
    @PrePersist
    @PreUpdate
    public void calculerTotalCommande() {
        this.totalCommande = this.qteVoulu * this.produitPrix;
    }

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

    public double getTotalCommande() {
        return totalCommande;
    }

    public void setTotalCommande(double totalCommande) {
        this.totalCommande = totalCommande;
    }

    public double getProduitPrix() {
        return produitPrix;
    }

    public void setProduitPrix(double produitPrix) {
        this.produitPrix = produitPrix;
    }

    public Commande getCommande() {
        return commande;
    }

    public void setCommande(Commande commande) {
        this.commande = commande;
    }

    public int getQteVoulu() {
        return qteVoulu;
    }

    public void setQteVoulu(int qteVoulu) {
        this.qteVoulu = qteVoulu;
    }
}
