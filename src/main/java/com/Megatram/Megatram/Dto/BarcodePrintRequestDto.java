package com.Megatram.Megatram.Dto;


public class BarcodePrintRequestDto {

    private String produitNom;
    private int quantite;

    // Constructeurs
    public BarcodePrintRequestDto() {}

    public BarcodePrintRequestDto(String produitNom, int quantite, int parPage) {
        this.produitNom = produitNom;
        this.quantite = quantite;
    }

    // Getters et Setters


    public String getProduitNom() {
        return produitNom;
    }

    public void setProduitNom(String produitNom) {
        this.produitNom = produitNom;
    }

    public int getQuantite() {
        return quantite;
    }

    public void setQuantite(int quantite) {
        this.quantite = quantite;
    }


}
