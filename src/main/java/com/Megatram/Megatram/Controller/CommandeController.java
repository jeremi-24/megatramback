package com.Megatram.Megatram.Controller;

import com.Megatram.Megatram.Dto.*;
import com.Megatram.Megatram.Entity.Client;
import com.Megatram.Megatram.Entity.Commande;
import com.Megatram.Megatram.Entity.LieuStock;
import com.Megatram.Megatram.repository.CommandeRepository;
import com.Megatram.Megatram.service.CommandeService;
import com.Megatram.Megatram.service.NotificationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/commandes")
@Tag(name = "Gestion des Commandes", description = "API pour créer, lire, valider et modifier les commandes")
public class CommandeController {

    @Autowired
    private CommandeService commandeService;

    @Autowired
    private CommandeRepository commandeRepository;

    @Autowired
    private NotificationService notificationService;

    @Operation(summary = "Créer une nouvelle commande", description = "Crée une nouvelle commande avec un statut 'EN_ATTENTE'. Le lieu de livraison est déterminé automatiquement à partir des produits.")
    @PreAuthorize("hasAuthority('COMMANDE_CREATE') or hasAnyRole('ADMIN','BOUTIQUIER','MAGASINIER','SECRETARIAT')")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Commande créée avec succès",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = CommandeResponseDTO.class))),
            @ApiResponse(responseCode = "400", description = "Requête invalide (ex: produits de lieux différents, client non trouvé)", content = @Content)
    })
    @PostMapping
    public ResponseEntity<CommandeResponseDTO> creerCommande(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "Données de la commande à créer (le lieu de livraison n'est pas requis).", required = true,
                    content = @Content(schema = @Schema(implementation = CommandeRequestDTO.class)))
            @RequestBody CommandeRequestDTO requestDTO) {
        CommandeResponseDTO nouvelleCommande = commandeService.creerCommande(requestDTO);

        try {
            notificationService.envoyerNotification("/topic/secretariat", "Nouvelle commande !"  );
        } catch (Exception e) {
            System.err.println("Erreur lors de l'envoi de la notification WebSocket : " + e.getMessage());
            // Log the exception properly instead of printing to stderr
        }

        return new ResponseEntity<>(nouvelleCommande, HttpStatus.CREATED);
    }

    // ... Les autres endpoints (GET, POST valider, PUT) restent fonctionnellement les mêmes mais bénéficieront de la correction ...

    @PreAuthorize("hasAuthority('COMMANDE_READ') or hasAnyRole('ADMIN','BOUTIQUIER','MAGASINIER','SECRETARIAT')")
    @Operation(summary = "Récupérer toutes les commandes", description = "Retourne une liste de toutes les commandes existantes.")
    @GetMapping
    public ResponseEntity<List<CommandeResponseDTO>> recupererLesCommandes() {
        List<CommandeResponseDTO> commandes = commandeService.recupererToutesLesCommandes();
        return ResponseEntity.ok(commandes);
    }

    @PreAuthorize("hasAuthority('COMMANDE_READ') or hasAnyRole('ADMIN','BOUTIQUIER','MAGASINIER','SECRETARIAT')")
    @Operation(summary = "Récupérer une commande par son ID", description = "Retourne les détails d'une commande spécifique.")
    @GetMapping("/{id}")
    public ResponseEntity<CommandeResponseDTO> recupererCommandeParId(@PathVariable Long id) {
        CommandeResponseDTO commande = commandeService.recupererCommandeParId(id);
        return ResponseEntity.ok(commande);
    }



    @PreAuthorize("hasAuthority('COMMANDE_VALIDATE') or hasAnyRole('ADMIN','SECRETARIAT')")
@Operation(summary = "Valider une commande", description = "Valide une commande 'EN ATTENTE', génère et valide automatiquement facture et bon de livraison.")
@PostMapping("/{id}/valider")
public ResponseEntity<CommandeService.ValidationResponse> validerCommande(@PathVariable Long id, Principal principal) {
    CommandeService.ValidationResponse response = commandeService.validerCommande(id, principal.getName());

    try {
        notificationService.envoyerNotification(
            "/topic/magasinier", 
            "Un bon de livraison a été émis pour la commande #" + id + ", veuillez le valider."
        );
    } catch (Exception e) {
        System.err.println("Erreur lors de l'envoi de la notification WebSocket : " + e.getMessage());
        // Optionnel : logger proprement avec un logger
    }

    return ResponseEntity.ok(response);
}


    @PreAuthorize("hasAuthority('COMMANDE_CANCEL') or hasAnyRole('ADMIN')")
    @Operation(summary = "annuler une commande")
    @PutMapping("/{id}/annuler")
    public ResponseEntity<?> annulerCommande(@PathVariable Long id) {
        try {
            commandeService.annulerCommande(id);
            return ResponseEntity.ok().build();
        } catch (EntityNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Erreur interne du serveur");
        }
    }




    @PreAuthorize("hasAuthority('COMMANDE_UPDATE') or hasAnyRole('ADMIN')")
    @Operation(summary = "Modifier une commande existante", description = "Met à jour une commande 'EN ATTENTE'.")
    @PutMapping("/{id}")
    public ResponseEntity<CommandeResponseDTO> modifierCommande(@PathVariable Long id, @RequestBody CommandeRequestDTO commandeRequestDTO) {
        CommandeResponseDTO commandeModifiee = commandeService.modifierCommande(id, commandeRequestDTO);
        return ResponseEntity.ok(commandeModifiee);
    }






    @Operation(summary = "Récupérer les commandes par ID de client",
            description = "Retourne une liste de toutes les commandes passées par un client spécifique.")
    @PreAuthorize("hasAuthority('COMMANDE_READ') or hasAnyRole('ADMIN')")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Commandes trouvées et retournées avec succès"),
            @ApiResponse(responseCode = "404", description = "Aucune commande trouvée pour ce client (la liste sera vide)")
    })
    @GetMapping("/client/{clientId}")
    public ResponseEntity<List<CommandeResponseDTO>> getCommandesParClientId(
            @Parameter(description = "ID du client pour filtrer les commandes", required = true)
            @PathVariable Long clientId
    ) {
        List<CommandeResponseDTO> commandes = commandeService.recupererCommandesParClientId(clientId);
        return ResponseEntity.ok(commandes);
    }




    /*  METHODE DE RECHERCHE */
    @PreAuthorize("hasAuthority('COMMANDE_READ') or hasAnyRole('ADMIN')")
    @Operation(summary = "RECHERCHE")
    @GetMapping("/search")
    public ResponseEntity<List<CommandeResponseDTO>> searchCommandes(@RequestParam String q) {
        List<CommandeResponseDTO> dtoList = commandeService.rechercherCommandes(q);
        return ResponseEntity.ok(dtoList);
    }




}
