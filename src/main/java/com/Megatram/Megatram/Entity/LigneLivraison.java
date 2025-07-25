package com.Megatram.Megatram.Entity;

import jakarta.persistence.*;

@Entity
public class LigneLivraison {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Retirer produitId car la relation ManyToOne le gérera
    // private Long produitId;

    // Nouvelle relation Many-to-One vers l'entité Produit
    @ManyToOne
    @JoinColumn(name = "produit_id") // Utiliser la même colonne que l'ancien produitId
    private Produit produit;

    private double produitPrix; // Ce sera le prix UNitaire au moment de la livraison
    private int qteLivre; // Cette quantité dépend de typeQuantite ("CARTON" ou "UNITE")
    private double totalLivraison;

    private String typeQuantite; // "CARTON" ou "UNITE"

    @ManyToOne
    @JoinColumn(name = "bon_livraison_id")
    private BonLivraison bonLivraison;

    @PrePersist
    @PreUpdate
    public void calculerTotal() {
         // Logique ajustée pour calculer le total en fonction du typeQuantite et du prix approprié
        if ("CARTON".equalsIgnoreCase(this.typeQuantite)) {
             if (this.produit != null) {
                // Si le type est CARTON, utiliser le prix par carton du produit et la quantité en cartons
                // NOTE : qteLivre est censé stocker le nombre de CARTONS livrés ici (selon l'Option A)
                this.totalLivraison = this.qteLivre * this.produit.getPrixCarton();
            } else {
                 this.totalLivraison = 0;
            }
        } else { // Par défaut ou si "UNITE"
             if (this.produit != null) {
                // Si le type est UNITE, utiliser le prix unitaire et la quantité en unités totales
                 // NOTE : qteLivre est censé stocker le nombre d'UNITÉS livrées ici (selon l'Option A)
                 // produitPrix doit être le prix unitaire au moment de la livraison.
                this.totalLivraison = this.qteLivre * this.produitPrix;
            } else {
                 this.totalLivraison = 0;
            }
        }
    }

    // Getters et Setters

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    // Getter/Setter pour la relation Produit
    public Produit getProduit() {
        return produit;
    }

    public void setProduit(Produit produit) {
        this.produit = produit;
    }

    // L'ancien getter/setter pour produitId est retiré
    // public Long getProduitId() { return produitId; }
    // public void setProduitId(Long produitId) { this.produitId = produitId; }

    public double getProduitPrix() {
        return produitPrix;
    }

    public void setProduitPrix(double produitPrix) {
        this.produitPrix = produitPrix;
    }

    public int getQteLivre() {
        return qteLivre;
    }

    public void setQteLivre(int qteLivre) {
        this.qteLivre = qteLivre;
    }

    public double getTotalLivraison() {
        return totalLivraison;
    }

    public void setTotalLivraison(double totalLivraison) {
        this.totalLivraison = totalLivraison;
    }

    public String getTypeQuantite() {
        return typeQuantite;
    }

    public void setTypeQuantite(String typeQuantite) {
        this.typeQuantite = typeQuantite;
    }

    public BonLivraison getBonLivraison() {
        return bonLivraison;
    }

    public void setBonLivraison(BonLivraison bonLivraison) {
        this.bonLivraison = bonLivraison;
    }
}
