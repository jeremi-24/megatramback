package com.Megatram.Megatram.Dto;

import com.Megatram.Megatram.Entity.Produit;

import java.util.Date;

public class StockDto {

    private Long id;
    private  String produitNom;
    private  String entrepotNom;
    private int quantte;
    private Date date;


    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getProduitNom() {
        return produitNom;
    }

    public void setProduitNom(String produitNom) {
        this.produitNom = produitNom;
    }

    public String getEntrepotNom() {
        return entrepotNom;
    }

    public void setEntrepotNom(String entrepotNom) {
        this.entrepotNom = entrepotNom;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public int getQuantte() {
        return quantte;
    }

    public void setQuantte(int quantte) {
        this.quantte = quantte;
    }
}
