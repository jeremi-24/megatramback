package com.Megatram.Megatram.service;

import com.Megatram.Megatram.Dto.BonLivraisonResponseDTO;
import com.Megatram.Megatram.Dto.LigneLivraisonDTO;
import com.Megatram.Megatram.Entity.BonLivraison;
import com.Megatram.Megatram.Entity.Commande;
import com.Megatram.Megatram.Entity.LigneLivraison;
import com.Megatram.Megatram.Entity.Produit;
import com.Megatram.Megatram.Entity.LieuStock;
import com.Megatram.Megatram.enums.BonLivraisonStatus;
import com.Megatram.Megatram.repository.BonLivraisonRepository;
import com.Megatram.Megatram.repository.CommandeRepository;
import com.Megatram.Megatram.repository.LigneLivraisonRepository;
import com.Megatram.Megatram.repository.ProduitRepos;
import com.Megatram.Megatram.repository.LieuStockRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class BonLivraisonService {

    private final BonLivraisonRepository bonLivraisonRepository;
    private final CommandeRepository commandeRepository;
    private final LigneLivraisonRepository ligneLivraisonRepository;
    private final ProduitRepos produitRepos;
    private final StockService stockService;

    @Autowired
    public BonLivraisonService(BonLivraisonRepository bonLivraisonRepository, CommandeRepository commandeRepository, LigneLivraisonRepository ligneLivraisonRepository, ProduitRepos produitRepos, StockService stockService) {
        this.bonLivraisonRepository = bonLivraisonRepository;
        this.commandeRepository = commandeRepository;
        this.ligneLivraisonRepository = ligneLivraisonRepository;
        this.produitRepos = produitRepos;
        this.stockService = stockService;
    }

    public BonLivraisonResponseDTO genererBonLivraison(Long commandeId) {
        Commande commande = commandeRepository.findById(commandeId)
                .orElseThrow(() -> new EntityNotFoundException("Commande non trouvée avec l'ID : " + commandeId));

        BonLivraison bonLivraison = new BonLivraison();
        bonLivraison.setCommande(commande);
        bonLivraison.setDateLivraison(LocalDateTime.now());
        bonLivraison.setStatut(BonLivraisonStatus.EN_ATTENTE);
        
        // La logique pour créer les lignes de livraison à partir des lignes de commande irait ici
        // ...

        BonLivraison savedBonLivraison = bonLivraisonRepository.save(bonLivraison);
        return new BonLivraisonResponseDTO(savedBonLivraison);
    }

    public BonLivraisonResponseDTO validerETALivrer(Long bonLivraisonId, String agentEmail) {
        BonLivraison bonLivraison = bonLivraisonRepository.findById(bonLivraisonId)
                .orElseThrow(() -> new EntityNotFoundException("Bon de Livraison non trouvé avec l'ID : " + bonLivraisonId));

        if (bonLivraison.getStatut() != BonLivraisonStatus.EN_ATTENTE) {
             throw new IllegalStateException("Le bon de livraison n'est pas au statut EN_ATTENTE pour la première validation.");
        }

        bonLivraison.setStatut(BonLivraisonStatus.A_LIVRER);
        BonLivraison updatedBonLivraison = bonLivraisonRepository.save(bonLivraison);
        return new BonLivraisonResponseDTO(updatedBonLivraison);
    }

    public BonLivraisonResponseDTO validerEtLivrer(Long bonLivraisonId, String agentEmail) {
        BonLivraison bonLivraison = bonLivraisonRepository.findById(bonLivraisonId)
                .orElseThrow(() -> new EntityNotFoundException("Bon de Livraison non trouvé avec l'ID : " + bonLivraisonId));

        if (bonLivraison.getStatut() != BonLivraisonStatus.A_LIVRER) {
             throw new IllegalStateException("Le bon de livraison n'est pas au statut A_LIVRER pour la validation finale.");
        }

        if (bonLivraison.getLignesLivraison() != null) {
            for (LigneLivraison ligne : bonLivraison.getLignesLivraison()) {
                Produit produit = ligne.getProduit();
                LieuStock lieuStock = produit.getLieuStock();

                if (lieuStock == null) {
                     throw new IllegalStateException("Le produit '" + produit.getNom() + "' n'a pas de lieu de stock attribué.");
                }

                int quantiteALivrer = ligne.getQteLivre();
                int qteParCarton = produit.getQteParCarton();

                int quantiteTotaleALivrerEnUnites;

                if ("CARTON".equalsIgnoreCase(ligne.getTypeQuantite())) {
                    if (qteParCarton <= 0) {
                        throw new IllegalStateException("Le produit '" + produit.getNom() + "' n'a pas une quantité par carton valide.");
                    }
                    quantiteTotaleALivrerEnUnites = quantiteALivrer * qteParCarton;
                } else {
                    quantiteTotaleALivrerEnUnites = quantiteALivrer;
                }

                stockService.removeStock(produit, lieuStock, quantiteTotaleALivrerEnUnites);
            }
        }

        bonLivraison.setStatut(BonLivraisonStatus.LIVRE);
        BonLivraison updatedBonLivraison = bonLivraisonRepository.save(bonLivraison);
        return new BonLivraisonResponseDTO(updatedBonLivraison);
    }

    public List<BonLivraisonResponseDTO> getAllBonLivraisons() {
        return bonLivraisonRepository.findAll().stream()
                .map(BonLivraisonResponseDTO::new)
                .collect(Collectors.toList());
    }
    
    public BonLivraisonResponseDTO getBonLivraisonById(Long id) {
        return bonLivraisonRepository.findById(id)
                .map(BonLivraisonResponseDTO::new)
                .orElseThrow(() -> new EntityNotFoundException("Bon de Livraison non trouvé avec l'ID : " + id));
    }

    public List<BonLivraisonResponseDTO> getBonsLivraisonParLieu(Long lieuId) {
        return bonLivraisonRepository.findByLieuStockId(lieuId).stream()
                .map(BonLivraisonResponseDTO::new)
                .collect(Collectors.toList());
    }

    public void deleteBonLivraison(Long id) {
        BonLivraison bonLivraison = bonLivraisonRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Bon de Livraison non trouvé pour la suppression : " + id));

        if (bonLivraison.getStatut() == BonLivraisonStatus.LIVRE) {
            if (bonLivraison.getLignesLivraison() != null) {
                for (LigneLivraison ligne : bonLivraison.getLignesLivraison()) {
                    Produit produit = ligne.getProduit();
                    LieuStock lieuStock = produit.getLieuStock();

                     if (lieuStock == null) {
                         System.err.println("AVERTISSEMENT: Lieu de stock manquant pour le produit " + produit.getNom() + " lors de la suppression du BL " + id);
                         continue;
                     }

                    int qteParCarton = produit.getQteParCarton();
                    int quantiteAReajouter = ligne.getQteLivre();
                    int quantiteTotalAReajouterEnUnites;

                    if ("CARTON".equalsIgnoreCase(ligne.getTypeQuantite())) {
                         if (qteParCarton <= 0) {
                            System.err.println("AVERTISSEMENT: QteParCarton invalide pour le produit " + produit.getNom() + ". Stock non réajouté.");
                            continue;
                        }
                        quantiteTotalAReajouterEnUnites = quantiteAReajouter * qteParCarton;
                    } else {
                        quantiteTotalAReajouterEnUnites = quantiteAReajouter;
                    }
                    stockService.addStock(produit, lieuStock, quantiteTotalAReajouterEnUnites);
                }
            }
        }
        bonLivraisonRepository.delete(bonLivraison);
    }
}