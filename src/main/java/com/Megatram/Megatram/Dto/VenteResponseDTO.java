package com.Megatram.Megatram.Dto;

import com.Megatram.Megatram.Entity.Vente;
import java.time.LocalDateTime; // CORRIGÉ : Utiliser LocalDateTime
import java.util.List;
import java.util.stream.Collectors;

public class VenteResponseDTO {

    private Long id;
    private LocalDateTime date; // CORRIGÉ : Type de date
    private String ref;
    private String caissier;
    private ClientDto client;
    private List<LigneVenteDto> lignes;
    private double total;

    public VenteResponseDTO() {
    }

    public VenteResponseDTO(Vente vente) {
        this.id = vente.getId();
        this.date = vente.getDate(); // CORRIGÉ : Le type correspond
        this.ref = vente.getRef();
        this.caissier = vente.getCaissier();

        if (vente.getClient() != null) {
            this.client = new ClientDto(vente.getClient());
        }

        if (vente.getLignes() != null) {
            this.lignes = vente.getLignes().stream()
                            .map(LigneVenteDto::new)
                            .collect(Collectors.toList());
        }

        this.total = vente.getTotal(); // CORRIGÉ : La méthode getTotal() existe maintenant sur l'entité
    }

    // --- Getters and Setters ---
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public LocalDateTime getDate() { return date; } // CORRIGÉ
    public void setDate(LocalDateTime date) { this.date = date; } // CORRIGÉ
    public String getRef() { return ref; }
    public void setRef(String ref) { this.ref = ref; }
    public String getCaissier() { return caissier; }
    public void setCaissier(String caissier) { this.caissier = caissier; }
    public ClientDto getClient() { return client; }
    public void setClient(ClientDto client) { this.client = client; }
    public List<LigneVenteDto> getLignes() { return lignes; }
    public void setLignes(List<LigneVenteDto> lignes) { this.lignes = lignes; }
    public double getTotal() { return total; }
    public void setTotal(double total) { this.total = total; }
}