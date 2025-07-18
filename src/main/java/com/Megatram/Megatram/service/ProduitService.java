package com.Megatram.Megatram.service;

import com.Megatram.Megatram.Dto.ProduitRequestDTO;
import com.Megatram.Megatram.Dto.ProduitDto;
import com.Megatram.Megatram.Entity.Categorie;
import com.Megatram.Megatram.Entity.LieuStock;
import com.Megatram.Megatram.Entity.Produit;
import com.Megatram.Megatram.repository.CategorieRep;
import com.Megatram.Megatram.repository.LieuStockRepository;
import com.Megatram.Megatram.repository.ProduitRepos;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import jakarta.persistence.EntityNotFoundException;
import org.apache.poi.ss.usermodel.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Transactional
public class ProduitService {

    private final ProduitRepos produitRepos;
    private final CategorieRep categorieRep;
    private final LieuStockRepository lieuStockRepository;

    // Définir le chemin de stockage des codes-barres une seule fois
    private final Path barcodeStoragePath = Paths.get("barcodes");

    @Autowired
    public ProduitService(ProduitRepos produitRepos, CategorieRep categorieRepository, LieuStockRepository lieuStockRepository) {
        this.produitRepos = produitRepos;
        this.categorieRep = categorieRepository;
        this.lieuStockRepository = lieuStockRepository;
    }

    /**
     * Crée un nouveau produit. La quantité initiale est toujours 0.
     * Le code-barres est généré et son image est sauvegardée.
     */
    public ProduitDto createProduit(ProduitRequestDTO dto) {
        Categorie categorie = categorieRep.findById(dto.getCategorieId())
                .orElseThrow(() -> new EntityNotFoundException("Catégorie non trouvée avec l'ID : " + dto.getCategorieId()));

        LieuStock lieuStock = lieuStockRepository.findById(dto.getLieuStockId())
                .orElseThrow(() -> new EntityNotFoundException("Lieu de stock non trouvé avec l'ID : " + dto.getLieuStockId()));

        Produit produit = new Produit();
        produit.setNom(dto.getNom());
        produit.setRef(dto.getRef());
        produit.setPrix(dto.getPrix());
        produit.setQteMin(dto.getQteMin());
        produit.setQte(0); // Règle métier : la quantité est 0 à la création.
        produit.setCategorie(categorie);
        produit.setLieuStock(lieuStock);

        // Le code-barres texte est généré par @PrePersist dans l'entité Produit
        Produit savedProduit = produitRepos.save(produit);

        // On génère l'image du code-barres après la sauvegarde
        generateBarcodeImage(savedProduit.getCodeBarre());

        return new ProduitDto(savedProduit);
    }

    /**
     * Met à jour les informations d'un produit.
     * Note: La quantité n'est pas modifiée ici, elle doit l'être via des mouvements de stock.
     */
    public ProduitDto updateProduit(Long id, ProduitRequestDTO dto) {
        Produit produitToUpdate = produitRepos.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Produit non trouvé pour la mise à jour : " + id));

        Categorie categorie = categorieRep.findById(dto.getCategorieId())
                .orElseThrow(() -> new EntityNotFoundException("Catégorie non trouvée avec l'ID : " + dto.getCategorieId()));

        LieuStock lieuStock = lieuStockRepository.findById(dto.getLieuStockId())
                .orElseThrow(() -> new EntityNotFoundException("Lieu de stock non trouvé avec l'ID : " + dto.getLieuStockId()));

        produitToUpdate.setNom(dto.getNom());
        produitToUpdate.setRef(dto.getRef());
        produitToUpdate.setPrix(dto.getPrix());
        produitToUpdate.setQteMin(dto.getQteMin());
        produitToUpdate.setCategorie(categorie);
        produitToUpdate.setLieuStock(lieuStock);

        Produit updatedProduit = produitRepos.save(produitToUpdate);

        return new ProduitDto(updatedProduit);
    }

    /**
     * Récupère un produit par son ID.
     */
    @Transactional(readOnly = true)
    public ProduitDto getProduitById(Long id) {
        Produit produit = produitRepos.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Produit non trouvé avec l'ID : " + id));
        return new ProduitDto(produit);
    }

