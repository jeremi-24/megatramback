package com.Megatram.Megatram.Dto;

import java.util.List;

public class CommandeRequestDTO {

    private Long clientId;

    private List<LigneCommandeRequestDTO> lignes;

    public Long getClientId() {
        return clientId;
    }

    public void setClientId(Long clientId) {
        this.clientId = clientId;
    }

    public List<LigneCommandeRequestDTO> getLignes() {
        return lignes;
    }

    public void setLignes(List<LigneCommandeRequestDTO> lignes) {
        this.lignes = lignes;
    }
}
