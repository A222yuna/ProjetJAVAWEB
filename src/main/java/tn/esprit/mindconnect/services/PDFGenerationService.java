package tn.esprit.mindconnect.services;

import tn.esprit.mindconnect.entities.ActiviteProgramme;
import tn.esprit.mindconnect.entities.ProgrammeBienEtre;

import com.itextpdf.io.font.constants.StandardFonts;
import com.itextpdf.kernel.colors.Color;
import com.itextpdf.kernel.colors.ColorConstants;
import com.itextpdf.kernel.colors.DeviceRgb;
import com.itextpdf.kernel.font.PdfFont;
import com.itextpdf.kernel.font.PdfFontFactory;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.element.Text;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.UnitValue;
import com.itextpdf.layout.properties.VerticalAlignment;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.awt.Desktop;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Service pour la génération de PDFs professionnels pour les programmes de bien-être.
 * Crée des PDFs élégants avec couleurs, tableaux et mise en forme professionnelle.
 */
public class PDFGenerationService {
    
    // Couleurs MindConnect
    private static final Color PRIMARY_GREEN = new DeviceRgb(124, 154, 133);
    private static final Color SECONDARY_GOLD = new DeviceRgb(201, 169, 110);
    private static final Color BACKGROUND_BEIGE = new DeviceRgb(247, 244, 238);
    private static final Color TEXT_DARK = new DeviceRgb(42, 42, 42);
    private static final Color TEXT_LIGHT = new DeviceRgb(139, 134, 128);
    
