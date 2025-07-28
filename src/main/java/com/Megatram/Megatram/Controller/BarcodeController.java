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
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource; // ✅ Bon import
import org.springframework.core.io.UrlResource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

@RestController
@RequestMapping("/api/barcode")
@Tag(name = "Barcode", description = "Gestion des Barcodes")
@CrossOrigin(origins = "http://localhost:3000")
@PreAuthorize("hasRole('ADMIN') ")

public class BarcodeController {

    private final ProduitRepos produitRepository;
    private final BarcodeService barcodeService;

    private final Path dossierBarcodes = Paths.get("barcodes");


    public BarcodeController(ProduitRepos produitRepository, BarcodeService barcodeService) {
        this.produitRepository = produitRepository;
        this.barcodeService = barcodeService;
    }


    @Operation(summary = "Récupérer l'image du code-barres d'un produit")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Image trouvée et retournée"),
            @ApiResponse(responseCode = "404", description = "Produit ou image du code-barres non trouvé"),
            @ApiResponse(responseCode = "500", description = "Erreur interne du serveur")
    })
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

    @Operation(summary = "Imprimer un PDF contenant les codes-barres")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "PDF généré avec succès"),
            @ApiResponse(responseCode = "403", description = "Accès refusé"),
            @ApiResponse(responseCode = "404", description = "Produit non trouvé"),
            @ApiResponse(responseCode = "500", description = "Erreur lors de l'impression des codes-barres")
    })
    @PostMapping(value = "/print", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_PDF_VALUE)
    public ResponseEntity<Resource> imprimerBarcodes(@RequestBody BarcodePrintRequestDto requestDto) {
        List<Produit> produits = produitRepository.findByNom(requestDto.getProduitNom());

        if (produits == null || produits.isEmpty()) {
            throw new RuntimeException("Aucun produit trouvé avec le nom : " + requestDto.getProduitNom());
        }

        Produit produit = produits.get(0); // ✅ On prend simplement le premier

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

    Document document = new Document(PageSize.A4, 36, 35, 15, 5);
    PdfWriter.getInstance(document, Files.newOutputStream(pdfPath));
    document.open();

    int columns = 3;
    int perPage = 21;
    int count = 0;

    PdfPTable table = createNewTable(columns);
    Font petitePolice = new Font(Font.FontFamily.HELVETICA, 10);

    for (int i = 0; i < quantite; i++) {
        // ✅ On recrée une nouvelle image à chaque itération
        Image barcodeImage = Image.getInstance(barcodePath.toAbsolutePath().toString());
        barcodeImage.scaleToFit(150f, 30f); // pour un bon équilibre taille/lecture

        PdfPTable innerTable = new PdfPTable(1);
        innerTable.setWidthPercentage(100);

        PdfPCell imageCell = new PdfPCell(barcodeImage, true);
        imageCell.setBorder(Rectangle.NO_BORDER);
        imageCell.setHorizontalAlignment(Element.ALIGN_CENTER);

        Phrase phraseNom = new Phrase(produit.getNom() + " - " + (i + 1), petitePolice);
        Phrase phrasePrix = new Phrase(String.format("%.2f FCFA", produit.getPrix()), petitePolice);

        Paragraph paraNom = new Paragraph(phraseNom);
        paraNom.setAlignment(Element.ALIGN_CENTER);

        Paragraph paraPrix = new Paragraph(phrasePrix);
        paraPrix.setAlignment(Element.ALIGN_CENTER);

        PdfPCell nameCell = new PdfPCell();
        nameCell.setBorder(Rectangle.NO_BORDER);
        nameCell.setHorizontalAlignment(Element.ALIGN_CENTER);
        nameCell.addElement(paraNom);
        nameCell.addElement(paraPrix);

        innerTable.addCell(imageCell);
        innerTable.addCell(nameCell);

        PdfPCell outerCell = new PdfPCell(innerTable);
       
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
            empty.setFixedHeight(140f);
            empty.setBorder(Rectangle.NO_BORDER);
            table.addCell(empty);
        }
        document.add(table);
    }

    document.close();

    return new FileSystemResource(pdfPath.toFile());
}





//
//    private PdfPTable createNewTable(int columns) {
//        PdfPTable table = new PdfPTable(columns);
//        table.setWidthPercentage(100);
//        return table;
//    }



   //  Méthode utilitaire pour créer une nouvelle table bien configurée
    private PdfPTable createNewTable(int columns) {
        PdfPTable table = new PdfPTable(columns);
        table.setWidthPercentage(100);
        table.setSpacingBefore(05f);
        table.setSpacingAfter(10f);
        table.getDefaultCell().setBorder(Rectangle.NO_BORDER);
        table.getDefaultCell().setHorizontalAlignment(Element.ALIGN_CENTER);
        table.getDefaultCell().setVerticalAlignment(Element.ALIGN_MIDDLE);
        return table;
    }






}