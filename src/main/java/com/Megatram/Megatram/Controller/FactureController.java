package com.Megatram.Megatram.Controller;

import com.Megatram.Megatram.Dto.FactureResponseDTO;
import com.Megatram.Megatram.service.FactureService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/factures")
@Tag(name = "Factures", description = "API pour la génération et la gestion des factures")
@CrossOrigin(origins = "http://localhost:3000") // Adaptez si nécessaire
public class FactureController {

    private final FactureService factureService;

    @Autowired
    public FactureController(FactureService factureService) {
        this.factureService = factureService;
    }

    @Operation(summary = "Génère une nouvelle facture pour une commande validée")
    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'SECRETARIAT') or hasAnyRole('ADMIN')") // Seuls les admins ou la secrétaire peuvent générer des factures
    public ResponseEntity<?> genererFacture(
            @Parameter(description = "ID de la commande à facturer", required = true)
            @RequestParam Long commandeId) {
        try {
            FactureResponseDTO nouvelleFacture = factureService.genererFacturePourCommande(commandeId);
            // 201 CREATED est le code de statut correct pour une création de ressource réussie
            return new ResponseEntity<>(nouvelleFacture, HttpStatus.CREATED);
        } catch (EntityNotFoundException e) {
            // Si la commande n'existe pas
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (IllegalStateException e) {
            // Si une règle métier n'est pas respectée (ex: commande non validée, facture déjà existante)
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    @Operation(summary = "Récupère la liste de toutes les factures")
    @GetMapping
   @PreAuthorize(" hasAuthority('FACTURE_GENERATE') or hasAnyRole('ADMIN', 'DG', 'CONTROLEUR')") // Seuls les rôles de supervision peuvent tout voir
    public ResponseEntity<List<FactureResponseDTO>> getAllFactures() {
        List<FactureResponseDTO> factures = factureService.getAllFactures();
        return ResponseEntity.ok(factures);
    }

    @Operation(summary = "Récupère une facture par son ID")
    @GetMapping("/{id}")
    public ResponseEntity<?> getFactureById(@PathVariable Long id) {
        try {
            FactureResponseDTO facture = factureService.getFactureById(id);
            return ResponseEntity.ok(facture);
        } catch (EntityNotFoundException e) {
            // Gère le cas où la facture avec cet ID n'est pas trouvée
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }
    }

    @Operation(summary = "Supprime une facture par son ID (Opération sensible)")
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')") // Seul un ADMIN peut supprimer une facture
    public ResponseEntity<?> deleteFacture(@PathVariable Long id) {
        try {
            factureService.deleteFacture(id);
            // 204 NO CONTENT est le code standard pour une suppression réussie
            return ResponseEntity.noContent().build();
        } catch (EntityNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }
    }
}