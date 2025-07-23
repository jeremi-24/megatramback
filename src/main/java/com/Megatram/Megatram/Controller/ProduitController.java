package com.Megatram.Megatram.Controller;

import com.Megatram.Megatram.Dto.AssignationProduitsDTO;
import com.Megatram.Megatram.Dto.ProduitDto;
import com.Megatram.Megatram.Dto.ProduitRequestDTO;
import com.Megatram.Megatram.Entity.Produit;
import com.Megatram.Megatram.service.ProduitService;
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
@RequestMapping("/api/produits") // Convention REST: nom de la ressource au pluriel
@Tag(name = "Produits", description = "API pour la gestion complète des produits")
@CrossOrigin(origins = "http://localhost:3000") // Adaptez si votre frontend est sur un autre port
public class ProduitController {

    // Le contrôleur ne dépend QUE du service. Plus de dépendance au Repository ici.
    private final ProduitService produitService;

    @Autowired
    public ProduitController(ProduitService produitService) {
        this.produitService = produitService;
    }

    @Operation(summary = "Crée un nouveau produit")
    @PostMapping
//    @PreAuthorize("hasRole('ADMIN')")
    @PreAuthorize("hasAuthority('PRODUIT_CREATE')")
    public ResponseEntity<ProduitDto> createProduit(@RequestBody ProduitRequestDTO requestDto) {
        // On envoie le DTO de requête (sécurisé) au service
        ProduitDto nouveauProduit = produitService.createProduit(requestDto);
        // On retourne le DTO de réponse (complet) avec le statut 201 CREATED
        return new ResponseEntity<>(nouveauProduit, HttpStatus.CREATED);
    }

    @Operation(summary = "Met à jour un produit existant par son ID")
    @PutMapping("/{id}")
//    @PreAuthorize("hasRole('ADMIN')")
    @PreAuthorize("hasAuthority('PRODUIT_UPDATE')")
    public ResponseEntity<ProduitDto> updateProduit(@PathVariable Long id, @RequestBody ProduitRequestDTO requestDto) {
        // Le service gère la logique de mise à jour
        ProduitDto produitMisAJour = produitService.updateProduit(id, requestDto);
        return ResponseEntity.ok(produitMisAJour);
    }





    @PutMapping("/assignation")
    @PreAuthorize("hasAuthority('PRODUIT_UPDATE')")
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



    @PreAuthorize("hasAuthority('PRODUIT_READ')")
    @Operation(summary = "Récupère la liste de tous les produits")
    @GetMapping
    public ResponseEntity<List<ProduitDto>> getAllProduits() {
        List<ProduitDto> produits = produitService.getAllProduits();
        return ResponseEntity.ok(produits);
    }

    @Operation(summary = "Récupère un produit par son ID")
    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('PRODUIT_READ')")
    public ResponseEntity<ProduitDto> getProduitById(@PathVariable Long id) {
        try {
            ProduitDto produit = produitService.getProduitById(id);
            return ResponseEntity.ok(produit);
        } catch (EntityNotFoundException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @Operation(summary = "Recherche un produit par son code-barres")
    @GetMapping("/code/{codeBarre}")
    @PreAuthorize("hasAuthority('PRODUIT_READ')")
    public ResponseEntity<?> getProduitByCodeBarre(@PathVariable String codeBarre) {
        try {
            // Cette méthode doit être ajoutée dans votre classe ProduitService
            ProduitDto produit = produitService.getProduitByCodeBarre(codeBarre);
            return ResponseEntity.ok(produit);
        } catch (EntityNotFoundException e) {
            // Si le service ne trouve rien, il lance une exception que l'on attrape ici
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }
    }







    @Operation(summary = "Supprime un produit par son ID")
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteProduit(@PathVariable Long id) {
        produitService.deleteProduit(id);
        return ResponseEntity.noContent().build();
    }



    @Operation(summary = "Supprime plusieurs produits par leurs IDs")
    @DeleteMapping
    @PreAuthorize("hasRole('ADMIN')")
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





//    @Operation(summary = "Importe des produits depuis un fichier Excel (.xlsx)")
//    @PostMapping(path = "/import", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
//    public ResponseEntity<List<ProduitDto>> importProduitsFromExcel(@RequestParam("file") MultipartFile file) {
//        List<ProduitDto> produits = produitService.importProduitsFromExcel(file);
//        return ResponseEntity.ok(produits);
//    }


    @PostMapping(path = "/import", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> importProduitsFromExcel(@RequestParam("file") MultipartFile file) {
        try {
            List<ProduitDto> produits = produitService.importProduitsFromExcel(file);
            return ResponseEntity.ok(produits);
        } catch (IllegalArgumentException e) {
            // Erreur attendue, colonnes manquantes ou fichier vide
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        } catch (Exception e) {
            // Erreur non prévue, serveur
            Map<String, String> error = new HashMap<>();
            error.put("error", "Erreur interne lors de l'import : " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }


    @Operation(summary = "Récupère l'image PNG d'un code-barres de produit")
    @GetMapping("/code/{codeBarre}/image")
    public ResponseEntity<Resource> getBarcodeImage(@PathVariable String codeBarre) {
        try {
            Path barcodePath = Paths.get("barcodes").resolve(codeBarre + ".png");
            Resource resource = new UrlResource(barcodePath.toUri());

            if (resource.exists() && resource.isReadable()) {
                return ResponseEntity.ok()
                        .contentType(MediaType.IMAGE_PNG)
                        .body(resource);
            } else {
                // Si le fichier image n'existe pas sur le serveur
                return ResponseEntity.notFound().build();
            }
        } catch (MalformedURLException e) {
            // Si le chemin du fichier est mal formé (ne devrait pas arriver ici)
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }


    @GetMapping("/search")
    public ResponseEntity<List<ProduitDto>> searchProduits(@RequestParam("q") String query) {
        List<ProduitDto> produits = produitService.searchProduits(query);
        if (produits.isEmpty()) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok(produits);
    }

}