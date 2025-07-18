package com.Megatram.Megatram.Controller;

import com.Megatram.Megatram.Dto.BarcodePrintRequestDto;
import com.Megatram.Megatram.Entity.Produit;
import com.Megatram.Megatram.repository.ProduitRepos;
import com.Megatram.Megatram.service.BarcodeService;
import com.itextpdf.text.*;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource; // ✅ Bon import
import org.springframework.core.io.UrlResource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@RestController
@RequestMapping("/api/barcode")
@Tag(name = "Barcode", description = "Gestion des Barcodes")
@CrossOrigin(origins = "http://localhost:3000")

public class BarcodeController {

    private final ProduitRepos produitRepository;
    private final BarcodeService barcodeService;

    private final Path dossierBarcodes = Paths.get("barcodes");


    public BarcodeController(ProduitRepos produitRepository, BarcodeService barcodeService) {
        this.produitRepository = produitRepository;
        this.barcodeService = barcodeService;
    }

    @GetMapping("/{produitId}")
    public ResponseEntity<Resource> getBarcodeImage(@PathVariable Long produitId) {
        Produit produit = produitRepository.findById(produitId)
                .orElseThrow(() -> new RuntimeException("Produit non trouvé: " + produitId));

        String codeBarre = produit.getCodeBarre();
        if (codeBarre == null || codeBarre.isBlank()) {
            throw new RuntimeException("Pas de code-barres défini pour ce produit.");
        }

        Path imagePath = dossierBarcodes.resolve(codeBarre + ".png");

        if (!Files.exists(imagePath)) {
            throw new RuntimeException("Image du code-barres introuvable.");
        }

        try {
            Resource file = new UrlResource(imagePath.toUri());
            return ResponseEntity.ok()
                    .contentType(MediaType.IMAGE_PNG)
                    .body(file);
        } catch (MalformedURLException e) {
            throw new RuntimeException("Erreur de chargement de l'image du code-barres.", e);
        }
    }


    @Operation(summary = "Imprimer un PDF")
    @PostMapping(value = "/print", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_PDF_VALUE)
    public ResponseEntity<Resource> imprimerBarcodes(@RequestBody BarcodePrintRequestDto requestDto) {
        Produit produit = produitRepository.findByNom(requestDto.getProduitNom());

        if (produit == null) {
            throw new RuntimeException("Produit non trouvé : " + requestDto.getProduitNom());
        }

        try {
            Resource pdfFile = genererPdfBarcodes(produit, requestDto.getQuantite());
            return ResponseEntity.ok()
                    .contentType(MediaType.APPLICATION_PDF)
                    .body(pdfFile);
        } catch (Exception e) {
            throw new RuntimeException("Erreur lors de l'impression des codes-barres", e);
        }
    }



    private Resource genererPdfBarcodes(Produit produit, int quantite) throws Exception {
        String codeBarre = produit.getCodeBarre();

        if (codeBarre == null || codeBarre.isBlank()) {
            throw new RuntimeException("Ce produit n’a pas de code-barres.");
        }

        Path barcodePath = dossierBarcodes.resolve(codeBarre + ".png");
        if (!Files.exists(barcodePath)) {
            throw new RuntimeException("Image du code-barres introuvable.");
        }

        Path pdfPath = dossierBarcodes.resolve("print_" + produit.getId() + ".pdf");

        Document document = new Document(PageSize.A4, 36, 35, 15, 5); // marges
        PdfWriter.getInstance(document, Files.newOutputStream(pdfPath));
        document.open();

        Image barcodeImage = Image.getInstance(barcodePath.toAbsolutePath().toString());
        barcodeImage.scaleToFit(100f, 60f); // ✅ taille du code-barres

        int columns = 3;
        int perPage = 18;
        int count = 0;

        PdfPTable table = createNewTable(columns);

        for (int i = 0; i < quantite; i++) {
            // ✅ Mini-table verticale contenant image + nom
            PdfPTable innerTable = new PdfPTable(1);
            innerTable.setWidthPercentage(100);

            PdfPCell imageCell = new PdfPCell(barcodeImage, true);
            imageCell.setBorder(Rectangle.NO_BORDER);
            imageCell.setHorizontalAlignment(Element.ALIGN_CENTER);

            PdfPCell nameCell = new PdfPCell(new Phrase(produit.getNom() + " - " + (i + 1)));
            nameCell.setBorder(Rectangle.NO_BORDER);
            nameCell.setHorizontalAlignment(Element.ALIGN_CENTER);

            innerTable.addCell(imageCell);
            innerTable.addCell(nameCell);

            PdfPCell outerCell = new PdfPCell(innerTable);
            outerCell.setFixedHeight(130f);
            outerCell.setBorder(Rectangle.NO_BORDER);
            outerCell.setPadding(5f);

            table.addCell(outerCell);
            count++;

            if (count % perPage == 0) {
                document.add(table);
                document.newPage();
                table = createNewTable(columns);
            }
        }

        if (count % perPage != 0) {
            int reste = perPage - (count % perPage);
            for (int i = 0; i < reste; i++) {
                PdfPCell empty = new PdfPCell();
                empty.setFixedHeight(130f);
                empty.setBorder(Rectangle.NO_BORDER);
                table.addCell(empty);
            }
            document.add(table);
        }

        document.close();

        return new FileSystemResource(pdfPath.toFile());
    }


    // Méthode utilitaire pour créer une nouvelle table bien configurée
    private PdfPTable createNewTable(int columns) {
        PdfPTable table = new PdfPTable(columns);
        table.setWidthPercentage(100);
        table.setSpacingBefore(10f);
        table.setSpacingAfter(10f);
        table.getDefaultCell().setBorder(Rectangle.NO_BORDER);
        table.getDefaultCell().setHorizontalAlignment(Element.ALIGN_CENTER);
        table.getDefaultCell().setVerticalAlignment(Element.ALIGN_MIDDLE);
        return table;
    }






}
