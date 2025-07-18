package com.Megatram.Megatram.service;

import com.Megatram.Megatram.Dto.*; // Assurez-vous d'avoir tous vos DTOs
import com.Megatram.Megatram.Entity.*;
import com.Megatram.Megatram.repository.*;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Transactional
public class VenteService {

    private final VenteRepository venteRepository;
    private final ProduitRepos produitRepos;
    private final LigneVenteRepo LigneVenteRepo;
    private final ClientRepository clientRepository;

    @Autowired
    public VenteService(VenteRepository venteRepository, ProduitRepos produitRepos, LigneVenteRepo LigneVenteRepo, ClientRepository clientRepository) {
        this.venteRepository = venteRepository;
        this.produitRepos = produitRepos;
        this.LigneVenteRepo = LigneVenteRepo;
        this.clientRepository = clientRepository;
    }

    /**
     * NOUVELLE MÉTHODE : Crée une Vente à partir d'un Bon de Livraison.
     * Cette méthode ne touche PAS au stock. Elle ne fait que créer l'enregistrement comptable.
     */
    public void creerVenteDepuisBonLivraison(BonLivraison bl, String agentEmail) {
        Commande commande = bl.getCommande();

        // Sécurité pour ne pas créer deux ventes pour la même commande
        if (venteRepository.existsByCommande_Id(commande.getId())) {
            return; // On sort silencieusement si la vente existe déjà
        }

        Vente vente = new Vente();
        vente.setClient(commande.getClient());
        vente.setCaissier(agentEmail); // L'agent qui a validé la livraison est le "caissier"
        vente.setRef("VENTE-CMD-" + commande.getId()); // Référence claire

        Vente savedVente = venteRepository.save(vente);

        List<LigneVente> lignesVente = new ArrayList<>();
        for (LigneLivraison ligneLivraison : bl.getLignesLivraison()) {
            LigneVente ligneVente = new LigneVente();
            ligneVente.setProduitId(ligneLivraison.getProduitId());
            ligneVente.setQteVendu(ligneLivraison.getQteLivre());
            ligneVente.setProduitPrix(ligneLivraison.getProduitPrix());
            ligneVente.setVente(savedVente);
            lignesVente.add(ligneVente);
        }
        LigneVenteRepo.saveAll(lignesVente);
    }

    /**
     * MÉTHODE EXISTANTE (légèrement améliorée) : Crée une Vente Directe (au comptoir).
     * C'est cette méthode qui décrémente le stock.
     */
    public VenteResponseDTO creerVenteDirecte(VenteDto venteDto, String agentEmail) {
        Vente vente = new Vente();
        vente.setCaissier(agentEmail);
        vente.setRef(venteDto.getRef()); // Ex: VENTE-DIRECTE-TIMESTAMP

        Client client = clientRepository.findById(venteDto.getClientId())
                .orElseThrow(() -> new EntityNotFoundException("Client non trouvé : ID " + venteDto.getClientId()));
        vente.setClient(client);

        List<LigneVente> lignes = new ArrayList<>();
        for (LigneVenteDto ligneDto : venteDto.getLignes()) {
            Produit produit = produitRepos.findById(ligneDto.getProduitId())
                    .orElseThrow(() -> new EntityNotFoundException("Produit introuvable: id=" + ligneDto.getProduitId()));

            if (produit.getQte() < ligneDto.getQteVendu()) {
                throw new IllegalStateException("Stock insuffisant pour le produit : " + produit.getNom());
            }

            LigneVente ligne = new LigneVente();
            ligne.setProduitId(produit.getId());
            ligne.setQteVendu(ligneDto.getQteVendu());
            ligne.setProduitPrix(produit.getPrix()); // Prix sécurisé depuis la BDD
            ligne.setVente(vente);
            lignes.add(ligne);

            // Décrémenter le stock immédiatement
            produit.setQte(produit.getQte() - ligneDto.getQteVendu());
            produitRepos.save(produit);
        }
        vente.setLignes(lignes);

        Vente savedVente = venteRepository.save(vente);
        return buildVenteResponseDTO(savedVente);
    }

    // --- Méthodes de lecture et suppression ---

    @Transactional(readOnly = true)
    public List<VenteResponseDTO> getAllVentes() {
        return venteRepository.findAll().stream()
                .map(this::buildVenteResponseDTO)
                .collect(Collectors.toList());
    }

    // ... Autres méthodes (getById, deleteVente...)

    /**
     * Méthode privée pour construire un DTO de réponse propre.
     */
    private VenteResponseDTO buildVenteResponseDTO(Vente vente) {
        ClientDto clientDto = (vente.getClient() != null) ? new ClientDto(vente.getClient()) : null;

        List<LigneVenteDto> lignesDto = vente.getLignes().stream()
                .map(ligne -> {
                    Produit p = produitRepos.findById(ligne.getProduitId()).orElse(null);
                    return new LigneVenteDto(ligne, p);
                })
                .collect(Collectors.toList());

        return new VenteResponseDTO(vente, lignesDto, clientDto);
    }
}