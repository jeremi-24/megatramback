package com.Megatram.Megatram.service;

import com.Megatram.Megatram.Dto.LieuStockDTO;
import com.Megatram.Megatram.Dto.LieuStockRequestDTO; // <-- Import du bon DTO
import com.Megatram.Megatram.Entity.LieuStock;
import com.Megatram.Megatram.repository.LieuStockRepository;
import com.Megatram.Megatram.repository.ProduitRepos;
import com.Megatram.Megatram.repository.UtilisateurRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class LieuStockService {

    private final LieuStockRepository lieuStockRepository;
    private final ProduitRepos produitRepos;
    private final UtilisateurRepository utilisateurRepository;

    @Autowired
    public LieuStockService(LieuStockRepository lieuStockRepository, ProduitRepos produitRepos, UtilisateurRepository utilisateurRepository) {
        this.lieuStockRepository = lieuStockRepository;
        this.produitRepos = produitRepos;
        this.utilisateurRepository = utilisateurRepository;
    }

    public LieuStockDTO createLieuStock(LieuStockRequestDTO requestDTO) { // <-- CORRECTION ICI
        if (lieuStockRepository.existsByNomIgnoreCase(requestDTO.getNom().trim())) {
            throw new IllegalStateException("Un lieu de stock avec le nom '" + requestDTO.getNom() + "' existe déjà.");
        }
        LieuStock nouveauLieu = new LieuStock();
        nouveauLieu.setNom(requestDTO.getNom());
        nouveauLieu.setType(requestDTO.getType());
        nouveauLieu.setLocalisation(requestDTO.getLocalisation());
        LieuStock savedLieu = lieuStockRepository.save(nouveauLieu);
        return new LieuStockDTO(savedLieu);
    }

    public LieuStockDTO updateLieuStock(Long id, LieuStockRequestDTO requestDTO) { // <-- CORRECTION ICI
        LieuStock lieu = lieuStockRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Lieu de stock non trouvé avec l'ID : " + id));
        lieu.setNom(requestDTO.getNom());
        lieu.setType(requestDTO.getType());
        lieu.setLocalisation(requestDTO.getLocalisation());
        LieuStock updatedLieu = lieuStockRepository.save(lieu);
        return new LieuStockDTO(updatedLieu);
    }

    @Transactional(readOnly = true)
    public List<LieuStockDTO> getAllLieuxStock() {
        return lieuStockRepository.findAll().stream()
                .map(LieuStockDTO::new)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public LieuStockDTO getLieuStockById(Long id) {
        LieuStock lieu = lieuStockRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Lieu de stock non trouvé avec l'ID : " + id));
        return new LieuStockDTO(lieu);
    }

    public void deleteLieuxStock(List<Long> ids) {
        for (Long id : ids) {
            if (!lieuStockRepository.existsById(id)) {
                throw new EntityNotFoundException("Lieu de stock non trouvé avec l'ID : " + id + ". Opération annulée.");
            }
            if (utilisateurRepository.existsByLieuId(id)) {
                throw new IllegalStateException("Impossible de supprimer le lieu (ID: " + id + ") car des utilisateurs y sont encore assignés.");
            }
        }
        lieuStockRepository.deleteAllById(ids);
    }

    @Transactional(readOnly = true)
    public Long getIdByNom(String nom) {
        LieuStock lieu = lieuStockRepository.findByNomIgnoreCase(nom.trim())
                .orElseThrow(() -> new EntityNotFoundException("Lieu de stock non trouvé avec le nom : " + nom));
        return lieu.getId();
    }

}