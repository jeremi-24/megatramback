package com.Megatram.Megatram.config;

import com.Megatram.Megatram.Entity.Client;
import com.Megatram.Megatram.Entity.Permission;
import com.Megatram.Megatram.Entity.Role;
import com.Megatram.Megatram.Entity.Utilisateur;
import com.Megatram.Megatram.enums.PermissionType;
import com.Megatram.Megatram.repository.ClientRepository;
import com.Megatram.Megatram.repository.PermissionRepository;
import com.Megatram.Megatram.repository.RoleRepository;
import com.Megatram.Megatram.repository.UtilisateurRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

@Component
public class DataInitializer implements CommandLineRunner {

    private final RoleRepository roleRepository;
    private final PermissionRepository permissionRepository;
    private final UtilisateurRepository utilisateurRepository;
    private final PasswordEncoder passwordEncoder;
    private final ClientRepository clientRepository;

    @Autowired
    public DataInitializer(RoleRepository roleRepository,
                           PermissionRepository permissionRepository,
                           UtilisateurRepository utilisateurRepository,
                           PasswordEncoder passwordEncoder,
                           ClientRepository clientRepository) {
        this.roleRepository = roleRepository;
        this.permissionRepository = permissionRepository;
        this.utilisateurRepository = utilisateurRepository;
        this.passwordEncoder = passwordEncoder;
        this.clientRepository = clientRepository;
    }

    @Override
    @Transactional
    public void run(String... args) throws Exception {
        // ON A SUPPRIMÉ LE 'if (roleRepository.count() > 0) return;'

        // --- 1. Création des Rôles (si nécessaire) ---
        Role adminRole = createRoleIfNotFound("ADMIN", Arrays.asList(PermissionType.values()));
        Role secretaireRole = createRoleIfNotFound("SECRETARIAT", List.of(/* ... */));
        Role magasinierRole = createRoleIfNotFound("MAGASINIER", List.of(/* ... */));
        Role dgRole = createRoleIfNotFound("DG", List.of(/* ... */));
        Role controleurRole = createRoleIfNotFound("CONTROLEUR", List.of(/* ... */));

        // --- 2. Création des Utilisateurs et Clients (si nécessaire) ---
        String defaultPassword = "123";
        createUserAndClient("admin@megatram.com", defaultPassword, adminRole);
//        createUserAndClient("fille@megatram.com", defaultPassword, secretaireRole);
//        createUserAndClient("mag1@megatram.com", defaultPassword, magasinierRole);
//        createUserAndClient("mag2@megatram.com", defaultPassword, magasinierRole);
//        createUserAndClient("dg@megatram.com", defaultPassword, dgRole);
//        createUserAndClient("rahim@megatram.com", defaultPassword, controleurRole);
    }

    /**
     * Méthode MISE À JOUR : Crée un rôle et ses permissions SEULEMENT s'il n'existe pas.
     */
    private Role createRoleIfNotFound(String roleName, List<PermissionType> permissions) {
        // On cherche d'abord si le rôle existe par son nom
        Optional<Role> roleOpt = roleRepository.findByNom(roleName);

        // S'il est présent, on le retourne directement. On ne fait rien de plus.
        if (roleOpt.isPresent()) {
            return roleOpt.get();
        }

        // Si le rôle n'a pas été trouvé, on le crée.
        Role role = new Role();
        role.setNom(roleName);
        role = roleRepository.save(role);

        for (PermissionType permType : permissions) {
            Permission permission = new Permission();
            permission.setAction(permType.name());
            permission.setAutorise(true);
            permission.setRole(role);
            permissionRepository.save(permission);
        }
        return role;
    }

    /**
     * Méthode privée pour créer un utilisateur ET un client associé s'ils n'existent pas.
     */
    private void createUserAndClient(String email, String password, Role role) {
        // On vérifie l'utilisateur par son email
        if (!utilisateurRepository.existsByEmail(email)) {
            Utilisateur user = new Utilisateur();
            user.setEmail(email);
            user.setPassword(passwordEncoder.encode(password));
            user.setRole(role);
            utilisateurRepository.save(user);
        }

        // On vérifie le client par son nom (qui est l'email)
        if (!clientRepository.existsByNom(email)) {
            Client clientInterne = new Client();
            clientInterne.setNom(email);
            clientInterne.setTel("00000000");
            clientRepository.save(clientInterne);
        }
    }
}