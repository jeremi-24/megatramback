package com.Megatram.Megatram.service;

import com.Megatram.Megatram.Dto.ClientDto;
import com.Megatram.Megatram.Entity.Client;
import com.Megatram.Megatram.Entity.Utilisateur;
import com.Megatram.Megatram.repository.ClientRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.security.access.AccessDeniedException;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class ClientService {

    private final ClientRepository clientRepository;
    private final UtilisateurService utilisateurService;

    @Autowired
    public ClientService(ClientRepository clientRepository, UtilisateurService utilisateurService) {
        this.clientRepository = clientRepository;
        this.utilisateurService = utilisateurService;
    }

    /**
     * Récupère la liste des clients en fonction de l'utilisateur connecté.
     */
    @Transactional(readOnly = true)
    public List<ClientDto> findAllForCurrentUser() {
        // --- CORRECTION APPLIQUÉE ICI ---

        // Étape 1: Récupérer l'utilisateur connecté. Il peut être null.
        Utilisateur utilisateur = utilisateurService.getUtilisateurConnecte();

        // Si personne n'est connecté, on ne renvoie que les clients externes
        if (utilisateur == null) {
            return clientRepository.findClientsExternes().stream()
                    .map(this::mapToDto)
                    .collect(Collectors.toList());
        }

        // Si un utilisateur est connecté :
        List<Client> clientsFiltres = new ArrayList<>();

        // Étape 2: Chercher le client interne correspondant à l'utilisateur connecté
        clientRepository.findByNom(utilisateur.getEmail()).ifPresent(clientsFiltres::add);

        // Étape 3: Ajouter tous les clients externes
        List<Client> clientsExternes = clientRepository.findClientsExternes();
        clientsFiltres.addAll(clientsExternes);

        // Étape 4: Convertir la liste finale en DTOs
        return clientsFiltres.stream()
                .distinct()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    // --- Le reste de votre service CRUD reste inchangé ---

    public ClientDto save(ClientDto dto) {
        Client saved = clientRepository.save(mapToEntity(dto));
        return mapToDto(saved);
    }

    @Transactional(readOnly = true)
    public ClientDto findById(Long id) {
        return clientRepository.findById(id)
                .map(this::mapToDto)
                .orElseThrow(() -> new RuntimeException("Client non trouvé"));
    }

    public void delete(Long id) {
        clientRepository.deleteById(id);
    }

    public ClientDto update(Long id, ClientDto dto) {
        Client existing = clientRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Client non trouvé"));
        existing.setNom(dto.getNom());
        existing.setTel(dto.getTel());
        return mapToDto(clientRepository.save(existing));
    }

    private ClientDto mapToDto(Client client) {
        ClientDto dto = new ClientDto();
        dto.setId(client.getId());
        dto.setNom(client.getNom());
        dto.setTel(client.getTel());
        return dto;
    }

    private Client mapToEntity(ClientDto dto) {
        Client client = new Client();
        client.setId(dto.getId());
        client.setNom(dto.getNom());
        client.setTel(dto.getTel());
        return client;
    }




    @Transactional(readOnly = true)
    public List<ClientDto> findAllClientsForAdminOrDG() {
        Utilisateur utilisateur = utilisateurService.getUtilisateurConnecte();
        if (utilisateur == null) {
            throw new AccessDeniedException("Accès refusé : utilisateur non connecté.");
        }

        String roleName = utilisateur.getRole().getNom();
        if (!roleName.equalsIgnoreCase("ADMIN") && !roleName.equalsIgnoreCase("DG")) {
            throw new AccessDeniedException("Accès refusé : rôle insuffisant.");
        }

        return clientRepository.findAll().stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

}