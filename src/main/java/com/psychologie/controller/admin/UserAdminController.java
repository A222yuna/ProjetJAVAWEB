package com.psychologie.controller.admin;

import com.psychologie.model.User;
import com.psychologie.controller.MainController;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;

import com.psychologie.util.DatabaseConnection;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import javafx.stage.FileChooser;

import java.awt.Color;
import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

public class UserAdminController {

    @FXML private Label totalUsersLabel;
    @FXML private Label dateLabel;
    @FXML private TextField searchField;
    @FXML private ComboBox<String> roleComboBox;
    @FXML private ComboBox<String> statusComboBox;
    @FXML private TableView<User> userTable;
    @FXML private TableColumn<User, User> userColumn;
    @FXML private TableColumn<User, String> roleColumn;
    @FXML private TableColumn<User, String> emailColumn;
    @FXML private TableColumn<User, String> statusColumn;
    @FXML private TableColumn<User, Boolean> verifiedColumn;
    @FXML private TableColumn<User, Void> actionsColumn;
    @FXML private TableColumn<User, Void> pdfColumn;

    private ObservableList<User> userData = FXCollections.observableArrayList();
    private ObservableList<User> fullUserData = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        if (dateLabel != null) dateLabel.setText(LocalDate.now().format(java.time.format.DateTimeFormatter.ofPattern("dd MMMM yyyy")));
        
        if (roleComboBox != null) {
            roleComboBox.setItems(FXCollections.observableArrayList("Tous les rôles", "Admin", "Psychologue", "Patient"));
            roleComboBox.setValue("Tous les rôles");
            roleComboBox.setOnAction(e -> applyFilters());
        }
        
        if (statusComboBox != null) {
            statusComboBox.setItems(FXCollections.observableArrayList("Tous les statuts", "Actif", "Bloqué"));
            statusComboBox.setValue("Tous les statuts");
            statusComboBox.setOnAction(e -> applyFilters());
        }

        if (searchField != null) {
            searchField.textProperty().addListener((observable, oldValue, newValue) -> applyFilters());
        }

