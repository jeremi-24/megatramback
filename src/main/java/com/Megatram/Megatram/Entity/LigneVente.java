package com.Megatram.Megatram.Entity;

import jakarta.persistence.*;



@Entity
public class LigneVente {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long produitId;


    private int qteVendu;

    private double produitPrix;

    private double total;

    @ManyToOne
    @JoinColumn(name = "vente_id")
    private Vente vente;

    @PrePersist
    @PreUpdate
    public void calculerTotal() {
        this.total = this.qteVendu * this.produitPrix;
    }

    // Getters et Setters


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

    public int getQteVendu() {
        return qteVendu;
    }

    public void setQteVendu(int qteVendu) {
        this.qteVendu = qteVendu;
    }

    public double getProduitPrix() {
        return produitPrix;
    }

    public void setProduitPrix(double produitPrix) {
        this.produitPrix = produitPrix;
    }

    public double getTotal() {
        return total;
    }

    public void setTotal(double total) {
        this.total = total;
    }

    public Vente getVente() {
        return vente;
    }

    public void setVente(Vente vente) {
        this.vente = vente;
    }


}
