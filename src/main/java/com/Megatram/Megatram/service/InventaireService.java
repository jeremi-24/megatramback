package com.Megatram.Megatram.service;
import java.util.Comparator;
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
import java.io.ByteArrayInputStream;
import java.io.IOException;

import java.util.List;
import java.util.Optional;
import java.util.Map;

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
    @Autowired
    private ExcelExportService excelExportService;

    // ========== MÉTHODES DE CALCUL (SANS MODIFICATION DU STOCK) ==========

    /**
     * Créer un inventaire et calculer les écarts SANS appliquer au stock
     */
    @Transactional
    public InventaireResponseDto creerInventaireSansApplique(InventaireRequestDto request) {
        Inventaire inventaire = new Inventaire();
        inventaire.setCharge(request.getCharge());

        LieuStock lieuStock = lieuStockRepo.findById(request.getLieuStockId())
                .orElseThrow(() -> new RuntimeException("Lieu de stock introuvable avec l'ID : " + request.getLieuStockId()));
        inventaire.setLieuStock(lieuStock);
        inventaire.setStatus("EN_ATTENTE_CONFIRMATION"); // On initialise le status en base
        inventaireRepo.save(inventaire);

        List<LigneResponseDto> lignesReponse = calculerEcartsSansAppliquer(inventaire, request.getProduits());

        InventaireResponseDto response = new InventaireResponseDto(inventaire);
        response.setLignes(lignesReponse);
        response.setStatus(inventaire.getStatus());
        return response;
    }

    /**
     * Modifier un inventaire et calculer les écarts SANS appliquer au stock
     */
    @Transactional
    public InventaireResponseDto modifierInventaireSansApplique(Long id, InventaireRequestDto request) {
        Inventaire inventaire = inventaireRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Inventaire introuvable"));

        inventaire.setCharge(request.getCharge());
        LieuStock lieuStock = lieuStockRepo.findById(request.getLieuStockId())
                .orElseThrow(() -> new RuntimeException("Lieu de stock introuvable avec l'ID : " + request.getLieuStockId()));
        inventaire.setLieuStock(lieuStock);
        inventaire.setStatus("EN_ATTENTE_CONFIRMATION"); // Remettre le status à en attente lors de modification
        inventaireRepo.save(inventaire);

        // Supprimer les anciennes lignes
        List<LigneInventaire> anciennesLignes = ligneRepo.findByInventaireId(id);
        ligneRepo.deleteAll(anciennesLignes);

        List<LigneResponseDto> lignesReponse = calculerEcartsSansAppliquer(inventaire, request.getProduits());

        InventaireResponseDto response = new InventaireResponseDto(inventaire);
        response.setLignes(lignesReponse);
        response.setStatus(inventaire.getStatus());
        return response;
    }

    /**
     * Méthode commune pour calculer les écarts sans appliquer au stock
     */
    private List<LigneResponseDto> calculerEcartsSansAppliquer(Inventaire inventaire, List<LigneInventaireDto> produits) {
        return produits.stream().map(ligneRequestDto -> {
            Produit produit = produitRepo.findById(ligneRequestDto.getProduitId())
                    .orElseThrow(() -> new RuntimeException("Produit introuvable"));

 LieuStock lieuStock = lieuStockRepo.findById(ligneRequestDto.getLieuStockId())
 .orElseThrow(() -> new RuntimeException("Lieu de stock introuvable avec l'ID : " + ligneRequestDto.getLieuStockId()));
            LigneInventaire ligneInventaire = new LigneInventaire();
            ligneInventaire.setInventaire(inventaire);
            ligneInventaire.setProduit(produit);
            ligneInventaire.setLieuStock(lieuStock);

            // Quantité avant scan
            Optional<com.Megatram.Megatram.Entity.Stock> stockOptional = stockRepository.findByProduitAndLieuStock(produit, lieuStock);
            int qteAvantScanTotaleUnites = stockOptional
                .map(stock -> (stock.getQteCartons() * produit.getQteParCarton()) + stock.getQteUnitesRestantes())
                .orElse(0);
            ligneInventaire.setQteAvantScan(qteAvantScanTotaleUnites);

            // Quantité scannée
            int qteScanneTotaleUnites;
            if ("CARTON".equalsIgnoreCase(ligneRequestDto.getTypeQuantiteScanne())) {
                if (produit.getQteParCarton() <= 0) {
                    throw new IllegalStateException("Le produit " + produit.getNom() + " n'a pas une quantité par carton valide.");
                }
                qteScanneTotaleUnites = ligneRequestDto.getQteScanne() * produit.getQteParCarton();
            } else {
                qteScanneTotaleUnites = ligneRequestDto.getQteScanne();
            }
            ligneInventaire.setQteScanne(qteScanneTotaleUnites);
            ligneInventaire.setTypeQuantiteScanne(ligneRequestDto.getTypeQuantiteScanne());

            // Calcul de l'écart
            int ecartUnitesTotales = qteScanneTotaleUnites - qteAvantScanTotaleUnites;
            ligneInventaire.setEcart(ecartUnitesTotales);

            // Sauvegarder SANS appliquer au stock
            ligneRepo.save(ligneInventaire);

            return new LigneResponseDto(ligneInventaire);
        }).collect(Collectors.toList());
    }

    // ========== MÉTHODES D'APPLICATION DES ÉCARTS ==========

    /**
     * Appliquer les écarts calculés au stock après confirmation
     */
    @Transactional
    public InventaireResponseDto appliquerEcartsAuStock(Long inventaireId, boolean premier) {
        List<LigneInventaire> lignes = ligneRepo.findByInventaireId(inventaireId);
        
        for (LigneInventaire ligne : lignes) {
            Produit produit = ligne.getProduit();
            LieuStock lieuStock = ligne.getLieuStock();
            
            if (premier) {
                // Si premier = true, on met seulement la quantité scannée sans faire d'écart
                stockService.addStock(produit, lieuStock, ligne.getQteScanne());
            } else {
                // Si premier = false, on applique les écarts comme avant
                int ecart = ligne.getEcart();
                
                if (ecart > 0) {
                    stockService.addStock(produit, lieuStock, ecart);
                } else if (ecart < 0) {
                    stockService.removeStock(produit, lieuStock, Math.abs(ecart));
                } else if (ecart == 0 && ligne.getQteAvantScan() == 0 && ligne.getQteScanne() > 0) {
                    // Cas spécial : création de stock pour un produit inexistant
                    stockService.addStock(produit, lieuStock, ligne.getQteScanne());
                }
            }
        }

        // Mettre à jour le status dans la base à "CONFIRME"
        Inventaire inventaire = inventaireRepo.findById(inventaireId)
                .orElseThrow(() -> new RuntimeException("Inventaire introuvable"));
        inventaire.setStatus("CONFIRME");
        inventaireRepo.save(inventaire);

        // Retourner la réponse avec le status mis à jour
        return getInventaireByIdWithStatus(inventaireId, null);
    }

    /**
     * Méthode combinée : créer et appliquer directement (pour compatibilité)
     */
    @Transactional
    public InventaireResponseDto enregistrerInventaire(InventaireRequestDto request, boolean premier) {
        InventaireResponseDto response = creerInventaireSansApplique(request);
        appliquerEcartsAuStock(response.getInventaireId(), premier);
        return getInventaireByIdWithStatus(response.getInventaireId(), null);
    }

    /**
     * Méthode combinée : modifier et appliquer directement (pour compatibilité)
     */
    @Transactional
    public InventaireResponseDto modifierInventaire(Long id, InventaireRequestDto request, boolean premier) {
        InventaireResponseDto response = modifierInventaireSansApplique(id, request);
        appliquerEcartsAuStock(id, premier);
        return getInventaireByIdWithStatus(id, null);
    }

    /**
 * Récupère les inventaires par lieu de stock
 */
 public List<InventaireResponseDto> getInventairesByLieuStock(Long lieuStockId) {
 return inventaireRepo.findByLieuStockId(lieuStockId).stream().map(inv -> {
 InventaireResponseDto response = new InventaireResponseDto(inv);
 List<LigneInventaire> lignes = ligneRepo.findByInventaireId(inv.getId());
 List<LigneResponseDto> ligneDtos = lignes.stream()
 .map(LigneResponseDto::new)
 .collect(Collectors.toList());
 response.setLignes(ligneDtos);
 // Utiliser le status en base
 response.setStatus(inv.getStatus());
 return response;
 }).collect(Collectors.toList());
 }

    // ========== MÉTHODES DE RÉCUPÉRATION ET EXPORT ==========

    public List<InventaireResponseDto> recupererTousLesInventaires() {
        return inventaireRepo.findAll().stream().map(inv -> {
            InventaireResponseDto response = new InventaireResponseDto(inv);
            List<LigneInventaire> lignes = ligneRepo.findByInventaireId(inv.getId());
            List<LigneResponseDto> ligneDtos = lignes.stream()
                                                .map(LigneResponseDto::new)
                                                .collect(Collectors.toList());
            response.setLignes(ligneDtos);
            
            // Utiliser le status en base
            response.setStatus(inv.getStatus());
            
            return response;
        }).collect(Collectors.toList());
    }

    public InventaireResponseDto getInventaireById(Long id) {
        return getInventaireByIdWithStatus(id, null);
    }

    /**
     * Méthode privée pour récupérer un inventaire avec un status spécifique
     */
    private InventaireResponseDto getInventaireByIdWithStatus(Long id, String forceStatus) {
        Inventaire inv = inventaireRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Inventaire introuvable"));
        
        InventaireResponseDto response = new InventaireResponseDto(inv);
        List<LigneInventaire> lignes = ligneRepo.findByInventaireId(id);
        List<LigneResponseDto> ligneDtos = lignes.stream()
                                                .map(LigneResponseDto::new)
                                                .sorted(Comparator.comparing(LigneResponseDto::getNomProduit))  .collect(Collectors.toList());
        response.setLignes(ligneDtos);
        
        // Définir le status en priorité par forceStatus sinon celui en base
        if (forceStatus != null) {
            response.setStatus(forceStatus);
        } else {
            response.setStatus(inv.getStatus());
        }
        
        return response;
    }

    public ByteArrayInputStream exportInventaireToExcel(Long inventaireId) throws IOException {
        InventaireResponseDto inventaireDto = getInventaireById(inventaireId);
        return excelExportService.generateInventaireExcel(inventaireDto);
    }

    // ========== MÉTHODES UTILITAIRES ==========

    /**
     * Obtenir un résumé des écarts avant application
     */
    public Map<String, Object> getResumerEcarts(Long inventaireId) {
        List<LigneInventaire> lignes = ligneRepo.findByInventaireId(inventaireId);
        
        int totalEcartsPositifs = lignes.stream()
            .filter(l -> l.getEcart() > 0)
            .mapToInt(LigneInventaire::getEcart)
            .sum();
            
        int totalEcartsNegatifs = lignes.stream()
            .filter(l -> l.getEcart() < 0)
            .mapToInt(l -> Math.abs(l.getEcart()))
            .sum();
            
        long nombreLignesAvecEcart = lignes.stream()
            .filter(l -> l.getEcart() != 0)
            .count();

        return Map.of(
            "totalEcartsPositifs", totalEcartsPositifs,
            "totalEcartsNegatifs", totalEcartsNegatifs,
            "nombreLignesAvecEcart", nombreLignesAvecEcart,
            "totalLignes", lignes.size()
        );
    }

    /**
     * Vérifier si un inventaire a des écarts appliqués
     */
    public boolean estEcartsAppliques(Long inventaireId) {
        Inventaire inventaire = inventaireRepo.findById(inventaireId)
                .orElseThrow(() -> new RuntimeException("Inventaire introuvable"));
        
        // On peut aussi simplement vérifier si le status est CONFIRME
        return "CONFIRME".equalsIgnoreCase(inventaire.getStatus());
    }
}