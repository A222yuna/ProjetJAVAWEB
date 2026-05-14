package com.psychologie.controller.admin;

import com.psychologie.controller.MainController;
import com.psychologie.util.DatabaseConnection;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.chart.PieChart;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.stage.FileChooser;
import org.apache.poi.ss.usermodel.BorderStyle;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.*;

import java.io.File;
import java.io.FileOutputStream;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.sql.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class StatsController {

    @FXML private Label    dateLabel;
    @FXML private PieChart roleChart;
    @FXML private PieChart statusChart;

    // Role colours
    private static final Map<String, String> ROLE_COLORS = new LinkedHashMap<>();
    static {
        ROLE_COLORS.put("Admin",       "#e74c3c");
        ROLE_COLORS.put("Psychologue", "#f5a623");
        ROLE_COLORS.put("Patient",     "#2a6f5b");
    }

    // Status colours
    private static final String COLOR_ACTIVE   = "#f5a623";
    private static final String COLOR_INACTIVE = "#e74c3c";

    @FXML
    public void initialize() {
        dateLabel.setText(LocalDate.now()
                .format(DateTimeFormatter.ofPattern("dd MMMM yyyy", java.util.Locale.FRENCH)));
        loadRoleStats();
        loadStatusStats();
    }

    // ── Role pie ──────────────────────────────────

    private void loadRoleStats() {
        ObservableList<PieChart.Data> data = FXCollections.observableArrayList();
        List<String> order = new ArrayList<>();

        try (Connection conn = DatabaseConnection.getConnection();
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(
                     "SELECT role, COUNT(*) n FROM users GROUP BY role ORDER BY role")) {
            while (rs.next()) {
                String role = rs.getString("role");
                data.add(new PieChart.Data(role, rs.getInt("n")));
                order.add(role);
            }
        } catch (Exception e) { e.printStackTrace(); }

        roleChart.setData(data);
        roleChart.setTitle("Utilisateurs par Rôle");
        roleChart.setAnimated(false);
        roleChart.setLabelsVisible(true);

        // Inject CSS colours by index
        StringBuilder css = new StringBuilder();
        for (int i = 0; i < order.size(); i++) {
            String hex = ROLE_COLORS.getOrDefault(order.get(i), "#aaaaaa");
            css.append(".default-color").append(i)
               .append(".chart-pie{-fx-pie-color:").append(hex).append(";}\n");
            css.append(".default-color").append(i)
               .append(".chart-legend-item-symbol{-fx-background-color:").append(hex).append(";}\n");
        }
        String encoded = URLEncoder.encode(css.toString(), StandardCharsets.UTF_8).replace("+", "%20");
        roleChart.getStylesheets().clear();
        roleChart.getStylesheets().add("data:text/css," + encoded);
    }

    // ── Status pie ────────────────────────────────

    private void loadStatusStats() {
        ObservableList<PieChart.Data> data = FXCollections.observableArrayList();
        List<String> order = new ArrayList<>();

        try (Connection conn = DatabaseConnection.getConnection();
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(
                     "SELECT est_actif, COUNT(*) n FROM users GROUP BY est_actif")) {
            while (rs.next()) {
                String label = rs.getBoolean("est_actif") ? "Actif" : "Bloqué / Inactif";
                data.add(new PieChart.Data(label, rs.getInt("n")));
                order.add(label);
            }
        } catch (Exception e) { e.printStackTrace(); }

        statusChart.setData(data);
        statusChart.setTitle("Statut des Comptes");
        statusChart.setAnimated(false);
        statusChart.setLabelsVisible(true);

        StringBuilder css = new StringBuilder();
        for (int i = 0; i < order.size(); i++) {
            String hex = order.get(i).equals("Actif") ? COLOR_ACTIVE : COLOR_INACTIVE;
            css.append(".default-color").append(i)
               .append(".chart-pie{-fx-pie-color:").append(hex).append(";}\n");
            css.append(".default-color").append(i)
               .append(".chart-legend-item-symbol{-fx-background-color:").append(hex).append(";}\n");
        }
        String encoded = URLEncoder.encode(css.toString(), StandardCharsets.UTF_8).replace("+", "%20");
        statusChart.getStylesheets().clear();
        statusChart.getStylesheets().add("data:text/css," + encoded);
    }

    // ── Excel export ──────────────────────────────

    @FXML
    public void handleExportCSV() {
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Enregistrer le rapport Excel");
        chooser.setInitialFileName("stats_admin_" +
                LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")) + ".xlsx");
        chooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Fichier Excel (*.xlsx)", "*.xlsx"));

        File dest = chooser.showSaveDialog(dateLabel.getScene().getWindow());
        if (dest == null) return;

        try (XSSFWorkbook wb = new XSSFWorkbook()) {

            // ── Styles ────────────────────────────
            byte[] accentRgb  = {28, 40, 36};
            byte[] headerRgb  = {42, 111, 91};
            byte[] whiteRgb   = {(byte)255,(byte)255,(byte)255};
            byte[] altRgb     = {(byte)230,(byte)240,(byte)236};

            XSSFCellStyle titleStyle = wb.createCellStyle();
            XSSFFont titleFont = wb.createFont();
            titleFont.setBold(true); titleFont.setFontHeightInPoints((short)16);
            titleFont.setColor(new XSSFColor(headerRgb, null));
            titleStyle.setFont(titleFont);

            XSSFCellStyle headerStyle = wb.createCellStyle();
            headerStyle.setFillForegroundColor(new XSSFColor(headerRgb, null));
            headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            XSSFFont hFont = wb.createFont(); hFont.setBold(true);
            hFont.setColor(new XSSFColor(whiteRgb, null));
            headerStyle.setFont(hFont);
            headerStyle.setAlignment(HorizontalAlignment.LEFT);

            XSSFCellStyle dataStyle = wb.createCellStyle();
            dataStyle.setFillForegroundColor(new XSSFColor(whiteRgb, null));
            dataStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);

            XSSFCellStyle altStyle = wb.createCellStyle();
            altStyle.setFillForegroundColor(new XSSFColor(altRgb, null));
            altStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);

            // ── Sheet 1: Users ────────────────────
            XSSFSheet users = wb.createSheet("Utilisateurs");
            users.setColumnWidth(0, 1000);
            users.setColumnWidth(1, 6000);
            users.setColumnWidth(2, 6000);
            users.setColumnWidth(3, 6000);
            users.setColumnWidth(4, 4000);
            users.setColumnWidth(5, 4000);
            users.setColumnWidth(6, 4000);

            Row titleRow = users.createRow(0);
            org.apache.poi.ss.usermodel.Cell tc = titleRow.createCell(0);
            tc.setCellValue("Rapport Utilisateurs – " +
                    LocalDate.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
            tc.setCellStyle(titleStyle);
            users.addMergedRegion(new CellRangeAddress(0,0,0,6));

            Row hRow = users.createRow(2);
            String[] cols = {"#","Nom","Prénom","Email","Rôle","Statut","Date inscription"};
            for (int i = 0; i < cols.length; i++) {
                org.apache.poi.ss.usermodel.Cell c = hRow.createCell(i);
                c.setCellValue(cols[i]); c.setCellStyle(headerStyle);
            }

            String q = "SELECT id_user,nom,prenom,email,role,est_actif,date_inscription " +
                       "FROM users ORDER BY id_user";
            try (Connection conn = DatabaseConnection.getConnection();
                 Statement st = conn.createStatement();
                 ResultSet rs = st.executeQuery(q)) {
                int r = 3;
                while (rs.next()) {
                    Row row = users.createRow(r);
                    XSSFCellStyle cs = (r % 2 == 0) ? dataStyle : altStyle;
                    setCell(row, 0, String.valueOf(rs.getInt("id_user")), cs);
                    setCell(row, 1, safe(rs.getString("nom")), cs);
                    setCell(row, 2, safe(rs.getString("prenom")), cs);
                    setCell(row, 3, safe(rs.getString("email")), cs);
                    setCell(row, 4, safe(rs.getString("role")), cs);
                    setCell(row, 5, rs.getBoolean("est_actif") ? "Actif" : "Inactif", cs);
                    java.sql.Date d = rs.getDate("date_inscription");
                    setCell(row, 6, d != null ? d.toLocalDate()
                            .format(DateTimeFormatter.ofPattern("dd/MM/yyyy")) : "", cs);
                    r++;
                }
            }

            // ── Sheet 2: Role summary ─────────────
            XSSFSheet roles = wb.createSheet("Par Rôle");
            roles.setColumnWidth(0, 5000); roles.setColumnWidth(1, 4000);
            Row rh = roles.createRow(0);
            org.apache.poi.ss.usermodel.Cell rh0 = rh.createCell(0); rh0.setCellValue("Rôle");    rh0.setCellStyle(headerStyle);
            org.apache.poi.ss.usermodel.Cell rh1 = rh.createCell(1); rh1.setCellValue("Nombre");  rh1.setCellStyle(headerStyle);
            try (Connection conn = DatabaseConnection.getConnection();
                 Statement st = conn.createStatement();
                 ResultSet rs = st.executeQuery(
                         "SELECT role, COUNT(*) n FROM users GROUP BY role ORDER BY role")) {
                int r = 1;
                while (rs.next()) {
                    Row row = roles.createRow(r);
                    XSSFCellStyle cs = (r % 2 == 0) ? dataStyle : altStyle;
                    setCell(row, 0, safe(rs.getString("role")), cs);
                    row.createCell(1).setCellValue(rs.getInt("n"));
                    row.getCell(1).setCellStyle(cs);
                    r++;
                }
            }

            // ── Sheet 3: Appointments summary ─────
            XSSFSheet appts = wb.createSheet("Rendez-vous");
            appts.setColumnWidth(0, 5000); appts.setColumnWidth(1, 4000);
            Row ah = appts.createRow(0);
            org.apache.poi.ss.usermodel.Cell ah0 = ah.createCell(0); ah0.setCellValue("Statut");  ah0.setCellStyle(headerStyle);
            org.apache.poi.ss.usermodel.Cell ah1 = ah.createCell(1); ah1.setCellValue("Nombre");  ah1.setCellStyle(headerStyle);
            try (Connection conn = DatabaseConnection.getConnection();
                 Statement st = conn.createStatement();
                 ResultSet rs = st.executeQuery(
                         "SELECT status, COUNT(*) n FROM appointments GROUP BY status ORDER BY status")) {
                int r = 1;
                while (rs.next()) {
                    Row row = appts.createRow(r);
                    XSSFCellStyle cs = (r % 2 == 0) ? dataStyle : altStyle;
                    setCell(row, 0, safe(rs.getString("status")), cs);
                    row.createCell(1).setCellValue(rs.getInt("n"));
                    row.getCell(1).setCellStyle(cs);
                    r++;
                }
            }

            // ── Write ─────────────────────────────
            try (FileOutputStream fos = new FileOutputStream(dest)) {
                wb.write(fos);
            }

            Alert ok = new Alert(Alert.AlertType.INFORMATION);
            ok.setTitle("Export réussi");
            ok.setHeaderText(null);
            ok.setContentText("Fichier enregistré :\n" + dest.getAbsolutePath());
            ok.showAndWait();

            try { java.awt.Desktop.getDesktop().open(dest); } catch (Exception ignored) {}

        } catch (Exception e) {
            e.printStackTrace();
            Alert err = new Alert(Alert.AlertType.ERROR);
            err.setTitle("Erreur export");
            err.setHeaderText(null);
            err.setContentText("Impossible de générer le fichier :\n" + e.getMessage());
            err.showAndWait();
        }
    }

    @FXML
    public void handleExportSheets() {
        Alert info = new Alert(Alert.AlertType.INFORMATION);
        info.setTitle("Google Sheets");
        info.setHeaderText(null);
        info.setContentText("L'export Google Sheets nécessite une connexion API.\nUtilisez l'export Excel à la place.");
        info.showAndWait();
    }

    @FXML
    public void handleBackToList() {
        MainController.getInstance().showUsers();
    }

    private void setCell(Row row, int col, String value, org.apache.poi.ss.usermodel.CellStyle style) {
        org.apache.poi.ss.usermodel.Cell c = row.createCell(col);
        c.setCellValue(value != null ? value : "");
        c.setCellStyle(style);
    }

    private String safe(String s) { return s == null ? "" : s; }
}
