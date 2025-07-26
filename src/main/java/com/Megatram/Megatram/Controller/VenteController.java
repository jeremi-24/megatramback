package com.Megatram.Megatram.Controller;

import com.Megatram.Megatram.Dto.VenteDto;
import com.Megatram.Megatram.Dto.VenteResponseDTO;
import com.Megatram.Megatram.service.VenteService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/api/ventes")
@Tag(name = "Ventes", description = "Gestion des opérations de vente")
@CrossOrigin(origins = "http://localhost:3000")
public class VenteController {

    private final VenteService venteService;

    @Autowired
    public VenteController(VenteService venteService) {
        this.venteService = venteService;
    }

    @Operation(summary = "Crée une nouvelle vente directe (pour les boutiques)")
    @PostMapping("/directe")
    @PreAuthorize("hasAnyRole('BOUTIQUIER', 'ADMIN')")
    public ResponseEntity<?> creerVenteDirecte(@RequestBody VenteDto venteDto, Principal principal) {
        try {
            // On passe l'email de l'agent connecté comme "caissier"
            VenteResponseDTO venteCreee = venteService.createVenteDirecte(venteDto, principal.getName());
            return new ResponseEntity<>(venteCreee, HttpStatus.CREATED);
        } catch (EntityNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    @Operation(summary = "Récupérer toutes les ventes enregistrées")
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'DG', 'CONTROLEUR')")
    public ResponseEntity<List<VenteResponseDTO>> getAllVentes() {
        return ResponseEntity.ok(venteService.getAllVentes());
    }

    // ... autres endpoints (getById, delete...) si nécessaires
}