package com.psychologie.util;

import com.psychologie.model.ActiviteProgramme;
import com.psychologie.model.Avis;
import com.psychologie.model.ProgrammeBienEtre;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDType1Font;

import java.awt.Color;
import java.io.File;
import java.io.IOException;
import java.sql.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * Generates a PDF report for a ProgrammeBienEtre,
 * including its activities and avis (reviews).
 */
public class PdfExportService {

    private static final float MARGIN        = 50f;
    private static final float PAGE_WIDTH    = PDRectangle.A4.getWidth();
    private static final float PAGE_HEIGHT   = PDRectangle.A4.getHeight();
    private static final float CONTENT_WIDTH = PAGE_WIDTH - 2 * MARGIN;

    // Brand colours
    private static final Color COLOR_ACCENT  = new Color(42, 111, 91);   // #2a6f5b
    private static final Color COLOR_MUTED   = new Color(90, 101, 96);   // #5a6560
    private static final Color COLOR_TEXT    = new Color(28, 40, 36);    // #1c2824
    private static final Color COLOR_LIGHT   = new Color(230, 240, 236); // #e6f0ec
    private static final Color COLOR_GOLD    = new Color(245, 166, 35);  // star gold

    // ─────────────────────────────────────────────
    //  Public entry point
    // ─────────────────────────────────────────────

    /**
     * Builds the PDF and saves it to {@code destFile}.
     *
     * @param programme the programme to export
     * @param destFile  target file (e.g. chosen via FileChooser)
     */
    public static void export(ProgrammeBienEtre programme, File destFile) throws IOException {
        List<ActiviteProgramme> activities = loadActivities(programme.getId());
        List<AvisRow>           avisList   = loadAvis(programme.getId());

        try (PDDocument doc = new PDDocument()) {
            PageWriter pw = new PageWriter(doc);

            // ── Header ──────────────────────────────────────────
            pw.fillRect(0, PAGE_HEIGHT - 80, PAGE_WIDTH, 80, COLOR_ACCENT);
            pw.text("PROGRAMME BIEN-ÊTRE", PDType1Font.HELVETICA_BOLD, 18,
                    MARGIN, PAGE_HEIGHT - 35, Color.WHITE);
            pw.text("Psychologie App", PDType1Font.HELVETICA, 11,
                    MARGIN, PAGE_HEIGHT - 55, new Color(200, 230, 220));

            String today = LocalDate.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
            pw.textRight("Généré le " + today, PDType1Font.HELVETICA, 10,
                    PAGE_WIDTH - MARGIN, PAGE_HEIGHT - 55, new Color(200, 230, 220));

            pw.moveTo(PAGE_HEIGHT - 100);

            // ── Programme info card ──────────────────────────────
            pw.sectionTitle("Informations du programme");
            pw.infoRow("Nom",        programme.getNom());
            pw.infoRow("Psychologue", psyName(programme));
            pw.infoRow("Objectif",   safe(programme.getObjectif()));
            pw.infoRow("Durée",      programme.getDuree() + " jours");
            pw.infoRow("Difficulté", safe(programme.getNiveauDifficulte()));
            pw.infoRow("Statut",     safe(programme.getStatut()));
            pw.gap(10);

            // ── Activities ───────────────────────────────────────
            pw.sectionTitle("Activités (" + activities.size() + ")");
            if (activities.isEmpty()) {
                pw.bodyText("Aucune activité pour ce programme.", COLOR_MUTED);
            } else {
                // Table header
                pw.tableHeader(new String[]{"Jour", "Heure", "Titre", "Durée", "Type"},
                               new float[]{40, 60, 200, 60, 100});
                for (ActiviteProgramme a : activities) {
                    String heure = a.getHeureDebut() != null
                            ? a.getHeureDebut().format(DateTimeFormatter.ofPattern("HH:mm")) : "-";
                    pw.tableRow(
                            new String[]{
                                String.valueOf(a.getJour()),
                                heure,
                                safe(a.getTitre()),
                                a.getDureeMinutes() + " min",
                                safe(a.getTypeActivite())
                            },
                            new float[]{40, 60, 200, 60, 100}
                    );
                }
            }
            pw.gap(10);

            // ── Avis ─────────────────────────────────────────────
            pw.sectionTitle("Avis (" + avisList.size() + ")");
            if (avisList.isEmpty()) {
                pw.bodyText("Aucun avis pour ce programme.", COLOR_MUTED);
            } else {
                for (AvisRow avis : avisList) {
                    pw.ensureSpace(60);
                    pw.avisCard(avis);
                }
            }

            pw.finish();
            doc.save(destFile);
        }
    }

