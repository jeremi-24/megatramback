package com.Megatram.Megatram.Dto;

public class LigneInventaireDto {

    public Long produitId;
    public int qteScanne;
    public Long lieuStockId;

 public String typeQuantiteScanne;

    public String ref;


    public Long getProduitId() {
        return produitId;
    }

    public void setProduitId(Long produitId) {
        this.produitId = produitId;
    }

    public int getQteScanne() {
        return qteScanne;
    }

    public void setQteScanne(int qteScanne) {
        this.qteScanne = qteScanne;
    }

    public Long getLieuStockId() {
        return lieuStockId;
    }

    public void setLieuStockId(Long lieuStockId) {
        this.lieuStockId = lieuStockId;
    }

    public String getRef() {
        return ref;
    }

    public void setRef(String ref) {
        this.ref = ref;
    }

 public String getTypeQuantiteScanne() {
 return typeQuantiteScanne;
 }

 public void setTypeQuantiteScanne(String typeQuantiteScanne) {
 this.typeQuantiteScanne = typeQuantiteScanne;
 }
}
