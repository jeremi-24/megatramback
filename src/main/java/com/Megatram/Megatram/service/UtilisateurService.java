package com.Megatram.Megatram.service;

import com.Megatram.Megatram.Dto.PermissionResponseDTO;
import com.Megatram.Megatram.Dto.UtilisateurRequestDTO;
import com.Megatram.Megatram.Dto.UtilisateurResponseDTO;
import com.Megatram.Megatram.Entity.*;
import com.Megatram.Megatram.repository.ClientRepository;
import com.Megatram.Megatram.repository.LieuStockRepository;

import com.Megatram.Megatram.repository.RoleRepository;
import com.Megatram.Megatram.repository.UtilisateurRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class UtilisateurService {

    private final UtilisateurRepository utilisateurRepository;
    private final RoleRepository roleRepository;
    private final LieuStockRepository lieuStockRepository;
    private final PasswordEncoder passwordEncoder;
    private final ClientRepository clientRepository;


    @Autowired
    public UtilisateurService(
            UtilisateurRepository utilisateurRepository,
            RoleRepository roleRepository,
            LieuStockRepository lieuStockRepository,
            PasswordEncoder passwordEncoder, ClientRepository clientRepository
    ) {
        this.utilisateurRepository = utilisateurRepository;
        this.roleRepository = roleRepository;
        this.lieuStockRepository = lieuStockRepository;
        this.passwordEncoder = passwordEncoder;

        this.clientRepository = clientRepository;
    }

    // ---------- AUTHENTIFICATION ----------

    public Utilisateur login(String email, String rawPassword) {
        return utilisateurRepository.findByEmail(email)
                .filter(utilisateur -> passwordEncoder.matches(rawPassword, utilisateur.getPassword()))
                .orElse(null);
    }

    public Utilisateur saveUtilisateur(Utilisateur utilisateur) {
        String rawPassword = utilisateur.getPassword();
        String encodedPassword = passwordEncoder.encode(rawPassword);
        utilisateur.setPassword(encodedPassword);
        return utilisateurRepository.save(utilisateur);
    }

    public Utilisateur getUtilisateurConnecte() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated() ||
                authentication.getPrincipal().equals("anonymousUser")) {
            return null;
        }

        String email = authentication.getName();
        return utilisateurRepository.findByEmail(email).orElse(null);
    }

    public String getRoleByEmail(String email) {
        return utilisateurRepository.findByEmail(email)
                .map(Utilisateur::getRole)
                .map(Role::getNom)
                .orElse(null);
    }

    // ---------- UTILISATEUR CRUD ----------

    public UtilisateurResponseDTO createUser(UtilisateurRequestDTO requestDTO) {
        // Vérifier si l'utilisateur existe déjà
        if (utilisateurRepository.existsByEmail(requestDTO.getEmail())) {
            throw new IllegalStateException("Un utilisateur avec l'email '" + requestDTO.getEmail() + "' existe déjà.");
        }

        // Vérifier si un client avec ce nom (email) existe déjà
        if (clientRepository.existsByNom(requestDTO.getEmail())) {
            throw new IllegalStateException("Un client avec le nom '" + requestDTO.getEmail() + "' existe déjà.");
        }

        // Charger le rôle
        Role role = roleRepository.findById(requestDTO.getRoleId())
                .orElseThrow(() -> new EntityNotFoundException("Rôle non trouvé avec l'ID : " + requestDTO.getRoleId()));

        // Charger le lieu si fourni
        LieuStock lieu = null;
        if (requestDTO.getLieuId() != null) {
            lieu = lieuStockRepository.findById(requestDTO.getLieuId())
                    .orElseThrow(() -> new EntityNotFoundException("Lieu non trouvé avec l'ID : " + requestDTO.getLieuId()));
        }

        // Créer l'utilisateur
        Utilisateur nouvelUtilisateur = new Utilisateur();
        nouvelUtilisateur.setEmail(requestDTO.getEmail());
        nouvelUtilisateur.setPassword(passwordEncoder.encode(requestDTO.getPassword()));
        nouvelUtilisateur.setRole(role);
        nouvelUtilisateur.setLieu(lieu);

        // Créer le client lié (nom = email)
        Client nouveauClient = new Client();
        nouveauClient.setNom(requestDTO.getEmail());
        nouveauClient.setTel(requestDTO.getClientTel() != null ? requestDTO.getClientTel() : "00000000");

        nouvelUtilisateur.setClient(nouveauClient); // CascadeType.PERSIST fera le travail

        // Sauvegarder l'utilisateur et son client
        Utilisateur savedUser = utilisateurRepository.save(nouvelUtilisateur);
        return buildUtilisateurResponseDTO(savedUser);
    }


