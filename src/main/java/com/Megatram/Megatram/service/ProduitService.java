package com.Megatram.Megatram.service;

import com.Megatram.Megatram.Dto.AssignationProduitsDTO;
import com.Megatram.Megatram.Dto.ProduitRequestDTO;
import com.Megatram.Megatram.Dto.ProduitDto;
import com.Megatram.Megatram.Entity.Categorie;
import com.Megatram.Megatram.Entity.LieuStock;
import com.Megatram.Megatram.Entity.Produit;
import com.Megatram.Megatram.repository.CategorieRep;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.Megatram.Megatram.repository.LieuStockRepository;
import com.Megatram.Megatram.repository.ProduitRepos;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.common.BitMatrix;
import jakarta.persistence.EntityNotFoundException; // Assurez-vous d'utiliser le bon import

import com.itextpdf.text.log.LoggerFactory;
import jakarta.persistence.EntityNotFoundException;
import org.apache.poi.ss.usermodel.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.logging.Logger;
import java.util.stream.Collectors;

@Service

@Transactional
public class ProduitService {

    @Autowired
 private StockService stockService;
    private final ProduitRepos produitRepos;

    private final CategorieRep categorieRep;

    private final Path barcodeStoragePath = Paths.get("barcodes");
    private final LieuStockRepository lieuStockRepository; // Inject LieuStockRepository


    @Autowired
    public ProduitService(ProduitRepos produitRepos, CategorieRep categorieRepository, StockService stockService, LieuStockRepository lieuStockRepository) {
        this.produitRepos = produitRepos;
        this.categorieRep = categorieRepository;
        this.lieuStockRepository = lieuStockRepository; // Initialize LieuStockRepository
 this.stockService = stockService;
    }


    public Produit getProduitEntityByRef(String ref) {
        return produitRepos.findByRef(ref)
                .orElseThrow(() -> new EntityNotFoundException("Aucun produit trouvé avec la référence : " + ref));
    }

    /**
     * Crée un nouveau produit avec ses informations, y compris quantité par carton et prix par carton.
     * La quantité initiale en stock est toujours 0 (gérée par le système de stock).
     * Le code-barres est généré et son image est sauvegardée.
     */
    public ProduitDto createProduit(ProduitRequestDTO dto) {
        Categorie categorie = categorieRep.findById(dto.getCategorieId())
                .orElseThrow(() -> new EntityNotFoundException("Catégorie non trouvée avec l'ID : " + dto.getCategorieId()));
    
        Produit produit = new Produit();
        
        // AJOUT DES CHAMPS MANQUANTS
        produit.setNom(dto.getNom());           // <- MANQUANT
        produit.setRef(dto.getRef());           // <- MANQUANT  
        produit.setPrix(dto.getPrix());         // <- MANQUANT
        
        // Champs existants
        produit.setQteMin(dto.getQteMin());
        produit.setCategorie(categorie);
        produit.setQteParCarton(dto.getQteParCarton());
        produit.setPrixCarton(dto.getPrixCarton());
    
        Produit savedProduit = produitRepos.save(produit);
    
        generateBarcodeImage(savedProduit.getCodeBarre());
    
        return new ProduitDto(savedProduit);
    }

    /**
     * Met à jour les informations d'un produit, y compris quantité par carton et prix par carton.
     * Note: La quantité en stock n'est pas modifiée ici.
     */
    public ProduitDto updateProduit(Long id, ProduitRequestDTO dto) {
        Produit produitToUpdate = produitRepos.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Produit non trouvé pour la mise à jour : " + id));

        produitToUpdate.setRef(dto.getRef());
        produitToUpdate.setPrix(dto.getPrix()); // Prix unitaire
        produitToUpdate.setQteMin(dto.getQteMin());
        produitToUpdate.setQteParCarton(dto.getQteParCarton()); // Mettre à jour la quantité par carton
        produitToUpdate.setPrixCarton(dto.getPrixCarton()); // Mettre à jour le prix par carton


        Produit updatedProduit = produitRepos.save(produitToUpdate);

        return new ProduitDto(updatedProduit); // Assurez-vous que le constructeur de ProduitDto gère les nouveaux champs
    }

    /**
     * Récupère un produit par son ID.
     */
    @Transactional(readOnly = true)
    public Produit getProduitEntityById(Long id) { // Changer le type de retour pour retourner l'entité
        return produitRepos.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Produit non trouvé avec l'ID : " + id));
    }

