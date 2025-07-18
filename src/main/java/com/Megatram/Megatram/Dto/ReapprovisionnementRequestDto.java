package com.Megatram.Megatram.Dto;

import java.util.List;

public class ReapprovisionnementRequestDto {

    public String source;
    public String agent;
    public List<LigneReapprovisionnementDto> lignes;

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

    public List<LigneReapprovisionnementDto> getLignes() {
        return lignes;
    }

    public void setLignes(List<LigneReapprovisionnementDto> lignes) {
        this.lignes = lignes;
    }
}
