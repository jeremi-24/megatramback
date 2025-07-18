package com.Megatram.Megatram.service;

import com.Megatram.Megatram.Dto.*;
import com.Megatram.Megatram.Entity.*;
import com.Megatram.Megatram.enums.StatutCommande;
import com.Megatram.Megatram.repository.ClientRepository;
import com.Megatram.Megatram.repository.CommandeRepository;
import com.Megatram.Megatram.repository.LieuStockRepository;
import com.Megatram.Megatram.repository.ProduitRepos;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
public class CommandeService {

    @Autowired
    private CommandeRepository commandeRepository;
    @Autowired
    private ClientRepository clientRepository;
    @Autowired
    private ProduitRepos produitRepos;
    // Les services de facture et BL sont toujours nécessaires pour la validation
    @Autowired
    private FactureService factureService;
    @Autowired
    private BonLivraisonService bonLivraisonService;

    @Transactional
    public CommandeResponseDTO creerCommande(CommandeRequestDTO requestDTO) {
        Client client = clientRepository.findById(requestDTO.getClientId())
                .orElseThrow(() -> new EntityNotFoundException("Client non trouvé avec l'ID : " + requestDTO.getClientId()));

        if (requestDTO.getLignes() == null || requestDTO.getLignes().isEmpty()) {
            throw new IllegalArgumentException("Une commande ne peut pas être créée sans lignes de commande.");
        }

        Commande commande = new Commande();
        commande.setClient(client);
        commande.setDate(LocalDateTime.now());
        commande.setStatut(StatutCommande.EN_ATTENTE);

        // **LOGIQUE MISE À JOUR : Déterminer le LieuStock à partir des produits**
        LieuStock lieuDeStockDeReference = null;
        List<LigneCommande> lignes = new ArrayList<>();

        for (LigneCommandeRequestDTO ligneDto : requestDTO.getLignes()) {
            Produit produit = produitRepos.findById(ligneDto.getProduitId())
                    .orElseThrow(() -> new EntityNotFoundException("Produit non trouvé avec l'ID : " + ligneDto.getProduitId()));

            // Initialiser le lieu de référence avec celui du premier produit
            if (lieuDeStockDeReference == null) {
                lieuDeStockDeReference = produit.getLieuStock();
                if (lieuDeStockDeReference == null) {
                    throw new IllegalStateException("Le produit ID " + produit.getId() + " n'est associé à aucun lieu de stock.");
                }
            } else {
                // Vérifier que tous les autres produits viennent du même lieu
                if (!Objects.equals(produit.getLieuStock().getId(), lieuDeStockDeReference.getId())) {
                    throw new IllegalArgumentException("Tous les produits d'une même commande doivent provenir du même lieu de stock.");
                }
            }

            LigneCommande ligne = new LigneCommande();
            ligne.setProduitId(produit.getId());
            ligne.setProduitPrix(produit.getPrix());
            ligne.setQteVoulu(ligneDto.getQteVoulu());
            ligne.setCommande(commande);
            lignes.add(ligne);
        }

        // Assigner le lieu de stock déterminé à la commande
        commande.setLieuStock(lieuDeStockDeReference);
        commande.setLignes(lignes);

        Commande savedCommande = commandeRepository.save(commande);
        return convertToResponseDTO(savedCommande);
    }

    // --- Les autres méthodes restent similaires mais dépendent de la conversion corrigée ---

    public List<CommandeResponseDTO> recupererToutesLesCommandes() {
        return commandeRepository.findAll().stream()
                .map(this::convertToResponseDTO)
                .collect(Collectors.toList());
    }

    public CommandeResponseDTO recupererCommandeParId(Long id) {
        Commande commande = commandeRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Commande non trouvée avec l'ID : " + id));
        return convertToResponseDTO(commande);
    }

    @Transactional
    public ValidationResponse validerCommande(Long id) {
        Commande commande = commandeRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Commande non trouvée avec l'ID : " + id));
        if (commande.getStatut() != StatutCommande.EN_ATTENTE) {
            throw new IllegalStateException("Seule une commande EN_ATTENTE peut être validée.");
        }

        commande.setStatut(StatutCommande.VALIDEE);
        Commande commandeValidee = commandeRepository.save(commande);

        FactureResponseDTO factureDto = factureService.genererFacturePourCommande(commandeValidee.getId());
        BonLivraisonResponseDTO bonLivraisonDto = bonLivraisonService.genererBonLivraison(commandeValidee.getId());

        return new ValidationResponse(convertToResponseDTO(commandeValidee), factureDto, bonLivraisonDto);
    }

