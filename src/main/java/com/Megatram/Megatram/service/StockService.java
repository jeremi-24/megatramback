package com.Megatram.Megatram.service;

import com.Megatram.Megatram.repository.ProduitRepos;
import com.Megatram.Megatram.repository.StockRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;


@Service
public class StockService {

    @Autowired
    private StockRepository stockRepository;

    @Autowired
    private ProduitRepos produitRepository;




//    public List<StockDto> getEtatStockParNoms(String produitNom, String entrepotNom, Date dateLimite) {
//        Produit produit = produitRepository.findByNom(produitNom)
//                .orElseThrow(() -> new RuntimeException("Produit introuvable"));
//        Entrepot entrepot = entrepotRepository.findByNom(entrepotNom)
//                .orElseThrow(() -> new RuntimeException("Entrepôt introuvable"));
//
//        return getEtatStockParProduitEtEntrepot(produit.getId(), entrepot.getId(), dateLimite);
//    }
//
//
//    public List<StockDto> getEtatGlobalParEntrepotNom(String entrepotNom, Date dateLimite) {
//        Entrepot entrepot = entrepotRepository.findByNom(entrepotNom)
//                .orElseThrow(() -> new RuntimeException("Entrepôt introuvable"));
//
//        return getEtatGlobalParEntrepot(entrepot.getId(), dateLimite);
//    }
//


}
//
//public List<StockDto> getAllStocks() {
//    return stockRepository.findAll().stream().map(this::convertToDto).collect(Collectors.toList());
//}
//
//public StockDto getStockById(Long id) {
//    return stockRepository.findById(id).map(this::convertToDto).orElse(null);
//}
//
//    public StockDto createStock(StockDto dto) {
//        Produit produit = produitRepository.findByNom(dto.getProduitNom());
//        Entrepot entrepot = entrepotRepository.findByNom(dto.getEntrepotNom());
//
//        if (produit == null || entrepot == null) return null;
//
//        Stock stock = new Stock();
//        stock.setProduit(produit);
//        stock.setEntrepot(entrepot);
//        stock.setQuantite(dto.getProduitQte());
//        stock.setDate(dto.getDate());
//
//        return convertToDto(stockRepository.save(stock));
//    }
//
//    public StockDto updateStock(Long id, StockDto dto) {
//        Optional<Stock> optional = stockRepository.findById(id);
//        if (optional.isEmpty()) return null;
//
//        Stock stock = optional.get();
//        Produit produit = produitRepository.findByNom(dto.getProduitNom());
//        Entrepot entrepot = entrepotRepository.findByNom(dto.getEntrepotNom());
//
//        if (produit == null || entrepot == null) return null;
//
//        stock.setProduit(produit);
//        stock.setEntrepot(entrepot);
//        stock.setQuantite(dto.getProduitQte());
//        stock.setDate(dto.getDate());
//
//        return convertToDto(stockRepository.save(stock));
//    }
//
//    public void deleteStock(Long id) {
//        stockRepository.deleteById(id);
//    }
//
//    private StockDto convertToDto(Stock stock) {
//        StockDto dto = new StockDto();
//        dto.setId(stock.getId());
//        dto.setProduitNom(stock.getProduit().getNom());
//        dto.setProduitQte(stock.getQuantite());
//        dto.setEntrepotNom(stock.getEntrepot().getNom());
//        dto.setDate(stock.getDate());
//        return dto;
//    }
//}