package com.psychologie.controller.admin;

import com.psychologie.model.Cabinet;
import com.psychologie.util.DatabaseConnection;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class AdminCabinetController {

    @FXML private Label dateLabel;
    @FXML private TableView<Cabinet> cabinetTable;
    @FXML private TableColumn<Cabinet, String> villeColumn;
    @FXML private TableColumn<Cabinet, String> adresseColumn;
    @FXML private TableColumn<Cabinet, String> horairesColumn;
    @FXML private TableColumn<Cabinet, String> statusColumn;
    @FXML private TableColumn<Cabinet, Void> actionsColumn;

    private ObservableList<Cabinet> cabinetData = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        dateLabel.setText(LocalDate.now().format(DateTimeFormatter.ofPattern("dd MMMM yyyy")));
        setupTable();
        loadCabinets();
    }

    private void setupTable() {
        villeColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getVille()));
        adresseColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getAdresse()));
        horairesColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getHoraires()));
        
        statusColumn.setCellValueFactory(cellData -> {
            Cabinet c = cellData.getValue();
            if (c.isArchive()) return new SimpleStringProperty("ARCHIVÉ");
            return new SimpleStringProperty(c.isValide() ? "VALIDÉ" : "EN ATTENTE");
        });

        statusColumn.setCellFactory(param -> new TableCell<Cabinet, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setGraphic(null);
                } else {
                    Label label = new Label(item);
                    label.setStyle("-fx-padding: 3 10; -fx-background-radius: 10; -fx-font-size: 10px; -fx-font-weight: bold; -fx-text-fill: white;");
                    if ("VALIDÉ".equals(item)) label.setStyle(label.getStyle() + "-fx-background-color: #27ae60;");
                    else if ("EN ATTENTE".equals(item)) label.setStyle(label.getStyle() + "-fx-background-color: #f39c12;");
                    else label.setStyle(label.getStyle() + "-fx-background-color: #95a5a6;");
                    setGraphic(label);
                }
            }
        });

        actionsColumn.setCellFactory(param -> new TableCell<Cabinet, Void>() {
            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    Cabinet c = getTableView().getItems().get(getIndex());
                    HBox box = new HBox(10);
                    box.setAlignment(javafx.geometry.Pos.CENTER_RIGHT);
                    box.setPadding(new javafx.geometry.Insets(0, 10, 0, 0));

                    if (!c.isValide() && !c.isArchive()) {
                        Button validBtn = new Button("Valider");
                        validBtn.setStyle("-fx-background-color: white; -fx-border-color: #27ae60; -fx-text-fill: #27ae60; -fx-border-radius: 15; -fx-background-radius: 15; -fx-font-size: 10; -fx-cursor: hand;");
                        validBtn.setOnAction(e -> handleUpdateStatus(c, "valide", true));
                        box.getChildren().add(validBtn);
                    }

                    if (!c.isArchive()) {
                        Button archBtn = new Button("Archiver");
                        archBtn.setStyle("-fx-background-color: white; -fx-border-color: #95a5a6; -fx-text-fill: #95a5a6; -fx-border-radius: 15; -fx-background-radius: 15; -fx-font-size: 10; -fx-cursor: hand;");
                        archBtn.setOnAction(e -> handleUpdateStatus(c, "archive", true));
                        box.getChildren().add(archBtn);
                    }

                    Button rejBtn = new Button("Rejeter");
                    rejBtn.setStyle("-fx-background-color: white; -fx-border-color: #e74c3c; -fx-text-fill: #e74c3c; -fx-border-radius: 15; -fx-background-radius: 15; -fx-font-size: 10; -fx-cursor: hand;");
                    rejBtn.setOnAction(e -> handleDelete(c));
                    box.getChildren().add(rejBtn);

                    setGraphic(box);
                }
            }
        });

        cabinetTable.setItems(cabinetData);
    }

    private void loadCabinets() {
        cabinetData.clear();
        String query = "SELECT * FROM cabinet ORDER BY id_cabinet DESC";
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            while (rs.next()) {
                Cabinet c = new Cabinet();
                c.setId(rs.getInt("id_cabinet"));
                c.setVille(rs.getString("ville"));
                c.setAdresse(rs.getString("adresse"));
                c.setHoraires(rs.getString("horaires"));
                c.setValide(rs.getBoolean("valide"));
                c.setArchive(rs.getBoolean("archive"));
                cabinetData.add(c);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void handleUpdateStatus(Cabinet c, String field, boolean value) {
        String query = "UPDATE cabinet SET " + field + " = ? WHERE id_cabinet = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setBoolean(1, value);
            pstmt.setInt(2, c.getId());
            pstmt.executeUpdate();
            loadCabinets();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void handleDelete(Cabinet c) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION, "Voulez-vous vraiment rejeter et supprimer ce cabinet ?", ButtonType.YES, ButtonType.NO);
        alert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.YES) {
                String query = "DELETE FROM cabinet WHERE id_cabinet = ?";
                try (Connection conn = DatabaseConnection.getConnection();
                     PreparedStatement pstmt = conn.prepareStatement(query)) {
                    pstmt.setInt(1, c.getId());
                    pstmt.executeUpdate();
                    loadCabinets();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }
}