//    public UtilisateurResponseDTO createUser(UtilisateurRequestDTO requestDTO) {
//        if (utilisateurRepository.existsByEmail(requestDTO.getEmail())) {
//            throw new IllegalStateException("Un utilisateur avec l'email '" + requestDTO.getEmail() + "' existe déjà.");
//        }
//
//        Role role = roleRepository.findById(requestDTO.getRoleId())
//                .orElseThrow(() -> new EntityNotFoundException("Rôle non trouvé avec l'ID : " + requestDTO.getRoleId()));
//
//        LieuStock lieu = null;
//        if (requestDTO.getLieuId() != null) {
//            lieu = lieuStockRepository.findById(requestDTO.getLieuId())
//                    .orElseThrow(() -> new EntityNotFoundException("Lieu non trouvé avec l'ID : " + requestDTO.getLieuId()));
//        }
//
//        Utilisateur nouvelUtilisateur = new Utilisateur();
//        nouvelUtilisateur.setEmail(requestDTO.getEmail());
//        nouvelUtilisateur.setPassword(passwordEncoder.encode(requestDTO.getPassword()));
//        nouvelUtilisateur.setRole(role);
//        nouvelUtilisateur.setLieu(lieu);
//
//        // ----- LOGIQUE D'INTÉGRATION DU CLIENT -----
//        // Si un nom de client est fourni, on crée un nouveau client.
//        if (StringUtils.hasText(requestDTO.getClientNom())) {
//            // Optionnel : Vérifier si un client avec ce téléphone existe déjà
//            // clientRepository.findByTel(requestDTO.getClientTel()).ifPresent(c -> {
//            //     throw new IllegalStateException("Un client avec ce téléphone existe déjà.");
//            // });
//
//            Client nouveauClient = new Client();
//            nouveauClient.setNom(requestDTO.getClientNom());
//            nouveauClient.setTel(requestDTO.getClientTel());
//
//            // On associe le nouveau client à l'utilisateur.
//            // Grâce à CascadeType.PERSIST, le client sera sauvegardé en même temps que l'utilisateur.
//            nouvelUtilisateur.setClient(nouveauClient);
//        }
//
//        Utilisateur savedUser = utilisateurRepository.save(nouvelUtilisateur);
//        return buildUtilisateurResponseDTO(savedUser);
//    }

    public UtilisateurResponseDTO updateUser(long id, UtilisateurRequestDTO requestDTO) {
        Utilisateur utilisateur = utilisateurRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Utilisateur non trouvé avec l'ID : " + id));

        Role role = roleRepository.findById(requestDTO.getRoleId())
                .orElseThrow(() -> new EntityNotFoundException("Rôle non trouvé avec l'ID : " + requestDTO.getRoleId()));

        LieuStock lieu = null;
        if (requestDTO.getLieuId() != null) {
            lieu = lieuStockRepository.findById(requestDTO.getLieuId())
                    .orElseThrow(() -> new EntityNotFoundException("Lieu non trouvé avec l'ID : " + requestDTO.getLieuId()));
        }

        utilisateur.setEmail(requestDTO.getEmail());
        utilisateur.setRole(role);
        utilisateur.setLieu(lieu);

        if (requestDTO.getPassword() != null && !requestDTO.getPassword().trim().isEmpty()) {
            utilisateur.setPassword(passwordEncoder.encode(requestDTO.getPassword()));
        }

        Utilisateur updatedUser = utilisateurRepository.save(utilisateur);
        return buildUtilisateurResponseDTO(updatedUser);
    }

    @Transactional(readOnly = true)
    public UtilisateurResponseDTO getUserById(long id) {
        Utilisateur utilisateur = utilisateurRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Utilisateur non trouvé avec l'ID : " + id));
        return buildUtilisateurResponseDTO(utilisateur);
    }

    @Transactional(readOnly = true)
    public List<UtilisateurResponseDTO> getAllUsers() {
        return utilisateurRepository.findAll().stream()
                .map(this::buildUtilisateurResponseDTO)
                .collect(Collectors.toList());
    }

    public void deleteUser(long id) {
        if (!utilisateurRepository.existsById(id)) {
            throw new EntityNotFoundException("Utilisateur non trouvé avec l'ID : " + id);
        }
        utilisateurRepository.deleteById(id);
    }

    private UtilisateurResponseDTO buildUtilisateurResponseDTO(Utilisateur utilisateur) {
        UtilisateurResponseDTO dto = new UtilisateurResponseDTO();
        dto.setId(utilisateur.getId());
        dto.setEmail(utilisateur.getEmail());

        if (utilisateur.getLieu() != null) {
            dto.setLieuNom(utilisateur.getLieu().getNom());
        }

        if (utilisateur.getRole() != null) {
            Role role = utilisateur.getRole();
            dto.setRoleId(role.getId());
            dto.setRoleNom(role.getNom());

            if (role.getPermissions() != null) {
                // --- CORRECTION DE LA LOGIQUE DE MAPPING ICI ---
                List<PermissionResponseDTO> permissions = role.getPermissions().stream()
                        .map(permission -> {
                            // On crée un DTO vide et on remplit chaque champ manuellement
                            PermissionResponseDTO pDto = new PermissionResponseDTO();
                            pDto.setId(permission.getId());
                            pDto.setAction(permission.getAction());
                            pDto.setAutorise(permission.getAutorise());
                            return pDto;
                        })
                        .collect(Collectors.toList());
                dto.setPermissions(permissions);
            } else {
                dto.setPermissions(Collections.emptyList());
            }

        } else {
            dto.setPermissions(Collections.emptyList());
        }

        return dto;
    }
}