package com.Megatram.Megatram.Entity;

import jakarta.persistence.*;


@Entity
public class LigneReapprovisionnement  {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    private Reapprovisionnement reapprovisionnement;

    // Remplacer le champ produit par la relation ManyToOne
    // private Produit produit;

    // Nouvelle relation Many-to-One vers l'entité Produit
    @ManyToOne
    @JoinColumn(name = "produit_id") // Assurez-vous que c'est le bon nom de colonne
    private Produit produit;

    private int qteAjoutee; // Cette quantité dépend de typeQuantite ("CARTON" ou "UNITE")

    @ManyToOne
    private LieuStock lieuStock;

    // Champ pour stocker le type de quantité ("CARTON" ou "UNITE")
    private String typeQuantite;


    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Reapprovisionnement getReapprovisionnement() {
        return reapprovisionnement;
    }

    public void setReapprovisionnement(Reapprovisionnement reapprovisionnement) {
        this.reapprovisionnement = reapprovisionnement;
    }

    // Getter/Setter pour la relation Produit
    public Produit getProduit() {
        return produit;
    }

    public void setProduit(Produit produit) {
        this.produit = produit;
    }

    public int getQteAjoutee() {
        return qteAjoutee;
    }

    public void setQteAjoutee(int qteAjoutee) {
        this.qteAjoutee = qteAjoutee;
    }

    public LieuStock getLieuStock() {
        return lieuStock;
    }

    public void setLieuStock(LieuStock lieuStock) {
        this.lieuStock = lieuStock;
    }

    public String getTypeQuantite() {
        return typeQuantite;
    }

    public void setTypeQuantite(String typeQuantite) {
        this.typeQuantite = typeQuantite;
    }
}
