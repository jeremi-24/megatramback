package com.Megatram.Megatram.Controller;

import com.Megatram.Megatram.Dto.InventaireRequestDto;
import com.Megatram.Megatram.Dto.InventaireResponseDto;
import com.Megatram.Megatram.service.InventaireService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize; // Importer PreAuthorize
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/inventaire")
@Tag(name = "Inventaire", description = "Gestion des inventaire")

public class InventaireController {

    @Autowired
    private InventaireService inventaireService;

    @Operation(summary = "post inventaire")
    @PostMapping
    @PreAuthorize("hasAuthority('INVENTAIRE_MANAGE') or hasAnyRole('ADMIN')") // Basé sur la permission INVENTAIRE_MANAGE
    public ResponseEntity<InventaireResponseDto> creerInventaire(@RequestBody InventaireRequestDto request) {
        InventaireResponseDto response = inventaireService.enregistrerInventaire(request);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Récupère tous les inventaires") // Ajouter summary
    @GetMapping
    @PreAuthorize("hasAuthority('INVENTAIRE_MANAGE') or hasAnyRole('ADMIN')") // Basé sur les permissions INVENTAIRE_MANAGE et INVENTAIRE_READ ") // Basé sur la permission INVENTAIRE_MANAGE pour lire
    public ResponseEntity<List<InventaireResponseDto>> getAllInventaires() {
        List<InventaireResponseDto> inventaires = inventaireService.recupererTousLesInventaires();
        return ResponseEntity.ok(inventaires);
    }

    @Operation(summary = "Récupère un inventaire par son ID") // Ajouter summary
    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('INVENTAIRE_MANAGE') or hasAnyRole('ADMIN')") // Basé sur la permission INVENTAIRE_MANAGE pour lire
    public ResponseEntity<InventaireResponseDto> getInventaire(@PathVariable Long id) {
        InventaireResponseDto response = inventaireService.getInventaireById(id);
        return ResponseEntity.ok(response);
    }

    // Si une méthode de suppression pour les inventaires existe ou est ajoutée,
    // elle devrait également avoir une annotation @PreAuthorize('INVENTAIRE_MANAGE') ou similaire.
}