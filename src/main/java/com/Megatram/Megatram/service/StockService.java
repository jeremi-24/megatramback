package com.Megatram.Megatram.service;

import com.Megatram.Megatram.Dto.StockDto;
import com.Megatram.Megatram.Entity.LieuStock;
import com.Megatram.Megatram.Entity.Produit;
import com.Megatram.Megatram.Entity.Stock;
import com.Megatram.Megatram.repository.LieuStockRepository;
import com.Megatram.Megatram.repository.ProduitRepos;
import com.Megatram.Megatram.repository.StockRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import jakarta.persistence.EntityNotFoundException;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class StockService {

    @Autowired
    private StockRepository stockRepository;

    @Autowired
    private NotificationService notificationService;

    @Autowired
    private ProduitRepos produitRepos; // Injecter ProduitRepos

    @Autowired
 private LieuStockRepository lieuStockRepository;

    public Optional<Stock> findStockByProduitAndLieuStock(Produit produit, LieuStock lieuStock) {
        return stockRepository.findByProduitAndLieuStock(produit, lieuStock);
    }

    // Nouvelle méthode pour vérifier et notifier le stock
    private void verifierEtNotifierProduit(Produit produit, int seuilTolerance) {
        int stockTotal = getQuantiteTotaleGlobaleByProduit(produit.getId());
    
        if (stockTotal <= produit.getQteMin() + seuilTolerance) {
            try {
                String message = "Attention, le stock du produit '" + produit.getNom()
                    + "' est proche du seuil minimal (" + stockTotal
                    + " unités restantes). Veuillez vérifier et réapprovisionner.";
    
                notificationService.envoyerNotificationGenerale(message);
            } catch (Exception e) {
                System.err.println("Erreur lors de l'envoi de la notification WebSocket : " + e.getMessage());
            }
        }
    }
    
    

    public Stock addStock(Produit produit, LieuStock lieuStock, int quantiteTotaleAjoutee) {
        // Récupère ou crée le stock existant
        Stock stock = findStockByProduitAndLieuStock(produit, lieuStock)
                .orElseGet(() -> {
                    Stock newStock = new Stock();
                    newStock.setProduit(produit);
                    newStock.setLieuStock(lieuStock);
                    newStock.setQteCartons(0);
                    newStock.setQteUnitesRestantes(0);
                    return newStock;
                });
    
        int ancienneQuantiteTotale;
        int nouvelleQuantiteTotale;
    
        if (produit.getQteParCarton() > 0) {
            // Produit en cartons
            ancienneQuantiteTotale = (stock.getQteCartons() * produit.getQteParCarton()) + stock.getQteUnitesRestantes();
            nouvelleQuantiteTotale = ancienneQuantiteTotale + quantiteTotaleAjoutee;
    
            stock.setQteCartons(nouvelleQuantiteTotale / produit.getQteParCarton());
            stock.setQteUnitesRestantes(nouvelleQuantiteTotale % produit.getQteParCarton());
        } else {
            // Produit non conditionné en cartons
            ancienneQuantiteTotale = stock.getQteUnitesRestantes(); // ignore les cartons
            nouvelleQuantiteTotale = ancienneQuantiteTotale + quantiteTotaleAjoutee;
    
            stock.setQteCartons(0);
            stock.setQteUnitesRestantes(nouvelleQuantiteTotale);
        }
    
        verifierEtNotifierProduit(produit, 10);
        return stockRepository.save(stock);
    }
    

    public Stock removeStock(Produit produit, LieuStock lieuStock, int quantiteTotaleRetiree) {
        // Récupère le stock ou lance une exception s'il n'existe pas
        Stock stock = findStockByProduitAndLieuStock(produit, lieuStock)
                .orElseThrow(() -> new RuntimeException("Stock non trouvé pour le produit : " + produit.getNom() + " dans le lieu : " + lieuStock.getNom()));
    
        int ancienneQuantiteTotale;
        int nouvelleQuantiteTotale;
    
        // Gère le cas des produits conditionnés en cartons
        if (produit.getQteParCarton() > 0) {
            ancienneQuantiteTotale = (stock.getQteCartons() * produit.getQteParCarton()) + stock.getQteUnitesRestantes();
    
            if (ancienneQuantiteTotale < quantiteTotaleRetiree) {
                throw new RuntimeException("Quantité insuffisante en stock pour le produit : " + produit.getNom());
            }
    
            nouvelleQuantiteTotale = ancienneQuantiteTotale - quantiteTotaleRetiree;
            stock.setQteCartons(nouvelleQuantiteTotale / produit.getQteParCarton());
            stock.setQteUnitesRestantes(nouvelleQuantiteTotale % produit.getQteParCarton());
        } 
        // Gère le cas des produits non conditionnés en cartons (vendus à l'unité)
        else {
            ancienneQuantiteTotale = stock.getQteUnitesRestantes(); // Seules les unités comptent
    
            if (ancienneQuantiteTotale < quantiteTotaleRetiree) {
                throw new RuntimeException("Quantité insuffisante en stock pour le produit : " + produit.getNom());
            }
    
            nouvelleQuantiteTotale = ancienneQuantiteTotale - quantiteTotaleRetiree;
            stock.setQteCartons(0); // S'assurer que les cartons restent à 0
            stock.setQteUnitesRestantes(nouvelleQuantiteTotale);
        }
       
        // Appel de la méthode pour vérifier et notifier
        verifierEtNotifierProduit(produit, 10);
        return stockRepository.save(stock);
    }

    public List<StockDto> getAllStocks() {
        return stockRepository.findAll().stream().map(this::convertToDto).collect(Collectors.toList());
    }

    public StockDto getStockById(Long id) {
        return stockRepository.findById(id).map(this::convertToDto).orElse(null);
    }

    public void deleteStock(Long id) {
        stockRepository.deleteById(id);
    }

    public void createStockEntryForImport(Produit produit, String lieuStockNom) {
        if (lieuStockNom != null && !lieuStockNom.isEmpty()) {
            LieuStock lieuStock = lieuStockRepository.findByNomIgnoreCase(lieuStockNom)
                    .orElseGet(() -> {
                        LieuStock nouveauLieu = new LieuStock();
                        nouveauLieu.setNom(lieuStockNom);
                        return lieuStockRepository.save(nouveauLieu);
                    });

            Optional<Stock> existingStock = findStockByProduitAndLieuStock(produit, lieuStock);

            if (!existingStock.isPresent()) {
                Stock newStock = new Stock();
                newStock.setProduit(produit);
                newStock.setLieuStock(lieuStock);
                newStock.setQteCartons(0);
                newStock.setQteUnitesRestantes(0);
                stockRepository.save(newStock);
            }
        }
    }

    public void createOrUpdateStockEntry(Produit produit, LieuStock lieuStock, int initialQuantity) {
        Stock stock = findStockByProduitAndLieuStock(produit, lieuStock)
                .orElseGet(() -> {
                    Stock newStock = new Stock();
                    newStock.setProduit(produit);
                    newStock.setLieuStock(lieuStock);
                    return newStock;
                });

        int currentTotalQuantity = (stock.getQteCartons() * produit.getQteParCarton()) + stock.getQteUnitesRestantes();
        int newTotalQuantity = currentTotalQuantity + initialQuantity;

        if (produit.getQteParCarton() > 0) {
            stock.setQteCartons(newTotalQuantity / produit.getQteParCarton());
            stock.setQteUnitesRestantes(newTotalQuantity % produit.getQteParCarton());
        } else {
            stock.setQteCartons(0); // S'assurer que les cartons restent à 0 si non conditionné
            stock.setQteUnitesRestantes(newTotalQuantity);
        }

        stockRepository.save(stock);

        // Optionally, you might want to call verifierEtNotifierProduit here as well
        // verifierEtNotifierProduit(produit, 10);
    }

    // Nouvelle méthode pour calculer la quantité totale globale par produit
    public int getQuantiteTotaleGlobaleByProduit(Long produitId) {
        Optional<Produit> produitOpt = produitRepos.findById(produitId);
        if (!produitOpt.isPresent()) {
            // Gérer le cas où le produit n'est pas trouvé, peut-être retourner 0 ou lancer une exception
            // Pour l'instant, retournons 0 pour éviter de bloquer l'affichage si un produit est supprimé mais toujours référencé ailleurs.
            return 0; 
        }
        Produit produit = produitOpt.get();

        List<Stock> stocks = stockRepository.findByProduit(produit);
        int quantiteTotaleGlobale = 0;

        for (Stock stock : stocks) {
            quantiteTotaleGlobale += (stock.getQteCartons() * produit.getQteParCarton()) + stock.getQteUnitesRestantes();
        }

        return quantiteTotaleGlobale;
    }

    private StockDto convertToDto(Stock stock) {
        return new StockDto(stock);
    }

    // Récupérer tous les stocks d'un lieu spécifique
public List<StockDto> getStocksByLieuStockId(Long lieuStockId) {
    LieuStock lieuStock = lieuStockRepository.findById(lieuStockId)
            .orElseThrow(() -> new EntityNotFoundException("Lieu de stock non trouvé avec l'id : " + lieuStockId));
    
    List<Stock> stocks = stockRepository.findByLieuStock(lieuStock);
    return stocks.stream().map(this::convertToDto).collect(Collectors.toList());
}

    public List<StockDto> getStocksByLieuStockNom(String lieuStockNom) {
 LieuStock lieuStock = lieuStockRepository.findByNomIgnoreCase(lieuStockNom)
 .orElseThrow(() -> new EntityNotFoundException("Lieu de stock non trouvé avec le nom : " + lieuStockNom));

 List<Stock> stocks = stockRepository.findByLieuStock(lieuStock);
 return stocks.stream().map(this::convertToDto).collect(Collectors.toList());
    }
}