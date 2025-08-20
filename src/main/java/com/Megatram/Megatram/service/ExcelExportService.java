package com.Megatram.Megatram.service;

import com.Megatram.Megatram.Dto.InventaireResponseDto;
import com.Megatram.Megatram.Dto.LigneResponseDto;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

@Service
public class ExcelExportService {

    public ByteArrayInputStream generateInventaireExcel(InventaireResponseDto inventaireDto) throws IOException {
        try (Workbook workbook = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream();) {

            Sheet sheet = workbook.createSheet("Inventaire N°" + inventaireDto.getInventaireId());

            // --- MODIFICATION 1 : Couleur du texte des en-têtes ---
            // Le texte des titres sera maintenant en noir.
            Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerFont.setColor(IndexedColors.BLACK.getIndex()); // Changé de BLUE à BLACK
            CellStyle headerCellStyle = workbook.createCellStyle();
            headerCellStyle.setFont(headerFont);

            // --- MODIFICATION 2 : Définition des nouvelles colonnes ---
            // "Ref" a été ajoutée et les colonnes "Qté Avant" ont été supprimées.
            String[] headers = {
                "Ref", "Produit", "Lieu de Stock",
                "Qté Scannée (Cartons)", "Qté Scannée (Unités)",
                "Écart (Cartons)", "Écart (Unités)"
            };
            Row headerRow = sheet.createRow(0);
            for (int col = 0; col < headers.length; col++) {
                Cell cell = headerRow.createCell(col);
                cell.setCellValue(headers[col]);
                cell.setCellStyle(headerCellStyle);
            }

            // --- MODIFICATION 3 : Remplissage des données avec la nouvelle structure ---
            int rowIdx = 1;
            for (LigneResponseDto ligne : inventaireDto.getLignes()) {
                Row row = sheet.createRow(rowIdx++);

                // IMPORTANT : Assurez-vous d'avoir une méthode getRefProduit() dans votre LigneResponseDto
                row.createCell(0).setCellValue(ligne.getRef());
                row.createCell(1).setCellValue(ligne.getNomProduit());
                row.createCell(2).setCellValue(ligne.getLieuStockNom());
                
                // Les colonnes "Qté Avant" ont été retirées, on passe directement à "Qté Scannée".
                row.createCell(3).setCellValue(ligne.getQteScanneCartons());
                row.createCell(4).setCellValue(ligne.getQteScanneUnitesRestantes());
                row.createCell(5).setCellValue(ligne.getEcartCartons() != null ? ligne.getEcartCartons() : 0);
                row.createCell(6).setCellValue(ligne.getEcartUnites() != null ? ligne.getEcartUnites() : 0);
            }

            // Ajuster automatiquement la largeur des colonnes au contenu
            for (int i = 0; i < headers.length; i++) {
                sheet.autoSizeColumn(i);
            }

            workbook.write(out);
            return new ByteArrayInputStream(out.toByteArray());
        }
    }
}