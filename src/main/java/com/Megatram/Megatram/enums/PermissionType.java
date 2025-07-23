package com.Megatram.Megatram.enums;

public enum PermissionType {

    // --- Gestion des Produits ---
    PRODUIT_CREATE("Créer un produit"),
    PRODUIT_READ("Lire les informations d'un produit"),
    PRODUIT_UPDATE("Mettre à jour un produit"),
    PRODUIT_DELETE("Supprimer un produit"),
    PRODUIT_IMPORT("Importer des produits depuis Excel"),

    // --- Gestion des Commandes ---
    COMMANDE_CREATE("Créer une commande"),
    COMMANDE_READ("Lire les commandes"),
    COMMANDE_VALIDATE("Valider une commande (action de la secrétaire)"),
    COMMANDE_CANCEL("Annuler une commande"),

    // --- Gestion des Livraisons ---
    LIVRAISON_GENERATE("Générer un bon de livraison"),
    LIVRAISON_READ("Consulter les bons de livraison"),
    LIVRAISON_VALIDATE("Valider une livraison et sortir le stock (action du magasinier)"),

    // --- Gestion des Ventes & Factures ---
    FACTURE_GENERATE("Générer une facture"),
    VENTE_READ("Consulter les ventes"),

    // === NOUVELLES PERMISSIONS ===
    // --- Gestion des Stocks ---
    INVENTAIRE_MANAGE("Gérer les inventaires (créer, valider)"),
    REAPPRO_MANAGE("Gérer les réapprovisionnements (transferts internes)"),

    // --- Gestion des Utilisateurs & Rôles (Admin seulement) ---
    USER_MANAGE("Gérer les utilisateurs et les rôles"),

    // --- Gestion des Rapports (DG / Contrôleur) ---
    REPORT_VIEW("Voir les rapports de performance");




    private final String description;

    PermissionType(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}