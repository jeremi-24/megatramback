package com.Megatram.Megatram.Dto;

import com.Megatram.Megatram.enums.StatutCommande;

import java.time.LocalDateTime;
import java.util.List;

public class CommandeResponseDTO {

    private Long id;
    private LocalDateTime date;
    private StatutCommande statut;
    private ClientDto client;
    private LieuStockDTO lieuLivraison;
    private double totalCommande;

    private List<LigneCommandeResponseDTO> lignes;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public LocalDateTime getDate() {
        return date;
    }

    public void setDate(LocalDateTime date) {
        this.date = date;
    }

    public StatutCommande getStatut() {
        return statut;
    }

    public void setStatut(StatutCommande statut) {
        this.statut = statut;
    }

    public ClientDto getClient() {
        return client;
    }

    public void setClient(ClientDto client) {
        this.client = client;
    }

    public LieuStockDTO getLieuLivraison() {
        return lieuLivraison;
    }

    public void setLieuLivraison(LieuStockDTO lieuLivraison) {
        this.lieuLivraison = lieuLivraison;
    }

    public List<LigneCommandeResponseDTO> getLignes() {
        return lignes;
    }

    public void setLignes(List<LigneCommandeResponseDTO> lignes) {
        this.lignes = lignes;
    }

    public double getTotalCommande() {
        return totalCommande;
    }

    public void setTotalCommande(double totalCommande) {
        this.totalCommande = totalCommande;
    }
}