    /**
     * Récupère la liste de tous les produits.
     */
    @Transactional(readOnly = true)
    public List<ProduitDto> getAllProduits() {
        return produitRepos.findAll().stream()
                .map(ProduitDto::new)
                .collect(Collectors.toList());
    }

    /**
     * Récupère un produit par son code-barres unique.
     * Lance une exception si non trouvé, qui sera gérée par le contrôleur.
     */
    @Transactional(readOnly = true)
    public ProduitDto getProduitByCodeBarre(String codeBarre) {
        // Utilise le repository pour trouver le produit
        Produit produit = produitRepos.findByCodeBarre(codeBarre)
                .orElseThrow(() -> new EntityNotFoundException("Produit non trouvé avec le code-barres : " + codeBarre));
        // Convertit l'entité trouvée en DTO de réponse
        return new ProduitDto(produit);
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

    /**
     * Supprime une liste de produits et leurs images de codes-barres.
     */
    public void deleteProduitsByIds(List<Long> ids) {
        List<Produit> produitsToDelete = produitRepos.findAllById(ids);
        for (Produit produit : produitsToDelete) {
            deleteBarcodeImage(produit.getCodeBarre());
        }
        produitRepos.deleteAll(produitsToDelete);
    }

    /**
     * Importer des produits en masse depuis un fichier Excel.
     */
    public List<ProduitDto> importProduitsFromExcel(MultipartFile file) {
        if (file.isEmpty()) {
            throw new IllegalArgumentException("Le fichier fourni est vide.");
        }

        List<Produit> produits = new ArrayList<>();
        DataFormatter formatter = new DataFormatter();

        try (Workbook workbook = WorkbookFactory.create(file.getInputStream())) {
            Sheet sheet = workbook.getSheetAt(0);
            Map<String, Integer> columnIndex = new HashMap<>();

            // 🔍 Lire les noms de colonnes dynamiquement
            Row headerRow = sheet.getRow(0);
            for (Cell cell : headerRow) {
                String colName = formatter.formatCellValue(cell).trim().toLowerCase();
                columnIndex.put(colName, cell.getColumnIndex());
            }

            for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);
                if (row == null) continue;

                String nom = formatter.formatCellValue(row.getCell(columnIndex.get("nom"))).trim();
                if (nom.isBlank()) continue;

                Produit produit = new Produit();
                produit.setNom(nom);
                produit.setRef(formatter.formatCellValue(row.getCell(columnIndex.get("ref"))).trim());
                produit.setPrix(Double.parseDouble(formatter.formatCellValue(row.getCell(columnIndex.get("prix"))).trim()));
                produit.setQteMin(Integer.parseInt(formatter.formatCellValue(row.getCell(columnIndex.get("qte"))).trim()));
                produit.setQte(0);

                // 🏷️ Catégorie (créée si elle n'existe pas)
                String categorieNom = formatter.formatCellValue(row.getCell(columnIndex.get("categorie"))).trim();
                if (!categorieNom.isBlank()) {
                    Categorie cat = categorieRep.findByNomIgnoreCase(categorieNom)
                            .orElseGet(() -> {
                                Categorie nouvelleCat = new Categorie();
                                nouvelleCat.setNom(categorieNom);
                                return categorieRep.save(nouvelleCat);
                            });
                    produit.setCategorie(cat);
                }

                // 📦 Lieu de stock (créé si il n'existe pas)
                if (columnIndex.containsKey("lieu") || columnIndex.containsKey("lieustock")) {
                    int lieuCol = columnIndex.containsKey("lieu") ? columnIndex.get("lieu") : columnIndex.get("lieustock");
                    String lieuNom = formatter.formatCellValue(row.getCell(lieuCol)).trim();
                    if (!lieuNom.isBlank()) {
                        LieuStock lieu = lieuStockRepository.findByNomIgnoreCase(lieuNom)
                                .orElseGet(() -> {
                                    LieuStock nouveau = new LieuStock();
                                    nouveau.setNom(lieuNom);
                                    return lieuStockRepository.save(nouveau);
                                });
                        produit.setLieuStock(lieu);
                    }
                }

                produits.add(produit);
            }

        } catch (Exception e) {
            throw new RuntimeException("Erreur lors de l'importation Excel : " + e.getMessage(), e);
        }

