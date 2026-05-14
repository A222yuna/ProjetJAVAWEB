package com.psychologie.util;

import org.apache.poi.ss.usermodel.BorderStyle;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.*;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.sql.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Generates an .xlsx statistics report for a psychologue.
 * Sheet 1 – Summary counters
 * Sheet 2 – All appointments list
 * Sheet 3 – Monthly breakdown
 */
public class ExcelExportService {

    // Brand colour: #2a6f5b  →  RGB 42,111,91
    private static final byte[] ACCENT_RGB   = {42, 111, 91};
    private static final byte[] HEADER_RGB   = {28,  40,  36};
    private static final byte[] ALT_ROW_RGB  = {(byte)230, (byte)240, (byte)236};
    private static final byte[] WHITE_RGB    = {(byte)255, (byte)255, (byte)255};

    public static void export(int psyId, String psyName, File dest) throws IOException, SQLException {
        try (XSSFWorkbook wb = new XSSFWorkbook()) {

            // ── Styles ────────────────────────────────────────────
            CellStyle titleStyle   = makeTitleStyle(wb);
            CellStyle headerStyle  = makeHeaderStyle(wb);
            CellStyle dataStyle    = makeDataStyle(wb, WHITE_RGB);
            CellStyle altStyle     = makeDataStyle(wb, ALT_ROW_RGB);
            CellStyle boldStyle    = makeBoldStyle(wb);
            CellStyle accentStyle  = makeAccentStyle(wb);

            // ── Sheet 1: Summary ──────────────────────────────────
            XSSFSheet summary = wb.createSheet("Résumé");
            summary.setColumnWidth(0, 7000);
            summary.setColumnWidth(1, 5000);

            addMergedTitle(summary, titleStyle,
                "Statistiques – " + psyName,
                "Généré le " + LocalDate.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));

            Map<String, Integer> counts = loadStatusCounts(psyId);
            int total = counts.values().stream().mapToInt(Integer::intValue).sum();

            addSummaryRow(summary, headerStyle, "Statut", "Nombre", 3);
            addSummaryRow(summary, boldStyle,   "TOTAL RDV", String.valueOf(total), 4);

            String[] statuses = {"SCHEDULED","CONFIRMED","PAID","COMPLETED","CANCELLED"};
            String[] labels   = {"En attente","Confirmés","Payés","Terminés","Annulés"};
            boolean alt = false;
            int row = 5;
            for (int i = 0; i < statuses.length; i++) {
                addSummaryRow(summary, alt ? altStyle : dataStyle,
                    labels[i], String.valueOf(counts.getOrDefault(statuses[i], 0)), row++);
                alt = !alt;
            }

            // ── Sheet 2: Appointments list ────────────────────────
            XSSFSheet appts = wb.createSheet("Rendez-vous");
            int[] colWidths = {3000, 6000, 6000, 5000, 5000, 5000};
            for (int i = 0; i < colWidths.length; i++) appts.setColumnWidth(i, colWidths[i]);

            Row apptHeader = appts.createRow(0);
            String[] apptCols = {"#","Patient","Date créée","Jour","Période","Statut"};
            for (int i = 0; i < apptCols.length; i++) {
                Cell c = apptHeader.createCell(i);
                c.setCellValue(apptCols[i]);
                c.setCellStyle(headerStyle);
            }

            loadAppointments(psyId, appts, dataStyle, altStyle);

            // ── Sheet 3: Monthly breakdown ────────────────────────
            XSSFSheet monthly = wb.createSheet("Par mois");
            monthly.setColumnWidth(0, 4000);
            monthly.setColumnWidth(1, 4000);

            Row mHeader = monthly.createRow(0);
            Cell mh0 = mHeader.createCell(0); mh0.setCellValue("Mois");    mh0.setCellStyle(headerStyle);
            Cell mh1 = mHeader.createCell(1); mh1.setCellValue("Nb RDV");  mh1.setCellStyle(headerStyle);

            loadMonthly(psyId, monthly, dataStyle, altStyle);

            // ── Write file ────────────────────────────────────────
            try (FileOutputStream fos = new FileOutputStream(dest)) {
                wb.write(fos);
            }
        }
    }

    // ─────────────────────────────────────────────
    //  DB helpers
    // ─────────────────────────────────────────────

    private static Map<String, Integer> loadStatusCounts(int psyId) throws SQLException {
        Map<String, Integer> map = new LinkedHashMap<>();
        String q = "SELECT a.status, COUNT(*) n " +
                   "FROM appointments a JOIN psychologue_plans p ON a.plan_id=p.id " +
                   "WHERE p.psychologue_id_user=? GROUP BY a.status";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(q)) {
            ps.setInt(1, psyId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) map.put(rs.getString("status").toUpperCase(), rs.getInt("n"));
        }
        return map;
    }

    private static void loadAppointments(int psyId, Sheet sheet,
                                         CellStyle even, CellStyle odd) throws SQLException {
        String q = "SELECT a.id, a.status, a.created_at, p.day_of_week, p.period, " +
                   "       u.prenom, u.nom " +
                   "FROM appointments a " +
                   "JOIN psychologue_plans p ON a.plan_id=p.id " +
                   "JOIN users u ON a.patient_id_user=u.id_user " +
                   "WHERE p.psychologue_id_user=? ORDER BY a.created_at DESC";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(q)) {
            ps.setInt(1, psyId);
            ResultSet rs = ps.executeQuery();
            int rowNum = 1;
            while (rs.next()) {
                Row row = sheet.createRow(rowNum);
                CellStyle cs = (rowNum % 2 == 0) ? even : odd;
                setCell(row, 0, String.valueOf(rs.getInt("id")), cs);
                setCell(row, 1, rs.getString("prenom") + " " + rs.getString("nom"), cs);
                Timestamp ts = rs.getTimestamp("created_at");
                setCell(row, 2, ts != null ? ts.toLocalDateTime()
                        .format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")) : "", cs);
                setCell(row, 3, rs.getString("day_of_week"), cs);
                setCell(row, 4, rs.getString("period"), cs);
                setCell(row, 5, rs.getString("status"), cs);
                rowNum++;
            }
        }
    }

    private static void loadMonthly(int psyId, Sheet sheet,
                                    CellStyle even, CellStyle odd) throws SQLException {
        // Pre-fill 12 months
        LocalDate start = LocalDate.now().minusMonths(11).withDayOfMonth(1);
        Map<String, Integer> monthly = new LinkedHashMap<>();
        for (int i = 0; i < 12; i++) {
            monthly.put(start.plusMonths(i).format(DateTimeFormatter.ofPattern("yyyy-MM")), 0);
        }

        String q = "SELECT DATE_FORMAT(a.created_at,'%Y-%m') m, COUNT(*) n " +
                   "FROM appointments a JOIN psychologue_plans p ON a.plan_id=p.id " +
                   "WHERE p.psychologue_id_user=? AND a.created_at>=? " +
                   "GROUP BY m ORDER BY m";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(q)) {
            ps.setInt(1, psyId);
            ps.setDate(2, Date.valueOf(start));
            ResultSet rs = ps.executeQuery();
            while (rs.next()) monthly.put(rs.getString("m"), rs.getInt("n"));
        }

        int rowNum = 1;
        for (Map.Entry<String, Integer> e : monthly.entrySet()) {
            Row row = sheet.createRow(rowNum);
            CellStyle cs = (rowNum % 2 == 0) ? even : odd;
            setCell(row, 0, e.getKey(), cs);
            Cell c = row.createCell(1);
            c.setCellValue(e.getValue());
            c.setCellStyle(cs);
            rowNum++;
        }
    }

    // ─────────────────────────────────────────────
    //  Layout helpers
    // ─────────────────────────────────────────────

    private static void addMergedTitle(XSSFSheet sheet, CellStyle style,
                                       String line1, String line2) {
        Row r0 = sheet.createRow(0);
        Cell c0 = r0.createCell(0);
        c0.setCellValue(line1);
        c0.setCellStyle(style);
        sheet.addMergedRegion(new CellRangeAddress(0, 0, 0, 1));

        Row r1 = sheet.createRow(1);
        Cell c1 = r1.createCell(0);
        c1.setCellValue(line2);
        sheet.addMergedRegion(new CellRangeAddress(1, 1, 0, 1));
    }

    private static void addSummaryRow(Sheet sheet, CellStyle style,
                                      String label, String value, int rowNum) {
        Row row = sheet.createRow(rowNum);
        Cell c0 = row.createCell(0); c0.setCellValue(label); c0.setCellStyle(style);
        Cell c1 = row.createCell(1); c1.setCellValue(value); c1.setCellStyle(style);
    }

    private static void setCell(Row row, int col, String value, CellStyle style) {
        Cell c = row.createCell(col);
        c.setCellValue(value);
        c.setCellStyle(style);
    }

    // ─────────────────────────────────────────────
    //  Style factories
    // ─────────────────────────────────────────────

    private static CellStyle makeTitleStyle(XSSFWorkbook wb) {
        XSSFCellStyle s = wb.createCellStyle();
        XSSFFont f = wb.createFont();
        f.setBold(true);
        f.setFontHeightInPoints((short) 16);
        f.setColor(new XSSFColor(ACCENT_RGB, null));
        s.setFont(f);
        return s;
    }

    private static CellStyle makeHeaderStyle(XSSFWorkbook wb) {
        XSSFCellStyle s = wb.createCellStyle();
        s.setFillForegroundColor(new XSSFColor(HEADER_RGB, null));
        s.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        XSSFFont f = wb.createFont();
        f.setBold(true);
        f.setColor(new XSSFColor(WHITE_RGB, null));
        s.setFont(f);
        s.setBorderBottom(BorderStyle.THIN);
        s.setAlignment(HorizontalAlignment.LEFT);
        return s;
    }

    private static CellStyle makeDataStyle(XSSFWorkbook wb, byte[] bgRgb) {
        XSSFCellStyle s = wb.createCellStyle();
        s.setFillForegroundColor(new XSSFColor(bgRgb, null));
        s.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        s.setBorderBottom(BorderStyle.THIN);
        s.setBorderTop(BorderStyle.THIN);
        s.setBottomBorderColor(new XSSFColor(new byte[]{(byte)220,(byte)220,(byte)220}, null));
        return s;
    }

    private static CellStyle makeBoldStyle(XSSFWorkbook wb) {
        XSSFCellStyle s = wb.createCellStyle();
        XSSFFont f = wb.createFont();
        f.setBold(true);
        f.setColor(new XSSFColor(ACCENT_RGB, null));
        s.setFont(f);
        return s;
    }

    private static CellStyle makeAccentStyle(XSSFWorkbook wb) {
        XSSFCellStyle s = wb.createCellStyle();
        s.setFillForegroundColor(new XSSFColor(ACCENT_RGB, null));
        s.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        XSSFFont f = wb.createFont();
        f.setColor(new XSSFColor(WHITE_RGB, null));
        s.setFont(f);
        return s;
    }
}