    @Transactional
    public CommandeResponseDTO modifierCommande(Long id, CommandeRequestDTO requestDTO) {
        // La logique de modification doit aussi ré-évaluer le lieu de stock
        // pour rester cohérente.
        Commande commande = commandeRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Commande non trouvée avec l'ID : " + id));
        if (commande.getStatut() != StatutCommande.EN_ATTENTE) {
            throw new IllegalStateException("Seule une commande EN_ATTENTE peut être modifiée.");
        }

        // Mettre à jour le client
        Client client = clientRepository.findById(requestDTO.getClientId())
                .orElseThrow(() -> new EntityNotFoundException("Client non trouvé."));
        commande.setClient(client);

        // Vider les anciennes lignes et reconstruire avec la même logique que pour la création
        commande.getLignes().clear();
        LieuStock lieuDeStockDeReference = null;
        for (LigneCommandeRequestDTO ligneDto : requestDTO.getLignes()) {
            Produit produit = produitRepos.findById(ligneDto.getProduitId())
                    .orElseThrow(() -> new EntityNotFoundException("Produit non trouvé avec l'ID : " + ligneDto.getProduitId()));

            if (lieuDeStockDeReference == null) {
                lieuDeStockDeReference = produit.getLieuStock();
            } else if (!Objects.equals(produit.getLieuStock().getId(), lieuDeStockDeReference.getId())) {
                throw new IllegalArgumentException("Tous les produits doivent provenir du même lieu de stock.");
            }

            LigneCommande nouvelleLigne = new LigneCommande();
            nouvelleLigne.setProduitId(produit.getId());
            nouvelleLigne.setProduitPrix(produit.getPrix());
            nouvelleLigne.setQteVoulu(ligneDto.getQteVoulu());
            nouvelleLigne.setCommande(commande);
            commande.getLignes().add(nouvelleLigne);
        }
        commande.setLieuStock(lieuDeStockDeReference);

        Commande commandeModifiee = commandeRepository.save(commande);
        return convertToResponseDTO(commandeModifiee);
    }


    /**
     * **MÉTHODE DE CONVERSION CORRIGÉE**
     * C'est ici que le problème des valeurs `null` est résolu.
     */
    private CommandeResponseDTO convertToResponseDTO(Commande commande) {
        CommandeResponseDTO dto = new CommandeResponseDTO();
        dto.setId(commande.getId());
        dto.setDate(commande.getDate());
        dto.setStatut(commande.getStatut());

        // **CORRECTION : Mapper explicitement les entités Client et LieuStock en leurs DTOs**
        if (commande.getClient() != null) {
            dto.setClient(new ClientDto(commande.getClient()));
        }
        if (commande.getLieuStock() != null) {
            dto.setLieuLivraison(new LieuStockDTO(commande.getLieuStock()));
        }

        // Conversion des lignes de commande
        List<LigneCommandeResponseDTO> lignesDto = commande.getLignes().stream().map(ligne -> {
            Produit produit = produitRepos.findById(ligne.getProduitId()).orElse(null); // Gérer le cas où le produit serait supprimé
            return new LigneCommandeResponseDTO(ligne, produit);
        }).collect(Collectors.toList());

        dto.setLignes(lignesDto);
        dto.setTotalCommande(lignesDto.stream().mapToDouble(LigneCommandeResponseDTO::getTotalLigne).sum());

        return dto;
    }


    @Transactional(readOnly = true) // C'est une opération de lecture seule, c'est une bonne pratique de le spécifier
    public List<CommandeResponseDTO> recupererCommandesParClientId(Long clientId) {
        // 1. Appeler la nouvelle méthode du repository
        List<Commande> commandes = commandeRepository.findByClientId(clientId);

        // 2. Mapper la liste d'entités en liste de DTOs en utilisant votre méthode existante
        return commandes.stream()
                .map(this::convertToResponseDTO)
                .collect(Collectors.toList());
    }

    // Classe interne pour la réponse de validation
    public static class ValidationResponse {
        // ... (inchangée)
        private CommandeResponseDTO commande;
        private FactureResponseDTO facture;
        private BonLivraisonResponseDTO bonLivraison;
        public ValidationResponse(CommandeResponseDTO commande, FactureResponseDTO facture, BonLivraisonResponseDTO bonLivraison) { this.commande = commande; this.facture = facture; this.bonLivraison = bonLivraison; }
        public CommandeResponseDTO getCommande() { return commande; }
        public FactureResponseDTO getFacture() { return facture; }
        public BonLivraisonResponseDTO getBonLivraison() { return bonLivraison; }
    }
}