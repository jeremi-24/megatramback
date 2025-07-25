package com.Megatram.Megatram.service;

import com.Megatram.Megatram.Dto.InventaireRequestDto;
import com.Megatram.Megatram.Dto.InventaireResponseDto;
import com.Megatram.Megatram.Dto.LigneInventaireDto;
import com.Megatram.Megatram.Dto.LigneResponseDto;
import com.Megatram.Megatram.Entity.Inventaire;
import com.Megatram.Megatram.Entity.LigneInventaire;
import com.Megatram.Megatram.Entity.Produit;
import com.Megatram.Megatram.Entity.LieuStock;
import com.Megatram.Megatram.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Transactional
public class InventaireService {

    @Autowired
    private InventaireRepository inventaireRepo;
    @Autowired
    private LigneInventaireRepository ligneRepo;
    @Autowired
    private ProduitRepos produitRepo;
    @Autowired
    private LieuStockRepository lieuStockRepo;
    @Autowired
    private StockService stockService;
    @Autowired
    private StockRepository stockRepository;

    @Transactional
    public InventaireResponseDto enregistrerInventaire(InventaireRequestDto request) {
        Inventaire inventaire = new Inventaire();
        inventaire.setCharge(request.getCharge()); // Utiliser le getter
        inventaireRepo.save(inventaire);

        List<LigneResponseDto> lignesReponse = request.getProduits().stream().map(ligneRequestDto -> { // Utiliser le getter
            Produit produit = produitRepo.findById(ligneRequestDto.getProduitId()) // Utiliser le getter
                    .orElseThrow(() -> new RuntimeException("Produit introuvable"));

            LieuStock lieuStock = lieuStockRepo.findByNom(ligneRequestDto.getLieuStockNom()) // Utiliser le getter
                    .orElseThrow(() -> new RuntimeException("Lieu de stock introuvable : " + ligneRequestDto.getLieuStockNom()));

            LigneInventaire ligneInventaire = new LigneInventaire();
            ligneInventaire.setInventaire(inventaire);
            ligneInventaire.setProduit(produit);
            ligneInventaire.setLieuStock(lieuStock);

            Optional<com.Megatram.Megatram.Entity.Stock> stockOptional = stockRepository.findByProduitAndLieuStock(produit, lieuStock);
            int qteAvantScanTotaleUnites = stockOptional
                .map(stock -> (stock.getQteCartons() * produit.getQteParCarton()) + stock.getQteUnitesRestantes())
                .orElse(0);
            ligneInventaire.setQteAvantScan(qteAvantScanTotaleUnites);

            int qteScanneTotaleUnites;
            if ("CARTON".equalsIgnoreCase(ligneRequestDto.getTypeQuantiteScanne())) { // Utiliser le getter
                if (produit.getQteParCarton() <= 0) {
                    throw new IllegalStateException("Le produit " + produit.getNom() + " n'a pas une quantité par carton valide.");
                }
                qteScanneTotaleUnites = ligneRequestDto.getQteScanne() * produit.getQteParCarton(); // Utiliser le getter
            } else {
                qteScanneTotaleUnites = ligneRequestDto.getQteScanne(); // Utiliser le getter
            }
            ligneInventaire.setQteScanne(qteScanneTotaleUnites);
            ligneInventaire.setTypeQuantiteScanne(ligneRequestDto.getTypeQuantiteScanne()); // Utiliser le getter

            int ecartUnitesTotales = qteScanneTotaleUnites - qteAvantScanTotaleUnites;
            ligneInventaire.setEcart(ecartUnitesTotales);

            if (ecartUnitesTotales > 0) {
                stockService.addStock(produit, lieuStock, ecartUnitesTotales);
            } else if (ecartUnitesTotales < 0) {
                stockService.removeStock(produit, lieuStock, Math.abs(ecartUnitesTotales));
            } else if (!stockOptional.isPresent() && qteScanneTotaleUnites > 0) {
                // CORRIGÉ: Faute de frappe
                stockService.addStock(produit, lieuStock, qteScanneTotaleUnites);
            }

            ligneRepo.save(ligneInventaire);

            // SIMPLIFIÉ: Utilisation du constructeur pour éliminer la redondance et les erreurs d'accès
            return new LigneResponseDto(ligneInventaire);
            
        }).collect(Collectors.toList());

        InventaireResponseDto response = new InventaireResponseDto(inventaire); // Utiliser un constructeur
        response.setLignes(lignesReponse); // Utiliser le setter

        return response;
    }

    public List<InventaireResponseDto> recupererTousLesInventaires() {
        return inventaireRepo.findAll().stream().map(inv -> {
            InventaireResponseDto response = new InventaireResponseDto(inv); // Utiliser le constructeur
            List<LigneInventaire> lignes = ligneRepo.findByInventaireId(inv.getId());
            List<LigneResponseDto> ligneDtos = lignes.stream()
                                                .map(LigneResponseDto::new)
                                                .collect(Collectors.toList());
            response.setLignes(ligneDtos); // Utiliser le setter
            return response;
        }).collect(Collectors.toList());
    }

    public InventaireResponseDto getInventaireById(Long id) {
        Inventaire inv = inventaireRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Inventaire introuvable"));
        
        InventaireResponseDto response = new InventaireResponseDto(inv); // Utiliser le constructeur
        List<LigneInventaire> lignes = ligneRepo.findByInventaireId(id);
        List<LigneResponseDto> ligneDtos = lignes.stream()
                                                .map(LigneResponseDto::new)
                                                .collect(Collectors.toList());
        response.setLignes(ligneDtos); // Utiliser le setter
        return response;
    }
}