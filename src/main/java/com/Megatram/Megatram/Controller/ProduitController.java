package com.Megatram.Megatram.Controller;

import com.Megatram.Megatram.Dto.AssignationProduitsDTO;
import com.Megatram.Megatram.Dto.ProduitAdminDto; // Importer le nouveau DTO
import com.Megatram.Megatram.Dto.ProduitRequestDTO;
import com.Megatram.Megatram.Dto.ProduitDto;
import com.Megatram.Megatram.Entity.Produit;
import com.Megatram.Megatram.service.ProduitService;
import com.Megatram.Megatram.service.StockService; // Importer StockService
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.net.MalformedURLException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/produits")
@Tag(name = "Produits", description = "API pour la gestion complète des produits")
@CrossOrigin(origins = "http://localhost:3000")
public class ProduitController {

    private final ProduitService produitService;
    private final StockService stockService; // Injecter StockService

    @Autowired
    public ProduitController(ProduitService produitService, StockService stockService) {
        this.produitService = produitService;
        this.stockService = stockService; // Assigner StockService
    }

    @Operation(summary = "Crée un nouveau produit")
    @PostMapping
    @PreAuthorize("hasAuthority('PRODUIT_CREATE') or hasAnyRole('ADMIN')")
    public ResponseEntity<ProduitAdminDto> createProduit(@RequestBody ProduitRequestDTO requestDto) {
        ProduitDto nouveauProduitDto = produitService.createProduit(requestDto);
        ProduitAdminDto nouveauProduitAdminDto = new ProduitAdminDto(produitService.getProduitEntityById(nouveauProduitDto.getId()));
        nouveauProduitAdminDto.setQuantiteTotaleGlobale(stockService.getQuantiteTotaleGlobaleByProduit(nouveauProduitDto.getId()));
        return new ResponseEntity<>(nouveauProduitAdminDto, HttpStatus.CREATED);
    }

    @Operation(summary = "Met à jour un produit existant par son ID")
    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('PRODUIT_UPDATE') or hasAnyRole('ADMIN')")
    public ResponseEntity<ProduitAdminDto> updateProduit(@PathVariable Long id, @RequestBody ProduitRequestDTO requestDto) {
        ProduitDto produitMisAJourDto = produitService.updateProduit(id, requestDto);
        ProduitAdminDto produitMisAJourAdminDto = new ProduitAdminDto(produitService.getProduitEntityById(produitMisAJourDto.getId()));
        produitMisAJourAdminDto.setQuantiteTotaleGlobale(stockService.getQuantiteTotaleGlobaleByProduit(produitMisAJourDto.getId()));
        return ResponseEntity.ok(produitMisAJourAdminDto);
    }

    @Operation(summary = "Assigner catégorie et lieu de stock à plusieurs produits")
    @PutMapping("/assignation")
    @PreAuthorize("hasAuthority('PRODUIT_UPDATE') or hasAnyRole('ADMIN')")
    public ResponseEntity<String> assignerCategorieEtLieuStock(@RequestBody AssignationProduitsDTO dto) {
        try {
            produitService.assignerCategorieEtEntrepot(dto);
            return ResponseEntity.ok("Assignation effectuée avec succès.");
        } catch (EntityNotFoundException | IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Une erreur est survenue lors de l'assignation.");
        }
    }

    @Operation(summary = "Récupère la liste de tous les produits")
    @GetMapping
    @PreAuthorize("hasAuthority('PRODUIT_READ') or hasAnyRole('ADMIN','BOUTIQUIER')")
    public ResponseEntity<List<ProduitAdminDto>> getAllProduits() {
        List<Produit> produits = produitService.getAllProduitEntities();
        List<ProduitAdminDto> produitAdminDtos = produits.stream()
                .map(produit -> {
                    ProduitAdminDto dto = new ProduitAdminDto(produit);
                    dto.setQuantiteTotaleGlobale(stockService.getQuantiteTotaleGlobaleByProduit(produit.getId()));
                    return dto;
                })
                .collect(Collectors.toList());
        return ResponseEntity.ok(produitAdminDtos);
    }

