package com.Megatram.Megatram.Controller;

import com.Megatram.Megatram.Dto.BonLivraisonResponseDTO;
import com.Megatram.Megatram.Entity.LieuStock;
import com.Megatram.Megatram.Entity.Utilisateur;
import com.Megatram.Megatram.service.BonLivraisonService;
import com.Megatram.Megatram.service.UtilisateurService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/livraisons")
@Tag(name = "Bons de Livraison", description = "API pour la gestion des livraisons et sorties de stock")
@CrossOrigin(origins = "http://localhost:3000")
public class BonLivraisonController {

    private final BonLivraisonService bonLivraisonService;

    @Autowired
    public BonLivraisonController(BonLivraisonService bonLivraisonService) {
        this.bonLivraisonService = bonLivraisonService;
    }

    @Autowired
    private UtilisateurService utilisateurService;

    @Operation(summary = "Génère un Bon de Livraison pour une commande")
    @PostMapping
    @PreAuthorize("hasAuthority('LIVRAISON_GENERATE') or hasAnyRole('ADMIN','SECRETARIAT','MAGASINIER')")
    public ResponseEntity<?> genererBonLivraison(@RequestParam Long commandeId) {
        try {
            BonLivraisonResponseDTO bl = bonLivraisonService.genererBonLivraison(commandeId);
            return new ResponseEntity<>(bl, HttpStatus.CREATED);
        } catch (EntityNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    @Operation(summary = "Valide la première étape d'une livraison")
    @PutMapping("/{id}/valider1")
    @PreAuthorize("hasAuthority('LIVRAISON_VALIDATE') or hasAnyRole('ADMIN','SECRETARIAT')")
    public ResponseEntity<?> validerLivraison1(@PathVariable Long id, Principal principal) {
        try {
            BonLivraisonResponseDTO bl = bonLivraisonService.validerETALivrer(id, principal.getName());
            Map<String, Object> response = new HashMap<>();
            response.put("status", HttpStatus.OK.value());
            response.put("message", "Livraison validée en attente de validation finale par le magasinier.");
            response.put("bonLivraison", bl);
            return ResponseEntity.ok(response);
        } catch (EntityNotFoundException e) {
            Map<String, Object> error = new HashMap<>();
            error.put("status", HttpStatus.NOT_FOUND.value());
            error.put("error", "Bon de livraison non trouvé");
            error.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
        } catch (IllegalStateException e) {
            Map<String, Object> error = new HashMap<>();
            error.put("status", HttpStatus.BAD_REQUEST.value());
            error.put("error", "Erreur de validation");
            error.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("status", HttpStatus.INTERNAL_SERVER_ERROR.value());
            error.put("error", "Erreur serveur");
            error.put("message", "Une erreur inattendue est survenue.");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    @Operation(summary = "Valide la deuxième et dernière étape d'une livraison et décrémente le stock")
    @PutMapping("/{id}/valider2")
    @PreAuthorize("hasAuthority('LIVRAISON_VALIDATE') or hasAnyRole('ADMIN','MAGASINIER')")
    public ResponseEntity<?> validerLivraison(@PathVariable Long id, Principal principal) {
        try {
            BonLivraisonResponseDTO bl = bonLivraisonService.validerEtLivrer(id, principal.getName());
            return ResponseEntity.ok(bl);
        } catch (EntityNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    @Operation(summary = "Récupère les bons de livraison pour le lieu concerné (Magasinier)")
    @GetMapping("/bons")
    @PreAuthorize("hasAuthority('LIVRAISON_READ') or hasAnyRole('ADMIN','BOUTIQUIER','MAGASINIER','SECRETARIAT')")
    public ResponseEntity<List<BonLivraisonResponseDTO>> getBonsLivraisonMagasinier() {
        Utilisateur magasinier = utilisateurService.getUtilisateurConnecte();

        if (magasinier == null || magasinier.getLieu() == null) {
             return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        Long lieuId = magasinier.getLieu().getId();
        String email = magasinier.getEmail();

        List<BonLivraisonResponseDTO> bons = bonLivraisonService.getBonsLivraisonParLieu(lieuId);

        bons.forEach(bon -> bon.setEmail(email));

        return ResponseEntity.ok(bons);
    }

    @Operation(
            summary = "Récupère tous les bons de livraison",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Liste des bons de livraison récupérée avec succès", content = @Content(mediaType = "application/json")),
                    @ApiResponse(responseCode = "403", description = "Accès refusé. L'utilisateur n'a pas les droits nécessaires.", content = @Content)
            }
    )
    @GetMapping
    @PreAuthorize("hasAuthority('LIVRAISON_READ') or hasAnyRole('ADMIN','SECRETARIAT')")
    public ResponseEntity<List<BonLivraisonResponseDTO>> getAllBonsLivraison() {
        List<BonLivraisonResponseDTO> bons = bonLivraisonService.getAllBonLivraisons();
        return ResponseEntity.ok(bons);
    }

    @Operation(summary = "Récupère un bon de livraison par son ID")
    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('LIVRAISON_READ') or hasAnyRole('ADMIN')")
    public ResponseEntity<BonLivraisonResponseDTO> getBonLivraisonById(@PathVariable Long id) {
        try {
            BonLivraisonResponseDTO bonLivraison = bonLivraisonService.getBonLivraisonById(id);
            return ResponseEntity.ok(bonLivraison);
        } catch (EntityNotFoundException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @Operation(summary = "Supprime un bon de livraison par son ID")
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('LIVRAISON_DELETE') or hasAnyRole('ADMIN')") // Basé sur la permission LIVRAISON_DELETE
    public ResponseEntity<Void> deleteBonLivraison(@PathVariable Long id) {
        try {
            bonLivraisonService.deleteBonLivraison(id);
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