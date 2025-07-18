package com.Megatram.Megatram.Controller;

import com.Megatram.Megatram.Dto.LieuStockDTO;
import com.Megatram.Megatram.Dto.LieuStockRequestDTO; // <-- Import du bon DTO
import com.Megatram.Megatram.service.LieuStockService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/lieustock")
@Tag(name = "Lieux de Stock", description = "API pour la gestion des magasins et boutiques")
@CrossOrigin(origins = "http://localhost:3000")
public class LieuStockController {

    private final LieuStockService lieuStockService;

    @Autowired
    public LieuStockController(LieuStockService lieuStockService) {
        this.lieuStockService = lieuStockService;
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> createLieuStock(@RequestBody LieuStockRequestDTO requestDTO) { // <-- CORRECTION ICI
        try {
            LieuStockDTO nouveauLieu = lieuStockService.createLieuStock(requestDTO);
            return new ResponseEntity<>(nouveauLieu, HttpStatus.CREATED);
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> updateLieuStock(@PathVariable Long id, @RequestBody LieuStockRequestDTO requestDTO) { // <-- CORRECTION ICI
        try {
            LieuStockDTO updatedLieu = lieuStockService.updateLieuStock(id, requestDTO);
            return ResponseEntity.ok(updatedLieu);
        } catch (EntityNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }
    }

    @GetMapping
    public ResponseEntity<List<LieuStockDTO>> getAllLieuxStock() {
        return ResponseEntity.ok(lieuStockService.getAllLieuxStock());
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getLieuStockById(@PathVariable Long id) {
        try {
            return ResponseEntity.ok(lieuStockService.getLieuStockById(id));
        } catch (EntityNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }
    }

    @DeleteMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> deleteLieuxStock(@RequestBody List<Long> ids) { // <-- CORRECTION ICI
        if (ids == null || ids.isEmpty()) {
            return ResponseEntity.badRequest().body("Le corps de la requête doit contenir un tableau d'IDs à supprimer.");
        }
        try {
            lieuStockService.deleteLieuxStock(ids);
            return ResponseEntity.noContent().build();
        } catch (EntityNotFoundException | IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    @GetMapping("/id")
    public ResponseEntity<Long> getIdByNom(@RequestParam String nom) {
        Long id = lieuStockService.getIdByNom(nom);
        return ResponseEntity.ok(id);
    }

}