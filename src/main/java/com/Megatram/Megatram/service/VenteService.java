package com.Megatram.Megatram.service;

import com.Megatram.Megatram.Dto.*;
import com.Megatram.Megatram.Entity.*;
import com.Megatram.Megatram.repository.*;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class VenteService {

    private final VenteRepository venteRepository;
    private final ProduitRepos produitRepos;
    private final LigneVenteRepo LigneVenteRepo;
    private final ClientRepository clientRepository;
    private final StockService stockService;

    @Autowired
    public VenteService(VenteRepository venteRepository, ProduitRepos produitRepos, LigneVenteRepo LigneVenteRepo, ClientRepository clientRepository, StockService stockService) {
        this.venteRepository = venteRepository;
        this.produitRepos = produitRepos;
        this.LigneVenteRepo = LigneVenteRepo;
        this.clientRepository = clientRepository;
        this.stockService = stockService;
    }

    public void creerVenteDepuisBonLivraison(BonLivraison bl, String agentEmail) {
        Commande commande = bl.getCommande();
        if (venteRepository.existsByCommande_Id(commande.getId())) {
            return;
        }

        Vente vente = new Vente();
        vente.setClient(commande.getClient());
        vente.setCaissier(agentEmail);
        vente.setRef("VENTE-CMD-" + commande.getId());

        List<LigneVente> lignesVente = new ArrayList<>();
        for (LigneLivraison ligneLivraison : bl.getLignesLivraison()) {
            LigneVente ligneVente = new LigneVente();
            
            // CORRIGÉ : L'entité LigneLivraison a une relation directe vers Produit
            Produit produit = ligneLivraison.getProduit(); 
            if (produit == null) {
                throw new EntityNotFoundException("Produit non trouvé pour la ligne de livraison id=" + ligneLivraison.getId());
            }
            ligneVente.setProduit(produit);
            ligneVente.setQteVendu(ligneLivraison.getQteLivre());
            ligneVente.setProduitPrix(ligneLivraison.getProduitPrix());
            ligneVente.setTypeQuantite(ligneLivraison.getTypeQuantite());
            ligneVente.setVente(vente);
            lignesVente.add(ligneVente);
        }
        vente.setLignes(lignesVente);
        venteRepository.save(vente);
    }

    public VenteResponseDTO creerVenteDirecte(VenteDto venteDto, String agentEmail) {
        Vente vente = new Vente();
        vente.setCaissier(agentEmail);
        vente.setRef(venteDto.getRef());

        Client client = clientRepository.findById(venteDto.getClientId())
                .orElseThrow(() -> new EntityNotFoundException("Client non trouvé : ID " + venteDto.getClientId()));
        vente.setClient(client);

        List<LigneVente> lignes = new ArrayList<>();
        for (LigneVenteDto ligneDto : venteDto.getLignes()) {
            Produit produit = produitRepos.findById(ligneDto.getProduitId())
                    .orElseThrow(() -> new EntityNotFoundException("Produit introuvable: id=" + ligneDto.getProduitId()));

            LieuStock lieuStock = produit.getLieuStock();
            if (lieuStock == null) {
                 throw new IllegalStateException("Le produit " + produit.getNom() + " n'a pas de lieu de stock attribué.");
            }
            
            // CORRIGÉ : Le getter est getQteVendueDansLigne()
            int quantiteVendue = ligneDto.getQteVendueDansLigne(); 
            int qteParCarton = produit.getQteParCarton();
            int quantiteTotaleVendueEnUnites;
            double prixApplique;

            if ("CARTON".equalsIgnoreCase(ligneDto.getTypeQuantite())) {
                if (qteParCarton <= 0) {
                    throw new IllegalStateException("Le produit " + produit.getNom() + " n'a pas une quantité par carton valide.");
                }
                quantiteTotaleVendueEnUnites = quantiteVendue * qteParCarton;
                prixApplique = produit.getPrixCarton();
            } else {
                quantiteTotaleVendueEnUnites = quantiteVendue;
                prixApplique = produit.getPrix();
            }

            stockService.removeStock(produit, lieuStock, quantiteTotaleVendueEnUnites);

            LigneVente ligne = new LigneVente();
            ligne.setProduit(produit);
            ligne.setQteVendu(quantiteVendue);
            ligne.setProduitPrix(prixApplique);
            ligne.setVente(vente);
            ligne.setTypeQuantite(ligneDto.getTypeQuantite());
            lignes.add(ligne);
        }
        vente.setLignes(lignes);

        Vente savedVente = venteRepository.save(vente);
        return new VenteResponseDTO(savedVente);
    }

    @Transactional(readOnly = true)
    public List<VenteResponseDTO> getAllVentes() {
        return venteRepository.findAll().stream()
                .map(VenteResponseDTO::new)
                .collect(Collectors.toList());
    }
}