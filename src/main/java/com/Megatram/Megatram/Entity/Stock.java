package com.Megatram.Megatram.Entity;

import jakarta.persistence.*;

@Entity
@Table(
    uniqueConstraints = @UniqueConstraint(columnNames = {"produit_id", "lieu_stock_id"})
)
public class Stock {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "produit_id", nullable = false) // Assurons l'unicité du stock par produit/lieu
    private Produit produit;

    @ManyToOne
    @JoinColumn(name = "LieuStock_id", nullable = false)
    private LieuStock lieuStock;

    @Column(nullable = false)
    private int qteCartons;

    @Column(nullable = false)
    private int qteUnitesRestantes;

    // Getters and Setters

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Produit getProduit() {
        return produit;
    }

    public void setProduit(Produit produit) {
        this.produit = produit;
    }

    public LieuStock getLieuStock() {
        return lieuStock;
    }

    public void setLieuStock(LieuStock lieuStock) {
        this.lieuStock = lieuStock;
    }

    public int getQteCartons() {
        return qteCartons;
    }

    public void setQteCartons(int qteCartons) {
        this.qteCartons = qteCartons;
    }

    public int getQteUnitesRestantes() {
        return qteUnitesRestantes;
    }

    public void setQteUnitesRestantes(int qteUnitesRestantes) {
        this.qteUnitesRestantes = qteUnitesRestantes;
    }
}