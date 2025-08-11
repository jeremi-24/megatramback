package com.Megatram.Megatram.Controller;

import com.Megatram.Megatram.Dto.InventaireRequestDto;
import com.Megatram.Megatram.Dto.InventaireResponseDto;
import com.Megatram.Megatram.service.InventaireService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/inventaire")
@Tag(name = "Inventaire", description = "Gestion des inventaire")
public class InventaireController {

    @Autowired
    private InventaireService inventaireService;

    // ========== WORKFLOW EN 2 ÉTAPES ==========

    /**
     * Étape 1 : Créer un inventaire et calculer les écarts (SANS appliquer au stock)
     */
    @Operation(summary = "Créer un inventaire et calculer les écarts sans appliquer au stock")
    @PostMapping("/calculer")
    @PreAuthorize("hasAuthority('INVENTAIRE_MANAGE') or hasAnyRole('ADMIN')")
    public ResponseEntity<InventaireResponseDto> calculerInventaire(@RequestBody InventaireRequestDto request) {
        InventaireResponseDto response = inventaireService.creerInventaireSansApplique(request);
        return ResponseEntity.ok(response);
    }

    /**
     * Étape 1 bis : Modifier un inventaire et recalculer les écarts (SANS appliquer au stock)
     */
    @Operation(summary = "Modifier un inventaire et recalculer les écarts sans appliquer au stock")
    @PutMapping("/{id}/calculer")
    @PreAuthorize("hasAuthority('INVENTAIRE_MANAGE') or hasAnyRole('ADMIN')")
    public ResponseEntity<InventaireResponseDto> recalculerInventaire(
            @PathVariable Long id, 
            @RequestBody InventaireRequestDto request) {
        InventaireResponseDto response = inventaireService.modifierInventaireSansApplique(id, request);
        return ResponseEntity.ok(response);
    }

    /**
     * Obtenir un résumé des écarts avant confirmation
     */
    @Operation(summary = "Obtenir un résumé des écarts calculés")
    @GetMapping("/{id}/resume-ecarts")
    @PreAuthorize("hasAuthority('INVENTAIRE_MANAGE') or hasAnyRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> getResumeEcarts(@PathVariable Long id) {
        Map<String, Object> resume = inventaireService.getResumerEcarts(id);
        return ResponseEntity.ok(resume);
    }

    /**
     * Étape 2 : Confirmer et appliquer les écarts au stock
     */
    @Operation(summary = "Confirmer et appliquer les écarts au stock")
    @PostMapping("/{id}/confirmer")
    @PreAuthorize("hasAuthority('INVENTAIRE_MANAGE') or hasAnyRole('ADMIN')")
    public ResponseEntity<String> confirmerEtAppliquerEcarts(
            @PathVariable Long id,
            @RequestParam(defaultValue = "false") boolean premier) {
        inventaireService.appliquerEcartsAuStock(id, premier);
        return ResponseEntity.ok("Écarts appliqués avec succès au stock");
    }

    // ========== WORKFLOW DIRECT (POUR COMPATIBILITÉ) ==========

    /**
     * Méthode directe : créer et appliquer en une seule fois
     */
    @Operation(summary = "Créer inventaire (méthode directe)")
    @PostMapping
    @PreAuthorize("hasAuthority('INVENTAIRE_MANAGE') or hasAnyRole('ADMIN')")
    public ResponseEntity<InventaireResponseDto> creerInventaire(
            @RequestBody InventaireRequestDto request,
            @RequestParam(defaultValue = "false") boolean premier) {
        InventaireResponseDto response = inventaireService.enregistrerInventaire(request, premier);
        return ResponseEntity.ok(response);
    }

    /**
     * Méthode directe : modifier et appliquer en une seule fois
     */
    @Operation(summary = "Modifie un inventaire existant (méthode directe)")
    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('INVENTAIRE_MANAGE') or hasAnyRole('ADMIN')")
    public ResponseEntity<InventaireResponseDto> modifierInventaire(
            @PathVariable Long id, 
            @RequestBody InventaireRequestDto request,
            @RequestParam(defaultValue = "false") boolean premier) {
        InventaireResponseDto response = inventaireService.modifierInventaire(id, request, premier);
        return ResponseEntity.ok(response);
    }

    // ========== AUTRES ENDPOINTS ==========

    @Operation(summary = "Récupère tous les inventaires")
    @GetMapping
    @PreAuthorize("hasAuthority('INVENTAIRE_MANAGE') or hasAnyRole('ADMIN')")
    public ResponseEntity<List<InventaireResponseDto>> getAllInventaires() {
        List<InventaireResponseDto> inventaires = inventaireService.recupererTousLesInventaires();
        return ResponseEntity.ok(inventaires);
    }

    @Operation(summary = "Récupère un inventaire par son ID")
    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('INVENTAIRE_MANAGE') or hasAnyRole('ADMIN')")
    public ResponseEntity<InventaireResponseDto> getInventaire(@PathVariable Long id) {
        return ResponseEntity.ok(inventaireService.getInventaireById(id));
    }

    @Operation(summary = "Exporte un inventaire en fichier Excel")
    @GetMapping("/{id}/export")
    @PreAuthorize("hasAuthority('INVENTAIRE_MANAGE') or hasAnyRole('ADMIN')")
    public ResponseEntity<Resource> exportInventaire(@PathVariable Long id) {
        try {
            ByteArrayInputStream in = inventaireService.exportInventaireToExcel(id);
            
            String timestamp = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss").format(new Date());
            String filename = "inventaire_" + id + "_" + timestamp + ".xlsx";

            HttpHeaders headers = new HttpHeaders();
            headers.add("Content-Disposition", "attachment; filename=" + filename);

            return ResponseEntity
                    .ok()
                    .headers(headers)
                    .contentType(MediaType.APPLICATION_OCTET_STREAM)
                    .body(new InputStreamResource(in));

        } catch (IOException e) {
            // Gérer l'exception de manière appropriée
            return ResponseEntity.status(500).build();
        }
    }
}