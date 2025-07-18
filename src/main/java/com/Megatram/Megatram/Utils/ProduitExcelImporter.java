package com.Megatram.Megatram.Utils;

import com.Megatram.Megatram.Entity.Produit;
import com.Megatram.Megatram.repository.CategorieRep;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public class ProduitExcelImporter {

    public static List<Produit> lireProduitsDepuisExcel(MultipartFile file, CategorieRep categorieRepository) {
        List<Produit> produits = new ArrayList<>();

        try (InputStream inputStream = file.getInputStream(); Workbook workbook = new XSSFWorkbook(inputStream)) {
            Sheet sheet = workbook.getSheetAt(0);
            boolean isFirstRow = true;

            for (Row row : sheet) {
                if (isFirstRow) {
                    isFirstRow = false;
                    continue;
                }

                Produit produit = new Produit();
                produit.setNom(row.getCell(0).getStringCellValue());
                produit.setRef(row.getCell(1).getStringCellValue());
                produit.setQte((int) row.getCell(2).getNumericCellValue());
                produit.setPrix(row.getCell(3).getNumericCellValue());

                // Catégorie en colonne 4
                String nomCategorie = row.getCell(4).getStringCellValue();
                produit.setCategorie(categorieRepository.findByNom(nomCategorie)
                        .orElseThrow(() -> new RuntimeException("Catégorie non trouvée: " + nomCategorie)));

                produits.add(produit);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return produits;
    }

}
