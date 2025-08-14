package com.Megatram.Megatram.Entity;

import jakarta.persistence.*;

@Entity
public class LigneVente {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Retirer produitId car la relation ManyToOne le gérera
    // private Long produitId;

    // Nouvelle relation Many-to-One vers l'entité Produit
    @ManyToOne
    @JoinColumn(name = "produit_id") // Utiliser la même colonne que l'ancien produitId
    private Produit produit;

    private int qteVendu; // Cette quantité est en UNITÉS TOTALES
    private double produitPrix; // Ce sera le prix UNitaire au moment de la vente
    private double total;

    private String typeQuantite; // "CARTON" ou "UNITE"

    @ManyToOne
    @JoinColumn(name = "vente_id")
    private Vente vente;

 @ManyToOne
 @JoinColumn(name = "lieu_stock_id")
 private LieuStock lieuStock;

    @PrePersist
    @PreUpdate
    public void calculerTotal() {
        // Logique ajustée pour calculer le total en fonction du typeQuantite
        if ("CARTON".equalsIgnoreCase(this.typeQuantite)) {
            // Si le type est CARTON, utiliser le prix par carton du produit et la quantité en cartons
            // NOTE : qteVendu est actuellement stocké en UNITÉS TOTALES.
            // Si vous enregistrez qteVendu comme la quantité DE CARTONS vendus lorsque typeQuantite est "CARTON",
            // alors utilisez directement this.qteVendu * this.produit.getPrixCarton()
            // Si qteVendu reste en UNITÉS TOTALES même pour les ventes en carton,
            // vous devez trouver le nombre de cartons vendus à partir de la quantité totale en unités et qteParCarton
            // C'est plus simple si qteVendu dans cette situation représente le nombre de CARTONS.
            // Je vais assumer que qteVendu ici est le nombre de CARTONS quand typeQuantite est "CARTON".
             if (this.produit != null) {
                this.total = this.qteVendu * this.produit.getPrixCarton();
            } else {
                // Gérer l'erreur ou définir le total à 0 si le produit n'est pas lié
                 this.total = 0;
            }

        } else { // Par défaut ou si "UNITE"
            // Si le type est UNITE, utiliser le prix unitaire et la quantité en unités totales
             // NOTE : produitPrix devrait être le prix unitaire au moment de la vente.
             // Vous devrez vous assurer que produitPrix est correctement défini lors de la création de la ligne de vente
            this.total = this.qteVendu * this.produitPrix;
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

    public String getTypeQuantite() {
        return typeQuantite;
    }

    public void setTypeQuantite(String typeQuantite) {
        this.typeQuantite = typeQuantite;
    }

    public Vente getVente() {
        return vente;
    }

    public void setVente(Vente vente) {
        this.vente = vente;
    }

    public LieuStock getLieuStock() {
        return lieuStock;
    }

    public void setLieuStock(LieuStock lieuStock) {
        this.lieuStock = lieuStock;
    }
}