    @Operation(summary = "Récupère un produit par son ID")
    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('PRODUIT_READ') or hasAnyRole('ADMIN')")
    public ResponseEntity<ProduitAdminDto> getProduitById(@PathVariable Long id) {
        try {
            Produit produit = produitService.getProduitEntityById(id);
            ProduitAdminDto produitAdminDto = new ProduitAdminDto(produit);
            produitAdminDto.setQuantiteTotaleGlobale(stockService.getQuantiteTotaleGlobaleByProduit(id));
            return ResponseEntity.ok(produitAdminDto);
        } catch (EntityNotFoundException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @Operation(summary = "Recherche un produit par son code-barres")
    @GetMapping("/code/{codeBarre}")
    @PreAuthorize("hasAuthority('PRODUIT_READ') or hasAnyRole('ADMIN')")
    public ResponseEntity<?> getProduitByCodeBarre(@PathVariable String codeBarre) {
        try {
            Produit produit = produitService.getProduitEntityByCodeBarre(codeBarre);
            ProduitAdminDto produitAdminDto = new ProduitAdminDto(produit);
            produitAdminDto.setQuantiteTotaleGlobale(stockService.getQuantiteTotaleGlobaleByProduit(produit.getId()));
            return ResponseEntity.ok(produitAdminDto);
        } catch (EntityNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }
    }

    @Operation(summary = "Supprime un produit par son ID")
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('PRODUIT_DELETE') or hasAnyRole('ADMIN')")
    public ResponseEntity<Void> deleteProduit(@PathVariable Long id) {
        try {
            produitService.deleteProduit(id);
            return ResponseEntity.noContent().build();
        } catch (EntityNotFoundException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @Operation(summary = "Supprime plusieurs produits par leurs IDs")
    @DeleteMapping
    @PreAuthorize("hasAuthority('PRODUIT_DELETE') or hasAnyRole('ADMIN')")
    public ResponseEntity<?> deleteMultipleProduits(@RequestBody List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return ResponseEntity.badRequest().body("La liste des IDs ne peut pas être vide.");
        }
        List<String> nomsNonSupprimes = produitService.deleteProduitsEnIgnorantErreurs(ids);
        if (!nomsNonSupprimes.isEmpty()) {
            String message = "Impossible de supprimer les produits suivants car ils sont utilisés : " + String.join(", ", nomsNonSupprimes);
            return ResponseEntity.status(HttpStatus.CONFLICT).body(message);
        }
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Importe des produits depuis un fichier Excel (.xlsx)")
    @PostMapping(path = "/import", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasAuthority('PRODUIT_IMPORT') or hasAnyRole('ADMIN')")
    public ResponseEntity<?> importProduitsFromExcel(@RequestParam("file") MultipartFile file) {
        try {
            List<ProduitDto> produits = produitService.importProduitsFromExcel(file);
            return ResponseEntity.ok(produits);
        } catch (IllegalArgumentException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Erreur interne lors de l'import : " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    @Operation(summary = "Récupère l'image PNG d'un code-barres de produit")
    @GetMapping("/code/{codeBarre}/image")
    @PreAuthorize("hasAuthority('PRODUIT_READ') or hasAnyRole('ADMIN')")
    public ResponseEntity<Resource> getBarcodeImage(@PathVariable String codeBarre) {
        try {
            Path barcodePath = Paths.get("barcodes").resolve(codeBarre + ".png");
            Resource resource = new UrlResource(barcodePath.toUri());

            if (resource.exists() && resource.isReadable()) {
                return ResponseEntity.ok()
                        .contentType(MediaType.IMAGE_PNG)
                        .body(resource);
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (MalformedURLException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @Operation(summary = "Recherche des produits")
    @GetMapping("/search")
    @PreAuthorize("hasAuthority('PRODUIT_READ') or hasAnyRole('ADMIN')")
    public ResponseEntity<List<ProduitAdminDto>> searchProduits(@RequestParam("q") String query) {
        List<Produit> produits = produitService.searchProduitEntities(query);
        List<ProduitAdminDto> produitAdminDtos = produits.stream()
                .map(produit -> {
                    ProduitAdminDto dto = new ProduitAdminDto(produit);
                    dto.setQuantiteTotaleGlobale(stockService.getQuantiteTotaleGlobaleByProduit(produit.getId()));
                    return dto;
                })
                .collect(Collectors.toList());
        if (produitAdminDtos.isEmpty()) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok(produitAdminDtos);
    }
}
