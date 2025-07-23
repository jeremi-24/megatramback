package com.Megatram.Megatram.service;

import com.Megatram.Megatram.Dto.*;
import com.Megatram.Megatram.Entity.Permission;
import com.Megatram.Megatram.Entity.Role;
import com.Megatram.Megatram.enums.PermissionType;
import com.Megatram.Megatram.repository.PermissionRepository;
import com.Megatram.Megatram.repository.RoleRepository;
import com.Megatram.Megatram.repository.UtilisateurRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Collections; // Import nécessaire pour la liste vide
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class RoleService {

    private final RoleRepository roleRepository;
    private final PermissionRepository permissionRepository;
    private final UtilisateurRepository utilisateurRepository;

    @Autowired
    public RoleService(RoleRepository roleRepository, PermissionRepository permissionRepository, UtilisateurRepository utilisateurRepository) {
        this.roleRepository = roleRepository;
        this.permissionRepository = permissionRepository;
        this.utilisateurRepository = utilisateurRepository;
    }

    /**
     * Crée un nouveau rôle avec ses permissions associées.
     */
    public RoleDetailResponseDTO createRole(RoleRequestDTO requestDTO) {
        if (roleRepository.findByNom(requestDTO.getNom()).isPresent()) {
            throw new IllegalStateException("Un rôle avec le nom '" + requestDTO.getNom() + "' existe déjà.");
        }
        Role nouveauRole = new Role();
        nouveauRole.setNom(requestDTO.getNom());
        Role savedRole = roleRepository.save(nouveauRole);
        List<Permission> permissions = new ArrayList<>();
        if (requestDTO.getPermissions() != null) {
            for (PermissionRequestDTO permDto : requestDTO.getPermissions()) {
                Permission perm = new Permission();
                perm.setAction(permDto.getAction());
                perm.setAutorise(permDto.getAutorise());
                perm.setRole(savedRole);
                permissions.add(perm);
            }
            permissionRepository.saveAll(permissions);
        }
        savedRole.setPermissions(permissions);
        return buildRoleDetailResponseDTO(savedRole);
    }




    /**
     * Récupère la liste de tous les rôles avec leurs permissions détaillées.
     */
    @Transactional(readOnly = true)
    public List<RoleDetailResponseDTO> getAllRoles() {
        // --- CORRECTION APPLIQUÉE ICI ---
        return roleRepository.findAll().stream()
                .map(this::buildRoleDetailResponseDTO) // On réutilise la méthode de mapping
                .collect(Collectors.toList());
    }

    /**
     * Récupère un rôle par son ID avec toutes ses permissions (vue détaillée).
     */
    @Transactional(readOnly = true)
    public RoleDetailResponseDTO getRoleById(long id) {
        Role role = roleRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Rôle non trouvé avec l'ID : " + id));
        return buildRoleDetailResponseDTO(role);
    }

    /**
     * Met à jour un rôle et ses permissions.
     */
    public RoleDetailResponseDTO updateRole(long id, RoleRequestDTO requestDTO) {
        Role role = roleRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Rôle non trouvé avec l'ID : " + id));

        role.setNom(requestDTO.getNom());
        permissionRepository.deleteAll(role.getPermissions());
        role.getPermissions().clear();

        List<Permission> nouvellesPermissions = new ArrayList<>();
        if (requestDTO.getPermissions() != null) {
            for (PermissionRequestDTO permDto : requestDTO.getPermissions()) {
                Permission perm = new Permission();
                perm.setAction(permDto.getAction());
                perm.setAutorise(permDto.getAutorise());
                perm.setRole(role);
                nouvellesPermissions.add(perm);
            }
            permissionRepository.saveAll(nouvellesPermissions);
        }
        role.setPermissions(nouvellesPermissions);
        Role updatedRole = roleRepository.save(role);
        return buildRoleDetailResponseDTO(updatedRole);
    }

    /**
     * Supprime un rôle, seulement si aucun utilisateur ne lui est assigné.
     */
    public void deleteRole(long id) {
        if (utilisateurRepository.existsByRoleId(id)) {
            throw new IllegalStateException("Impossible de supprimer le rôle ID " + id + " car il est toujours assigné à des utilisateurs.");
        }
        roleRepository.deleteById(id);
    }

    /**
     * Méthode privée pour construire le DTO de réponse détaillé.
     */
    private RoleDetailResponseDTO buildRoleDetailResponseDTO(Role role) {
        RoleDetailResponseDTO dto = new RoleDetailResponseDTO();
        dto.setId(role.getId());
        dto.setNom(role.getNom());

        // On gère le cas où un rôle fraîchement créé n'aurait pas encore sa liste de permissions initialisée
        if (role.getPermissions() != null) {
            List<PermissionResponseDTO> permDtos = role.getPermissions().stream()
                    .map(perm -> {
                        PermissionResponseDTO pDto = new PermissionResponseDTO();
                        pDto.setId(perm.getId());
                        pDto.setAction(perm.getAction());
                        pDto.setAutorise(perm.getAutorise());
                        return pDto;
                    })
                    .collect(Collectors.toList());
            dto.setPermissions(permDtos);
        } else {
            // On s'assure de toujours retourner une liste, même vide, pour éviter les erreurs côté frontend
            dto.setPermissions(Collections.emptyList());
        }

        return dto;
    }
}