    /**
     * Génère un PDF professionnel pour un programme de bien-être.
     */
    public byte[] generateProgramPDF(ProgrammeBienEtre programme, List<ActiviteProgramme> activities) {
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            PdfWriter writer = new PdfWriter(baos);
            PdfDocument pdfDoc = new PdfDocument(writer);
            Document document = new Document(pdfDoc);
            
            // Polices with exception handling
            PdfFont titleFont, headerFont, bodyFont;
            try {
                titleFont = PdfFontFactory.createFont(StandardFonts.HELVETICA_BOLD);
                headerFont = PdfFontFactory.createFont(StandardFonts.HELVETICA_BOLD);
                bodyFont = PdfFontFactory.createFont(StandardFonts.HELVETICA);
            } catch (Exception e) {
                // Fallback to default fonts if Helvetica fails
                titleFont = PdfFontFactory.createFont(StandardFonts.COURIER_BOLD);
                headerFont = PdfFontFactory.createFont(StandardFonts.COURIER_BOLD);
                bodyFont = PdfFontFactory.createFont(StandardFonts.COURIER);
                System.out.println("⚠️ Using fallback fonts due to: " + e.getMessage());
            }
            
            // En-tête
            addHeader(document, programme, titleFont);
            
            // Informations du programme
            addProgramInfo(document, programme, headerFont, bodyFont);
            
            // Tableau des activités
            addActivitiesTable(document, activities, headerFont, bodyFont);
            
            // Conseils et footer
            addTipsAndFooter(document, titleFont, bodyFont);
            
            document.close();
            
            System.out.println("✅ PDF professionnel généré avec succès pour: " + programme.getNom());
            return baos.toByteArray();
            
        } catch (Exception e) {
            System.err.println("❌ Erreur lors de la génération du PDF: " + e.getMessage());
            e.printStackTrace();
            return createErrorPDF();
        }
    }
    
    /**
     * Ajoute l'en-tête professionnel au PDF.
     */
    private void addHeader(Document document, ProgrammeBienEtre programme, PdfFont titleFont) {
        // Fond coloré pour l'en-tête
        Table headerTable = new Table(UnitValue.createPercentArray(new float[]{100}));
        headerTable.setWidth(UnitValue.createPercentValue(100));
        
        Cell headerCell = new Cell();
        headerCell.setBackgroundColor(PRIMARY_GREEN);
        headerCell.setPadding(20);
        headerCell.setBorder(null);
        
        Paragraph title = new Paragraph("MINDCONNECT")
            .setFont(titleFont)
            .setFontColor(ColorConstants.WHITE)
            .setFontSize(28)
            .setTextAlignment(TextAlignment.CENTER)
            .setMarginBottom(5);
        
        Paragraph subtitle = new Paragraph("Programme de Bien-Être Personnalisé")
            .setFont(titleFont)
            .setFontColor(ColorConstants.WHITE)
            .setFontSize(16)
            .setTextAlignment(TextAlignment.CENTER);
        
        headerCell.add(title);
        headerCell.add(subtitle);
        headerTable.addCell(headerCell);
        
        document.add(headerTable);
        
        // Ligne de séparation
        document.add(new Paragraph("\n"));
    }
    
    /**
     * Ajoute les informations du programme dans un tableau stylisé.
     */
    private void addProgramInfo(Document document, ProgrammeBienEtre programme, PdfFont headerFont, PdfFont bodyFont) {
        Paragraph infoTitle = new Paragraph("📋 INFORMATIONS DU PROGRAMME")
            .setFont(headerFont)
            .setFontColor(PRIMARY_GREEN)
            .setFontSize(18)
            .setMarginBottom(10);
        document.add(infoTitle);
        
        Table infoTable = new Table(UnitValue.createPercentArray(new float[]{30, 70}));
        infoTable.setWidth(UnitValue.createPercentValue(100));
        infoTable.setMarginBottom(20);
        
        // Ligne Nom
        addInfoRow(infoTable, "Nom du programme", programme.getNom(), headerFont, bodyFont);
        
        // Ligne Objectif
        addInfoRow(infoTable, "Objectif", programme.getObjectif(), headerFont, bodyFont);
        
        // Ligne Durée
        addInfoRow(infoTable, "Durée", programme.getDuree() + " jours", headerFont, bodyFont);
        
        // Ligne Niveau
        addInfoRow(infoTable, "Niveau", programme.getNiveauDifficulte(), headerFont, bodyFont);
        
        // Ligne Statut
        addInfoRow(infoTable, "Statut", programme.getStatut(), headerFont, bodyFont);
        
        document.add(infoTable);
    }
    
    /**
     * Ajoute une ligne d'information au tableau.
     */
    private void addInfoRow(Table table, String label, String value, PdfFont headerFont, PdfFont bodyFont) {
        // Cellule du label
        Cell labelCell = new Cell();
        labelCell.setBackgroundColor(BACKGROUND_BEIGE);
        labelCell.setPadding(12);
        labelCell.setBorder(null);
        
        Paragraph labelPara = new Paragraph(label)
            .setFont(headerFont)
            .setFontColor(PRIMARY_GREEN)
            .setFontSize(12);
        labelCell.add(labelPara);
        
        // Cellule de la valeur
        Cell valueCell = new Cell();
        valueCell.setPadding(12);
        valueCell.setBorder(null);
        
        Paragraph valuePara = new Paragraph(value)
            .setFont(bodyFont)
            .setFontColor(TEXT_DARK)
            .setFontSize(12);
        valueCell.add(valuePara);
        
        table.addCell(labelCell);
        table.addCell(valueCell);
    }
    
    /**
     * Ajoute le tableau des activités organisées par jour.
     */
    private void addActivitiesTable(Document document, List<ActiviteProgramme> activities, PdfFont headerFont, PdfFont bodyFont) {
        Paragraph activitiesTitle = new Paragraph("📅 PLANNING DES ACTIVITÉS")
            .setFont(headerFont)
            .setFontColor(PRIMARY_GREEN)
            .setFontSize(18)
            .setMarginTop(20)
            .setMarginBottom(15);
        document.add(activitiesTitle);
        
        if (activities == null || activities.isEmpty()) {
            Paragraph noActivities = new Paragraph("Aucune activité définie pour ce programme.")
                .setFont(bodyFont)
                .setFontColor(TEXT_LIGHT)
                .setItalic()
                .setTextAlignment(TextAlignment.CENTER);
            document.add(noActivities);
            return;
        }
        
        // Grouper les activités par jour
        Map<Integer, List<ActiviteProgramme>> activitiesByDay = activities.stream()
            .collect(Collectors.groupingBy(ActiviteProgramme::getJour));
        
        // Créer le tableau des activités
        Table activitiesTable = new Table(UnitValue.createPercentArray(new float[]{15, 40, 15, 30}));
        activitiesTable.setWidth(UnitValue.createPercentValue(100));
        activitiesTable.setMarginBottom(20);
        
        // En-tête du tableau
        addTableHeader(activitiesTable, "Jour", "Activité", "Durée", "Heure");
        
        // Ajouter les activités
        for (Map.Entry<Integer, List<ActiviteProgramme>> entry : activitiesByDay.entrySet()) {
            int day = entry.getKey();
            List<ActiviteProgramme> dayActivities = entry.getValue();
            
            for (ActiviteProgramme activity : dayActivities) {
                addActivityRow(activitiesTable, day, activity, bodyFont);
            }
        }
        
        document.add(activitiesTable);
    }
    
    /**
     * Ajoute l'en-tête du tableau des activités.
     */
    private void addTableHeader(Table table, String... headers) {
        for (String header : headers) {
            Cell headerCell = new Cell();
            headerCell.setBackgroundColor(PRIMARY_GREEN);
            headerCell.setFontColor(ColorConstants.WHITE);
            headerCell.setPadding(10);
            headerCell.setTextAlignment(TextAlignment.CENTER);
            headerCell.setVerticalAlignment(VerticalAlignment.MIDDLE);
            
            try {
                Paragraph headerPara = new Paragraph(header)
                    .setFont(PdfFontFactory.createFont(StandardFonts.HELVETICA_BOLD))
                    .setFontSize(12);
                headerCell.add(headerPara);
            } catch (Exception e) {
                // Fallback if font creation fails
                Paragraph headerPara = new Paragraph(header)
                    .setFontSize(12);
                headerCell.add(headerPara);
            }
            
            table.addCell(headerCell);
        }
    }
    
    /**
     * Ajoute une ligne d'activité au tableau.
     */
    private void addActivityRow(Table table, int day, ActiviteProgramme activity, PdfFont bodyFont) {
        // Cellule Jour
        Cell dayCell = new Cell();
        dayCell.setBackgroundColor(BACKGROUND_BEIGE);
        dayCell.setPadding(8);
        dayCell.setTextAlignment(TextAlignment.CENTER);
        dayCell.add(new Paragraph("Jour " + day).setFont(bodyFont).setFontColor(PRIMARY_GREEN).setFontSize(11));
        table.addCell(dayCell);
        
        // Cellule Activité
        Cell activityCell = new Cell();
        activityCell.setPadding(8);
        activityCell.add(new Paragraph(activity.getTitre()).setFont(bodyFont).setFontColor(TEXT_DARK).setFontSize(11));
        table.addCell(activityCell);
        
        // Cellule Durée
        Cell durationCell = new Cell();
        durationCell.setPadding(8);
        durationCell.setTextAlignment(TextAlignment.CENTER);
        durationCell.add(new Paragraph(activity.getDureeMinutes() + " min").setFont(bodyFont).setFontColor(TEXT_LIGHT).setFontSize(11));
        table.addCell(durationCell);
        
        // Cellule Heure
        Cell timeCell = new Cell();
        timeCell.setPadding(8);
        timeCell.setTextAlignment(TextAlignment.CENTER);
        timeCell.add(new Paragraph(activity.getHeureDebut().toString().substring(0, 5)).setFont(bodyFont).setFontColor(TEXT_LIGHT).setFontSize(11));
        table.addCell(timeCell);
    }
    
    /**
     * Ajoute les conseils et le pied de page.
     */
    private void addTipsAndFooter(Document document, PdfFont titleFont, PdfFont bodyFont) {
        document.add(new Paragraph("\n"));
        
        // Section conseils
        Paragraph tipsTitle = new Paragraph("💡 CONSEILS DE RÉUSSITE")
            .setFont(titleFont)
            .setFontColor(SECONDARY_GOLD)
            .setFontSize(16)
            .setMarginBottom(10);
        document.add(tipsTitle);
        
        Table tipsTable = new Table(UnitValue.createPercentArray(new float[]{100}));
        tipsTable.setWidth(UnitValue.createPercentValue(100));
        tipsTable.setBackgroundColor(BACKGROUND_BEIGE);
        
        Cell tipsCell = new Cell();
        tipsCell.setPadding(15);
        tipsCell.setBorder(null);
        
        String[] tips = {
            "• Suivez le programme régulièrement pour de meilleurs résultats",
            "• Prenez des notes sur votre progression et vos ressentis",
            "• N'hésitez pas à adapter les horaires à votre emploi du temps",
            "• Soyez patient et bienveillant avec vous-même",
            "• Célébrez chaque petite victoire dans votre parcours"
        };
        
        for (String tip : tips) {
            Paragraph tipPara = new Paragraph(tip)
                .setFont(bodyFont)
                .setFontColor(TEXT_DARK)
                .setFontSize(11)
                .setMarginBottom(5);
            tipsCell.add(tipPara);
        }
        
        tipsTable.addCell(tipsCell);
        document.add(tipsTable);
        
        // Pied de page
        document.add(new Paragraph("\n"));
        
        Table footerTable = new Table(UnitValue.createPercentArray(new float[]{100}));
        footerTable.setWidth(UnitValue.createPercentValue(100));
        
        Cell footerCell = new Cell();
        footerCell.setBackgroundColor(PRIMARY_GREEN);
        footerCell.setPadding(15);
        footerCell.setBorder(null);
        
        Paragraph footerText = new Paragraph()
            .setFontColor(ColorConstants.WHITE)
            .setFontSize(10)
            .setTextAlignment(TextAlignment.CENTER);
        
        footerText.add(new Text("Généré le " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")) + "\n"));
        footerText.add(new Text("© 2024 MindConnect - Votre partenaire bien-être\n"));
        footerText.add(new Text("Contact: hassanjebri99@gmail.com"));
        
        footerCell.add(footerText);
        footerTable.addCell(footerCell);
        document.add(footerTable);
    }
    
    /**
     * Crée un PDF d'erreur.
     */
    private byte[] createErrorPDF() {
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            PdfWriter writer = new PdfWriter(baos);
            PdfDocument pdfDoc = new PdfDocument(writer);
            Document document = new Document(pdfDoc);
            
            PdfFont font = PdfFontFactory.createFont(StandardFonts.HELVETICA);
            
            Paragraph errorTitle = new Paragraph("❌ ERREUR DE GÉNÉRATION")
                .setFont(font)
                .setFontColor(ColorConstants.RED)
                .setFontSize(20)
                .setTextAlignment(TextAlignment.CENTER)
                .setMarginBottom(20);
            
            Paragraph errorMessage = new Paragraph("Une erreur est survenue lors de la génération du PDF. Veuillez réessayer plus tard.")
                .setFont(font)
                .setFontColor(TEXT_DARK)
                .setFontSize(12)
                .setTextAlignment(TextAlignment.CENTER);
            
            document.add(errorTitle);
            document.add(errorMessage);
            document.close();
            
            return baos.toByteArray();
        } catch (Exception e) {
            return "Erreur PDF".getBytes();
        }
    }
    
    /**
     * Sauvegarde le programme sur le disque comme fichier PDF.
     */
    public void savePDFToFile(byte[] pdfContent, String fileName) {
        try {
            File file = new File(fileName + ".pdf");
            try (FileOutputStream fos = new FileOutputStream(file)) {
                fos.write(pdfContent);
            }
            
            System.out.println("📄 Programme sauvegardé: " + file.getAbsolutePath());
            
            // Ouvrir le fichier après sauvegarde
            if (Desktop.isDesktopSupported()) {
                Desktop.getDesktop().open(file);
            }
            
        } catch (Exception e) {
            System.err.println("❌ Erreur lors de la sauvegarde du fichier: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Génère un nom de fichier pour le programme.
     */
    public String generateFileName(ProgrammeBienEtre programme) {
        String safeName = programme.getNom().replaceAll("[^a-zA-Z0-9\\s]", "").trim();
        return "MindConnect_" + safeName.replaceAll("\\s+", "_") + "_" + 
               LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
    }
}
