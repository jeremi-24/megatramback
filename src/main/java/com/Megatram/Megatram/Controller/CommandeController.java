package com.Megatram.Megatram.Controller;

import com.Megatram.Megatram.Dto.*;
import com.Megatram.Megatram.Entity.Client;
import com.Megatram.Megatram.Entity.Commande;
import com.Megatram.Megatram.Entity.LieuStock;
import com.Megatram.Megatram.repository.CommandeRepository;
import com.Megatram.Megatram.service.CommandeService;
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

    @Operation(summary = "Créer une nouvelle commande", description = "Crée une nouvelle commande avec un statut 'EN_ATTENTE'. Le lieu de livraison est déterminé automatiquement à partir des produits.")
    @PreAuthorize("hasAuthority('COMMANDE_CREATE')")
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

    @PreAuthorize("hasAuthority('COMMANDE_READ')")
    @Operation(summary = "Récupérer toutes les commandes", description = "Retourne une liste de toutes les commandes existantes.")
    @GetMapping
    public ResponseEntity<List<CommandeResponseDTO>> recupererLesCommandes() {
        List<CommandeResponseDTO> commandes = commandeService.recupererToutesLesCommandes();
        return ResponseEntity.ok(commandes);
    }

    @PreAuthorize("hasAuthority('COMMANDE_READ')")
    @Operation(summary = "Récupérer une commande par son ID", description = "Retourne les détails d'une commande spécifique.")
    @GetMapping("/{id}")
    public ResponseEntity<CommandeResponseDTO> recupererCommandeParId(@PathVariable Long id) {
        CommandeResponseDTO commande = commandeService.recupererCommandeParId(id);
        return ResponseEntity.ok(commande);
    }


    @PreAuthorize("hasAuthority('COMMANDE_VALIDATE')")
    @Operation(summary = "Valider une commande", description = "Valide une commande 'EN ATTENTE', génère et valide automatiquement facture et bon de livraison.")
    @PostMapping("/{id}/valider")
    public ResponseEntity<CommandeService.ValidationResponse> validerCommande(@PathVariable Long id, Principal principal) {
        CommandeService.ValidationResponse response = commandeService.validerCommande(id, principal.getName());
        return ResponseEntity.ok(response);
    }

    @PreAuthorize("hasAuthority('COMMANDE_CANCEL')")
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




    @PreAuthorize("hasAuthority('COMMANDE_UPDATE')")
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




    /*  METHODE DE RECHERCHE */
    @Operation(summary = "RECHERCHE")
    @GetMapping("/search")
    public ResponseEntity<List<CommandeResponseDTO>> searchCommandes(@RequestParam String q) {
        List<CommandeResponseDTO> dtoList = commandeService.rechercherCommandes(q);
        return ResponseEntity.ok(dtoList);
    }

    //    @GetMapping("/search")
//    public ResponseEntity<List<CommandeResponseDTO>> searchCommandes(@RequestParam String q) {
//        List<Commande> commandes = commandeRepository.searchCommandes(q);
//        List<CommandeResponseDTO> dtoList = commandes.stream()
//                .map(this::convertToResponseDTO)
//                .collect(Collectors.toList());
//        return ResponseEntity.ok(dtoList);
//    }
//
//    private CommandeResponseDTO convertToResponseDTO(Commande commande) {
//        CommandeResponseDTO dto = new CommandeResponseDTO();
//
//        dto.setId(commande.getId());
//        dto.setDate(commande.getDate());
//        dto.setStatut(commande.getStatut());
//
//        // Convertir le client
//        Client client = commande.getClient();
//        if (client != null) {
//            ClientDto clientDto = new ClientDto();
//            clientDto.setId(client.getId());
//            clientDto.setNom(client.getNom());
//            dto.setClient(clientDto);
//        }
//
//        // Convertir le lieu de livraison (lieuStock)
//        LieuStock lieuStock = commande.getLieuStock();
//        if (lieuStock != null) {
//            LieuStockDTO lieuDto = new LieuStockDTO();
//            lieuDto.setId(lieuStock.getId());
//            lieuDto.setNom(lieuStock.getNom());
//            dto.setLieuLivraison(lieuDto);
//        }
//
//        // Convertir les lignes
//        List<LigneCommandeResponseDTO> ligneDtos = commande.getLignes().stream().map(ligne -> {
//            LigneCommandeResponseDTO lDto = new LigneCommandeResponseDTO();
//            lDto.setId(ligne.getId());
//            lDto.setProduitPrix(ligne.getProduitPrix());
//            lDto.setQteVoulu(ligne.getQteVoulu());
//            return lDto;
//        }).collect(Collectors.toList());
//
//        dto.setLignes(ligneDtos);
//
//        // Calculer le total de la commande
//        double total = ligneDtos.stream()
//                .mapToDouble(l -> l.getProduitPrix() * l.getQteVoulu())
//                .sum();
//        dto.setTotalCommande(total);
//
//        return dto;
//    }




}