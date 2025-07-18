package com.Megatram.Megatram.Controller;

import com.Megatram.Megatram.Dto.InventaireRequestDto;
import com.Megatram.Megatram.Dto.InventaireResponseDto;
import com.Megatram.Megatram.service.InventaireService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
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
    public ResponseEntity<InventaireResponseDto> creerInventaire(@RequestBody InventaireRequestDto request,
                                                                 @RequestParam(defaultValue = "false") boolean premier) {
        InventaireResponseDto response = inventaireService.enregistrerInventaire(request, premier);
        return ResponseEntity.ok(response);
    }

    @GetMapping
    public ResponseEntity<List<InventaireResponseDto>> getAllInventaires() {
        List<InventaireResponseDto> inventaires = inventaireService.recupererTousLesInventaires();
        return ResponseEntity.ok(inventaires);
    }

    @GetMapping("/{id}")
    public ResponseEntity<InventaireResponseDto> getInventaire(@PathVariable Long id) {
        InventaireResponseDto response = inventaireService.getInventaireById(id);
        return ResponseEntity.ok(response);
    }
}