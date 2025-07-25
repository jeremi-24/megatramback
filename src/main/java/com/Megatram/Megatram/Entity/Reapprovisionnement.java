package com.Megatram.Megatram.Entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.List; // Importer List

@Entity
public class Reapprovisionnement {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String source;
    private String agent;

    private LocalDateTime date = LocalDateTime.now();

    // AJOUTÉ : La relation manquante vers les lignes
    @OneToMany(mappedBy = "reapprovisionnement", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<LigneReapprovisionnement> lignes;

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public String getAgent() {
        return agent;
    }

    public void setAgent(String agent) {
        this.agent = agent;
    }

    public LocalDateTime getDate() {
        return date;
    }

    public void setDate(LocalDateTime date) {
        this.date = date;
    }

    // AJOUTÉ : Le getter et setter pour la nouvelle relation
    public List<LigneReapprovisionnement> getLignes() {
        return lignes;
    }

    public void setLignes(List<LigneReapprovisionnement> lignes) {
        this.lignes = lignes;
    }
}