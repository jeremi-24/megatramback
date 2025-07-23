//import com.Megatram.Megatram.Dto.BonLivraisonResponseDTO;
//import com.Megatram.Megatram.Dto.LigneLivraisonDTO;
//import com.Megatram.Megatram.Entity.BonLivraison;
//import com.Megatram.Megatram.Entity.LigneLivraison;
//import com.Megatram.Megatram.Entity.Produit;
//import com.Megatram.Megatram.enums.BonLivraisonStatus;
//import com.Megatram.Megatram.enums.StatutCommande;
//import jakarta.persistence.EntityNotFoundException;
//import org.springframework.security.core.context.SecurityContextHolder;
//import org.springframework.security.core.userdetails.UserDetails;
//import org.springframework.transaction.annotation.Transactional;
//
//import java.util.List;
//import java.util.stream.Collectors;
//
///**
// * Action du magasinier : valide la livraison, décrémente le stock, crée la vente et notifie la secrétaire.
// */
//public BonLivraisonResponseDTO validerEtLivrer(Long bonLivraisonId, String agentEmail) {
//    BonLivraison bl = bonLivraisonRepository.findById(bonLivraisonId)
//            .orElseThrow(() -> new EntityNotFoundException("Bon de Livraison non trouvé: " + bonLivraisonId));
//
//    if (bl.getStatut() != BonLivraisonStatus.EN_ATTENTE && bl.getStatut() != BonLivraisonStatus.A_LIVRER) {            throw new IllegalStateException("Cette livraison a déjà été validée.");
//    }
//
//    // --- Logique de décrémentation du stock (inchangée) ---
//    for (LigneLivraison ligne : bl.getLignesLivraison()) {
//        Produit produit = produitRepos.findById(ligne.getProduitId())
//                .orElseThrow(() -> new EntityNotFoundException("Produit non trouvé: " + ligne.getProduitId()));
//        if (produit.getQte() < ligne.getQteLivre()) {
//            throw new IllegalStateException("Stock insuffisant pour le produit '" + produit.getNom() + "'.");
//        }
//        produit.setQte(produit.getQte() - ligne.getQteLivre());
//        produitRepos.save(produit);
//    }
//
//    // --- Mise à jour des statuts (inchangée) ---
//    bl.setStatut(BonLivraisonStatus.LIVRE);
//    bl.getCommande().setStatut(StatutCommande.VALIDEE); // La commande est maintenant terminée
//    BonLivraison updatedBl = bonLivraisonRepository.save(bl);
//
//    // --- Création de la vente (inchangée) ---
//    venteService.creerVenteDepuisBonLivraison(updatedBl, agentEmail);
//
//    // --- NOTIFICATION DE CONFIRMATION À LA SECRÉTAIRE ---
//    String messageConfirmation = "La livraison N°" + updatedBl.getId() + " (commande N°" + updatedBl.getCommande().getId() + ") a été complétée.";
//    notificationService.envoyerNotification("/topic/secretariat", messageConfirmation);
//
//    return buildResponseDTO(updatedBl);
//}
//
//
//
//
//public BonLivraisonResponseDTO validerETAttendre(Long bonLivraisonId, String agentEmail) {
//    BonLivraison bl = bonLivraisonRepository.findById(bonLivraisonId)
//            .orElseThrow(() -> new EntityNotFoundException("Bon de Livraison non trouvé: " + bonLivraisonId));
//
//    // ❌ Suppression de la mise à jour du statut "A_LIVRER"
//    // ✅ On remet explicitement le statut à EN_ATTENTE (optionnel si c’est déjà son état actuel)
//    bl.setStatut(BonLivraisonStatus.EN_ATTENTE);
//
//    // Enregistrement
//    BonLivraison updatedBl = bonLivraisonRepository.save(bl);
//
//    // ❌ Pas de vente, pas de notification
//
//    return buildResponseDTO(updatedBl);
//}
//
//
///**
// * Récupère les BLs pour un lieu de stock spécifique.
// * C'est ce que le magasinier utilisera pour voir sa liste de travail.
// */
//@Transactional(readOnly = true)
//public List<BonLivraisonResponseDTO> getBonsLivraisonParLieu(Long lieuId) {
//    return bonLivraisonRepository.findByCommande_LieuStock_Id(lieuId).stream()
//            .map(this::buildResponseDTO)
//            .collect(Collectors.toList());
//}
//
//@Transactional(readOnly = true)
//public List<BonLivraisonResponseDTO> getBonsLivraisonParLieuAvecStatutALivrer(Long lieuId) {
//    return bonLivraisonRepository.findByCommande_LieuStock_Id(lieuId).stream()
//            .map(bon -> {
//                BonLivraisonResponseDTO dto = buildResponseDTO(bon);
//                dto.setStatut(BonLivraisonStatus.A_LIVRER);
//                return dto;
//            })
//            .collect(Collectors.toList());
//}
//
//
//private BonLivraisonResponseDTO buildResponseDTO(BonLivraison bon) {
//    BonLivraisonResponseDTO dto = new BonLivraisonResponseDTO();
//    dto.setId(bon.getId());
//    dto.setDateLivraison(bon.getDateLivraison());
//    dto.setCommandeId(bon.getCommande().getId());
//    dto.setLignesLivraison(mapLignes(bon.getLignesLivraison()));
//    dto.setLieuStock(bon.getCommande().getLieuStock());
//    dto.setStatut(bon.getStatut()); // ✅ Ne pas oublier cette ligne
//    return dto;
//}
//
//private BonLivraisonResponseDTO buildResponseDTO(BonLivraison bon, String email) {
//    BonLivraisonResponseDTO dto = new BonLivraisonResponseDTO();
//    dto.setId(bon.getId());
//    dto.setDateLivraison(bon.getDateLivraison());
//    dto.setCommandeId(bon.getCommande().getId());
//    dto.setLignesLivraison(mapLignes(bon.getLignesLivraison()));
//    dto.setLieuStock(bon.getCommande().getLieuStock());
//    dto.setStatut(bon.getStatut());
//    dto.setEmail(email);
//    return dto;
//}
//
//
//
///**
// * Méthode "assistante" privée pour convertir une entité BonLivraison en DTO.
// */
//
//private List<LigneLivraisonDTO> mapLignes(List<LigneLivraison> lignesLivraison) {
//    return lignesLivraison.stream()
//            .map(ligne -> {
//                Produit produit = produitRepos.findById(ligne.getProduitId()).orElse(null);
//                LigneLivraisonDTO dto = new LigneLivraisonDTO();
//                dto.setId(ligne.getId());
//                dto.setProduitNom(produit != null ? produit.getNom() : "Inconnu");
//                dto.setQteLivre(ligne.getQteLivre());
//                dto.setProduitPrix(ligne.getProduitPrix());
//                return dto;
//            })
//            .collect(Collectors.toList());
//}
//
//
//
//
//
//
//public List<BonLivraisonResponseDTO> getAllBonsLivraison() {
//    String email = getEmailUtilisateurConnecte();
//    List<BonLivraison> bons = bonLivraisonRepository.findAll();
//    return bons.stream()
//            .map(this::convertToDto)
//            .collect(Collectors.toList());
//}
//
//public String getEmailUtilisateurConnecte() {
//    Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
//    if (principal instanceof UserDetails) {
//        return ((UserDetails) principal).getUsername();  // généralement l’email ou le login
//    } else {
//        return principal.toString();
//    }
//}
//
//private BonLivraisonResponseDTO convertToDto(BonLivraison bon) {
//    String email = getEmailUtilisateurConnecte(); // récupère ici l'email
//
//    BonLivraisonResponseDTO dto = new BonLivraisonResponseDTO();
//    dto.setId(bon.getId());
//    dto.setDateLivraison(bon.getDateLivraison());
//    dto.setCommandeId(bon.getCommande() != null ? bon.getCommande().getId() : null);
//    dto.setStatut(bon.getStatut());
//
//    if (bon.getCommande() != null && bon.getCommande().getLieuStock() != null) {
//        dto.setLieuStock(bon.getCommande().getLieuStock());
//    } else {
//        dto.setLieuStock(bon.getLieuStock());
//    }
//
//    List<LigneLivraisonDTO> lignesDTO = bon.getLignesLivraison().stream()
//            .map(ligne -> {
//                LigneLivraisonDTO ligneDTO = new LigneLivraisonDTO();
//                ligneDTO.setId(ligne.getId());
//                ligneDTO.setQteLivre(ligne.getQteLivre());
//                ligneDTO.setProduitPrix(ligne.getProduitPrix());
//
//                if (ligne.getProduitId() != null) {
//                    produitRepos.findById(ligne.getProduitId())
//                            .ifPresent(produit -> ligneDTO.setProduitNom(produit.getNom()));
//                } else {
//                    ligneDTO.setProduitNom("Inconnu");
//                }
//                return ligneDTO;
//            })
//            .collect(Collectors.toList());
//
//    dto.setLignesLivraison(lignesDTO);
//
//    dto.setEmail(email);
//
//    return dto;
//}