package com.Megatram.Megatram.Entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.List; // N'oubliez pas cet import

@Entity
public class Inventaire {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private LocalDateTime date = LocalDateTime.now();
    private String charge;

    // AJOUTÉ : La relation OneToMany vers les lignes d'inventaire
    @OneToMany(mappedBy = "inventaire", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<LigneInventaire> lignes;

    // --- Getters et Setters ---

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
    public String getCharge() {
        return charge;
    }
    public void setCharge(String charge) {
        this.charge = charge;
    }

    // AJOUTÉ : Les getters et setters pour la liste de lignes
    public List<LigneInventaire> getLignes() {
        return lignes;
    }
    public void setLignes(List<LigneInventaire> lignes) {
        this.lignes = lignes;
    }
}