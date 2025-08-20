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
 * UNIQUEMENT si le statut est EN_ATTENTE_CONFIRMATION
 */
@Transactional
public InventaireResponseDto modifierInventaireSansApplique(Long id, InventaireRequestDto request) {
    Inventaire inventaire = inventaireRepo.findById(id)
            .orElseThrow(() -> new RuntimeException("Inventaire introuvable"));

    // Vérifier que l'inventaire est en attente de confirmation
    if (!"EN_ATTENTE_CONFIRMATION".equalsIgnoreCase(inventaire.getStatus())) {
        throw new RuntimeException("Impossible de modifier l'inventaire : le statut doit être 'EN_ATTENTE_CONFIRMATION'. Statut actuel : " + inventaire.getStatus());
    }

    // Mise à jour des propriétés de l'inventaire
    inventaire.setCharge(request.getCharge());
    LieuStock lieuStock = lieuStockRepo.findById(request.getLieuStockId())
            .orElseThrow(() -> new RuntimeException("Lieu de stock introuvable avec l'ID : " + request.getLieuStockId()));
    inventaire.setLieuStock(lieuStock);
    
    // Sauvegarder d'abord l'inventaire mis à jour
    inventaireRepo.save(inventaire);

    // Supprimer proprement toutes les anciennes lignes
    List<LigneInventaire> anciennesLignes = ligneRepo.findByInventaireId(id);
    if (!anciennesLignes.isEmpty()) {
        ligneRepo.deleteAll(anciennesLignes);
        ligneRepo.flush(); // Force l'écriture en base
    }

    // Calculer et créer les nouvelles lignes
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
    // Log de début avec les paramètres
    System.out.println("=== DEBUT appliquerEcartsAuStock ===");
    System.out.println("InventaireId: " + inventaireId);
    System.out.println("Premier: " + premier);
    
    List<LigneInventaire> lignes = ligneRepo.findByInventaireId(inventaireId);
    
    // Log des données retournées par findByInventaireId
    System.out.println("--- Données retournées par findByInventaireId ---");
    System.out.println("Nombre de lignes trouvées: " + (lignes != null ? lignes.size() : "null"));
    
    if (lignes != null) {
        for (int i = 0; i < lignes.size(); i++) {
            LigneInventaire ligne = lignes.get(i);
            System.out.println("Ligne " + (i + 1) + ":");
            System.out.println("  - ID: " + ligne.getId());
            System.out.println("  - Produit: " + (ligne.getProduit() != null ? ligne.getProduit().getNom() + " (ID: " + ligne.getProduit().getId() + ")" : "null"));
            System.out.println("  - LieuStock: " + (ligne.getLieuStock() != null ? ligne.getLieuStock().getNom() + " (ID: " + ligne.getLieuStock().getId() + ")" : "null"));
            System.out.println("  - QteAvantScan: " + ligne.getQteAvantScan());
            System.out.println("  - QteScanne: " + ligne.getQteScanne());
            System.out.println("  - Ecart: " + ligne.getEcart());
            System.out.println("  ---");
        }
    }
    
    for (LigneInventaire ligne : lignes) {
        System.out.println("--- Traitement de la ligne ID: " + ligne.getId() + " ---");
        
        Produit produit = ligne.getProduit();
        LieuStock lieuStock = ligne.getLieuStock();
        
        System.out.println("Produit récupéré: " + (produit != null ? produit.getNom() : "null"));
        System.out.println("LieuStock récupéré: " + (lieuStock != null ? lieuStock.getNom() : "null"));
        
        if (premier) {
            System.out.println("Mode PREMIER - Ajout de stock: " + ligne.getQteScanne());
            // Si premier = true, on met seulement la quantité scannée sans faire d'écart
            stockService.addStock(produit, lieuStock, ligne.getQteScanne());
        } else {
            // Si premier = false, on applique les écarts comme avant
            int ecart = ligne.getEcart();
            System.out.println("Mode NORMAL - Ecart calculé: " + ecart);
            
            if (ecart > 0) {
                System.out.println("Ecart positif - Ajout de stock: " + ecart);
                stockService.addStock(produit, lieuStock, ecart);
            } else if (ecart < 0) {
                System.out.println("Ecart négatif - Suppression de stock: " + Math.abs(ecart));
                stockService.removeStock(produit, lieuStock, Math.abs(ecart));
            } else if (ecart == 0 && ligne.getQteAvantScan() == 0 && ligne.getQteScanne() > 0) {
                System.out.println("Cas spécial - Création de stock pour produit inexistant: " + ligne.getQteScanne());
                // Cas spécial : création de stock pour un produit inexistant
                stockService.addStock(produit, lieuStock, ligne.getQteScanne());
            } else {
                System.out.println("Aucune action requise pour cette ligne");
            }
        }
        System.out.println("--- Fin traitement ligne ---");
    }
    
    // Log avant mise à jour du status
    System.out.println("--- Mise à jour du status ---");
    
    // Mettre à jour le status dans la base à "CONFIRME"
    Inventaire inventaire = inventaireRepo.findById(inventaireId)
            .orElseThrow(() -> new RuntimeException("Inventaire introuvable"));
    
    System.out.println("Inventaire trouvé - ID: " + inventaire.getId() + ", Status avant: " + inventaire.getStatus());
    
    inventaire.setStatus("CONFIRME");
    inventaireRepo.save(inventaire);
    
    System.out.println("Status mis à jour: " + inventaire.getStatus());
    
    // Log avant retour
    System.out.println("--- Appel de getInventaireByIdWithStatus ---");
    
    // Retourner la réponse avec le status mis à jour
    InventaireResponseDto response = getInventaireByIdWithStatus(inventaireId, null);
    
    System.out.println("Response générée - Status: " + (response != null ? response.getStatus() : "null"));
    System.out.println("=== FIN appliquerEcartsAuStock ===");
    
    return response;
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
   /**
 * Méthode combinée : modifier et appliquer directement (pour compatibilité)
 * UNIQUEMENT si le statut est EN_ATTENTE_CONFIRMATION
 */
@Transactional
public InventaireResponseDto modifierInventaire(Long id, InventaireRequestDto request, boolean premier) {
    // La vérification du statut est déjà faite dans modifierInventaireSansApplique
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