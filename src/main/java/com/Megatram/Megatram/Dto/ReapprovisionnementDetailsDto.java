package com.Megatram.Megatram.Dto;

import java.time.LocalDateTime;
import java.util.List;

public class ReapprovisionnementDetailsDto {

    public Long id;
    public String source;
    public String agent;
    public LocalDateTime date;
    public List<LigneReapprovisionnementDto> lignes;

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

    public List<LigneReapprovisionnementDto> getLignes() {
        return lignes;
    }

    public void setLignes(List<LigneReapprovisionnementDto> lignes) {
        this.lignes = lignes;
    }
}
