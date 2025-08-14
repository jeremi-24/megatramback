package com.Megatram.Megatram.Dto;

import java.util.List;

public class InventaireRequestDto {

    public String charge;
 public Long lieuStockId;
    public List<LigneInventaireDto> produits;

    public String getCharge() { return charge; }
    public void setCharge(String charge) { this.charge = charge; }
    public List<LigneInventaireDto> getProduits() { return produits; }
    public void setProduits(List<LigneInventaireDto> produits) { this.produits = produits; }

    public Long getLieuStockId() { return lieuStockId; }
    public void setLieuStockId(Long lieuStockId) { this.lieuStockId = lieuStockId; }

}