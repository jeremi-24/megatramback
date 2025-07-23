package com.Megatram.Megatram.Controller;

import com.Megatram.Megatram.Dto.ClientDto;
import com.Megatram.Megatram.service.ClientService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/clients") // Convention: nom de la ressource au pluriel
@Tag(name = "Clients", description = "CRUD pour l'entité Client")
@CrossOrigin(origins = "http://localhost:3000") // Ne pas oublier cette ligne si votre frontend est séparé
public class ClientApi {

    private final ClientService clientService;

    @Autowired
    public ClientApi(ClientService clientService) {
        this.clientService = clientService;
    }

    @Operation(summary = "Ajouter un nouveau client externe")
    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'SECRETARIAT')") // Définir qui peut créer des clients
    public ResponseEntity<ClientDto> createClient(@RequestBody ClientDto dto) {
        // On pourrait utiliser un RequestDTO ici aussi pour plus de sécurité
        ClientDto savedClient = clientService.save(dto);
        return new ResponseEntity<>(savedClient, HttpStatus.CREATED);
    }

    @Operation(summary = "Récupère la liste des clients visibles par l'utilisateur connecté")
    @GetMapping
    public ResponseEntity<List<ClientDto>> getVisibleClients() {
        // C'est le seul endpoint GET pour lister les clients. Il appelle la bonne méthode du service.
        List<ClientDto> clients = clientService.findAllForCurrentUser();
        return ResponseEntity.ok(clients);
    }

    @Operation(summary = "Récupérer un client par son ID")
    @GetMapping("/{id}")
    public ResponseEntity<ClientDto> getClientById(@PathVariable Long id) {
        // Le service gère déjà l'erreur si non trouvé. C'est parfait.
        return ResponseEntity.ok(clientService.findById(id));
    }

    @Operation(summary = "Mettre à jour les informations d'un client")
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'SECRETARIAT')")
    public ResponseEntity<ClientDto> updateClient(@PathVariable Long id, @RequestBody ClientDto dto) {
        return ResponseEntity.ok(clientService.update(id, dto));
    }

    @Operation(summary = "Supprimer un client")
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteClient(@PathVariable Long id) {
        clientService.delete(id);
        return ResponseEntity.noContent().build();
    }


//    @PreAuthorize("hasAnyRole('ADMIN')")
    @GetMapping("/all")
    public ResponseEntity<List<ClientDto>> getAllClientsForAdminOrDG() {
        List<ClientDto> clients = clientService.findAllClientsForAdminOrDG();
        return ResponseEntity.ok(clients);
    }





}