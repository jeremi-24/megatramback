package com.Megatram.Megatram.Controller;
import org.springframework.web.bind.annotation.PathVariable;

import com.Megatram.Megatram.Dto.StockDto;
import com.Megatram.Megatram.service.StockService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/stocks")
@Tag(name = "Stocks", description = "Gestion des stocks")
@CrossOrigin(origins = "http://localhost:3000")
public class StockController {

    @Autowired
    private StockService stockService;

    @Operation(summary = "Récupère tous les stocks")
    @GetMapping
    @PreAuthorize("hasAuthority('INVENTAIRE_MANAGE') or hasAnyRole('ADMIN','BOUTIQUIER')") // Utilisation de la permission existante pour la gestion des stocks
    public ResponseEntity<List<StockDto>> getAllStocks() {
        List<StockDto> stocks = stockService.getAllStocks();
        return ResponseEntity.ok(stocks);
    }

    @Operation(summary = "Récupère tous les stocks pour un lieu spécifique")
@GetMapping("/lieu/{lieuStockId}")
@PreAuthorize("hasAuthority('INVENTAIRE_MANAGE') or hasAnyRole('ADMIN','BOUTIQUIER')")
public ResponseEntity<List<StockDto>> getStocksByLieuStockId(@PathVariable Long lieuStockId) {
    List<StockDto> stocks = stockService.getStocksByLieuStockId(lieuStockId);
    return ResponseEntity.ok(stocks);
}

 @Operation(summary = "Récupère tous les stocks pour un lieu spécifique par nom")
    @GetMapping("/lieuStock/{nom}")
    @PreAuthorize("hasAuthority('INVENTAIRE_MANAGE') or hasAnyRole('ADMIN','BOUTIQUIER')")
    public ResponseEntity<List<StockDto>> getStocksByLieuStockNom(@PathVariable String nom) {
        List<StockDto> stocks = stockService.getStocksByLieuStockNom(nom);
        if (stocks.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(stocks);
    }
}