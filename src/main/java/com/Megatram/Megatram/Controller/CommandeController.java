package com.Megatram.Megatram.Controller;

import com.Megatram.Megatram.Dto.CommandeRequestDTO;
import com.Megatram.Megatram.Dto.CommandeResponseDTO;
import com.Megatram.Megatram.service.CommandeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/commandes")
@Tag(name = "Gestion des Commandes", description = "API pour créer, lire, valider et modifier les commandes")
public class CommandeController {

    @Autowired
    private CommandeService commandeService;

    @Operation(summary = "Créer une nouvelle commande", description = "Crée une nouvelle commande avec un statut 'EN_ATTENTE'. Le lieu de livraison est déterminé automatiquement à partir des produits.")
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
        return new ResponseEntity<>(nouvelleCommande, HttpStatus.CREATED);
    }

    // ... Les autres endpoints (GET, POST valider, PUT) restent fonctionnellement les mêmes mais bénéficieront de la correction ...

    @Operation(summary = "Récupérer toutes les commandes", description = "Retourne une liste de toutes les commandes existantes.")
    @GetMapping
    public ResponseEntity<List<CommandeResponseDTO>> recupererLesCommandes() {
        List<CommandeResponseDTO> commandes = commandeService.recupererToutesLesCommandes();
        return ResponseEntity.ok(commandes);
    }

    @Operation(summary = "Récupérer une commande par son ID", description = "Retourne les détails d'une commande spécifique.")
    @GetMapping("/{id}")
    public ResponseEntity<CommandeResponseDTO> recupererCommandeParId(@PathVariable Long id) {
        CommandeResponseDTO commande = commandeService.recupererCommandeParId(id);
        return ResponseEntity.ok(commande);
    }

    @Operation(summary = "Valider une commande", description = "Valide une commande 'EN ATTENTE', générant facture et bon de livraison.")
    @PostMapping("/{id}/valider")
    public ResponseEntity<CommandeService.ValidationResponse> validerCommande(@PathVariable Long id) {
        CommandeService.ValidationResponse response = commandeService.validerCommande(id);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Modifier une commande existante", description = "Met à jour une commande 'EN ATTENTE'.")
    @PutMapping("/{id}")
    public ResponseEntity<CommandeResponseDTO> modifierCommande(@PathVariable Long id, @RequestBody CommandeRequestDTO commandeRequestDTO) {
        CommandeResponseDTO commandeModifiee = commandeService.modifierCommande(id, commandeRequestDTO);
        return ResponseEntity.ok(commandeModifiee);
    }

    @Operation(summary = "Récupérer les commandes par ID de client",
            description = "Retourne une liste de toutes les commandes passées par un client spécifique.")
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
}