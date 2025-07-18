package com.Megatram.Megatram.Entity;

import com.Megatram.Megatram.enums.BonLivraisonStatus;
import jakarta.persistence.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Entity
public class BonLivraison {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    private LocalDateTime dateLivraison;

    @ManyToOne
    @JoinColumn(name = "commande_id")
    private Commande commande;

    @Enumerated(EnumType.STRING)
    private BonLivraisonStatus statut;

    @OneToMany(mappedBy = "bonLivraison", cascade = CascadeType.ALL)
    private List<LigneLivraison> lignesLivraison;

    @ManyToOne
    @JoinColumn(name = "lieuLivraison_id")
    private LieuStock lieuStock;


    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public LocalDateTime getDateLivraison() {
        return dateLivraison;
    }

    public void setDateLivraison(LocalDateTime dateLivraison) {
        this.dateLivraison = dateLivraison;
    }

    public Commande getCommande() {
        return commande;
    }

    public void setCommande(Commande commande) {
        this.commande = commande;
    }

    public BonLivraisonStatus getStatut() {
        return statut;
    }

    public void setStatut(BonLivraisonStatus statut) {
        this.statut = statut;
    }

    public List<LigneLivraison> getLignesLivraison() {
        return lignesLivraison;
    }

    public void setLignesLivraison(List<LigneLivraison> lignesLivraison) {
        this.lignesLivraison = lignesLivraison;
    }


    public LieuStock getLieuStock() {
        return lieuStock;
    }

    public void setLieuStock(LieuStock lieuStock) {
        this.lieuStock = lieuStock;
    }
}