    // ─────────────────────────────────────────────
    //  DB helpers
    // ─────────────────────────────────────────────

    private static List<ActiviteProgramme> loadActivities(int programId) {
        List<ActiviteProgramme> list = new ArrayList<>();
        String q = "SELECT * FROM activite_programme WHERE idProgramme = ? ORDER BY jour, heureDebut";
        try (Connection c = DatabaseConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(q)) {
            ps.setInt(1, programId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                ActiviteProgramme a = new ActiviteProgramme();
                a.setJour(rs.getInt("jour"));
                Time t = rs.getTime("heureDebut");
                a.setHeureDebut(t != null ? t.toLocalTime() : java.time.LocalTime.of(9, 0));
                a.setTitre(rs.getString("titre"));
                a.setDescription(rs.getString("description"));
                a.setDureeMinutes(rs.getInt("dureeMinutes"));
                a.setTypeActivite(rs.getString("typeActivite"));
                list.add(a);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    private static List<AvisRow> loadAvis(int programId) {
        List<AvisRow> list = new ArrayList<>();
        String q = "SELECT a.note, a.commentaire, a.dateAvis, u.prenom, u.nom " +
                   "FROM avis a LEFT JOIN users u ON a.psychologue_id_user = u.id_user " +
                   "WHERE a.idProgramme = ? ORDER BY a.dateAvis DESC";
        try (Connection c = DatabaseConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(q)) {
            ps.setInt(1, programId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                AvisRow row = new AvisRow();
                row.note       = rs.getInt("note");
                row.commentaire = safe(rs.getString("commentaire"));
                java.sql.Date d = rs.getDate("dateAvis");
                row.date       = d != null ? d.toLocalDate().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")) : "";
                row.auteur     = (safe(rs.getString("prenom")) + " " + safe(rs.getString("nom"))).trim();
                list.add(row);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    // ─────────────────────────────────────────────
    //  Helpers
    // ─────────────────────────────────────────────

    private static String safe(String s)  { return s == null ? "" : s; }
    private static String psyName(ProgrammeBienEtre p) {
        if (p.getPsychologue() == null) return "";
        return "Dr. " + safe(p.getPsychologue().getPrenom()) + " " + safe(p.getPsychologue().getNom());
    }

    // ─────────────────────────────────────────────
    //  Inner data class
    // ─────────────────────────────────────────────

    private static class AvisRow {
        int    note;
        String commentaire;
        String date;
        String auteur;
    }

    // ─────────────────────────────────────────────
    //  Page writer — manages cursor & page breaks
    // ─────────────────────────────────────────────

    private static class PageWriter {
        private final PDDocument doc;
        private PDPage           page;
        private PDPageContentStream cs;
        private float              y;          // current Y cursor (top-down)
        private boolean            odd = true; // for alternating table rows

        PageWriter(PDDocument doc) throws IOException {
            this.doc = doc;
            newPage();
        }

        // ── Page management ─────────────────────────────────

        private void newPage() throws IOException {
            if (cs != null) cs.close();
            page = new PDPage(PDRectangle.A4);
            doc.addPage(page);
            cs = new PDPageContentStream(doc, page);
            y  = PAGE_HEIGHT - MARGIN;
        }

        void moveTo(float newY) { y = newY; }

        void ensureSpace(float needed) throws IOException {
            if (y - needed < MARGIN + 20) newPage();
        }

        void finish() throws IOException { if (cs != null) cs.close(); }

        // ── Drawing primitives ───────────────────────────────

        void fillRect(float x, float rectY, float w, float h, Color color) throws IOException {
            cs.setNonStrokingColor(color);
            cs.addRect(x, rectY, w, h);
            cs.fill();
            cs.setNonStrokingColor(Color.BLACK);
        }

        void text(String txt, PDType1Font font, float size, float x, float textY, Color color) throws IOException {
            cs.beginText();
            cs.setFont(font, size);
            cs.setNonStrokingColor(color);
            cs.newLineAtOffset(x, textY);
            cs.showText(sanitize(txt));
            cs.endText();
            cs.setNonStrokingColor(Color.BLACK);
        }

        void textRight(String txt, PDType1Font font, float size, float rightX, float textY, Color color) throws IOException {
            float w = font.getStringWidth(sanitize(txt)) / 1000 * size;
            text(txt, font, size, rightX - w, textY, color);
        }

        // ── High-level layout helpers ────────────────────────

        void gap(float px) { y -= px; }

        void sectionTitle(String title) throws IOException {
            ensureSpace(40);
            y -= 8;
            // Accent bar
            fillRect(MARGIN, y - 4, CONTENT_WIDTH, 28, COLOR_ACCENT);
            text(title, PDType1Font.HELVETICA_BOLD, 12, MARGIN + 8, y + 8, Color.WHITE);
            y -= 36;
        }

        void infoRow(String label, String value) throws IOException {
            ensureSpace(22);
            text(label + ":", PDType1Font.HELVETICA_BOLD, 10, MARGIN, y, COLOR_MUTED);
            text(value,        PDType1Font.HELVETICA,      10, MARGIN + 100, y, COLOR_TEXT);
            y -= 18;
        }

        void bodyText(String txt, Color color) throws IOException {
            ensureSpace(20);
            text(txt, PDType1Font.HELVETICA_OBLIQUE, 10, MARGIN, y, color);
            y -= 18;
        }

        void tableHeader(String[] cols, float[] widths) throws IOException {
            ensureSpace(24);
            fillRect(MARGIN, y - 4, CONTENT_WIDTH, 22, new Color(42, 111, 91, 200));
            float x = MARGIN + 6;
            for (int i = 0; i < cols.length; i++) {
                text(cols[i], PDType1Font.HELVETICA_BOLD, 9, x, y + 4, Color.WHITE);
                x += widths[i];
            }
            y -= 26;
            odd = true;
        }

        void tableRow(String[] cols, float[] widths) throws IOException {
            ensureSpace(20);
            Color bg = odd ? new Color(249, 249, 249) : Color.WHITE;
            fillRect(MARGIN, y - 4, CONTENT_WIDTH, 18, bg);
            float x = MARGIN + 6;
            for (int i = 0; i < cols.length; i++) {
                // Truncate long text to fit column
                String val = truncate(cols[i], widths[i] - 8, PDType1Font.HELVETICA, 9);
                text(val, PDType1Font.HELVETICA, 9, x, y + 2, COLOR_TEXT);
                x += widths[i];
            }
            y -= 20;
            odd = !odd;
        }

        void avisCard(AvisRow avis) throws IOException {
            ensureSpace(55);
            // Card background
            fillRect(MARGIN, y - 44, CONTENT_WIDTH, 50, new Color(250, 250, 250));
            // Left border accent
            fillRect(MARGIN, y - 44, 4, 50, COLOR_ACCENT);

            // Author + date
            text(avis.auteur.isBlank() ? "Anonyme" : avis.auteur,
                 PDType1Font.HELVETICA_BOLD, 11, MARGIN + 12, y - 4, COLOR_TEXT);
            textRight(avis.date, PDType1Font.HELVETICA, 9,
                      PAGE_WIDTH - MARGIN, y - 4, COLOR_MUTED);

            // Stars
            drawStars(avis.note, MARGIN + 12, y - 20);

            // Comment
            if (!avis.commentaire.isBlank()) {
                String comment = truncate(avis.commentaire, CONTENT_WIDTH - 20, PDType1Font.HELVETICA, 9);
                text(comment, PDType1Font.HELVETICA, 9, MARGIN + 12, y - 36, COLOR_MUTED);
            }

            y -= 58;
        }

        private void drawStars(int note, float x, float starY) throws IOException {
            for (int i = 1; i <= 5; i++) {
                Color c = i <= note ? COLOR_GOLD : new Color(200, 200, 200);
                text(i <= note ? "★" : "☆", PDType1Font.HELVETICA, 12, x + (i - 1) * 14, starY, c);
            }
        }

        // ── Utility ──────────────────────────────────────────

        /** Strip characters PDFBox's built-in fonts can't encode. */
        private String sanitize(String s) {
            if (s == null) return "";
            return s.replaceAll("[^\u0000-\u00FF]", "?");
        }

        private String truncate(String s, float maxWidth, PDType1Font font, float size) {
            if (s == null) return "";
            try {
                String clean = sanitize(s);
                while (!clean.isEmpty()
                        && font.getStringWidth(clean) / 1000 * size > maxWidth) {
                    clean = clean.substring(0, clean.length() - 1);
                }
                return clean;
            } catch (IOException e) {
                return s.length() > 40 ? s.substring(0, 40) : s;
            }
        }
    }
}