    /**
     * Récupère la liste de tous les produits.
     */
    @Transactional(readOnly = true)
    public List<Produit> getAllProduitEntities() { // Changer le type de retour pour retourner la liste d'entités
        return produitRepos.findAll();
    }

    /**
     * Récupère un produit par son code-barres unique.
     * Lance une exception si non trouvé, qui sera gérée par le contrôleur.
     */
    @Transactional(readOnly = true)
    public Produit getProduitEntityByCodeBarre(String codeBarre) { // Changer le type de retour pour retourner l'entité
        return produitRepos.findByCodeBarre(codeBarre)
                .orElseThrow(() -> new EntityNotFoundException("Produit non trouvé avec le code-barres : " + codeBarre));
    }

    /**
     * Supprime un produit et l'image de son code-barres.
     */
    public void deleteProduit(Long id) {
        Produit produit = produitRepos.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Produit non trouvé pour la suppression : " + id));

        deleteBarcodeImage(produit.getCodeBarre());
        produitRepos.delete(produit);
    }

    public List<String> deleteProduitsEnIgnorantErreurs(List<Long> ids) {
        List<String> nomsNonSupprimes = new ArrayList<>();

        for (Long id : ids) {
            try {
                produitRepos.deleteById(id);
            } catch (DataIntegrityViolationException e) {
                produitRepos.findById(id).ifPresent(produit -> nomsNonSupprimes.add(produit.getNom()));
            }
        }

        return nomsNonSupprimes;
    }


