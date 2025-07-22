package com.Megatram.Megatram.Controller;

import com.Megatram.Megatram.Dto.CategorieDto;
import com.Megatram.Megatram.Dto.CommandeResponseDTO;
import io.swagger.v3.oas.annotations.Operation;

import java.util.HashMap;
import java.util.List;


import com.Megatram.Megatram.Entity.Categorie;
import com.Megatram.Megatram.service.CategorieService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/categorie")
@Tag(name = "Categorie", description = "Gestion des Categories")
@CrossOrigin(origins = "http://localhost:3000")

public class CategorieController {

    @Autowired
    private CategorieService categorieService;

    @Operation(summary = "total")
    @GetMapping("/total")
    public ResponseEntity<Long> getNombreTotalCategories() {
        long total = categorieService.getNombreTotalCategories();
        return ResponseEntity.ok(total);
    }

    @Operation(summary = "all ")
    @GetMapping
    public List<CategorieDto> getAllCategories() {
        return categorieService.getAllCategories();
    }


    @Operation(summary = "Récupérer une catégorie par son ID")
    @GetMapping("/{id}")
    public ResponseEntity<Categorie> getCategorieById(@PathVariable Long id) {
        Categorie categorie = categorieService.getById(id);
        if (categorie != null) {
            return ResponseEntity.ok(categorie);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @Operation(summary = "add ")
    @PostMapping
    public Categorie addCategorie(@RequestBody CategorieDto dto) {
        return categorieService.addCategorie(dto);
    }






    @Operation(summary = "put ")
    @PutMapping("/{id}")
    public Categorie updateCategorie(@PathVariable Long id, @RequestBody CategorieDto dto) {
        return categorieService.updateCategorie(id, dto);
    }

    @Operation(summary = "delete by id ")
    @DeleteMapping("/{id}")
    public void deleteCategorie(@PathVariable Long id) {
        categorieService.deleteCategorie(id);
    }


    @Operation(summary = "Supprimer plusieurs catégories ou toutes")
    @DeleteMapping
    public ResponseEntity<Void> deleteByIdsOrAll(
            @RequestBody List<Long> ids
    ) {
        if (ids == null) {
            return ResponseEntity.badRequest().build();
        }
        if (ids.isEmpty()) {
            categorieService.deleteAllCategories();
        } else {
            categorieService.deleteCategoriesByIds(ids);
        }
        return ResponseEntity.noContent().build();
    }







//    @Operation(summary = "Importation des catégories")
//    @PostMapping(path = "/import", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
//    public ResponseEntity<?> importerCategories(@RequestParam("file") MultipartFile file) {
//        if (file.isEmpty() || !file.getOriginalFilename().toLowerCase().endsWith(".xlsx")) {
//            return ResponseEntity.badRequest().body("Veuillez fournir un fichier Excel (.xlsx) valide.");
//        }
//
//        List<Categorie> categories = categorieService.importerCategoriesDepuisExcel(file);
//        return ResponseEntity.ok("Importation réussie de " + categories.size() + " catégories.");
//    }
//
    @Operation(summary = "retrouver une catégorie par nom ")
    @GetMapping("/id")
    public ResponseEntity<Long> getCategorieIdByNom(@RequestParam String nom) {
        Long id = categorieService.getCategorieIdByNom(nom);
        return ResponseEntity.ok(id);
    }




}
