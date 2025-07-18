package com.Megatram.Megatram.service;

import com.Megatram.Megatram.Dto.FactureResponseDTO;
import com.Megatram.Megatram.Dto.LigneCommandeResponseDTO;
import com.Megatram.Megatram.Entity.Commande;
import com.Megatram.Megatram.Entity.Facture;
import com.Megatram.Megatram.Entity.Produit;
import com.Megatram.Megatram.enums.StatutCommande;
import com.Megatram.Megatram.repository.CommandeRepository;
import com.Megatram.Megatram.repository.FactureRepository;
import com.Megatram.Megatram.repository.ProduitRepos;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class FactureService {


    private final FactureRepository factureRepository;
    private final CommandeRepository commandeRepository;
    private final ProduitRepos produitRepos;
    private final NotificationService notificationService; // <-- ÉTAPE 1 : DÉCLARATION

    @Autowired
    public FactureService(FactureRepository factureRepository,
                          CommandeRepository commandeRepository,
                          ProduitRepos produitRepos,
                          NotificationService notificationService) { // <-- ÉTAPE 2 : INJECTION
        this.factureRepository = factureRepository;
        this.commandeRepository = commandeRepository;
        this.produitRepos = produitRepos;
        this.notificationService = notificationService; // <-- ASSIGNATION
    }

    /**
     * Génère une nouvelle facture pour une commande et notifie les parties concernées.
     */
    public FactureResponseDTO genererFacturePourCommande(Long commandeId) {
        // 1. Récupérer la commande (inchangé)
        Commande commande = commandeRepository.findById(commandeId)
                .orElseThrow(() -> new EntityNotFoundException("Commande non trouvée avec l'ID : " + commandeId));

        // 2. Vérifier les règles métier (inchangé)
        if (commande.getStatut() != StatutCommande.VALIDEE) {
            throw new IllegalStateException("Une facture ne peut être générée que pour une commande VALIDÉE. Statut actuel : " + commande.getStatut());
        }
        if (factureRepository.existsByCommandeId(commandeId)) {
            throw new IllegalStateException("Une facture existe déjà pour la commande ID : " + commandeId);
        }

        // 3. Créer la nouvelle entité Facture (inchangé)
        Facture nouvelleFacture = new Facture();
        nouvelleFacture.setCommande(commande);
        nouvelleFacture.setDateFacture(LocalDateTime.now());
        Facture savedFacture = factureRepository.save(nouvelleFacture);

        // --- ÉTAPE 3 : ENVOI DE NOTIFICATIONS ---

        // Notification au client (la boutique qui a commandé) que sa facture est disponible.
        // On suppose que le nom du client est unique et peut servir à définir un canal.
        String clientNom = commande.getClient().getNom().replaceAll("\\s+", "-"); // Remplace les espaces pour une URL propre
        String destinationClient = "/topic/client/" + clientNom;
        String messageClient = "Votre facture N°" + savedFacture.getId() + " pour la commande N°" + commandeId + " est maintenant disponible.";
        notificationService.envoyerNotification(destinationClient, messageClient);

        // Notification de confirmation à la secrétaire (pour son journal d'activité par exemple)
        String messageSecretaire = "Facture N°" + savedFacture.getId() + " générée avec succès pour la commande N°" + commandeId;
        notificationService.envoyerNotification("/topic/secretariat", messageSecretaire);

        // 4. Préparer et retourner le DTO de réponse
        List<LigneCommandeResponseDTO> lignesDto = buildLigneCommandeResponseDTOs(commande);
        return new FactureResponseDTO(savedFacture, lignesDto);
    }

    /**
     * Récupère toutes les factures, avec les détails complets des lignes.
     */
    @Transactional(readOnly = true)
    public List<FactureResponseDTO> getAllFactures() {
        return factureRepository.findAll().stream()
                .map(facture -> {
                    List<LigneCommandeResponseDTO> lignesDto = buildLigneCommandeResponseDTOs(facture.getCommande());
                    return new FactureResponseDTO(facture, lignesDto);
                })
                .collect(Collectors.toList());
    }

    /**
     * Récupère une facture par son ID (Long), avec les détails complets des lignes.
     */
    @Transactional(readOnly = true)
    public FactureResponseDTO getFactureById(Long id) {
        Facture facture = factureRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Facture non trouvée avec l'ID : " + id));

        List<LigneCommandeResponseDTO> lignesDto = buildLigneCommandeResponseDTOs(facture.getCommande());

        return new FactureResponseDTO(facture, lignesDto);
    }

    /**
     * Supprime une facture.
     */
    public void deleteFacture(Long id) {
        if (!factureRepository.existsById(id)) {
            throw new EntityNotFoundException("Facture non trouvée, impossible de la supprimer : " + id);
        }
        factureRepository.deleteById(id);
    }

    /**
     * Méthode privée et réutilisable pour construire la liste des DTOs de lignes de commande.
     * C'est ici que la magie opère, en combinant les données de la commande et des produits.
     */
    private List<LigneCommandeResponseDTO> buildLigneCommandeResponseDTOs(Commande commande) {
        if (commande == null || commande.getLignes() == null) {
            return Collections.emptyList(); // Retourne une liste vide si pas de commande ou pas de lignes
        }

        return commande.getLignes().stream()
                .map(ligne -> {
                    // Pour chaque ligne, on va chercher le produit correspondant
                    Produit produit = produitRepos.findById(ligne.getProduitId())
                            .orElse(null); // Gère le cas où un produit aurait été supprimé

                    // On utilise le constructeur parfait de LigneCommandeResponseDTO
                    return new LigneCommandeResponseDTO(ligne, produit);
                })
                .collect(Collectors.toList());
    }
}