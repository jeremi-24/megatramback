package com.Megatram.Megatram.service;

import com.Megatram.Megatram.Dto.InventaireResponseDto;
import com.Megatram.Megatram.Dto.LigneResponseDto;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;

@Service
public class ExcelExportService {

    public ByteArrayInputStream generateInventaireExcel(InventaireResponseDto inventaireDto) throws IOException {
        try (Workbook workbook = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream();) {

            Sheet sheet = workbook.createSheet("Inventaire N°" + inventaireDto.getInventaireId());

            // Créer le style pour les en-têtes
            Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerFont.setColor(IndexedColors.BLUE.getIndex());
            CellStyle headerCellStyle = workbook.createCellStyle();
            headerCellStyle.setFont(headerFont);

            // Créer la ligne d'en-tête
            String[] headers = {
                "Produit", "Lieu de Stock",
                "Qté Avant (Cartons)", "Qté Avant (Unités)",
                "Qté Scannée (Cartons)", "Qté Scannée (Unités)",
                "Écart (Cartons)", "Écart (Unités)"
            };
            Row headerRow = sheet.createRow(0);
            for (int col = 0; col < headers.length; col++) {
                Cell cell = headerRow.createCell(col);
                cell.setCellValue(headers[col]);
                cell.setCellStyle(headerCellStyle);
            }

            // Remplir les données
            int rowIdx = 1;
            for (LigneResponseDto ligne : inventaireDto.getLignes()) {
                Row row = sheet.createRow(rowIdx++);

                row.createCell(0).setCellValue(ligne.getNomProduit());
                row.createCell(1).setCellValue(ligne.getLieuStockNom());
                row.createCell(2).setCellValue(ligne.getQteAvantScanCartons());
                row.createCell(3).setCellValue(ligne.getQteAvantScanUnitesRestantes());
                row.createCell(4).setCellValue(ligne.getQteScanneCartons());
                row.createCell(5).setCellValue(ligne.getQteScanneUnitesRestantes());
                row.createCell(6).setCellValue(ligne.getEcartCartons() != null ? ligne.getEcartCartons() : 0);
                row.createCell(7).setCellValue(ligne.getEcartUnites() != null ? ligne.getEcartUnites() : 0);
            }
            
            // Ajuster la largeur des colonnes
           

            workbook.write(out);
            return new ByteArrayInputStream(out.toByteArray());
        }
    }
}