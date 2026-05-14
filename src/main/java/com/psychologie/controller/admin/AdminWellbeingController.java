package com.psychologie.controller.admin;

import com.psychologie.controller.MainController;
import com.psychologie.model.ProgrammeBienEtre;
import com.psychologie.model.User;
import com.psychologie.util.DatabaseConnection;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.List;
import java.util.stream.Collectors;

public class AdminWellbeingController {

    @FXML private Label totalProgramsLabel;
    @FXML private Label activeProgramsLabel;
    @FXML private Label activitiesLabel;
    @FXML private Label reviewsLabel;

    @FXML private TextField searchField;
    @FXML private ComboBox<String> statusFilter;
    @FXML private ComboBox<String> levelFilter;

    @FXML private TableView<ProgrammeBienEtre> programTable;
    @FXML private TableColumn<ProgrammeBienEtre, String> idColumn;
    @FXML private TableColumn<ProgrammeBienEtre, ProgrammeBienEtre> programColumn;
    @FXML private TableColumn<ProgrammeBienEtre, String> psyColumn;
    @FXML private TableColumn<ProgrammeBienEtre, String> statusColumn;
    @FXML private TableColumn<ProgrammeBienEtre, String> levelColumn;
    @FXML private TableColumn<ProgrammeBienEtre, String> activitiesCountColumn;
    @FXML private TableColumn<ProgrammeBienEtre, String> reviewsCountColumn;
    @FXML private TableColumn<ProgrammeBienEtre, Void> actionsColumn;

    private ObservableList<ProgrammeBienEtre> allPrograms = FXCollections.observableArrayList();
    private ObservableList<ProgrammeBienEtre> filteredPrograms = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        statusFilter.setItems(FXCollections.observableArrayList("Tous", "ACTIF", "BROUILLON"));
        statusFilter.setValue("Tous");
        levelFilter.setItems(FXCollections.observableArrayList("Tous", "facile", "moyen", "difficile"));
        levelFilter.setValue("Tous");

        setupTable();
        loadPrograms();
        
