package com.Megatram.Megatram.Dto;

import java.time.LocalDateTime;
import java.util.List;

public class InventaireResponseDto {
    public Long inventaireId;
    public String charge;
    public LocalDateTime date;
    public List<LigneResponseDto> lignes;


}