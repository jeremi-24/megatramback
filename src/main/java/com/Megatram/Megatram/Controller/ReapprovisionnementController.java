package com.Megatram.Megatram.Controller;

import com.Megatram.Megatram.Dto.InventaireResponseDto;
import com.Megatram.Megatram.Dto.ReapprovisionnementResponseDto;
import com.Megatram.Megatram.Dto.ReapprovisionnementDetailsDto;
import com.Megatram.Megatram.Dto.ReapprovisionnementRequestDto;
import com.Megatram.Megatram.service.ReapprovisionnementService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.persistence.EntityNotFoundException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize; // Importer PreAuthorize
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.HashMap;

@RestController
@RequestMapping("/api/reappro")
@Tag(name = "Réapprovisionnements", description = "Gestion des opérations de réapprovisionnement")
public class ReapprovisionnementController {

    @Autowired
    private ReapprovisionnementService reapproService;

    @Operation(summary = "Enregistre un nouveau réapprovisionnement")
    @PostMapping
    @PreAuthorize("hasAuthority('REAPPRO_MANAGE') or hasAnyRole('ADMIN')") // Basé sur la permission REAPPRO_MANAGE
    public ResponseEntity<ReapprovisionnementResponseDto> enregistrer(@RequestBody ReapprovisionnementRequestDto request) {
        ReapprovisionnementResponseDto response = reapproService.enregistrerReapprovisionnement(request);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Récupère tous les réapprovisionnements")
    @GetMapping
    @PreAuthorize("hasAuthority('REAPPRO_MANAGE') or hasAnyRole('ADMIN')") // Basé sur la permission REAPPRO_MANAGE pour lire
    public List<ReapprovisionnementResponseDto> getAll() {
        return reapproService.getAllReapprovisionnements();
    }

    @Operation(summary = "Récupère un réapprovisionnement par son ID")
    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('REAPPRO_MANAGE')or hasAnyRole('ADMIN')") // Basé sur la permission REAPPRO_MANAGE pour lire
    public ResponseEntity<ReapprovisionnementDetailsDto> getById(@PathVariable Long id) {
        try {
            ReapprovisionnementDetailsDto response = reapproService.getDetails(id);
            return ResponseEntity.ok(response);
        } catch (EntityNotFoundException e) {
             return ResponseEntity.notFound().build();
        }
    }

    @Operation(summary = "Supprime un réapprovisionnement par son ID")
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('REAPPRO_MANAGE') or hasAnyRole('ADMIN')") // Basé sur la permission REAPPRO_MANAGE pour supprimer
    public ResponseEntity<Void> deleteReapprovisionnement(@PathVariable Long id) {
        try {
            reapproService.deleteReapprovisionnement(id);
            return ResponseEntity.noContent().build();
        } catch (EntityNotFoundException e) {
             return ResponseEntity.notFound().build();
        } catch (IllegalStateException e) {
             return ResponseEntity.status(HttpStatus.CONFLICT).body(null);
        } catch (Exception e) {
             return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }
}