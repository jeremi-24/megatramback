package com.Megatram.Megatram.Controller;

import com.Megatram.Megatram.Entity.Permission;

import com.Megatram.Megatram.Dto.*;
import com.Megatram.Megatram.Entity.Client;
import com.Megatram.Megatram.Entity.Role;
import com.Megatram.Megatram.Entity.Utilisateur;
import com.Megatram.Megatram.repository.ClientRepository;
import com.Megatram.Megatram.service.RoleService;
import com.Megatram.Megatram.service.UtilisateurService;
import com.Megatram.Megatram.config.JwtUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.Collections;

@RestController
@RequestMapping("/api/users")
@Tag(name = "Utilisateurs", description = "Gestion et authentification des utilisateurs")
@SecurityRequirement(name = "bearerAuth") // Indique que la plupart des endpoints nécessitent une authentification
public class UtilisateurController {

    private final UtilisateurService utilisateurService;
    private final RoleService roleService;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final ClientRepository clientRepository;

    @Autowired
    public UtilisateurController(UtilisateurService utilisateurService, RoleService roleService,
                                 PasswordEncoder passwordEncoder, JwtUtil jwtUtil , ClientRepository clientRepository) {
        this.utilisateurService = utilisateurService;
        this.roleService = roleService;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
        this.clientRepository = clientRepository; // <-- 3. ASSIGNER

    }


    // ===============================================
    // NOUVEL ENDPOINT POUR LA CRÉATION MANUELLE/ADMIN
    // ===============================================

    @Operation(summary = "Créer un nouvel utilisateur (Admin)",
            description = "Permet à un administrateur de créer un nouvel utilisateur avec un rôle et un lieu spécifiques. Nécessite la permission 'USER_MANAGE' ou le rôle 'ADMIN'.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Utilisateur créé avec succès",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = UtilisateurResponseDTO.class))),
            @ApiResponse(responseCode = "400", description = "Requête invalide (ex: email déjà utilisé, rôle non trouvé)", content = @Content),
            @ApiResponse(responseCode = "403", description = "Accès refusé. L'utilisateur n'a pas les droits nécessaires.", content = @Content)
    })
    @PostMapping
    @PreAuthorize(" hasRole('ADMIN')")
    public ResponseEntity<UtilisateurResponseDTO> creerUtilisateur(@RequestBody UtilisateurRequestDTO requestDTO) {
        UtilisateurResponseDTO nouvelUtilisateur = utilisateurService.createUser(requestDTO);
        return new ResponseEntity<>(nouvelUtilisateur, HttpStatus.CREATED);
    }


    // --- Les autres endpoints ---

