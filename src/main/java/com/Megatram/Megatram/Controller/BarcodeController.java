package com.Megatram.Megatram.Controller;

import com.Megatram.Megatram.Dto.BarcodePrintRequestDto;
import com.Megatram.Megatram.Entity.Produit;
import com.Megatram.Megatram.repository.ProduitRepos;
import com.Megatram.Megatram.service.BarcodeService;
import com.itextpdf.text.*;
import com.itextpdf.text.pdf.Barcode128;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.UUID;

import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.io.ByteArrayOutputStream;

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
        
        try {
            if (!Files.exists(dossierBarcodes)) {
                Files.createDirectories(dossierBarcodes);
            }
        } catch (Exception e) {
            System.err.println("Impossible de créer le dossier barcodes: " + e.getMessage());
        }
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
        
        Produit produit = produitRepository.findById(requestDto.getProduitId())
                .orElseThrow(() -> new RuntimeException("Aucun produit trouvé avec l'ID : " + requestDto.getProduitId()));
        
        try {
            Resource pdfFile = genererPdfBarcodesAvecGenerationDirecte(produit, requestDto.getQuantite());
            return ResponseEntity.ok()
                    .contentType(MediaType.APPLICATION_PDF)
                    .body(pdfFile);
        } catch (Exception e) {
            throw new RuntimeException("Erreur lors de l'impression des codes-barres", e);
        }
    }

    /**
     * NOUVELLE MÉTHODE : Génère le PDF en créant directement les codes-barres
     */
    private Resource genererPdfBarcodesAvecGenerationDirecte(Produit produit, int quantite) throws Exception {
        String codeBarre = produit.getCodeBarre();

        if (codeBarre == null || codeBarre.isBlank()) {
            throw new RuntimeException("Ce produit n'a pas de code-barres.");
        }

        Path pdfPath = dossierBarcodes.resolve("print_direct_" + produit.getId() + "_" + System.currentTimeMillis() + ".pdf");

        Document document = new Document(PageSize.A4, 36, 35, 15, 5);
        PdfWriter writer = PdfWriter.getInstance(document, Files.newOutputStream(pdfPath));
        document.open();

        int columns = 3;
        int perPage = 21;
        int count = 0;

        PdfPTable table = createNewTable(columns);
        Font petitePolice = new Font(Font.FontFamily.HELVETICA, 10);

        for (int i = 0; i < quantite; i++) {
            // Génération directe du code-barres
            Barcode128 barcode = new Barcode128();
            barcode.setCode(codeBarre);
            barcode.setSize(12f); // Augmenté pour plus de visibilité
            barcode.setBaseline(15f); // Augmenté pour plus de hauteur
            barcode.setBarHeight(25f); // Hauteur explicite des barres
            
            Image barcodeImage = barcode.createImageWithBarcode(writer.getDirectContent(), BaseColor.BLACK, BaseColor.WHITE);
            barcodeImage.scaleToFit(180f, 50f); // Augmenté la taille

            PdfPTable innerTable = new PdfPTable(1);
            innerTable.setWidthPercentage(100);

            PdfPCell imageCell = new PdfPCell(barcodeImage, true);
            imageCell.setBorder(Rectangle.NO_BORDER);
            imageCell.setHorizontalAlignment(Element.ALIGN_CENTER);
            imageCell.setPaddingBottom(4f);

            String textContent = produit.getNom();
            if (produit.getRef() != null && !produit.getRef().isBlank()) {
                textContent += " - " + produit.getRef();
            }

            Phrase phraseNom = new Phrase(textContent, petitePolice);
            Paragraph paraNom = new Paragraph(phraseNom);
            paraNom.setAlignment(Element.ALIGN_CENTER);
            
            PdfPCell nameCell = new PdfPCell();
            nameCell.setBorder(Rectangle.NO_BORDER);
            nameCell.setHorizontalAlignment(Element.ALIGN_CENTER);
            nameCell.addElement(paraNom);

            innerTable.addCell(imageCell);
            innerTable.addCell(nameCell);

            PdfPCell outerCell = new PdfPCell(innerTable);
            outerCell.setBorder(Rectangle.NO_BORDER);
            outerCell.setPadding(5f);
            outerCell.setFixedHeight(100f); // Même hauteur que la version multiple

            table.addCell(outerCell);
            count++;

            if (count % perPage == 0 && (i < quantite - 1)) {
                document.add(table);
                document.newPage();
                table = createNewTable(columns);
            }
        }

        if (count % perPage != 0) {
            document.add(table);
        }

        document.close();
        
        System.out.println("PDF généré avec génération directe. Taille: " + Files.size(pdfPath) + " octets");

        return new FileSystemResource(pdfPath.toFile());
    }

    private PdfPTable createNewTable(int columns) {
        PdfPTable table = new PdfPTable(columns);
        table.setWidthPercentage(100);
        table.setSpacingBefore(5f);
        table.setSpacingAfter(10f);
        table.getDefaultCell().setBorder(Rectangle.NO_BORDER);
        table.getDefaultCell().setHorizontalAlignment(Element.ALIGN_CENTER);
        table.getDefaultCell().setVerticalAlignment(Element.ALIGN_MIDDLE);
        return table;
    }

    /**
     * NOUVELLE VERSION : Génération multiple avec création directe des codes-barres
     */
    @Operation(summary = "Imprimer un PDF contenant les codes-barres pour plusieurs produits")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "PDF généré avec succès"),
            @ApiResponse(responseCode = "400", description = "Requête invalide ou produit non trouvé"),
            @ApiResponse(responseCode = "500", description = "Erreur lors de la génération du PDF")
    })
    @PostMapping(value = "/print-multiple", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_PDF_VALUE)
    public ResponseEntity<Resource> imprimerBarcodesMultiples(@RequestBody List<BarcodePrintRequestDto> requestList) {
        if (requestList == null || requestList.isEmpty()) {
            throw new IllegalArgumentException("La liste des produits à imprimer ne peut pas être vide.");
        }

        System.out.println("=== DÉBUT IMPRESSION MULTIPLE ===");
        System.out.println("Nombre de demandes: " + requestList.size());

        try {
            Resource pdfFile = genererPdfPourPlusieursProduits_Nouvelle(requestList);
            return ResponseEntity.ok()
                    .contentType(MediaType.APPLICATION_PDF)
                    .body(pdfFile);
        } catch (Exception e) {
            System.err.println("ERREUR FATALE: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Erreur lors de l'impression des codes-barres : " + e.getMessage(), e);
        }
    }

    /**
     * NOUVELLE MÉTHODE : Génération avec création directe des codes-barres
     */
    private Resource genererPdfPourPlusieursProduits_Nouvelle(List<BarcodePrintRequestDto> requestList) throws Exception {
        String uniquePdfName = "print_multiple_direct_" + UUID.randomUUID().toString() + ".pdf";
        Path pdfPath = dossierBarcodes.resolve(uniquePdfName);
    
        Document document = new Document(PageSize.A4, 36, 35, 15, 5);
        PdfWriter writer = PdfWriter.getInstance(document, Files.newOutputStream(pdfPath));
        document.open();
    
        int columns = 3;
        Font petitePolice = new Font(Font.FontFamily.HELVETICA, 10);
        boolean hasValidProducts = false;
    
        System.out.println("=== DÉBUT IMPRESSION MULTIPLE PAR PRODUIT ===");
        System.out.println("Nombre de demandes: " + requestList.size());
    
        for (int reqIndex = 0; reqIndex < requestList.size(); reqIndex++) {
            BarcodePrintRequestDto request = requestList.get(reqIndex);
            System.out.println("--- Traitement produit ID: " + request.getProduitId() + " (quantité: " + request.getQuantite() + ")");
    
            try {
                Produit produit = produitRepository.findById(request.getProduitId())
                        .orElseThrow(() -> new RuntimeException("Produit non trouvé avec l'ID : " + request.getProduitId()));
    
                String codeBarre = produit.getCodeBarre();
                if (codeBarre == null || codeBarre.isBlank()) {
                    System.err.println("SKIP: Pas de code-barres pour " + produit.getNom());
                    continue;
                }
    
                System.out.println("Code-barres trouvé: " + codeBarre);
                hasValidProducts = true;
    
                // Nouveau tableau pour ce produit
                PdfPTable table = createNewTable(columns);
    
                // Créer le générateur de code-barres une fois
                Barcode128 barcode = new Barcode128();
                barcode.setCode(codeBarre);
                barcode.setSize(12f);
                barcode.setBaseline(15f);
                barcode.setBarHeight(25f);
    
                // Remplir les cellules pour ce produit
                for (int i = 0; i < request.getQuantite(); i++) {
                    Image barcodeImage = barcode.createImageWithBarcode(writer.getDirectContent(), BaseColor.BLACK, BaseColor.WHITE);
                    barcodeImage.scaleToFit(180f, 50f);
    
                    PdfPCell barcodeCell = createBarcodeCell_Nouvelle(produit, barcodeImage, petitePolice);
                    table.addCell(barcodeCell);
                }
    
                // Compléter la dernière ligne si elle est incomplète
                int cellsInLastRow = request.getQuantite() % columns;
                if (cellsInLastRow != 0) {
                    int emptyCellsNeeded = columns - cellsInLastRow;
                    for (int i = 0; i < emptyCellsNeeded; i++) {
                        PdfPCell emptyCell = new PdfPCell();
                        emptyCell.setBorder(Rectangle.NO_BORDER);
                        emptyCell.setFixedHeight(100f);
                        table.addCell(emptyCell);
                    }
                }
    
                // Ajouter le tableau de ce produit dans le PDF
                document.add(table);
    
                // Saut de page sauf pour le dernier produit
                if (reqIndex < requestList.size() - 1) {
                    document.newPage();
                }
    
            } catch (Exception e) {
                System.err.println("ERREUR produit ID " + request.getProduitId() + ": " + e.getMessage());
                e.printStackTrace();
            }
        }
    
        document.close();
    
        if (!hasValidProducts) {
            throw new RuntimeException("Aucun produit valide trouvé avec des codes-barres");
        }
    
        long fileSize = Files.size(pdfPath);
        System.out.println("=== PDF GÉNÉRÉ PAR PRODUIT ===");
        System.out.println("Taille fichier: " + fileSize + " octets");
        System.out.println("Fichier: " + pdfPath.toAbsolutePath());
    
        return new FileSystemResource(pdfPath.toFile());
    }
    

    /**
     * MÉTHODE DE TEST : Génération d'un PDF simple pour diagnostic
     */
    @PostMapping(value = "/test-simple", produces = MediaType.APPLICATION_PDF_VALUE)
    public ResponseEntity<Resource> testPdfSimple() {
        try {
            String uniquePdfName = "test_simple_" + System.currentTimeMillis() + ".pdf";
            Path pdfPath = dossierBarcodes.resolve(uniquePdfName);

            Document document = new Document(PageSize.A4, 36, 35, 15, 5);
            PdfWriter writer = PdfWriter.getInstance(document, Files.newOutputStream(pdfPath));
            document.open();

            // Ajouter du texte simple
            Font font = new Font(Font.FontFamily.HELVETICA, 14, Font.BOLD);
            Paragraph titre = new Paragraph("TEST PDF - GÉNÉRATION SIMPLE", font);
            titre.setAlignment(Element.ALIGN_CENTER);
            document.add(titre);
            
            document.add(new Paragraph(" ")); // Ligne vide
            
            // Générer un code-barres de test
            Barcode128 barcode = new Barcode128();
            barcode.setCode("TEST123456");
            barcode.setSize(12f);
            barcode.setBaseline(15f);
            barcode.setBarHeight(25f);
            
            Image barcodeImage = barcode.createImageWithBarcode(writer.getDirectContent(), BaseColor.BLACK, BaseColor.WHITE);
            barcodeImage.scaleToFit(200f, 60f);
            barcodeImage.setAlignment(Element.ALIGN_CENTER);
            
            document.add(barcodeImage);
            
            document.add(new Paragraph(" ")); // Ligne vide
            document.add(new Paragraph("Code-barres: TEST123456", new Font(Font.FontFamily.HELVETICA, 12)));
            
            document.close();
            
            long fileSize = Files.size(pdfPath);
            System.out.println("PDF TEST SIMPLE - Taille: " + fileSize + " octets");
            
            return ResponseEntity.ok()
                    .contentType(MediaType.APPLICATION_PDF)
                    .body(new FileSystemResource(pdfPath.toFile()));
                    
        } catch (Exception e) {
            throw new RuntimeException("Erreur lors de la génération du PDF test", e);
        }
    }
    private PdfPCell createBarcodeCell_Nouvelle(Produit produit, Image barcodeImage, Font font) {
        PdfPTable innerTable = new PdfPTable(1);
        innerTable.setWidthPercentage(100);

        // Cellule pour l'image du code-barres
        PdfPCell imageCell = new PdfPCell(barcodeImage, true);
        imageCell.setBorder(Rectangle.NO_BORDER);
        imageCell.setHorizontalAlignment(Element.ALIGN_CENTER);
        imageCell.setPaddingBottom(1f);

        // Cellule pour le texte
        String textContent = produit.getNom();
        if (produit.getRef() != null && !produit.getRef().isBlank()) {
            textContent += " - " + produit.getRef();
        }
        
        Phrase phraseNom = new Phrase(textContent, font);
        Paragraph paraNom = new Paragraph(phraseNom);
        paraNom.setAlignment(Element.ALIGN_CENTER);
        
        PdfPCell nameCell = new PdfPCell();
        nameCell.addElement(paraNom);
        nameCell.setBorder(Rectangle.NO_BORDER);
        nameCell.setHorizontalAlignment(Element.ALIGN_CENTER);

        innerTable.addCell(imageCell);
        innerTable.addCell(nameCell);

        // Cellule externe
        PdfPCell outerCell = new PdfPCell(innerTable);
        outerCell.setBorder(Rectangle.NO_BORDER);
        outerCell.setMinimumHeight(100f); // au lieu de FixedHeight
        outerCell.setHorizontalAlignment(Element.ALIGN_CENTER);
        outerCell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        outerCell.setPadding(8f); // Plus de padding

        return outerCell;
    }
}