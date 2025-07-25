package com.Megatram.Megatram.Dto;

import com.Megatram.Megatram.Entity.Reapprovisionnement;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

public class ReapprovisionnementResponseDto {

    private Long id;
    private String source;
    private String agent;
    private LocalDateTime date;
    private List<LigneReapprovisionnementResponseDto> lignes;

    public ReapprovisionnementResponseDto() {
    }

    // Constructeur pratique pour convertir une entité en DTO
    public ReapprovisionnementResponseDto(Reapprovisionnement reappro) {
        this.id = reappro.getId();
        this.source = reappro.getSource();
        this.agent = reappro.getAgent();
        this.date = reappro.getDate();
        if (reappro.getLignes() != null) {
            this.lignes = reappro.getLignes().stream()
                .map(LigneReapprovisionnementResponseDto::new) // Utilise le constructeur de LigneReapprovisionnementResponseDto
                .collect(Collectors.toList());
        }
    }

    // --- Getters and Setters ---

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

    public List<LigneReapprovisionnementResponseDto> getLignes() {
        return lignes;
    }

    public void setLignes(List<LigneReapprovisionnementResponseDto> lignes) {
        this.lignes = lignes;
    }
}