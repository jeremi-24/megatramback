package com.Megatram.Megatram.service;

import com.Megatram.Megatram.Dto.*;
import com.Megatram.Megatram.Entity.LieuStock;
import com.Megatram.Megatram.Entity.LigneReapprovisionnement;
import com.Megatram.Megatram.Entity.Produit;
import com.Megatram.Megatram.Entity.Reapprovisionnement;
import com.Megatram.Megatram.repository.LieuStockRepository;
import com.Megatram.Megatram.repository.LigneReapprovisionnementRepository;
import com.Megatram.Megatram.repository.ProduitRepos;
import com.Megatram.Megatram.repository.ReapprovisionnementRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ReapprovisionnementService {

    @Autowired
    private ReapprovisionnementRepository reapproRepo;

    @Autowired
    private LigneReapprovisionnementRepository ligneRepo;

    @Autowired
    private ProduitRepos produitRepo;

    @Autowired
    private LieuStockRepository lieuStockRepo;

    @Transactional
    public ReapprovisionnementResponseDto enregistrerReapprovisionnement(ReapprovisionnementRequestDto request) {
        Reapprovisionnement reapprovisionnement = new Reapprovisionnement();
        reapprovisionnement.setSource(request.source);
        reapprovisionnement.setAgent(request.agent);

        // Sauvegarde initiale pour obtenir l'ID
        reapprovisionnement = reapproRepo.save(reapprovisionnement);

        if (request.lignes == null || request.lignes.isEmpty()) {
            throw new IllegalArgumentException("Aucune ligne de réapprovisionnement reçue.");
        }

        ReapprovisionnementResponseDto response = new ReapprovisionnementResponseDto();
        response.id = reapprovisionnement.getId();
        response.source = reapprovisionnement.getSource();
        response.agent = reapprovisionnement.getAgent();
        response.lignes = new ArrayList<>();

        for (LigneReapprovisionnementDto dto : request.lignes) {
            Produit produit = produitRepo.findById(dto.produitId)
                    .orElseThrow(() -> new RuntimeException("Produit introuvable"));

            // Récupération du lieu de stockage
            LieuStock lieuStock = lieuStockRepo.findByNom(dto.lieuStockNom)
                    .orElseThrow(() -> new RuntimeException("LieuStock introuvable"));

            // Mise à jour de la quantité du produit
            produit.setQte(produit.getQte() + dto.qteAjoutee);
            produitRepo.save(produit);

            // Création et sauvegarde de la ligne de réapprovisionnement
            LigneReapprovisionnement ligne = new LigneReapprovisionnement();
            ligne.setReapprovisionnement(reapprovisionnement);
            ligne.setProduit(produit);
            ligne.setQteAjoutee(dto.qteAjoutee);
            ligne.setLieuStock(lieuStock);
            ligne = ligneRepo.save(ligne);

            // Préparation de la réponse pour cette ligne
            LigneReapprovisionnementResponseDto ligneResponse = new LigneReapprovisionnementResponseDto();
            ligneResponse.id = ligne.getId();
            ligneResponse.produitId = produit.getId();
            ligneResponse.produitNom = produit.getNom();
            ligneResponse.qteAjoutee = dto.qteAjoutee;
            ligneResponse.lieuStockNom = lieuStock.getNom();

            response.lignes.add(ligneResponse);
        }

        return response;
    }

    public List<ReapprovisionnementResponseDto> getAllReapprovisionnements() {
        List<Reapprovisionnement> reapprovisionnements = reapproRepo.findAll();

        return reapprovisionnements.stream().map(reappro -> {
            ReapprovisionnementResponseDto dto = new ReapprovisionnementResponseDto();
            dto.id = reappro.getId();
            dto.source = reappro.getSource();
            dto.agent = reappro.getAgent();
            dto.date = reappro.getDate();

            List<LigneReapprovisionnement> lignes = ligneRepo.findByReapprovisionnement(reappro);
            dto.lignes = lignes.stream().map(ligne -> {
                LigneReapprovisionnementResponseDto ligneDto = new LigneReapprovisionnementResponseDto();
                ligneDto.id = ligne.getId();
                ligneDto.produitId = ligne.getProduit().getId();
                ligneDto.produitNom = ligne.getProduit().getNom();
                ligneDto.qteAjoutee = ligne.getQteAjoutee();

                // Récupération du nom du lieuStock via l'entité
                ligneDto.lieuStockNom = ligne.getLieuStock().getNom();

                return ligneDto;
            }).collect(Collectors.toList());

            return dto;
        }).collect(Collectors.toList());
    }

    public ReapprovisionnementDetailsDto getDetails(Long id) {
        Reapprovisionnement r = reapproRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Réapprovisionnement introuvable"));

        List<LigneReapprovisionnement> lignes = ligneRepo.findByReapprovisionnement(r);

        ReapprovisionnementDetailsDto dto = new ReapprovisionnementDetailsDto();
        dto.id = r.getId();
        dto.source = r.getSource();
        dto.agent = r.getAgent();
        dto.date = r.getDate();

        dto.lignes = lignes.stream().map(l -> {
            LigneReapprovisionnementDto ligneDto = new LigneReapprovisionnementDto();
            ligneDto.produitId = l.getProduit().getId();
            ligneDto.produitNom = l.getProduit().getNom();
            ligneDto.qteAjoutee = l.getQteAjoutee();

            // Récupération du nom du lieuStock via l'entité
            ligneDto.lieuStockNom = l.getLieuStock().getNom();

            return ligneDto;
        }).collect(Collectors.toList());

        return dto;
    }
}
