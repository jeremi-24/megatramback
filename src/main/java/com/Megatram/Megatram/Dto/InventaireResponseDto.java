package com.Megatram.Megatram.Dto;

import com.Megatram.Megatram.Entity.Inventaire;

import java.time.LocalDateTime;
import java.util.Date; // Maintenir si java.util.Date est aussi utilisé
import java.util.List;
import java.util.stream.Collectors;

public class InventaireResponseDto {

    public Long inventaireId;
    public String charge;
    public LocalDateTime date; // Utiliser LocalDateTime si c'est le type dans l'entité
    public List<LigneResponseDto> lignes;


    public InventaireResponseDto() {
    }

    // Constructeur pour créer un DTO de réponse à partir de l'entité Inventaire
    public InventaireResponseDto(Inventaire inventaire) {
        this.inventaireId = inventaire.getId();
        this.charge = inventaire.getCharge();
        this.date = inventaire.getDate(); // Lire la date de l'entité

        // Mapper les lignes en utilisant le constructeur mis à jour de LigneResponseDto
        if (inventaire.getLignes() != null) { // Assurez-vous que l'entité Inventaire a une liste de LigneInventaire et une méthode getLignes
            this.lignes = inventaire.getLignes().stream()
                            // Utiliser le constructeur qui prend l'entité LigneInventaire
                            .map(LigneResponseDto::new)
                            .collect(Collectors.toList());
        } else {
            this.lignes = null;
        }
    }


    // Getters and Setters

    public Long getInventaireId() {
        return inventaireId;
    }

    public void setInventaireId(Long inventaireId) {
        this.inventaireId = inventaireId;
    }

    public String getCharge() {
        return charge;
    }

    public void setCharge(String charge) {
        this.charge = charge;
    }

    public LocalDateTime getDate() {
        return date;
    }

    public void setDate(LocalDateTime date) {
        this.date = date;
    }

    public List<LigneResponseDto> getLignes() {
        return lignes;
    }

    public void setLignes(List<LigneResponseDto> lignes) {
        this.lignes = lignes;
    }
}
