package com.Megatram.Megatram.Dto;

public class BarcodePrintRequestDto {

    // AJOUTÉ : L'identifiant unique du produit. C'est la clé de la solution.
    private Long produitId;

    // CONSERVÉ : La quantité d'étiquettes à imprimer.
    private int quantite;

    // SUPPRIMÉ : Le nom du produit, qui causait le problème car il n'est pas unique.
    // private String produitNom;

    // Constructeurs
    public BarcodePrintRequestDto() {}

    // Constructeur mis à jour pour utiliser produitId
    public BarcodePrintRequestDto(Long produitId, int quantite) {
        this.produitId = produitId;
        this.quantite = quantite;
    }

    // Getters et Setters mis à jour

    public Long getProduitId() {
        return produitId;
    }

    public void setProduitId(Long produitId) {
        this.produitId = produitId;
    }

    public int getQuantite() {
        return quantite;
    }

    public void setQuantite(int quantite) {
        this.quantite = quantite;
    }
}