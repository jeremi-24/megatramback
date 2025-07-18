package com.Megatram.Megatram.Dto;

public class LigneCommandeRequestDTO {

    private Long produitId;
    private int qteVoulu;

    public Long getProduitId() {
        return produitId;
    }

    public void setProduitId(Long produitId) {
        this.produitId = produitId;
    }

    public int getQteVoulu() {
        return qteVoulu;
    }

    public void setQteVoulu(int qteVoulu) {
        this.qteVoulu = qteVoulu;
    }
}
