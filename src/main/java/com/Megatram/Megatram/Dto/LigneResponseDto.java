package com.Megatram.Megatram.Dto;

import com.Megatram.Megatram.Entity.LigneInventaire;
import com.Megatram.Megatram.Entity.Produit;
import com.Megatram.Megatram.Entity.LieuStock; // Maintenir si nécessaire

public class LigneResponseDto {

    private Long produitId;
    private String nomProduit;
    private String lieuStockNom;

    // Quantités stockées dans l'entité (en unités totales) et écart (en unités totales)
    private int qteAvantScanTotaleUnites;
    private int qteScanneTotaleUnites;
    private Integer ecartTotalUnites; // Integer pour permettre null si pas d'écart calculé

    // Nouveau champ pour le type de quantité scannée (informatif)
    private String typeQuantiteScanne;

    // Nouveaux champs pour l'affichage des quantités converties (Avant Scan)
    private int qteAvantScanCartons;
    private int qteAvantScanUnitesRestantes;

    // Nouveaux champs pour l'affichage des quantités converties (Scannée)
    private int qteScanneCartons;
    private int qteScanneUnitesRestantes;

    // Nouveaux champs pour l'affichage de l'écart converti
    private Integer ecartCartons; // Integer pour permettre null
    private Integer ecartUnites; // Integer pour permettre null


    public LigneResponseDto() {
    }

    // Constructeur pour créer un DTO à partir d'une entité LigneInventaire
    public LigneResponseDto(LigneInventaire ligneInventaire) {
        // Accéder au produit via la relation dans l'entité
        Produit produit = ligneInventaire.getProduit();
        if (produit != null) {
             this.produitId = produit.getId();
             this.nomProduit = produit.getNom();
        } else {
            this.produitId = null;
            this.nomProduit = "Produit Inconnu";
        }

        // Accéder au lieu de stock via la relation dans l'entité
        LieuStock lieuStock = ligneInventaire.getLieuStock();
        if (lieuStock != null) {
            this.lieuStockNom = lieuStock.getNom();
        } else {
            this.lieuStockNom = "Lieu Inconnu";
        }

        // Lire les quantités et l'écart en unités totales de l'entité
        this.qteAvantScanTotaleUnites = ligneInventaire.getQteAvantScan();
        this.qteScanneTotaleUnites = ligneInventaire.getQteScanne();
        this.ecartTotalUnites = ligneInventaire.getEcart();
        this.typeQuantiteScanne = ligneInventaire.getTypeQuantiteScanne(); // Lire le type de quantité scannée

        // Calculer les quantités en cartons et unités pour l'affichage (Avant Scan)
        if (produit != null && produit.getQteParCarton() > 0) {
             this.qteAvantScanCartons = this.qteAvantScanTotaleUnites / produit.getQteParCarton();
             this.qteAvantScanUnitesRestantes = this.qteAvantScanTotaleUnites % produit.getQteParCarton();

             // Calculer les quantités en cartons et unités pour l'affichage (Scannée)
             this.qteScanneCartons = this.qteScanneTotaleUnites / produit.getQteParCarton();
             this.qteScanneUnitesRestantes = this.qteScanneTotaleUnites % produit.getQteParCarton();

            // Calculer l'écart en cartons et unités
            if (this.ecartTotalUnites != null) {
                 this.ecartCartons = this.ecartTotalUnites / produit.getQteParCarton();
                 this.ecartUnites = this.ecartTotalUnites % produit.getQteParCarton();
            } else {
                 this.ecartCartons = null;
                 this.ecartUnites = null;
            }

        } else {
            // Si qteParCarton est invalide ou produit est null, afficher les unités totales comme unités
            this.qteAvantScanCartons = 0;
            this.qteAvantScanUnitesRestantes = this.qteAvantScanTotaleUnites;
            this.qteScanneCartons = 0;
            this.qteScanneUnitesRestantes = this.qteScanneTotaleUnites;
             this.ecartCartons = null;
             this.ecartUnites = this.ecartTotalUnites;
        }
    }


    // Getters and Setters

    public Long getProduitId() {
        return produitId;
    }

    public void setProduitId(Long produitId) {
        this.produitId = produitId;
    }

    public String getNomProduit() {
        return nomProduit;
    }

    public void setNomProduit(String nomProduit) {
        this.nomProduit = nomProduit;
    }

    public String getLieuStockNom() {
        return lieuStockNom;
    }

    public void setLieuStockNom(String lieuStockNom) {
        this.lieuStockNom = lieuStockNom;
    }

    public int getQteAvantScanTotaleUnites() {
        return qteAvantScanTotaleUnites;
    }

    public void setQteAvantScanTotaleUnites(int qteAvantScanTotaleUnites) {
        this.qteAvantScanTotaleUnites = qteAvantScanTotaleUnites;
    }

    public int getQteScanneTotaleUnites() {
        return qteScanneTotaleUnites;
    }

    public void setQteScanneTotaleUnites(int qteScanneTotaleUnites) {
        this.qteScanneTotaleUnites = qteScanneTotaleUnites;
    }

    public Integer getEcartTotalUnites() {
        return ecartTotalUnites;
    }

    public void setEcartTotalUnites(Integer ecartTotalUnites) {
        this.ecartTotalUnites = ecartTotalUnites;
    }

    public String getTypeQuantiteScanne() {
        return typeQuantiteScanne;
    }

    public void setTypeQuantiteScanne(String typeQuantiteScanne) {
        this.typeQuantiteScanne = typeQuantiteScanne;
    }

    public int getQteAvantScanCartons() {
        return qteAvantScanCartons;
    }

    public void setQteAvantScanCartons(int qteAvantScanCartons) {
        this.qteAvantScanCartons = qteAvantScanCartons;
    }

    public int getQteAvantScanUnitesRestantes() {
        return qteAvantScanUnitesRestantes;
    }

    public void setQteAvantScanUnitesRestantes(int qteAvantScanUnitesRestantes) {
        this.qteAvantScanUnitesRestantes = qteAvantScanUnitesRestantes;
    }

    public int getQteScanneCartons() {
        return qteScanneCartons;
    }

    public void setQteScanneCartons(int qteScanneCartons) {
        this.qteScanneCartons = qteScanneCartons;
    }

    public int getQteScanneUnitesRestantes() {
        return qteScanneUnitesRestantes;
    }

    public void setQteScanneUnitesRestantes(int qteScanneUnitesRestantes) {
        this.qteScanneUnitesRestantes = qteScanneUnitesRestantes;
    }

    public Integer getEcartCartons() {
        return ecartCartons;
    }

    public void setEcartCartons(Integer ecartCartons) {
        this.ecartCartons = ecartCartons;
    }

    public Integer getEcartUnites() {
        return ecartUnites;
    }

    public void setEcartUnites(Integer ecartUnites) {
        this.ecartUnites = ecartUnites;
    }
}
