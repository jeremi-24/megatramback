package com.Megatram.Megatram.service;

import com.Megatram.Megatram.Dto.BonLivraisonResponseDTO;
import com.Megatram.Megatram.Dto.LigneLivraisonDTO;
import com.Megatram.Megatram.Entity.*;
import com.Megatram.Megatram.enums.BonLivraisonStatus;
import com.Megatram.Megatram.enums.StatutCommande;
import com.Megatram.Megatram.repository.*;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class BonLivraisonService {


    private final BonLivraisonRepository bonLivraisonRepository;
    private final CommandeRepository commandeRepository;
    private final ProduitRepos produitRepos;
    private final LigneLivraisonRepository ligneLivraisonRepository;
    private final VenteService venteService;
    private final NotificationService notificationService; // <-- ÉTAPE 1 : DÉCLARATION

    @Autowired
    public BonLivraisonService(BonLivraisonRepository bonLivraisonRepository,
                               CommandeRepository commandeRepository,
                               ProduitRepos produitRepos,
                               LigneLivraisonRepository ligneLivraisonRepository,
                               VenteService venteService,
                               NotificationService notificationService) { // <-- ÉTAPE 2 : INJECTION
        this.bonLivraisonRepository = bonLivraisonRepository;
        this.commandeRepository = commandeRepository;
        this.produitRepos = produitRepos;
        this.ligneLivraisonRepository = ligneLivraisonRepository;
        this.venteService = venteService;
        this.notificationService = notificationService; // <-- ASSIGNATION
    }

    /**
     * Génère un Bon de Livraison et notifie le magasinier concerné.
     */
    public BonLivraisonResponseDTO genererBonLivraison(Long commandeId) {
        Commande commande = commandeRepository.findById(commandeId)
                .orElseThrow(() -> new EntityNotFoundException("Commande non trouvée: " + commandeId));

        if (commande.getStatut() != StatutCommande.VALIDEE) {
            throw new IllegalStateException("Un Bon de Livraison ne peut être généré que pour une commande VALIDÉE.");
        }
        if (bonLivraisonRepository.existsByCommandeId(commandeId)) {
            throw new IllegalStateException("Un Bon de Livraison existe déjà pour cette commande.");
        }

        // 1. Créer l'objet parent BonLivraison en mémoire
        BonLivraison bl = new BonLivraison();
        bl.setCommande(commande);
        bl.setDateLivraison(LocalDateTime.now());
        bl.setStatut(BonLivraisonStatus.A_LIVRER);

        // 2. Créer les lignes de livraison et établir la relation bidirectionnelle
        List<LigneLivraison> lignes = new ArrayList<>();
        for (LigneCommande ligneCommande : commande.getLignes()) {
            LigneLivraison ligneLivraison = new LigneLivraison();

            // CORRECTION : Établir la relation de la ligne vers son parent (BonLivraison)
            ligneLivraison.setBonLivraison(bl);

            ligneLivraison.setProduitId(ligneCommande.getProduitId());
            ligneLivraison.setQteLivre(ligneCommande.getQteVoulu());
            ligneLivraison.setProduitPrix(ligneCommande.getProduitPrix());
            lignes.add(ligneLivraison);
        }

        // 3. Associer la liste complète des lignes au Bon de Livraison parent
        bl.setLignesLivraison(lignes);

        // 4. SAUVEGARDER LE PARENT SEULEMENT.
        // La cascade s'occupe de sauvegarder les lignes et de définir les clés étrangères.
        BonLivraison savedBl = bonLivraisonRepository.save(bl);

        // --- La logique de notification reste la même ---
        LieuStock lieuDuMagasinier = commande.getLieuStock();
        if (lieuDuMagasinier != null) {
            String destination = "/topic/magasin/" + lieuDuMagasinier.getId();
            String message = "Nouveau Bon de Livraison N°" + savedBl.getId() + " à préparer pour le client '" + commande.getClient().getNom() + "'.";
            notificationService.envoyerNotification(destination, message);
        }

        return buildResponseDTO(savedBl);
    }
    /**
     * Action du magasinier : valide la livraison, décrémente le stock, crée la vente et notifie la secrétaire.
     */
    public BonLivraisonResponseDTO validerEtLivrer(Long bonLivraisonId, String agentEmail) {
        BonLivraison bl = bonLivraisonRepository.findById(bonLivraisonId)
                .orElseThrow(() -> new EntityNotFoundException("Bon de Livraison non trouvé: " + bonLivraisonId));

        if (bl.getStatut() != BonLivraisonStatus.A_LIVRER) {
            throw new IllegalStateException("Cette livraison a déjà été validée.");
        }

        // --- Logique de décrémentation du stock (inchangée) ---
        for (LigneLivraison ligne : bl.getLignesLivraison()) {
            Produit produit = produitRepos.findById(ligne.getProduitId())
                    .orElseThrow(() -> new EntityNotFoundException("Produit non trouvé: " + ligne.getProduitId()));
            if (produit.getQte() < ligne.getQteLivre()) {
                throw new IllegalStateException("Stock insuffisant pour le produit '" + produit.getNom() + "'.");
            }
            produit.setQte(produit.getQte() - ligne.getQteLivre());
            produitRepos.save(produit);
        }

        // --- Mise à jour des statuts (inchangée) ---
        bl.setStatut(BonLivraisonStatus.LIVRE);
        bl.getCommande().setStatut(StatutCommande.VALIDEE); // La commande est maintenant terminée
        BonLivraison updatedBl = bonLivraisonRepository.save(bl);

        // --- Création de la vente (inchangée) ---
        venteService.creerVenteDepuisBonLivraison(updatedBl, agentEmail);

        // --- NOTIFICATION DE CONFIRMATION À LA SECRÉTAIRE ---
        String messageConfirmation = "La livraison N°" + updatedBl.getId() + " (commande N°" + updatedBl.getCommande().getId() + ") a été complétée.";
        notificationService.envoyerNotification("/topic/secretariat", messageConfirmation);

        return buildResponseDTO(updatedBl);
    }
    /**
     * Récupère les BLs pour un lieu de stock spécifique.
     * C'est ce que le magasinier utilisera pour voir sa liste de travail.
     */
    @Transactional(readOnly = true)
    public List<BonLivraisonResponseDTO> getBonsLivraisonParLieu(Long lieuId) {
        return bonLivraisonRepository.findByCommande_LieuStock_Id(lieuId).stream()
                .map(this::buildResponseDTO)
                .collect(Collectors.toList());
    }

    private BonLivraisonResponseDTO buildResponseDTO(BonLivraison bon) {
        BonLivraisonResponseDTO dto = new BonLivraisonResponseDTO();
        dto.setId(bon.getId());
        dto.setDateLivraison(bon.getDateLivraison());
        dto.setCommandeId(bon.getCommande().getId());
        dto.setLignesLivraison(mapLignes(bon.getLignesLivraison()));
        dto.setLieuStock(bon.getCommande().getLieuStock());
        dto.setStatut(bon.getStatut()); // ✅ Ne pas oublier cette ligne
        return dto;
    }

    private BonLivraisonResponseDTO buildResponseDTO(BonLivraison bon, String email) {
        BonLivraisonResponseDTO dto = new BonLivraisonResponseDTO();
        dto.setId(bon.getId());
        dto.setDateLivraison(bon.getDateLivraison());
        dto.setCommandeId(bon.getCommande().getId());
        dto.setLignesLivraison(mapLignes(bon.getLignesLivraison()));
        dto.setLieuStock(bon.getCommande().getLieuStock());
        dto.setStatut(bon.getStatut());
        dto.setEmail(email);
        return dto;
    }



    /**
     * Méthode "assistante" privée pour convertir une entité BonLivraison en DTO.
     */

    private List<LigneLivraisonDTO> mapLignes(List<LigneLivraison> lignesLivraison) {
        return lignesLivraison.stream()
                .map(ligne -> {
                    Produit produit = produitRepos.findById(ligne.getProduitId()).orElse(null);
                    LigneLivraisonDTO dto = new LigneLivraisonDTO();
                    dto.setId(ligne.getId());
                    dto.setProduitNom(produit != null ? produit.getNom() : "Inconnu");
                    dto.setQteLivre(ligne.getQteLivre());
                    dto.setProduitPrix(ligne.getProduitPrix());
                    return dto;
                })
                .collect(Collectors.toList());
    }






    public List<BonLivraisonResponseDTO> getAllBonsLivraison() {
        String email = getEmailUtilisateurConnecte();
        List<BonLivraison> bons = bonLivraisonRepository.findAll();
        return bons.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    public String getEmailUtilisateurConnecte() {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (principal instanceof UserDetails) {
            return ((UserDetails) principal).getUsername();  // généralement l’email ou le login
        } else {
            return principal.toString();
        }
    }

    private BonLivraisonResponseDTO convertToDto(BonLivraison bon) {
        String email = getEmailUtilisateurConnecte(); // récupère ici l'email

        BonLivraisonResponseDTO dto = new BonLivraisonResponseDTO();
        dto.setId(bon.getId());
        dto.setDateLivraison(bon.getDateLivraison());
        dto.setCommandeId(bon.getCommande() != null ? bon.getCommande().getId() : null);
        dto.setStatut(bon.getStatut());

        if (bon.getCommande() != null && bon.getCommande().getLieuStock() != null) {
            dto.setLieuStock(bon.getCommande().getLieuStock());
        } else {
            dto.setLieuStock(bon.getLieuStock());
        }

        List<LigneLivraisonDTO> lignesDTO = bon.getLignesLivraison().stream()
                .map(ligne -> {
                    LigneLivraisonDTO ligneDTO = new LigneLivraisonDTO();
                    ligneDTO.setId(ligne.getId());
                    ligneDTO.setQteLivre(ligne.getQteLivre());
                    ligneDTO.setProduitPrix(ligne.getProduitPrix());

                    if (ligne.getProduitId() != null) {
                        produitRepos.findById(ligne.getProduitId())
                                .ifPresent(produit -> ligneDTO.setProduitNom(produit.getNom()));
                    } else {
                        ligneDTO.setProduitNom("Inconnu");
                    }
                    return ligneDTO;
                })
                .collect(Collectors.toList());

        dto.setLignesLivraison(lignesDTO);

        dto.setEmail(email);

        return dto;
    }







}