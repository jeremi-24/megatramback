package com.Megatram.Megatram.service;

import com.Megatram.Megatram.Dto.InventaireRequestDto;
import com.Megatram.Megatram.Dto.InventaireResponseDto;
import com.Megatram.Megatram.Dto.LigneResponseDto;
import com.Megatram.Megatram.Entity.Inventaire;
import com.Megatram.Megatram.Entity.LigneInventaire;
import com.Megatram.Megatram.Entity.Produit;
import com.Megatram.Megatram.Entity.LieuStock;
import com.Megatram.Megatram.repository.InventaireRepository;
import com.Megatram.Megatram.repository.LigneInventaireRepository;
import com.Megatram.Megatram.repository.ProduitRepos;
import com.Megatram.Megatram.repository.LieuStockRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class InventaireService {

    @Autowired
    private InventaireRepository inventaireRepo;

    @Autowired
    private LigneInventaireRepository ligneRepo;

    @Autowired
    private ProduitRepos produitRepo;

    @Autowired
    private LieuStockRepository lieuStockRepo;

    public InventaireResponseDto enregistrerInventaire(InventaireRequestDto request, boolean estPremierInventaire) {
        Inventaire inventaire = new Inventaire();
        inventaire.setCharge(request.charge);
        inventaireRepo.save(inventaire);

        List<LigneResponseDto> lignes = request.produits.stream().map(Dto -> {
            Produit produit = produitRepo.findById(Dto.produitId)
                    .orElseThrow(() -> new RuntimeException("Produit introuvable"));

            LieuStock lieuStock = lieuStockRepo.findByNom(Dto.lieuStockNom)
                    .orElseThrow(() -> new RuntimeException("Lieu de stock introuvable : " + Dto.lieuStockNom));

            LigneInventaire ligne = new LigneInventaire();
            ligne.setInventaire(inventaire);
            ligne.setProduit(produit);
            ligne.setQteAvantScan(produit.getQte());
            ligne.setQteScanne(Dto.qteScanne);
            ligne.setLieuStock(lieuStock);

            if (!estPremierInventaire) {
                ligne.setEcart(Dto.qteScanne - produit.getQte());
            } else {
                ligne.setEcart(null);
                produit.setQte(Dto.qteScanne);
                produitRepo.save(produit);
            }

            ligneRepo.save(ligne);

            LigneResponseDto res = new LigneResponseDto();
            res.produitId = produit.getId();
            res.nomProduit = produit.getNom();
            res.qteAvantScan = ligne.getQteAvantScan();
            res.qteScanne = ligne.getQteScanne();
            res.ecart = ligne.getEcart();
            res.lieuStockNom = lieuStock.getNom();
            return res;
        }).collect(Collectors.toList());

        InventaireResponseDto response = new InventaireResponseDto();
        response.inventaireId = inventaire.getId();
        response.charge = inventaire.getCharge();
        response.date = inventaire.getDate();
        response.lignes = lignes;

        return response;
    }

    public List<InventaireResponseDto> recupererTousLesInventaires() {
        return inventaireRepo.findAll().stream().map(inv -> {
            List<LigneInventaire> lignes = ligneRepo.findByInventaireId(inv.getId());
            List<LigneResponseDto> ligneDtos = lignes.stream().map(ligne -> {
                LigneResponseDto Dto = new LigneResponseDto();
                Dto.produitId = ligne.getProduit().getId();
                Dto.nomProduit = ligne.getProduit().getNom();
                Dto.qteScanne = ligne.getQteScanne();
                Dto.qteAvantScan = ligne.getQteAvantScan();
                Dto.ecart = ligne.getEcart();
                Dto.lieuStockNom = ligne.getLieuStock() != null ? ligne.getLieuStock().getNom() : null;
                return Dto;
            }).collect(Collectors.toList());

            InventaireResponseDto response = new InventaireResponseDto();
            response.inventaireId = inv.getId();
            response.charge = inv.getCharge();
            response.date = inv.getDate();
            response.lignes = ligneDtos;
            return response;
        }).collect(Collectors.toList());
    }

    public InventaireResponseDto getInventaireById(Long id) {
        Inventaire inv = inventaireRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Inventaire introuvable"));

        List<LigneInventaire> lignes = ligneRepo.findByInventaireId(id);
        List<LigneResponseDto> ligneDtos = lignes.stream().map(ligne -> {
            LigneResponseDto Dto = new LigneResponseDto();
            Dto.produitId = ligne.getProduit().getId();
            Dto.nomProduit = ligne.getProduit().getNom();
            Dto.qteScanne = ligne.getQteScanne();
            Dto.qteAvantScan = ligne.getQteAvantScan();
            Dto.ecart = ligne.getEcart();
            Dto.lieuStockNom = ligne.getLieuStock() != null ? ligne.getLieuStock().getNom() : null;
            return Dto;
        }).collect(Collectors.toList());

        InventaireResponseDto response = new InventaireResponseDto();
        response.inventaireId = inv.getId();
        response.charge = inv.getCharge();
        response.date = inv.getDate();
        response.lignes = ligneDtos;
        return response;
    }
}
