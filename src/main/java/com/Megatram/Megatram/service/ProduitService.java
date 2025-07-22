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

//    /**
//     * Supprime un produit  ou  plusieurs  l'image de son code-barres.
//     */
    public void deleteProduit(Long id) {
        Produit produit = produitRepos.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Produit non trouvé pour la suppression : " + id));

        deleteBarcodeImage(produit.getCodeBarre());
        produitRepos.delete(produit); // fonctionne maintenant grâce à ON DELETE SET NULL
    }

    public List<String> deleteProduitsEnIgnorantErreurs(List<Long> ids) {
        List<String> nomsNonSupprimes = new ArrayList<>();

        for (Long id : ids) {
            try {
                produitRepos.deleteById(id);
            } catch (DataIntegrityViolationException e) {
                // Récupère le nom du produit qui n'a pas pu être supprimé
                produitRepos.findById(id).ifPresent(produit -> nomsNonSupprimes.add(produit.getNom()));
            }
        }

        return nomsNonSupprimes; // Retourne la liste des noms des produits non supprimés
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
                if (nom.isEmpty()) continue; // Ignore ligne vide

                String ref = formatter.formatCellValue(row.getCell(colIndexMap.get("ref"))).trim();
                if (ref.isEmpty()) continue; // Ignore si pas de ref

                Produit p = new Produit();
                p.setNom(nom);
                p.setRef(ref);

                // Prix (optionnel)
                if (colIndexMap.containsKey("prix")) {
                    String prixStr = formatter.formatCellValue(row.getCell(colIndexMap.get("prix"))).trim();
                    p.setPrix(parseDoubleSafe(prixStr, 0.0));
                }

                // Quantité (optionnelle)
                if (colIndexMap.containsKey("qte")) {
                    String qteStr = formatter.formatCellValue(row.getCell(colIndexMap.get("qte"))).trim();
                    p.setQteMin(parseIntSafe(qteStr, 0));
                }
                p.setQte(0); // Toujours initialisé à 0

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

                // Lieu (optionnel)
                if (colIndexMap.containsKey("lieu")) {
                    String lieuStockNom = formatter.formatCellValue(row.getCell(colIndexMap.get("lieu"))).trim();
                    if (!lieuStockNom.isEmpty()) {
                        LieuStock lieu = lieuStockRepository.findByNomIgnoreCase(lieuStockNom)
                                .orElseGet(() -> {
                                    LieuStock nouveauLieu = new LieuStock();
                                    nouveauLieu.setNom(lieuStockNom);
                                    return lieuStockRepository.save(nouveauLieu);
                                });
                        p.setLieuStock(lieu);
                    }
                }

                produits.add(p);
            }

        } catch (Exception e) {
            throw new RuntimeException("Erreur lors de la lecture du fichier Excel: " + e.getMessage(), e);
        }

        List<Produit> savedProduits = produitRepos.saveAll(produits);
        savedProduits.forEach(p -> generateBarcodeImage(p.getCodeBarre()));

        return savedProduits.stream()
                .map(ProduitDto::new)
                .collect(Collectors.toList());
    }







    // Méthodes utilitaires
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



    /**
     * Récupère la valeur d'une cellule de manière sécurisée
     */
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
        // Si pas de texte, on fait rien
        if (barcodeText == null || barcodeText.isEmpty()) return;

        try {
            // Créer le dossier si il existe pas
            Files.createDirectories(barcodeStoragePath);

            // Chemin du fichier
            Path fichier = barcodeStoragePath.resolve(barcodeText + ".png");

            // Générer le code-barres et sauvegarder
            BitMatrix matrix = new MultiFormatWriter().encode(barcodeText, BarcodeFormat.CODE_128, 200, 150);
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





    //RECHERCHE
    public List<ProduitDto> searchProduits(String searchTerm) {
        List<Produit> produits = produitRepos.searchProduits(searchTerm);
        return produits.stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    private ProduitDto mapToDto(Produit produit) {
        ProduitDto dto = new ProduitDto(produit); // produit est une instance valide
        dto.setId(produit.getId());
        dto.setNom(produit.getNom());
        dto.setRef(produit.getRef());
        dto.setQte(produit.getQte());
        dto.setPrix(produit.getPrix());
        dto.setCodeBarre(produit.getCodeBarre());
        dto.setCategorieId(produit.getCategorie() != null ? produit.getCategorie().getId() : null);
        dto.setCategorieNom(produit.getCategorie() != null ? produit.getCategorie().getNom() : null);
        dto.setLieuStockId(produit.getLieuStock() != null ? produit.getLieuStock().getId() : null);
        dto.setLieuStockNom(produit.getLieuStock() != null ? produit.getLieuStock().getNom() : null);
        dto.setQteMin(produit.getQteMin());
        return dto;
    }



}