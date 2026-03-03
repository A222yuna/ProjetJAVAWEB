package com.psychoapp.controller;

import com.psychoapp.model.Utilisateur;
import com.psychoapp.util.DatabaseConnection;
import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.PageSize;
import com.lowagie.text.Phrase;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.stage.FileChooser;
import javafx.scene.control.cell.PropertyValueFactory;

import java.awt.Color;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.sql.*;
import java.time.LocalDate;
import java.util.ResourceBundle;

public class ValidationController implements Initializable {

    @FXML private TableView<Utilisateur>              tableView;
    @FXML private TableColumn<Utilisateur, Integer>   colId;
    @FXML private TableColumn<Utilisateur, String>    colNom;
    @FXML private TableColumn<Utilisateur, String>    colPrenom;
    @FXML private TableColumn<Utilisateur, String>    colEmail;
    @FXML private TableColumn<Utilisateur, LocalDate> colDate;
    @FXML private TableColumn<Utilisateur, String>    colStatut;
    @FXML private Label                               statusLabel;
    @FXML private ComboBox<String>                    filterCombo;

    private final ObservableList<Utilisateur> list = FXCollections.observableArrayList();

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        setupTable();
        filterCombo.setItems(FXCollections.observableArrayList("en_attente","approuve","rejete"));
        filterCombo.setValue("en_attente");
        filterCombo.valueProperty().addListener((obs, o, n) -> loadUsers(n));
        loadUsers("en_attente");
    }

    private void setupTable() {
        colId.setCellValueFactory(new PropertyValueFactory<>("idUser"));
        colNom.setCellValueFactory(new PropertyValueFactory<>("nom"));
        colPrenom.setCellValueFactory(new PropertyValueFactory<>("prenom"));
        colEmail.setCellValueFactory(new PropertyValueFactory<>("email"));
        colDate.setCellValueFactory(new PropertyValueFactory<>("dateInscription"));
        colStatut.setCellValueFactory(new PropertyValueFactory<>("statutValidation"));
        colStatut.setCellFactory(col -> new TableCell<>() {
            @Override protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) { setText(null); setStyle(""); return; }
                switch (item) {
                    case "approuve" -> { setText("✔ Approuvé");  setStyle("-fx-text-fill:#38a169;-fx-font-weight:bold;"); }
                    case "rejete"   -> { setText("✘ Rejeté");    setStyle("-fx-text-fill:#e53e3e;-fx-font-weight:bold;"); }
                    default         -> { setText(" En attente"); setStyle("-fx-text-fill:#d69e2e;-fx-font-weight:bold;"); }
                }
            }
        });
        tableView.setItems(list);
    }

    private void loadUsers(String filtre) {
        list.clear();
        String sql = "SELECT * FROM users WHERE role='Psychologue' AND statut_validation=? ORDER BY date_inscription DESC";
        try (Connection conn = DatabaseConnection.getConnection()) {
            // Check if column exists first
            ResultSet check = conn.getMetaData().getColumns(null, null, "users", "statut_validation");
            if (!check.next()) {
                statusLabel.setText("⚠  Colonne statut_validation manquante. Exécutez le SQL de setup.");
                return;
            }
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setString(1, filtre);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                Utilisateur u = new Utilisateur();
                u.setIdUser(rs.getInt("id_user"));
                u.setNom(rs.getString("nom"));
                u.setPrenom(rs.getString("prenom"));
                u.setEmail(rs.getString("email"));
                u.setRole(Utilisateur.Role.Psychologue);
                u.setStatutValidation(rs.getString("statut_validation"));
                Date d = rs.getDate("date_inscription");
                if (d != null) u.setDateInscription(d.toLocalDate());
                list.add(u);
            }
            statusLabel.setText(list.size() + " psychologue(s).");
        } catch (SQLException e) {
            statusLabel.setText("Erreur : " + e.getMessage());
        }
    }

    @FXML private void handleApprove() { updateStatut("approuve"); }
    @FXML private void handleReject()  { updateStatut("rejete"); }

    private void updateStatut(String statut) {
        Utilisateur u = tableView.getSelectionModel().getSelectedItem();
        if (u == null) { new Alert(Alert.AlertType.WARNING, "Sélectionnez un psychologue.").showAndWait(); return; }
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement("UPDATE users SET statut_validation=? WHERE id_user=?")) {
            ps.setString(1, statut);
            ps.setInt(2, u.getIdUser());
            ps.executeUpdate();
            loadUsers(filterCombo.getValue());
        } catch (SQLException e) { statusLabel.setText("Erreur : " + e.getMessage()); }
    }

    @FXML private void handleRefresh() { loadUsers(filterCombo.getValue()); }

    @FXML
    private void handleExportPdf() {
        if (list.isEmpty()) {
            new Alert(Alert.AlertType.INFORMATION, "Aucune donnee a exporter. Choisissez un filtre (ex: approuve) et rafraichissez.").showAndWait();
            return;
        }
        String filtre = filterCombo.getValue();
        String defaultName = "psychologues_" + filtre + ".pdf";
        FileChooser fc = new FileChooser();
        fc.setTitle("Enregistrer la liste en PDF");
        fc.setInitialFileName(defaultName);
        fc.getExtensionFilters().add(new FileChooser.ExtensionFilter("PDF", "*.pdf"));
        java.io.File file = fc.showSaveDialog(tableView.getScene().getWindow());
        if (file == null) return;
        String path = file.getAbsolutePath();
        if (!path.toLowerCase().endsWith(".pdf")) path += ".pdf";

        try (FileOutputStream fos = new FileOutputStream(path)) {
            Document document = new Document(PageSize.A4.rotate());
            PdfWriter.getInstance(document, fos);
            document.open();

            PdfPTable table = new PdfPTable(6);
            table.setWidthPercentage(100f);
            table.setSpacingBefore(12f);
            table.setSpacingAfter(12f);
            float[] widths = {0.8f, 1.5f, 1.5f, 2.2f, 1.5f, 1.2f};
            table.setWidths(widths);

            String[] headers = {"ID", "Nom", "Prenom", "Email", "Inscription", "Statut"};
            for (String h : headers) {
                PdfPCell cell = new PdfPCell(new Phrase(h));
                cell.setBackgroundColor(new Color(220, 220, 220));
                table.addCell(cell);
            }
            for (Utilisateur u : list) {
                table.addCell(String.valueOf(u.getIdUser()));
                table.addCell(u.getNom() != null ? u.getNom() : "");
                table.addCell(u.getPrenom() != null ? u.getPrenom() : "");
                table.addCell(u.getEmail() != null ? u.getEmail() : "");
                table.addCell(u.getDateInscription() != null ? u.getDateInscription().toString() : "");
                table.addCell(u.getStatutValidation() != null ? u.getStatutValidation() : "");
            }
            document.add(new Phrase("Liste des psychologues - filtre: " + filtre + " (" + list.size() + ")\n\n"));
            document.add(table);
            document.close();

            if (statusLabel != null) statusLabel.setText("PDF enregistre: " + path);
            new Alert(Alert.AlertType.INFORMATION, "PDF enregistre:\n" + path).showAndWait();
        } catch (DocumentException | IOException e) {
            if (statusLabel != null) statusLabel.setText("Erreur export: " + e.getMessage());
            new Alert(Alert.AlertType.ERROR, "Erreur lors de l'export PDF: " + e.getMessage()).showAndWait();
        }
    }
}