//    @Operation(summary = "Récupère la liste de tous les utilisateurs", description = "Nécessite la permission 'USER_MANAGE' ou le rôle 'ADMIN'.")
//    @GetMapping
//    @PreAuthorize("hasAuthority('USER_MANAGE') or hasRole('ADMIN')")
//    public ResponseEntity<List<UtilisateurResponseDTO>> getAllUtilisateurs() {
//        List<UtilisateurResponseDTO> utilisateurs = utilisateurService.getAllUsers();
//        return ResponseEntity.ok(utilisateurs);
//    }

    @Operation(
            summary = "Récupère la liste de tous les utilisateurs",
            description = "Nécessite la permission 'USER_MANAGE' ou le rôle 'ADMIN'.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Liste récupérée avec succès."),
                    @ApiResponse(responseCode = "403", description = "Accès refusé. vous n'avez pas les droits nécessaires.", content = @Content)
            }
    )
    @GetMapping
    @PreAuthorize("hasAuthority('USER_MANAGE') or hasRole('ADMIN')")
    public ResponseEntity<List<UtilisateurResponseDTO>> getAllUtilisateurs() {
        List<UtilisateurResponseDTO> utilisateurs = utilisateurService.getAllUsers();
        return ResponseEntity.ok(utilisateurs);
    }

    // Le reste de votre controller est déjà bien structuré...
    // ... (login, register, me, getRoleByEmail, getAllRoles, etc.)

    @Operation(summary = "Connexion d'un utilisateur")
    @PostMapping("/login")
    public ResponseEntity<Map<String, Object>> login(@RequestBody Map<String, String> credentials) {
        Map<String, Object> response = new HashMap<>();
        String email = credentials.get("email");
        String rawPassword = credentials.get("password");

        Utilisateur utilisateur = utilisateurService.login(email, rawPassword);
        if (utilisateur != null) {

            List<String> permissions = utilisateur.getRole().getPermissions() != null
            ? utilisateur.getRole().getPermissions().stream()
                .filter(Permission::getAutorise)
                .map(Permission::getAction)
                .collect(Collectors.toList())
            : List.of(); // Si la liste des permissions est null
    
    String token = jwtUtil.generateToken(email, utilisateur.getRole().getNom(), permissions);
    
            response.put("success", true);
            response.put("token", token);
            response.put("message", "Login réussi");
            response.put("role", utilisateur.getRole().getNom());

            // ==========================================================
            // ===               MODIFICATION DEMANDÉE                ===
            // ==========================================================

            // 4. Utiliser l'email de l'utilisateur pour rechercher le client correspondant
            Optional<Client> clientOptional = clientRepository.findByNom(utilisateur.getEmail());

            // 5. Extraire l'ID du client s'il existe, sinon mettre null
            Long clientId = clientOptional.map(Client::getId).orElse(null);

            // 6. Ajouter l'ID du client à la réponse
            response.put("clientId", clientId);

            // ==========================================================

            return ResponseEntity.ok(response);
        } else {
            response.put("success", false);
            response.put("message", "Email ou mot de passe incorrect");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
        }
    }


    @Operation(summary = "Inscription publique d'un compte")
    @PostMapping("/register")
    public ResponseEntity<Map<String, String>> register(@RequestBody Utilisateur utilisateur) {
        // ... (code inchangé)
        utilisateurService.saveUtilisateur(utilisateur);
        Map<String, String> response = new HashMap<>();
        response.put("status", "success");
        response.put("message", "Utilisateur enregistré avec succès");
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }



    @Operation(summary = "Obtenir l'utilisateur actuellement connecté")
    @GetMapping("/me")
    public ResponseEntity<UtilisateurResponseDTO> getCurrentUser() {
        Utilisateur utilisateur = utilisateurService.getUtilisateurConnecte();

        if (utilisateur != null) {
            UtilisateurResponseDTO dto = new UtilisateurResponseDTO();
            dto.setId(utilisateur.getId());
            dto.setEmail(utilisateur.getEmail());

            if (utilisateur.getRole() != null) {
                dto.setRoleId(utilisateur.getRole().getId());
                dto.setRoleNom(utilisateur.getRole().getNom());
            }

            if (utilisateur.getLieu() != null) {
                dto.setLieuNom(utilisateur.getLieu().getNom());
            }

            if (utilisateur.getRole() != null && utilisateur.getRole().getPermissions() != null) {
                List<PermissionResponseDTO> permissionDtos = utilisateur.getRole().getPermissions().stream()
                        .map(permission -> {
                            PermissionResponseDTO pDto = new PermissionResponseDTO();
                            pDto.setAction(permission.getAction());
                            pDto.setId(permission.getId());
                            pDto.setAutorise(permission.getAutorise());
                            return pDto;
                        })
                        .collect(Collectors.toList());
                dto.setPermissions(permissionDtos);
            } else {
                dto.setPermissions(java.util.Collections.emptyList());
            }

            // ===================== AJOUT DU clientId ======================
            Optional<Client> clientOptional = clientRepository.findByNom(utilisateur.getEmail());
            Long clientId = clientOptional.map(Client::getId).orElse(null);
            dto.setClientId(clientId);
            // ==============================================================

            return ResponseEntity.ok(dto);
        } else {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
    }


    @Operation(summary = "Obtenir le rôle d'un utilisateur par son email")
    @GetMapping("/role/{email}")
    public ResponseEntity<?> getRoleByEmail(@PathVariable String email) {
        // ... (code inchangé)
        String roleNom = utilisateurService.getRoleByEmail(email);
        if (roleNom != null) {
            return ResponseEntity.ok(roleNom);
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Rôle introuvable");
        }
    }

    @Operation(summary = "Récupérer tous les rôles disponibles")
    @GetMapping("/roles")
    public ResponseEntity<List<RoleDetailResponseDTO>> getAllRoles() {
        // ... (code inchangé)
        List<RoleDetailResponseDTO> roles = roleService.getAllRoles();
        return ResponseEntity.ok(roles);
    }



}