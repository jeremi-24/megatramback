package com.Megatram.Megatram.Entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.List;

@Entity
public class Vente {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String ref;
    private String caissier;

    @ManyToOne
    @JoinColumn(name = "commande_id")
    private Commande commande;

    @ManyToOne
    @JoinColumn(name = "client_id")
    private Client client;

    // CORRIGÉ : Renommé de 'paiement' à 'total' pour la clarté
    private double total;

    @OneToMany(mappedBy = "vente", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<LigneVente> lignes;

    private LocalDateTime date;

 @ManyToOne
 @JoinColumn(name = "lieu_stock_id")
 private LieuStock lieuStock;

    @PrePersist
    @PreUpdate
    public void prePersistAndUpdate() {
        if (this.date == null) {
            this.date = LocalDateTime.now();
        }
        if (lignes != null && !lignes.isEmpty()) {
            // CORRIGÉ : Met à jour le champ 'total'
            this.total = lignes.stream()
                    .mapToDouble(LigneVente::getTotal)
                    .sum();
        } else {
            this.total = 0.0;
        }
    }

    // Getters & Setters
    public LocalDateTime getDate() { return date; }
    public void setDate(LocalDateTime date) { this.date = date; }
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getRef() { return ref; }
    public void setRef(String ref) { this.ref = ref; }
    public String getCaissier() { return caissier; }
    public void setCaissier(String caissier) { this.caissier = caissier; }
    public Client getClient() { return client; }
    public void setClient(Client client) { this.client = client; }
    public List<LigneVente> getLignes() { return lignes; }
    public void setLignes(List<LigneVente> lignes) { this.lignes = lignes; }

    // CORRIGÉ : Getter et Setter pour le champ 'total'
    public double getTotal() {
        return total;
    }
    public void setTotal(double total) {
        this.total = total;
    }

    // Getter et Setter pour lieuStock
    public LieuStock getLieuStock() {
 return lieuStock;
    }

    public void setLieuStock(LieuStock lieuStock) {
 this.lieuStock = lieuStock;
    }
}