package com.Megatram.Megatram.service;

import com.Megatram.Megatram.Dto.*;
import com.Megatram.Megatram.Entity.LieuStock;
import com.Megatram.Megatram.Entity.LigneReapprovisionnement;
import com.Megatram.Megatram.Entity.Produit;
import com.Megatram.Megatram.Entity.Reapprovisionnement;
import com.Megatram.Megatram.repository.LieuStockRepository;
import com.Megatram.Megatram.repository.LigneReapprovisionnementRepository;
import com.Megatram.Megatram.repository.ProduitRepos;
import com.Megatram.Megatram.repository.ReapprovisionnementRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class ReapprovisionnementService {

    @Autowired
    private ReapprovisionnementRepository reapproRepo;
    @Autowired
    private LigneReapprovisionnementRepository ligneRepo;
    @Autowired
    private ProduitRepos produitRepo;
    @Autowired
    private LieuStockRepository lieuStockRepo;
    @Autowired
    private StockService stockService;

    @Transactional
    public ReapprovisionnementResponseDto enregistrerReapprovisionnement(ReapprovisionnementRequestDto request) {
        Reapprovisionnement reapprovisionnement = new Reapprovisionnement();
        reapprovisionnement.setSource(request.getSource()); // CORRIGÉ: Utiliser le getter
        reapprovisionnement.setAgent(request.getAgent());   // CORRIGÉ: Utiliser le getter

        Reapprovisionnement savedReappro = reapproRepo.save(reapprovisionnement);

        if (request.getLignes() == null || request.getLignes().isEmpty()) { // CORRIGÉ: Utiliser le getter
            throw new IllegalArgumentException("Aucune ligne de réapprovisionnement reçue.");
        }

        List<LigneReapprovisionnement> lignesEntite = request.getLignes().stream().map(dto -> { // CORRIGÉ: Utiliser le getter
            Produit produit = produitRepo.findById(dto.getProduitId()) // CORRIGÉ: Utiliser le getter
                    .orElseThrow(() -> new RuntimeException("Produit introuvable"));

            LieuStock lieuStock = lieuStockRepo.findByNom(dto.getLieuStockNom()) // CORRIGÉ: Utiliser le getter
                    .orElseThrow(() -> new RuntimeException("LieuStock introuvable"));

            int quantiteAjoutee = dto.getQteAjoutee(); // CORRIGÉ: Utiliser le getter
            int qteParCarton = produit.getQteParCarton();
            int quantiteTotaleAjouteeEnUnites;

            if ("CARTON".equalsIgnoreCase(dto.getTypeQuantite())) { // CORRIGÉ: Utiliser le getter
                if (qteParCarton <= 0) {
                    throw new IllegalStateException("Le produit " + produit.getNom() + " n'a pas une quantité par carton valide.");
                }
                quantiteTotaleAjouteeEnUnites = quantiteAjoutee * qteParCarton;
            } else {
                quantiteTotaleAjouteeEnUnites = quantiteAjoutee;
            }

            stockService.addStock(produit, lieuStock, quantiteTotaleAjouteeEnUnites);

            LigneReapprovisionnement ligne = new LigneReapprovisionnement();
            ligne.setReapprovisionnement(savedReappro);
            ligne.setProduit(produit);
            ligne.setQteAjoutee(quantiteAjoutee);
            ligne.setLieuStock(lieuStock);
            ligne.setTypeQuantite(dto.getTypeQuantite()); // CORRIGÉ: Utiliser le getter
            return ligneRepo.save(ligne);
        }).collect(Collectors.toList());

        savedReappro.setLignes(lignesEntite);

        // CORRIGÉ: Utiliser le constructeur du DTO pour construire la réponse
        return new ReapprovisionnementResponseDto(savedReappro);
    }

    public List<ReapprovisionnementResponseDto> getAllReapprovisionnements() {
        return reapproRepo.findAll().stream()
                .map(ReapprovisionnementResponseDto::new) // CORRIGÉ: Utiliser le constructeur
                .collect(Collectors.toList());
    }

    public ReapprovisionnementDetailsDto getDetails(Long id) {
        Reapprovisionnement r = reapproRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Réapprovisionnement introuvable"));
        // La construction du DTO de détails est déjà assez propre. On la garde.
        return new ReapprovisionnementDetailsDto(r); 
    }

    public void deleteReapprovisionnement(Long id) {
        Reapprovisionnement reapprovisionnement = reapproRepo.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Réapprovisionnement non trouvé : " + id));

        // CORRIGÉ: getLignes() existe maintenant sur l'entité Reapprovisionnement
        if (reapprovisionnement.getLignes() != null) {
            for (LigneReapprovisionnement ligne : reapprovisionnement.getLignes()) {
                Produit produit = ligne.getProduit();
                LieuStock lieuStock = ligne.getLieuStock();

                if (produit == null || lieuStock == null) {
                     System.err.println("AVERTISSEMENT: Données de ligne incomplètes pour la suppression du réappro " + id);
                     continue;
                }
                
                int qteParCarton = produit.getQteParCarton();
                int quantiteTotalARetirerEnUnites;

                if ("CARTON".equalsIgnoreCase(ligne.getTypeQuantite())) {
                    if (qteParCarton <= 0) {
                         System.err.println("AVERTISSEMENT: QteParCarton invalide pour le produit " + produit.getNom());
                         continue;
                    }
                    quantiteTotalARetirerEnUnites = ligne.getQteAjoutee() * qteParCarton;
                } else {
                    quantiteTotalARetirerEnUnites = ligne.getQteAjoutee();
                }

                try {
                    stockService.removeStock(produit, lieuStock, quantiteTotalARetirerEnUnites);
                } catch (Exception e) {
                     System.err.println("ERREUR lors du retrait du stock pour le réappro " + id + ": " + e.getMessage());
                }
            }
        }
        reapproRepo.delete(reapprovisionnement);
    }
}