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

    public Optional<Stock> findStockByProduitAndLieuStock(Produit produit, LieuStock lieuStock) {
        return stockRepository.findByProduitAndLieuStock(produit, lieuStock);
    }

    // Nouvelle méthode pour vérifier et notifier le stock
    private void verifierEtNotifierProduit(Produit produit, int seuilTolerance) {
        int stockTotal = getQuantiteTotaleGlobaleByProduit(produit.getId()); // total en stock
    
        if (stockTotal <= produit.getQteMin() + seuilTolerance) {
            try {
                String message = "Attention, le stock du produit '" + produit.getNom() + "' est proche du seuil minimal (" 
                                 + stockTotal + " unités restantes). Veuillez vérifier et réapprovisionner.";
                notificationService.envoyerNotification("/app", message);
            } catch (Exception e) {
                System.err.println("Erreur lors de l'envoi de la notification WebSocket : " + e.getMessage());
            }
        }
    }
    

    public Stock addStock(Produit produit, LieuStock lieuStock, int quantiteTotaleAjoutee) {
        if (produit.getQteParCarton() <= 0) {
            throw new IllegalArgumentException("La quantité par carton pour le produit " + produit.getNom() + " doit être supérieure à 0.");
        }

        Stock stock = findStockByProduitAndLieuStock(produit, lieuStock)
                .orElseGet(() -> {
                    Stock newStock = new Stock();
                    newStock.setProduit(produit);
                    newStock.setLieuStock(lieuStock);
                    newStock.setQteCartons(0);
                    newStock.setQteUnitesRestantes(0);
                    return newStock;
                });

        int ancienneQuantiteTotale = (stock.getQteCartons() * produit.getQteParCarton()) + stock.getQteUnitesRestantes();
        int nouvelleQuantiteTotale = ancienneQuantiteTotale + quantiteTotaleAjoutee;

        stock.setQteCartons(nouvelleQuantiteTotale / produit.getQteParCarton());
        stock.setQteUnitesRestantes(nouvelleQuantiteTotale % produit.getQteParCarton());

        verifierEtNotifierProduit(produit, 10); // Appel de la méthode pour vérifier et notifier
        return stockRepository.save(stock);
    }

    public Stock removeStock(Produit produit, LieuStock lieuStock, int quantiteTotaleRetiree) {
        if (produit.getQteParCarton() <= 0) {
            throw new IllegalArgumentException("La quantité par carton pour le produit " + produit.getNom() + " doit être supérieure à 0.");
        }

        Stock stock = findStockByProduitAndLieuStock(produit, lieuStock)
                .orElseThrow(() -> new RuntimeException("Stock non trouvé pour le produit : " + produit.getNom() + " dans le lieu : " + lieuStock.getNom()));

        int ancienneQuantiteTotale = (stock.getQteCartons() * produit.getQteParCarton()) + stock.getQteUnitesRestantes();

        if (ancienneQuantiteTotale < quantiteTotaleRetiree) {
            throw new RuntimeException("Quantité insuffisante en stock pour le produit : " + produit.getNom());
        }

        int nouvelleQuantiteTotale = ancienneQuantiteTotale - quantiteTotaleRetiree;
        stock.setQteCartons(nouvelleQuantiteTotale / produit.getQteParCarton());
        stock.setQteUnitesRestantes(nouvelleQuantiteTotale % produit.getQteParCarton());
       
        verifierEtNotifierProduit(produit, 10); // Appel de la méthode pour vérifier et notifier
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
}