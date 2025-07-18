package com.Megatram.Megatram.Dto;

import com.Megatram.Megatram.Entity.Vente;

import java.time.LocalDateTime;
import java.util.List;

public class VenteResponseDTO {

    private Long id;
    private String ref;
    private LocalDateTime date;
    private String caissier;
    private ClientDto client;
    private double paiement;
    private List<LigneVenteDto> lignes;

    public VenteResponseDTO(Vente vente, List<LigneVenteDto> lignesDto, ClientDto clientDto) {
        this.id = vente.getId();
        this.ref = vente.getRef();
        this.caissier = vente.getCaissier();
        this.paiement = vente.getPaiement();
        this.date = vente.getDate();
        this.lignes = lignesDto;
        this.client = clientDto;
    }
}