        // Sauvegarde en base
        List<Produit> savedProduits = produitRepos.saveAll(produits);
        savedProduits.forEach(p -> generateBarcodeImage(p.getCodeBarre()));

        return savedProduits.stream()
                .map(ProduitDto::new)
                .collect(Collectors.toList());
    }


//    public List<ProduitDto> importProduitsFromExcel(MultipartFile file) {
//        if (file.isEmpty()) {
//            throw new IllegalArgumentException("Le fichier fourni est vide.");
//        }
//
//        List<Produit> produits = new ArrayList<>();
//        DataFormatter formatter = new DataFormatter(); // Format sûr pour lecture de toutes les cellules
//
//        try (Workbook workbook = WorkbookFactory.create(file.getInputStream())) {
//            Sheet sheet = workbook.getSheetAt(0);
//
//            for (Row row : sheet) {
//                if (row.getRowNum() == 0) continue; // Ignorer l'en-tête
//
//                Cell nomCell = row.getCell(0);
//                if (nomCell == null || nomCell.getCellType() == CellType.BLANK) continue; // Ignorer lignes vides
//
//                Produit p = new Produit();
//
//                // Lecture des valeurs texte ou numériques en toute sécurité
//                p.setNom(formatter.formatCellValue(row.getCell(0)).trim());
//                p.setRef(formatter.formatCellValue(row.getCell(1)).trim());
//                p.setPrix(Double.parseDouble(formatter.formatCellValue(row.getCell(2)).trim()));
//                p.setQteMin(Integer.parseInt(formatter.formatCellValue(row.getCell(3)).trim()));
//                p.setQte(0);
//
//                // --- LOGIQUE D'ASSOCIATION ---
//
//                // Catégorie
//                String categorieNom = formatter.formatCellValue(row.getCell(4)).trim();
//                if (!categorieNom.isBlank()) {
//                    Categorie cat = categorieRep.findByNomIgnoreCase(categorieNom)
//                            .orElseGet(() -> {
//                                Categorie nouvelleCat = new Categorie();
//                                nouvelleCat.setNom(categorieNom);
//                                return categorieRep.save(nouvelleCat);
//                            });
//                    p.setCategorie(cat);
//                }
//
//                // Lieu de stock
//                String lieuStockNom = formatter.formatCellValue(row.getCell(6)).trim();
//                System.out.println("Lieu stock lu depuis Excel : '" + lieuStockNom + "'"); // Debug
//                if (!lieuStockNom.isBlank()) {
//                    LieuStock lieu = lieuStockRepository.findByNomIgnoreCase(lieuStockNom)
//                            .orElseGet(() -> {
//                                LieuStock nouveauLieu = new LieuStock();
//                                nouveauLieu.setNom(lieuStockNom);
//                                return lieuStockRepository.save(nouveauLieu);
//                            });
//                    p.setLieuStock(lieu);
//                }
//
//                produits.add(p);
//            }
//        } catch (Exception e) {
//            throw new RuntimeException("Erreur lors de la lecture du fichier Excel: " + e.getMessage(), e);
//        }
//
//        List<Produit> savedProduits = produitRepos.saveAll(produits);
//        savedProduits.forEach(p -> generateBarcodeImage(p.getCodeBarre()));
//
//        return savedProduits.stream()
//                .map(ProduitDto::new)
//                .collect(Collectors.toList());
//    }

    // --- Méthodes privées utilitaires ---

    private void generateBarcodeImage(String barcodeText) {
        if (barcodeText == null || barcodeText.isEmpty()) {
            return;
        }
        try {
            if (!Files.exists(barcodeStoragePath)) {
                Files.createDirectories(barcodeStoragePath);
            }
            Path outputPath = barcodeStoragePath.resolve(barcodeText + ".png");
            BitMatrix bitMatrix = new MultiFormatWriter().encode(barcodeText, BarcodeFormat.CODE_128, 300, 100);
            MatrixToImageWriter.writeToPath(bitMatrix, "PNG", outputPath);
        } catch (Exception e) {
            // Logguer l'erreur mais ne pas faire échouer la transaction principale
            System.err.println("ERREUR: Impossible de générer l'image du code-barres '" + barcodeText + "': " + e.getMessage());
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
}