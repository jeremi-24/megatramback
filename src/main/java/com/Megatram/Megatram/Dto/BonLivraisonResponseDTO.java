package com.Megatram.Megatram.Dto;

import com.Megatram.Megatram.Entity.BonLivraison;
import com.Megatram.Megatram.Entity.LieuStock;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

public class BonLivraisonResponseDTO {

    private Long id;
    private LocalDateTime dateLivraison;
    private Long commandeId;
    private List<LigneLivraisonDTO> lignesLivraison;
    private String status;
    private LieuStock lieuStock;
    private String email;
    private double totalLivraison; // CHAMP AJOUTÉ

    public BonLivraisonResponseDTO() {
    }

    public BonLivraisonResponseDTO(BonLivraison bonLivraison) {
        this.id = bonLivraison.getId();
        this.dateLivraison = bonLivraison.getDateLivraison();

        if (bonLivraison.getCommande() != null) {
            this.commandeId = bonLivraison.getCommande().getId();
            // Calcule et assigne le total de la commande
            this.totalLivraison = bonLivraison.getCommande().getLignes().stream()
                    .mapToDouble(ligne -> ligne.getQteVoulu() * ligne.getProduitPrix())
                    .sum();
        }

        if (bonLivraison.getStatut() != null) {
            this.status = bonLivraison.getStatut().name();
        }

        if (bonLivraison.getLignesLivraison() != null) {
            this.lignesLivraison = bonLivraison.getLignesLivraison().stream()
                    .map(LigneLivraisonDTO::new)
                    .collect(Collectors.toList());
        }

        this.lieuStock = bonLivraison.getLieuStock();
    }

    // --- Getters and Setters ---
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public LocalDateTime getDateLivraison() { return dateLivraison; }
    public void setDateLivraison(LocalDateTime dateLivraison) { this.dateLivraison = dateLivraison; }
    public Long getCommandeId() { return commandeId; }
    public void setCommandeId(Long commandeId) { this.commandeId = commandeId; }
    public List<LigneLivraisonDTO> getLignesLivraison() { return lignesLivraison; }
    public void setLignesLivraison(List<LigneLivraisonDTO> lignesLivraison) { this.lignesLivraison = lignesLivraison; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public LieuStock getLieuStock() { return lieuStock; }
    public void setLieuStock(LieuStock lieuStock) { this.lieuStock = lieuStock; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    
    // GETTER ET SETTER AJOUTÉS
    public double getTotalLivraison() { return totalLivraison; }
    public void setTotalLivraison(double totalLivraison) { this.totalLivraison = totalLivraison; }
}