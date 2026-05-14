package com.psychologie.controller.admin;

import com.psychologie.controller.MainController;
import com.psychologie.model.User;
import com.psychologie.util.DatabaseConnection;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.FileChooser;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.*;

import java.io.File;
import java.io.FileOutputStream;
import java.sql.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class LoginHistoryController {

    @FXML private TextField        searchField;
    @FXML private ComboBox<String> roleFilter;
    @FXML private Label            countLabel;

    @FXML private TableView<User>              historyTable;
    @FXML private TableColumn<User, String>    userCol;
    @FXML private TableColumn<User, String>    emailCol;
    @FXML private TableColumn<User, String>    roleCol;
    @FXML private TableColumn<User, String>    loginCol;
    @FXML private TableColumn<User, String>    statusCol;

    private final ObservableList<User> allData      = FXCollections.observableArrayList();
    private final ObservableList<User> displayData  = FXCollections.observableArrayList();

    private static final DateTimeFormatter FMT =
            DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    @FXML
    public void initialize() {
        roleFilter.setItems(FXCollections.observableArrayList(
                "Tous les rôles", "Admin", "Psychologue", "Patient"));
        roleFilter.setValue("Tous les rôles");
        roleFilter.setOnAction(e -> applyFilter());
        searchField.textProperty().addListener((o, ov, nv) -> applyFilter());

        setupTable();
        loadData();
    }

    // ─────────────────────────────────────────────
    //  Table
    // ─────────────────────────────────────────────

    private void setupTable() {
        userCol.setCellValueFactory(cd ->
            new SimpleStringProperty(cd.getValue().getPrenom() + " " + cd.getValue().getNom()));

        emailCol.setCellValueFactory(cd ->
            new SimpleStringProperty(safe(cd.getValue().getEmail())));

        roleCol.setCellValueFactory(cd ->
            new SimpleStringProperty(safe(cd.getValue().getRole())));
        roleCol.setCellFactory(col -> new TableCell<>() {
            @Override protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) { setGraphic(null); return; }
                Label lbl = new Label(item.toUpperCase());
                String bg = "PATIENT".equalsIgnoreCase(item) ? "#1abc9c"
                          : "ADMIN".equalsIgnoreCase(item)   ? "#e74c3c"
                          :                                    "#3498db";
                lbl.setStyle("-fx-background-color:" + bg + "; -fx-text-fill:white;" +
                             "-fx-padding:3 10; -fx-background-radius:10;" +
                             "-fx-font-size:10px; -fx-font-weight:bold;");
                setGraphic(lbl);
            }
        });

        loginCol.setCellValueFactory(cd -> {
            if (cd.getValue().getDerniereConnexion() != null)
                return new SimpleStringProperty(
                        cd.getValue().getDerniereConnexion().format(FMT));
            return new SimpleStringProperty("Jamais connecté");
        });
        loginCol.setCellFactory(col -> new TableCell<>() {
            @Override protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) { setText(null); return; }
                setText(item);
                setStyle("Jamais connecté".equals(item)
                        ? "-fx-text-fill:#adb5bd; -fx-font-style:italic;"
                        : "-fx-text-fill:#1c2824;");
            }
        });

        statusCol.setCellValueFactory(cd ->
            new SimpleStringProperty(cd.getValue().isEstActif() ? "ACTIF" : "BLOQUÉ"));
        statusCol.setCellFactory(col -> new TableCell<>() {
            @Override protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) { setGraphic(null); return; }
                Label lbl = new Label(item);
                lbl.setStyle("ACTIF".equals(item)
                    ? "-fx-background-color:#e8f8f5;-fx-text-fill:#1abc9c;-fx-padding:3 10;-fx-background-radius:10;-fx-font-size:10px;-fx-font-weight:bold;"
                    : "-fx-background-color:#f2f4f4;-fx-text-fill:#95a5a6;-fx-padding:3 10;-fx-background-radius:10;-fx-font-size:10px;-fx-font-weight:bold;");
                setGraphic(lbl);
            }
        });

        historyTable.setItems(displayData);
    }

    // ─────────────────────────────────────────────
    //  Data
    // ─────────────────────────────────────────────

    private void loadData() {
        allData.clear();
        String q = "SELECT id_user, nom, prenom, email, role, est_actif, " +
                   "       derniere_connexion " +
                   "FROM users ORDER BY derniere_connexion DESC";
        try (Connection conn = DatabaseConnection.getConnection();
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(q)) {
            while (rs.next()) {
                User u = new User();
                u.setId(rs.getInt("id_user"));
                u.setNom(rs.getString("nom"));
                u.setPrenom(rs.getString("prenom"));
                u.setEmail(rs.getString("email"));
                u.setRole(rs.getString("role"));
                u.setEstActif(rs.getBoolean("est_actif"));
                Timestamp ts = rs.getTimestamp("derniere_connexion");
                if (ts != null) u.setDerniereConnexion(ts.toLocalDateTime());
                allData.add(u);
            }
        } catch (SQLException e) { e.printStackTrace(); }
        applyFilter();
    }

    private void applyFilter() {
        String search = searchField.getText().toLowerCase();
        String role   = roleFilter.getValue();
        List<User> filtered = allData.stream().filter(u -> {
            boolean matchSearch = search.isEmpty()
                || safe(u.getNom()).toLowerCase().contains(search)
                || safe(u.getPrenom()).toLowerCase().contains(search)
                || safe(u.getEmail()).toLowerCase().contains(search);
            boolean matchRole = "Tous les rôles".equals(role)
                || safe(u.getRole()).equalsIgnoreCase(role);
            return matchSearch && matchRole;
        }).collect(Collectors.toList());

        displayData.setAll(filtered);
        countLabel.setText(filtered.size() + " utilisateur" + (filtered.size() > 1 ? "s" : ""));
    }

    // ─────────────────────────────────────────────
    //  Excel export
    // ─────────────────────────────────────────────

    @FXML
    private void handleExport() {
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Enregistrer l'historique");
        chooser.setInitialFileName("historique_connexions_" +
                LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")) + ".xlsx");
        chooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Excel (*.xlsx)", "*.xlsx"));
        File dest = chooser.showSaveDialog(historyTable.getScene().getWindow());
        if (dest == null) return;

        try (XSSFWorkbook wb = new XSSFWorkbook()) {
            XSSFSheet sheet = wb.createSheet("Historique");
            sheet.setColumnWidth(0, 5000); sheet.setColumnWidth(1, 6000);
            sheet.setColumnWidth(2, 4000); sheet.setColumnWidth(3, 5000);
            sheet.setColumnWidth(4, 3000);

            // Header style
            XSSFCellStyle hs = wb.createCellStyle();
            hs.setFillForegroundColor(new XSSFColor(new byte[]{42,111,91}, null));
            hs.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            XSSFFont hf = wb.createFont(); hf.setBold(true);
            hf.setColor(new XSSFColor(new byte[]{(byte)255,(byte)255,(byte)255}, null));
            hs.setFont(hf);

            Row hRow = sheet.createRow(0);
            String[] cols = {"Utilisateur","Email","Rôle","Dernière connexion","Statut"};
            for (int i = 0; i < cols.length; i++) {
                org.apache.poi.ss.usermodel.Cell c = hRow.createCell(i);
                c.setCellValue(cols[i]); c.setCellStyle(hs);
            }

            int r = 1;
            for (User u : displayData) {
                Row row = sheet.createRow(r++);
                row.createCell(0).setCellValue(u.getPrenom() + " " + u.getNom());
                row.createCell(1).setCellValue(safe(u.getEmail()));
                row.createCell(2).setCellValue(safe(u.getRole()));
                row.createCell(3).setCellValue(u.getDerniereConnexion() != null
                        ? u.getDerniereConnexion().format(FMT) : "Jamais");
                row.createCell(4).setCellValue(u.isEstActif() ? "Actif" : "Bloqué");
            }

            try (FileOutputStream fos = new FileOutputStream(dest)) { wb.write(fos); }

            Alert ok = new Alert(Alert.AlertType.INFORMATION);
            ok.setTitle("Export réussi"); ok.setHeaderText(null);
            ok.setContentText("Fichier enregistré :\n" + dest.getAbsolutePath());
            ok.showAndWait();
            try { java.awt.Desktop.getDesktop().open(dest); } catch (Exception ignored) {}

        } catch (Exception e) {
            e.printStackTrace();
            new Alert(Alert.AlertType.ERROR, "Erreur : " + e.getMessage()).showAndWait();
        }
    }

    @FXML private void handleFilter() { applyFilter(); }
    @FXML private void handleBack()   { MainController.getInstance().showUsers(); }

    private String safe(String s) { return s == null ? "" : s; }
}