        setupTable();
        loadDataFromDatabase();
    }

    private void setupTable() {
        userColumn.setCellValueFactory(cellData -> new SimpleObjectProperty<>(cellData.getValue()));
        userColumn.setCellFactory(param -> new TableCell<User, User>() {
            @Override
            protected void updateItem(User item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setGraphic(null);
                } else {
                    VBox vbox = new VBox(2);
                    Label name = new Label(item.getPrenom() + " " + item.getNom());
                    name.setStyle("-fx-font-weight: bold;");
                    Label date = new Label("Inscrit le " + (item.getDateInscription() != null ? item.getDateInscription() : "N/A"));
                    date.setStyle("-fx-font-size: 10px; -fx-text-fill: #95a5a6;");
                    vbox.getChildren().addAll(name, date);
                    setGraphic(vbox);
                }
            }
        });

        roleColumn.setCellValueFactory(new PropertyValueFactory<>("role"));
        roleColumn.setCellFactory(param -> new TableCell<User, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setGraphic(null);
                } else {
                    Label label = new Label(item.toUpperCase());
                    label.getStyleClass().add("badge-role-psy"); // Default
                    if (item.equalsIgnoreCase("PATIENT")) {
                        label.getStyleClass().clear();
                        label.getStyleClass().add("badge-role-patient");
                    }
                    setGraphic(label);
                }
            }
        });

        emailColumn.setCellValueFactory(new PropertyValueFactory<>("email"));
        
        statusColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().isEstActif() ? "ACTIF" : "BLOQUÉ"));
        statusColumn.setCellFactory(param -> new TableCell<User, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setGraphic(null);
                } else {
                    Label label = new Label(item);
                    if (item.equals("ACTIF")) label.getStyleClass().add("badge-status-actif");
                    else label.getStyleClass().add("badge-status-bloque");
                    setGraphic(label);
                }
            }
        });

        verifiedColumn.setCellValueFactory(new PropertyValueFactory<>("emailVerifie"));
        verifiedColumn.setCellFactory(param -> new TableCell<User, Boolean>() {
            @Override
            protected void updateItem(Boolean item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setGraphic(null);
                } else {
                    Label check = new Label(item ? "✔" : "✘");
                    check.setStyle("-fx-text-fill: " + (item ? "#27ae60;" : "#e67e22;") + " -fx-font-weight: bold;");
                    setGraphic(check);
                }
            }
        });

        actionsColumn.setCellFactory(param -> new TableCell<User, Void>() {
            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    User user = getTableView().getItems().get(getIndex());
                    HBox box = new HBox(10);
                    box.setAlignment(javafx.geometry.Pos.CENTER);

                    if (!"ADMIN".equalsIgnoreCase(user.getRole())) {
                        // Toggle Status Button (Activate/Deactivate)
                        Button toggleBtn = new Button(user.isEstActif() ? "Deactivate" : "Activate");
                        toggleBtn.setStyle("-fx-font-size: 10px; -fx-background-color: " + (user.isEstActif() ? "#e67e22" : "#27ae60") + "; -fx-text-fill: white; -fx-cursor: hand; -fx-background-radius: 10;");
                        toggleBtn.setOnAction(e -> handleToggleUserStatus(user));

                        // Delete Button
                        Button deleteBtn = new Button("Delete");
                        deleteBtn.setStyle("-fx-font-size: 10px; -fx-background-color: #e74c3c; -fx-text-fill: white; -fx-cursor: hand; -fx-background-radius: 10;");
                        deleteBtn.setOnAction(e -> handleSupprimerUser(user));

                        box.getChildren().addAll(toggleBtn, deleteBtn);
                    } else {
                        Label lbl = new Label("N/A");
                        lbl.setStyle("-fx-font-size: 10px; -fx-text-fill: #95a5a6;");
                        box.getChildren().add(lbl);
                    }
                    setGraphic(box);
                }
            }
        });
        
        userTable.setItems(userData);

        // PDF column — download user info as PDF
        if (pdfColumn != null) {
            pdfColumn.setCellFactory(param -> new TableCell<>() {
                @Override
                protected void updateItem(Void item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty) { setGraphic(null); return; }
                    Button btn = new Button("⬇");
                    btn.setStyle("-fx-background-color:#17a2b8;-fx-text-fill:white;" +
                                 "-fx-background-radius:8;-fx-cursor:hand;-fx-font-size:11px;-fx-padding:4 8;");
                    btn.setTooltip(new Tooltip("Télécharger PDF"));
                    btn.setOnAction(e -> handleDownloadPdf(
                            getTableView().getItems().get(getIndex())));
                    setGraphic(btn);
                }
            });
        }
    }

    private void handleToggleUserStatus(User user) {
        String query = "UPDATE users SET est_actif = ? WHERE id_user = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setBoolean(1, !user.isEstActif());
            pstmt.setInt(2, user.getId());
            pstmt.executeUpdate();
            loadDataFromDatabase();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void handleSupprimerUser(User user) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION, "Supprimer l'utilisateur " + user.getPrenom() + " ?", ButtonType.YES, ButtonType.NO);
        alert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.YES) {
                String query = "DELETE FROM users WHERE id_user = ?";
                try (Connection conn = DatabaseConnection.getConnection();
                     PreparedStatement pstmt = conn.prepareStatement(query)) {
                    pstmt.setInt(1, user.getId());
                    pstmt.executeUpdate();
                    loadDataFromDatabase();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    private void loadDataFromDatabase() {
        fullUserData.clear();
        String query = "SELECT * FROM users ORDER BY date_inscription DESC";
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {

            while (rs.next()) {
                User user = new User();
                user.setId(rs.getInt("id_user"));
                user.setNom(rs.getString("nom"));
                user.setPrenom(rs.getString("prenom"));
                user.setEmail(rs.getString("email"));
                user.setTelephone(rs.getString("telephone"));
                user.setRole(rs.getString("role"));
                user.setEstActif(rs.getBoolean("est_actif"));
                user.setEmailVerifie(rs.getBoolean("email_verifie"));
                java.sql.Date date = rs.getDate("date_inscription");
                if (date != null) user.setDateInscription(date.toLocalDate());
                java.sql.Timestamp ts = rs.getTimestamp("derniere_connexion");
                if (ts != null) user.setDerniereConnexion(ts.toLocalDateTime());
                fullUserData.add(user);
            }
            applyFilters();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void applyFilters() {
        String searchText = searchField.getText().toLowerCase();
        String selectedRole = roleComboBox.getValue();
        String selectedStatus = statusComboBox.getValue();

        List<User> filtered = fullUserData.stream().filter(u -> {
            boolean matchesSearch = searchText.isEmpty() || 
                (u.getNom() != null && u.getNom().toLowerCase().contains(searchText)) || 
                (u.getPrenom() != null && u.getPrenom().toLowerCase().contains(searchText)) || 
                (u.getEmail() != null && u.getEmail().toLowerCase().contains(searchText));
            
            boolean matchesRole = selectedRole.equals("Tous les rôles") || 
                (u.getRole() != null && u.getRole().equalsIgnoreCase(selectedRole));
            
            boolean matchesStatus = selectedStatus.equals("Tous les statuts") || 
                (selectedStatus.equals("Actif") && u.isEstActif()) || 
                (selectedStatus.equals("Bloqué") && !u.isEstActif());

            return matchesSearch && matchesRole && matchesStatus;
        }).collect(Collectors.toList());

        userData.setAll(filtered);
        if (totalUsersLabel != null) totalUsersLabel.setText(String.valueOf(fullUserData.size()));
    }

    @FXML
    public void handleStats() {
        MainController.getInstance().showStats();
    }

    @FXML
    public void handleHistorique() {
        MainController.getInstance().loadView("/com/psychologie/view/admin/LoginHistory.fxml");
    }

    @FXML public void handleRefresh() { loadDataFromDatabase(); }

    // ─────────────────────────────────────────────
    //  PDF per user
    // ─────────────────────────────────────────────

    private void handleDownloadPdf(User user) {
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Enregistrer le PDF");
        chooser.setInitialFileName("user_" + user.getId() + "_" +
                user.getNom().replaceAll("\\s+", "_") + ".pdf");
        chooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("PDF (*.pdf)", "*.pdf"));
        File dest = chooser.showSaveDialog(userTable.getScene().getWindow());
        if (dest == null) return;

        try (PDDocument doc = new PDDocument()) {
            PDPage page = new PDPage(PDRectangle.A4);
            doc.addPage(page);

            float W = PDRectangle.A4.getWidth();
            float H = PDRectangle.A4.getHeight();
            float M = 50f;

            try (PDPageContentStream cs = new PDPageContentStream(doc, page)) {

                // Header bar
                cs.setNonStrokingColor(new Color(42, 111, 91));
                cs.addRect(0, H - 70, W, 70); cs.fill();

                // Title
                cs.beginText();
                cs.setFont(PDType1Font.HELVETICA_BOLD, 18);
                cs.setNonStrokingColor(Color.WHITE);
                cs.newLineAtOffset(M, H - 45);
                cs.showText("Fiche Utilisateur");
                cs.endText();

                // Date
                String today = LocalDate.now()
                        .format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
                cs.beginText();
                cs.setFont(PDType1Font.HELVETICA, 10);
                cs.setNonStrokingColor(new Color(200, 230, 220));
                cs.newLineAtOffset(W - M - 80, H - 45);
                cs.showText("Généré le " + today);
                cs.endText();

                // Avatar circle background
                cs.setNonStrokingColor(new Color(230, 240, 236));
                cs.addRect(M, H - 160, 70, 70); cs.fill();

                // Initials
                String initials = "";
                if (user.getPrenom() != null && !user.getPrenom().isEmpty())
                    initials += user.getPrenom().charAt(0);
                if (user.getNom() != null && !user.getNom().isEmpty())
                    initials += user.getNom().charAt(0);
                cs.beginText();
                cs.setFont(PDType1Font.HELVETICA_BOLD, 24);
                cs.setNonStrokingColor(new Color(42, 111, 91));
                cs.newLineAtOffset(M + 15, H - 130);
                cs.showText(initials.toUpperCase());
                cs.endText();

                // Full name
                cs.beginText();
                cs.setFont(PDType1Font.HELVETICA_BOLD, 20);
                cs.setNonStrokingColor(new Color(28, 40, 36));
                cs.newLineAtOffset(M + 85, H - 110);
                cs.showText(safe(user.getPrenom()) + " " + safe(user.getNom()));
                cs.endText();

                cs.beginText();
                cs.setFont(PDType1Font.HELVETICA, 12);
                cs.setNonStrokingColor(new Color(90, 101, 96));
                cs.newLineAtOffset(M + 85, H - 130);
                cs.showText(safe(user.getRole()) + "  •  " + safe(user.getEmail()));
                cs.endText();

                // Divider
                cs.setStrokingColor(new Color(220, 216, 208));
                cs.moveTo(M, H - 175); cs.lineTo(W - M, H - 175); cs.stroke();

                // Info rows
                float y = H - 210;
                float labelX = M, valueX = M + 160;
                String[][] rows = {
                    {"ID Utilisateur",    String.valueOf(user.getId())},
                    {"Nom",               safe(user.getNom())},
                    {"Prénom",            safe(user.getPrenom())},
                    {"Email",             safe(user.getEmail())},
                    {"Téléphone",         safe(user.getTelephone())},
                    {"Rôle",              safe(user.getRole())},
                    {"Statut",            user.isEstActif() ? "Actif" : "Bloqué / Inactif"},
                    {"Email vérifié",     user.isEmailVerifie() ? "Oui" : "Non"},
                    {"Date inscription",  user.getDateInscription() != null
                            ? user.getDateInscription()
                                    .format(DateTimeFormatter.ofPattern("dd/MM/yyyy")) : "—"},
                    {"Dernière connexion", user.getDerniereConnexion() != null
                            ? user.getDerniereConnexion()
                                    .format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")) : "Jamais"},
                };

                boolean alt = false;
                for (String[] row : rows) {
                    if (alt) {
                        cs.setNonStrokingColor(new Color(248, 249, 250));
                        cs.addRect(M - 5, y - 6, W - 2 * M + 10, 22); cs.fill();
                    }
                    cs.beginText();
                    cs.setFont(PDType1Font.HELVETICA_BOLD, 10);
                    cs.setNonStrokingColor(new Color(90, 101, 96));
                    cs.newLineAtOffset(labelX, y);
                    cs.showText(row[0]);
                    cs.endText();

                    cs.beginText();
                    cs.setFont(PDType1Font.HELVETICA, 10);
                    cs.setNonStrokingColor(new Color(28, 40, 36));
                    cs.newLineAtOffset(valueX, y);
                    cs.showText(row[1] != null ? row[1] : "—");
                    cs.endText();

                    y -= 26;
                    alt = !alt;
                }

                // Footer
                cs.setNonStrokingColor(new Color(90, 101, 96));
                cs.beginText();
                cs.setFont(PDType1Font.HELVETICA, 9);
                cs.newLineAtOffset(M, 30);
                cs.showText("Psychologie App – Document confidentiel – " + today);
                cs.endText();
            }

            doc.save(dest);

            Alert ok = new Alert(Alert.AlertType.INFORMATION);
            ok.setTitle("PDF généré"); ok.setHeaderText(null);
            ok.setContentText("Fichier enregistré :\n" + dest.getAbsolutePath());
            ok.showAndWait();
            try { java.awt.Desktop.getDesktop().open(dest); } catch (Exception ignored) {}

        } catch (IOException e) {
            e.printStackTrace();
            new Alert(Alert.AlertType.ERROR, "Erreur PDF : " + e.getMessage()).showAndWait();
        }
    }

    private String safe(String s) { return s == null ? "" : s; }
}
