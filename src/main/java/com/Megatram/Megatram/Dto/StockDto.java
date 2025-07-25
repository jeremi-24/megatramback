package com.Megatram.Megatram.Dto;

import com.Megatram.Megatram.Entity.Stock;

public class StockDto {

    private Long id;
    private String produitNom;
    private String lieuStockNom;
    private int qteCartons;
    private int qteUnitesRestantes;

    public StockDto() {
    }

    // Ce constructeur est maintenant valide car l'entité Stock a les bonnes méthodes
    public StockDto(Stock stock) {
        this.id = stock.getId();
        this.produitNom = stock.getProduit().getNom();
        this.lieuStockNom = stock.getLieuStock().getNom();
        this.qteCartons = stock.getQteCartons();
        this.qteUnitesRestantes = stock.getQteUnitesRestantes();
    }

    // Getters and Setters

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getProduitNom() { return produitNom; }
    public void setProduitNom(String produitNom) { this.produitNom = produitNom; }
    public String getLieuStockNom() { return lieuStockNom; }
    public void setLieuStockNom(String lieuStockNom) { this.lieuStockNom = lieuStockNom; }
    public int getQteCartons() { return qteCartons; }
    public void setQteCartons(int qteCartons) { this.qteCartons = qteCartons; }
    public int getQteUnitesRestantes() { return qteUnitesRestantes; }
    public void setQteUnitesRestantes(int qteUnitesRestantes) { this.qteUnitesRestantes = qteUnitesRestantes; }
}