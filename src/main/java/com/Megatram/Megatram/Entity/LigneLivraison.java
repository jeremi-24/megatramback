package com.Megatram.Megatram.Entity;

import jakarta.persistence.*;

@Entity
public class LigneLivraison {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long produitId;
    private double produitPrix;
    private int qteLivre;
    private double totalLivraison;

    @ManyToOne
    @JoinColumn(name = "bon_livraison_id") // C'est la seule relation nécessaire
    private BonLivraison bonLivraison;

    @PrePersist
    @PreUpdate
    public void calculerTotalLivraison() { // Renommé pour plus de clarté
        this.totalLivraison = this.qteLivre * this.produitPrix;
    }

    // --- Getters et Setters pour les champs restants ---

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getProduitId() { return produitId; }
    public void setProduitId(Long produitId) { this.produitId = produitId; }
    public double getProduitPrix() { return produitPrix; }
    public void setProduitPrix(double produitPrix) { this.produitPrix = produitPrix; }
    public int getQteLivre() { return qteLivre; }
    public void setQteLivre(int qteLivre) { this.qteLivre = qteLivre; }
    public double getTotalLivraison() { return totalLivraison; }
    public void setTotalLivraison(double totalLivraison) { this.totalLivraison = totalLivraison; }
    public BonLivraison getBonLivraison() { return bonLivraison; }
    public void setBonLivraison(BonLivraison bonLivraison) { this.bonLivraison = bonLivraison; }
}