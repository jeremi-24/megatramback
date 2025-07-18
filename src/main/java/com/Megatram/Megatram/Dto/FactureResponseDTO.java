package com.Megatram.Megatram.Dto;

import com.Megatram.Megatram.Entity.Facture;
import com.Megatram.Megatram.Entity.LigneCommande;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

public class FactureResponseDTO {

    private Long idFacture;
    private LocalDateTime dateFacture;
    private Long commandeId;
    private String clientNom;
    private double montantTotal;
    private List<LigneCommandeResponseDTO> lignes; // <-- Votre DTO parfait

    // Constructeur vide
    public FactureResponseDTO() {
    }

    // LE CONSTRUCTEUR CORRIGÉ
    public FactureResponseDTO(Facture facture, List<LigneCommandeResponseDTO> lignesDto) {
        this.idFacture = facture.getId();
        this.dateFacture = facture.getDateFacture();

        // On assigne directement la liste de DTOs préparée par le service
        this.lignes = lignesDto;

        if (facture.getCommande() != null) {
            this.commandeId = facture.getCommande().getId();
            this.clientNom = facture.getCommande().getClient() != null ? facture.getCommande().getClient().getNom() : "Client non spécifié";

            // On peut calculer le total à partir des DTOs déjà préparés
            this.montantTotal = lignesDto.stream()
                    .mapToDouble(LigneCommandeResponseDTO::getTotalLigne)
                    .sum();
        }
    }

    public Long getIdFacture() {
        return idFacture;
    }

    public void setIdFacture(Long idFacture) {
        this.idFacture = idFacture;
    }

    public LocalDateTime getDateFacture() {
        return dateFacture;
    }

    public void setDateFacture(LocalDateTime dateFacture) {
        this.dateFacture = dateFacture;
    }

    public Long getCommandeId() {
        return commandeId;
    }

    public void setCommandeId(Long commandeId) {
        this.commandeId = commandeId;
    }

    public String getClientNom() {
        return clientNom;
    }

    public void setClientNom(String clientNom) {
        this.clientNom = clientNom;
    }

    public double getMontantTotal() {
        return montantTotal;
    }

    public void setMontantTotal(double montantTotal) {
        this.montantTotal = montantTotal;
    }

    public List<LigneCommandeResponseDTO> getLignes() {
        return lignes;
    }

    public void setLignes(List<LigneCommandeResponseDTO> lignes) {
        this.lignes = lignes;
    }

    // Getters et Setters...

}