    /**
     * Importer des produits en masse depuis un fichier Excel.
     * Adapte pour lire qteParCarton et prixCarton si les colonnes existent.
     */
    public List<ProduitDto> importProduitsFromExcel(MultipartFile file) {
        if (file.isEmpty()) {
            throw new IllegalArgumentException("Le fichier fourni est vide.");
        }

        List<Produit> produits = new ArrayList<>();
        DataFormatter formatter = new DataFormatter();

        try (Workbook workbook = WorkbookFactory.create(file.getInputStream())) {
            Sheet sheet = workbook.getSheetAt(0);
            Row headerRow = sheet.getRow(0);

            Map<String, Integer> colIndexMap = new HashMap<>();
            for (Cell cell : headerRow) {
                String header = formatter.formatCellValue(cell).trim().toLowerCase();
                colIndexMap.put(header, cell.getColumnIndex());
            }

            // Vérifie uniquement les colonnes obligatoires : nom et ref
            String[] requiredCols = {"nom", "ref"};
            for (String col : requiredCols) {
                if (!colIndexMap.containsKey(col)) {
                    throw new IllegalArgumentException("Colonne obligatoire manquante dans le fichier Excel: " + col);
                }
            }

            for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);
                if (row == null) continue;

                String nom = formatter.formatCellValue(row.getCell(colIndexMap.get("nom"))).trim();
                if (nom.isEmpty()) continue;

                String ref = formatter.formatCellValue(row.getCell(colIndexMap.get("ref"))).trim();
                if (ref.isEmpty()) continue;

                Produit p = new Produit();
                p.setNom(nom);
                p.setRef(ref);

                // Prix unitaire (optionnel, nom de colonne 'prix' ou 'prix_unitaire')
                if (colIndexMap.containsKey("prix")) {
                    String prixStr = formatter.formatCellValue(row.getCell(colIndexMap.get("prix"))).trim();
                    p.setPrix(parseDoubleSafe(prixStr, 0.0));
                } else if (colIndexMap.containsKey("prix_unitaire")) {
                    String prixStr = formatter.formatCellValue(row.getCell(colIndexMap.get("prix_unitaire"))).trim();
                    p.setPrix(parseDoubleSafe(prixStr, 0.0));
                }


                // Quantité minimale (optionnelle, nom de colonne 'qte_min')
                if (colIndexMap.containsKey("qte_min")) {
                    String qteStr = formatter.formatCellValue(row.getCell(colIndexMap.get("qte_min"))).trim();
                    p.setQteMin(parseIntSafe(qteStr, 0));
                }
                // p.setQte(0); // Ancienne gestion de quantité

                // Quantité par carton (optionnelle, nom de colonne 'qte_par_carton')
                 if (colIndexMap.containsKey("qte_par_carton")) {
                    String qteCartonStr = formatter.formatCellValue(row.getCell(colIndexMap.get("qte_par_carton"))).trim();
                    p.setQteParCarton(parseIntSafe(qteCartonStr, 0));
                }

                // Prix par carton (optionnel, nom de colonne 'prix_carton')
                 if (colIndexMap.containsKey("prix_carton")) {
                    String prixCartonStr = formatter.formatCellValue(row.getCell(colIndexMap.get("prix_carton"))).trim();
                    p.setPrixCarton(parseDoubleSafe(prixCartonStr, 0.0));
                }


                // Catégorie (optionnelle)
                if (colIndexMap.containsKey("categorie")) {
                    String categorieNom = formatter.formatCellValue(row.getCell(colIndexMap.get("categorie"))).trim();
                    if (!categorieNom.isEmpty()) {
                        Categorie cat = categorieRep.findByNomIgnoreCase(categorieNom)
                                .orElseGet(() -> {
                                    Categorie nouvelleCat = new Categorie();
                                    nouvelleCat.setNom(categorieNom);
                                    return categorieRep.save(nouvelleCat);
                                });
                        p.setCategorie(cat);
                    }
                }


                produits.add(p);
            }

        } catch (Exception e) {
            throw new RuntimeException("Erreur lors de l'lecture du fichier Excel: " + e.getMessage(), e);
        }

        List<Produit> savedProduits = produitRepos.saveAll(produits);
        savedProduits.forEach(p -> generateBarcodeImage(p.getCodeBarre()));

        return savedProduits.stream()
                .map(ProduitDto::new) // Utiliser le constructeur de ProduitDto
                .collect(Collectors.toList());
    }


    // Méthodes utilitaires (inchangées)
    private double parseDoubleSafe(String value, double defaultValue) {
        try {
            return Double.parseDouble(value.replace(",", "."));
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    private int parseIntSafe(String value, int defaultValue) {
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    private String getValueSafely(Row row, Integer columnIndex, DataFormatter formatter) {
        if (columnIndex == null) {
            return "";
        }
        Cell cell = row.getCell(columnIndex);
        if (cell == null) {
            return "";
        }
        return formatter.formatCellValue(cell).trim();
    }

    private void generateBarcodeImage(String barcodeText) {
        if (barcodeText == null || barcodeText.isEmpty()) return;
        try {
            Files.createDirectories(barcodeStoragePath);
            Path fichier = barcodeStoragePath.resolve(barcodeText + ".png");
            BitMatrix matrix = new MultiFormatWriter().encode(barcodeText, BarcodeFormat.CODE_128, 200, 130);
            MatrixToImageWriter.writeToPath(matrix, "PNG", fichier);
        } catch (Exception e) {
            System.out.println("Erreur code-barres: " + e.getMessage());
        }
    }

    private void deleteBarcodeImage(String barcodeText) {
        if (barcodeText == null || barcodeText.isEmpty()) {
            return;
        }
        try {
            Path fileToDelete = barcodeStoragePath.resolve(barcodeText + ".png");
            Files.deleteIfExists(fileToDelete);
        } catch (IOException e) {
            System.err.println("ERREUR: Impossible de supprimer l'image du code-barres '" + barcodeText + "': " + e.getMessage());
        }
    }


    //RECHERCHE (adapter mapToDto)
    public List<Produit> searchProduitEntities(String searchTerm) { // Changer le type de retour pour retourner la liste d'entités
        return produitRepos.searchProduits(searchTerm);
    }


    @Transactional
    public void assignerCategorieEtEntrepot(AssignationProduitsDTO dto) {
        List<Produit> produits = produitRepos.findAllById(dto.getProduitIds());

        if (produits.isEmpty()) {
            throw new IllegalArgumentException("Aucun produit trouvé avec les IDs fournis.");
        }

        Categorie categorie = null;
        LieuStock lieuStock = null; // Initialize lieuStock

        if (dto.getCategorieId() != null) {
            categorie = categorieRep.findById(dto.getCategorieId())
                    .orElseThrow(() -> new EntityNotFoundException("Catégorie non trouvée avec l'ID : " + dto.getCategorieId()));
        }

        if (dto.getLieuStockId() != null) {
            lieuStock = lieuStockRepository.findById(dto.getLieuStockId()).orElseThrow(() -> new EntityNotFoundException("Lieu de stock non trouvé avec l'ID : " + dto.getLieuStockId()));
        }

        for (Produit produit : produits) {
            if (categorie != null) {
                produit.setCategorie(categorie);
            }
            if (lieuStock != null) {
                stockService.createOrUpdateStockEntry(produit, lieuStock, 0); // Use StockService
            }
        }

        produitRepos.saveAll(produits);
    }
}
