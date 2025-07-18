package com.Megatram.Megatram.Controller;

import com.Megatram.Megatram.Dto.RoleDetailResponseDTO;
import com.Megatram.Megatram.Dto.RoleRequestDTO;
import com.Megatram.Megatram.Dto.RoleResponseDTO;
import com.Megatram.Megatram.service.RoleService;
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
@RequestMapping("/api/roles")
@Tag(name = "Rôles & Permissions", description = "API pour la gestion des rôles utilisateurs")
@PreAuthorize("hasRole('ADMIN')") // Seuls les admins peuvent gérer les rôles
@CrossOrigin(origins = "http://localhost:3000")
public class RoleController {

    private final RoleService roleService;

    @Autowired
    public RoleController(RoleService roleService) {
        this.roleService = roleService;
    }

    @Operation(summary = "Crée un nouveau rôle avec ses permissions")
    @PostMapping
    public ResponseEntity<?> createRole(@RequestBody RoleRequestDTO requestDTO) {
        try {
            RoleDetailResponseDTO nouveauRole = roleService.createRole(requestDTO);
            return new ResponseEntity<>(nouveauRole, HttpStatus.CREATED);
        } catch (IllegalStateException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @Operation(summary = "Récupère la liste de tous les rôles")
    @GetMapping
    public ResponseEntity<List<RoleDetailResponseDTO>> getAllRoles() {
        return ResponseEntity.ok(roleService.getAllRoles());
    }

    @Operation(summary = "Récupère un rôle par ID avec ses permissions détaillées")
    @GetMapping("/{id}")
    public ResponseEntity<?> getRoleById(@PathVariable long id) {
        try {
            return ResponseEntity.ok(roleService.getRoleById(id));
        } catch (EntityNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }
    }

    @Operation(summary = "Met à jour un rôle et ses permissions")
    @PutMapping("/{id}")
    public ResponseEntity<?> updateRole(@PathVariable long id, @RequestBody RoleRequestDTO requestDTO) {
        try {
            return ResponseEntity.ok(roleService.updateRole(id, requestDTO));
        } catch (EntityNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }
    }

    @Operation(summary = "Supprime un rôle (si non utilisé)")
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteRole(@PathVariable long id) {
        try {
            roleService.deleteRole(id);
            return ResponseEntity.noContent().build();
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }
}