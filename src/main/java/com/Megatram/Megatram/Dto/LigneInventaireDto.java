package com.Megatram.Megatram.Dto;

public class LigneInventaireDto {

    public Long produitId;
    public int qteScanne;
    public String lieuStockNom;

    // Nouveau champ pour spécifier le type de quantité scannée ("CARTON" ou "UNITE")
    public String typeQuantiteScanne;


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

    public String getLieuStockNom() {
        return lieuStockNom;
    }

    public void setLieuStockNom(String lieuStockNom) {
        this.lieuStockNom = lieuStockNom;
    }

    // Getter et Setter pour le nouveau champ typeQuantiteScanne
    public String getTypeQuantiteScanne() {
        return typeQuantiteScanne;
    }

    public void setTypeQuantiteScanne(String typeQuantiteScanne) {
        this.typeQuantiteScanne = typeQuantiteScanne;
    }
}
