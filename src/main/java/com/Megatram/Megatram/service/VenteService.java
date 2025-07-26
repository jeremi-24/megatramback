package com.Megatram.Megatram.service;

import com.Megatram.Megatram.Dto.*;
import com.Megatram.Megatram.Entity.*;
import com.Megatram.Megatram.repository.*;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

@Service
@Transactional
public class VenteService {

    private final VenteRepository venteRepository;
    private final ProduitRepos produitRepos;
    private final LigneVenteRepo LigneVenteRepo;
    private final ClientRepository clientRepository;
    private final StockService stockService;
    private final UtilisateurRepository utilisateurRepository; // Ajouté pour le caissier

    @Autowired
    public VenteService(VenteRepository venteRepository, ProduitRepos produitRepos, LigneVenteRepo LigneVenteRepo, ClientRepository clientRepository, StockService stockService, UtilisateurRepository utilisateurRepository) {
        this.venteRepository = venteRepository;
        this.produitRepos = produitRepos;
        this.LigneVenteRepo = LigneVenteRepo;
        this.clientRepository = clientRepository;
        this.stockService = stockService;
        this.utilisateurRepository = utilisateurRepository; // Ajouté
    }

    // ... (la méthode creerVenteDepuisBonLivraison reste inchangée)
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


    /**
     * CORRIGÉ : Cette méthode correspond maintenant aux DTOs sécurisés attendus du frontend.
     */
    public VenteResponseDTO createVenteDirecte(VenteDto venteDto, String agentEmail) {
        Vente vente = new Vente();
        vente.setCaissier(agentEmail);

        // Génération d'une référence de vente unique côté serveur
        long timestamp = Instant.now().toEpochMilli();
        long randomNum = ThreadLocalRandom.current().nextLong(1000, 9999);
        vente.setRef("POS-" + timestamp + "-" + randomNum);
        
        // Récupération du client à partir de l'ID fourni dans le DTO
        // Utilisation de clientId au lieu de getIdClient()
        if (venteDto.getClientId() != null && venteDto.getClientId() > 0) {
            Client client = clientRepository.findById(venteDto.getClientId())
                    .orElseThrow(() -> new EntityNotFoundException("Client non trouvé : ID " + venteDto.getClientId()));
            vente.setClient(client);
        }

        List<LigneVente> lignes = new ArrayList<>();
        // Utilisation de getLignes() au lieu de getLignesVente()
        for (LigneVenteDto ligneDto : venteDto.getLignes()) {
            
            // On cherche le produit par son CODE BARRE, pas par son ID
            Produit produit = produitRepos.findByCodeBarre(ligneDto.getCodeProduit())
                    .orElseThrow(() -> new EntityNotFoundException("Produit introuvable: code=" + ligneDto.getCodeProduit()));

            LieuStock lieuStock = produit.getLieuStock();
            if (lieuStock == null) {
                 throw new IllegalStateException("Le produit " + produit.getNom() + " n'a pas de lieu de stock attribué.");
            }
            
            // Le nom du champ de quantité est "quantite" dans LigneVenteDto
            int quantiteVendue = ligneDto.getQteVendueDansLigne();
            Integer qteParCarton = produit.getQteParCarton();
            int quantiteTotaleVendueEnUnites;
            double prixApplique;

            if ("CARTON".equalsIgnoreCase(ligneDto.getTypeQuantite())) {
                if (qteParCarton == null || qteParCarton <= 0) {
                    throw new IllegalStateException("Le produit " + produit.getNom() + " n'a pas une quantité par carton valide.");
                }
                quantiteTotaleVendueEnUnites = quantiteVendue * qteParCarton;
                prixApplique = produit.getPrixCarton();
            } else { // "UNITE"
                quantiteTotaleVendueEnUnites = quantiteVendue;
                prixApplique = produit.getPrix();
            }

            // Mise à jour du stock
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