        searchField.textProperty().addListener((obs, old, newVal) -> applyFilters());
        statusFilter.setOnAction(e -> applyFilters());
        levelFilter.setOnAction(e -> applyFilters());
    }

    private void setupTable() {
        idColumn.setCellValueFactory(cellData -> new SimpleStringProperty(String.valueOf(cellData.getValue().getId())));
        
        programColumn.setCellValueFactory(cellData -> new javafx.beans.property.SimpleObjectProperty<>(cellData.getValue()));
        programColumn.setCellFactory(param -> new TableCell<ProgrammeBienEtre, ProgrammeBienEtre>() {
            @Override
            protected void updateItem(ProgrammeBienEtre item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setGraphic(null);
                } else {
                    VBox box = new VBox(2);
                    Hyperlink name = new Hyperlink(item.getNom());
                    name.setStyle("-fx-font-weight: bold; -fx-text-fill: #3498db; -fx-underline: false; -fx-padding: 0;");
                    name.setOnAction(e -> handleViewDetail(item));
                    
                    Label obj = new Label(item.getObjectif());
                    obj.setStyle("-fx-font-size: 10px; -fx-text-fill: #95a5a6;");
                    box.getChildren().addAll(name, obj);
                    setGraphic(box);
                }
            }
        });

        psyColumn.setCellValueFactory(cellData -> new SimpleStringProperty(
            cellData.getValue().getPsychologue() != null ? 
            cellData.getValue().getPsychologue().getPrenom() + " " + cellData.getValue().getPsychologue().getNom() : "N/A"));

        statusColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getStatut()));
        statusColumn.setCellFactory(param -> new TableCell<ProgrammeBienEtre, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setGraphic(null);
                } else {
                    Label label = new Label(item.toUpperCase());
                    label.setStyle("-fx-padding: 2 8; -fx-background-radius: 5; -fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 10px;");
                    if ("ACTIF".equalsIgnoreCase(item)) label.setStyle(label.getStyle() + "-fx-background-color: #7f8c8d;");
                    else label.setStyle(label.getStyle() + "-fx-background-color: #95a5a6;");
                    setGraphic(label);
                }
            }
        });

        levelColumn.setCellValueFactory(new javafx.scene.control.cell.PropertyValueFactory<>("niveauDifficulte"));
        activitiesCountColumn.setCellValueFactory(cellData -> new SimpleStringProperty(String.valueOf(cellData.getValue().getActivites() != null ? cellData.getValue().getActivites().size() : 0)));
        reviewsCountColumn.setCellValueFactory(cellData -> new SimpleStringProperty(String.valueOf(cellData.getValue().getAvis() != null ? cellData.getValue().getAvis().size() : 0)));

        actionsColumn.setCellFactory(param -> new TableCell<ProgrammeBienEtre, Void>() {
            private final Button deleteBtn = new Button("Supprimer");
            {
                deleteBtn.setStyle("-fx-background-color: white; -fx-border-color: #e74c3c; -fx-text-fill: #e74c3c; -fx-border-radius: 5; -fx-font-size: 10px; -fx-cursor: hand;");
                deleteBtn.setOnAction(e -> handleDelete(getTableView().getItems().get(getIndex())));
            }
            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) setGraphic(null);
                else setGraphic(deleteBtn);
            }
        });

        programTable.setItems(filteredPrograms);
    }

    private void loadPrograms() {
        allPrograms.clear();
        String query = "SELECT p.*, u.prenom, u.nom FROM programme_bien_etre p " +
                       "LEFT JOIN users u ON p.psychologue_id_user = u.id_user";
        
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            
            int total = 0, active = 0;
            while (rs.next()) {
                ProgrammeBienEtre p = new ProgrammeBienEtre();
                p.setId(rs.getInt("idProgramme"));
                p.setNom(rs.getString("nom") != null ? rs.getString("nom") : "Sans nom");
                p.setObjectif(rs.getString("objectif") != null ? rs.getString("objectif") : "");
                p.setStatut(rs.getString("statut") != null ? rs.getString("statut") : "BROUILLON");
                p.setNiveauDifficulte(rs.getString("niveauDifficulte") != null ? rs.getString("niveauDifficulte") : "facile");
                p.setDuree(rs.getInt("duree"));
                
                User psy = new User();
                psy.setPrenom(rs.getString("prenom") != null ? rs.getString("prenom") : "Inconnu");
                psy.setNom(rs.getString("nom") != null ? rs.getString("nom") : "");
                p.setPsychologue(psy);

                allPrograms.add(p);
                total++;
                if ("ACTIF".equalsIgnoreCase(p.getStatut())) active++;

                // Load activities and reviews counts for each program
                int progId = rs.getInt("idProgramme");
                
                // Activities count
                String actQuery = "SELECT COUNT(*) FROM activite_programme WHERE idProgramme = ?";
                try (PreparedStatement pstmtAct = conn.prepareStatement(actQuery)) {
                    pstmtAct.setInt(1, progId);
                    try (ResultSet rsAct = pstmtAct.executeQuery()) {
                        if (rsAct.next()) {
                            int count = rsAct.getInt(1);
                            p.setActivites(new java.util.ArrayList<>(java.util.Collections.nCopies(count, null)));
                        }
                    }
                }

                // Reviews count
                String revQuery = "SELECT COUNT(*) FROM avis WHERE idProgramme = ?";
                try (PreparedStatement pstmtRev = conn.prepareStatement(revQuery)) {
                    pstmtRev.setInt(1, progId);
                    try (ResultSet rsRev = pstmtRev.executeQuery()) {
                        if (rsRev.next()) {
                            int count = rsRev.getInt(1);
                            p.setAvis(new java.util.ArrayList<>(java.util.Collections.nCopies(count, null)));
                        }
                    }
                }
            }
            
            totalProgramsLabel.setText(String.valueOf(total));
            activeProgramsLabel.setText(String.valueOf(active));

            // Global stats for the cards
            String globalActQuery = "SELECT COUNT(*) FROM activite_programme";
            try (PreparedStatement pstmtGlobalAct = conn.prepareStatement(globalActQuery);
                 ResultSet rsGlobalAct = pstmtGlobalAct.executeQuery()) {
                if (rsGlobalAct.next()) activitiesLabel.setText(String.valueOf(rsGlobalAct.getInt(1)));
            }

            String globalRevQuery = "SELECT COUNT(*) FROM avis";
            try (PreparedStatement pstmtGlobalRev = conn.prepareStatement(globalRevQuery);
                 ResultSet rsGlobalRev = pstmtGlobalRev.executeQuery()) {
                if (rsGlobalRev.next()) reviewsLabel.setText(String.valueOf(rsGlobalRev.getInt(1)));
            }
            
            applyFilters();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    public void applyFilters() {
        if (allPrograms == null || filteredPrograms == null) return;
        
        String search = (searchField != null && searchField.getText() != null) ? searchField.getText().toLowerCase() : "";
        String status = (statusFilter != null && statusFilter.getValue() != null) ? statusFilter.getValue() : "Tous";
        String level = (levelFilter != null && levelFilter.getValue() != null) ? levelFilter.getValue() : "Tous";

        List<ProgrammeBienEtre> filtered = allPrograms.stream().filter(p -> {
            String nom = p.getNom() != null ? p.getNom().toLowerCase() : "";
            String obj = p.getObjectif() != null ? p.getObjectif().toLowerCase() : "";
            String stat = p.getStatut() != null ? p.getStatut() : "";
            String niv = p.getNiveauDifficulte() != null ? p.getNiveauDifficulte() : "";

            boolean matchesSearch = search.isEmpty() || nom.contains(search) || obj.contains(search);
            boolean matchesStatus = "Tous".equals(status) || stat.equalsIgnoreCase(status);
            boolean matchesLevel = "Tous".equals(level) || niv.equalsIgnoreCase(level);
            return matchesSearch && matchesStatus && matchesLevel;
        }).collect(Collectors.toList());

        filteredPrograms.setAll(filtered);
    }

    private void handleDelete(ProgrammeBienEtre program) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION, "Supprimer ce programme ?", ButtonType.YES, ButtonType.NO);
        alert.showAndWait().ifPresent(res -> {
            if (res == ButtonType.YES) {
                try (Connection conn = DatabaseConnection.getConnection();
                     PreparedStatement pstmt = conn.prepareStatement("DELETE FROM programme_bien_etre WHERE idProgramme = ?")) {
                    pstmt.setInt(1, program.getId());
                    pstmt.executeUpdate();
                    loadPrograms();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    private void handleViewDetail(ProgrammeBienEtre program) {
        System.out.println("Displaying details for: " + program.getNom());
        com.psychologie.controller.ProgramDetailController.setProgram(program);
        MainController.getInstance().loadView("/com/psychologie/view/ProgramDetailView.fxml");
    }
}
