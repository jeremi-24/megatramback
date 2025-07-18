package com.Megatram.Megatram.Dto;

import com.Megatram.Megatram.Entity.LieuStock;
import com.Megatram.Megatram.enums.BonLivraisonStatus;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;

import java.time.LocalDateTime;
import java.util.List;

public class BonLivraisonResponseDTO {

    private Long id;
    private LocalDateTime dateLivraison;
    private Long commandeId;
    private List<LigneLivraisonDTO> lignesLivraison;
    private BonLivraisonStatus statut;
    private LieuStock lieuStock;
    private String email;


    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public LocalDateTime getDateLivraison() {
        return dateLivraison;
    }

    public void setDateLivraison(LocalDateTime dateLivraison) {
        this.dateLivraison = dateLivraison;
    }

    public Long getCommandeId() {
        return commandeId;
    }

    public void setCommandeId(Long commandeId) {
        this.commandeId = commandeId;
    }

    public List<LigneLivraisonDTO> getLignesLivraison() {
        return lignesLivraison;
    }

    public void setLignesLivraison(List<LigneLivraisonDTO> lignesLivraison) {
        this.lignesLivraison = lignesLivraison;
    }


    public BonLivraisonStatus getStatut() {
        return statut;
    }

    public void setStatut(BonLivraisonStatus statut) {
        this.statut = statut;
    }

    public LieuStock getLieuStock() {
        return lieuStock;
    }

    public void setLieuStock(LieuStock lieuStock) {
        this.lieuStock = lieuStock;
    }
}
