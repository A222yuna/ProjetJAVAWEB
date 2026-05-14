package com.psychologie.controller.psychologue;

import com.psychologie.controller.MainController;
import com.psychologie.controller.SecurityController;
import com.psychologie.model.Cabinet;
import com.psychologie.model.User;
import com.psychologie.util.DatabaseConnection;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class PsyCabinetsController {

    @FXML private FlowPane cabinetsContainer;

    @FXML
    public void initialize() {
        loadCabinets();
    }

    private void loadCabinets() {
        User user = SecurityController.getCurrentUser();
        if (user == null) return;

        cabinetsContainer.getChildren().clear();
        String query = "SELECT c.* FROM cabinet c " +
                       "JOIN psy_cabinet pc ON c.id_cabinet = pc.id_cabinet " +
                       "WHERE pc.psychologue_id_user = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            
            pstmt.setInt(1, user.getId());
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                Cabinet c = new Cabinet();
                c.setId(rs.getInt("id_cabinet"));
                c.setVille(rs.getString("ville"));
                c.setAdresse(rs.getString("adresse"));
                c.setHoraires(rs.getString("horaires"));
                c.setDescription(rs.getString("description"));
                c.setValide(rs.getBoolean("valide"));
                c.setArchive(rs.getBoolean("archive"));
                
                cabinetsContainer.getChildren().add(createCabinetCard(c));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private VBox createCabinetCard(Cabinet c) {
        VBox card = new VBox(15);
        card.setStyle("-fx-background-color: white; -fx-background-radius: 15; -fx-padding: 20; " +
                     "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.1), 10, 0, 0, 5);");
        card.setMinWidth(250);
        card.setPrefWidth(250);

        HBox header = new HBox(10);
        header.setAlignment(Pos.CENTER_LEFT);
        
        Label villeLabel = new Label(c.getVille());
        villeLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #3498db;");
        
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        
        Label statusBadge = new Label(c.isValide() ? "VALIDE" : "EN ATTENTE");
        statusBadge.setStyle("-fx-padding: 3 10; -fx-background-radius: 10; -fx-font-size: 10px; -fx-font-weight: bold; -fx-text-fill: " + 
                            (c.isValide() ? "#27ae60;" : "#f39c12;") + " -fx-background-color: " + 
                            (c.isValide() ? "#e8f8f5;" : "#fef9e7;"));
        
        header.getChildren().addAll(villeLabel, spacer, statusBadge);

        Label adresseLabel = new Label(c.getAdresse());
        adresseLabel.setStyle("-fx-text-fill: #95a5a6; -fx-font-size: 12px;");
        adresseLabel.setWrapText(true);

        HBox horairesBox = new HBox(5);
        horairesBox.setAlignment(Pos.CENTER_LEFT);
        Label clockIcon = new Label("🕒");
        Label horairesLabel = new Label(c.getHoraires());
        horairesLabel.setStyle("-fx-text-fill: #7f8c8d; -fx-font-size: 12px;");
        horairesBox.getChildren().addAll(clockIcon, horairesLabel);

        Button detailsBtn = new Button("Détails");
        detailsBtn.setMaxWidth(Double.MAX_VALUE);
        detailsBtn.setStyle("-fx-background-color: transparent; -fx-border-color: #3498db; -fx-border-radius: 15; " +
                           "-fx-text-fill: #3498db; -fx-cursor: hand;");
        detailsBtn.setOnAction(e -> showCabinetDetails(c));
        
        card.getChildren().addAll(header, adresseLabel, horairesBox, detailsBtn);
        return card;
    }

    private void showCabinetDetails(Cabinet cabinet) {
        Dialog<Void> dialog = new Dialog<>();
        dialog.setTitle("Details du cabinet");
        dialog.setHeaderText(nullSafe(cabinet.getVille()));
        dialog.getDialogPane().getButtonTypes().add(ButtonType.CLOSE);

        VBox content = new VBox(14);
        content.setPadding(new Insets(10));
        content.setPrefWidth(430);

        content.getChildren().addAll(
                detailRow("Adresse", cabinet.getAdresse()),
                detailRow("Horaires", cabinet.getHoraires()),
                detailRow("Description", cabinet.getDescription()),
                detailRow("Statut", cabinet.isValide() ? "VALIDE" : "EN ATTENTE"),
                detailRow("Archive", cabinet.isArchive() ? "Oui" : "Non"),
                availabilityBlock(cabinet.getId())
        );

        dialog.getDialogPane().setContent(content);
        dialog.showAndWait();
    }

    private HBox detailRow(String label, String value) {
        HBox row = new HBox(12);
        row.setAlignment(Pos.TOP_LEFT);

        Label key = new Label(label + " :");
        key.setMinWidth(95);
        key.setStyle("-fx-font-weight: bold; -fx-text-fill: #1c2824;");

        Label val = new Label(nullSafe(value).isBlank() ? "-" : value);
        val.setWrapText(true);
        val.setStyle("-fx-text-fill: #5a6560;");
        HBox.setHgrow(val, Priority.ALWAYS);

        row.getChildren().addAll(key, val);
        return row;
    }

    private VBox availabilityBlock(Integer cabinetId) {
        VBox block = new VBox(8);
        Label title = new Label("Disponibilites");
        title.setStyle("-fx-font-weight: bold; -fx-text-fill: #1c2824;");
        block.getChildren().add(title);

        String query = "SELECT jour, heure_debut, heure_fin, duree_consultation " +
                "FROM disponibilite WHERE cabinet_id = ? ORDER BY jour, heure_debut";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setInt(1, cabinetId);
            ResultSet rs = pstmt.executeQuery();

            boolean hasRows = false;
            while (rs.next()) {
                hasRows = true;
                String text = "Jour " + rs.getInt("jour") + " - " +
                        formatTime(rs.getTime("heure_debut")) + " / " +
                        formatTime(rs.getTime("heure_fin")) + " (" +
                        rs.getInt("duree_consultation") + " min)";
                Label item = new Label(text);
                item.setStyle("-fx-text-fill: #5a6560;");
                block.getChildren().add(item);
            }

            if (!hasRows) {
                Label empty = new Label("Aucune disponibilite pour ce cabinet.");
                empty.setStyle("-fx-text-fill: #95a5a6;");
                block.getChildren().add(empty);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            Label error = new Label("Impossible de charger les disponibilites.");
            error.setStyle("-fx-text-fill: #dc3545;");
            block.getChildren().add(error);
        }

        return block;
    }

    private String formatTime(Time time) {
        return time == null ? "--:--" : time.toLocalTime().toString();
    }

    private String nullSafe(String value) {
        return value == null ? "" : value;
    }

    @FXML
    public void handleAddCabinet() {
        // Dialog for adding a cabinet
        Dialog<Cabinet> dialog = new Dialog<>();
        dialog.setTitle("Ajouter un cabinet");
        dialog.setHeaderText("Demander l'ajout d'un nouveau cabinet");

        ButtonType addButtonType = new ButtonType("Ajouter", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(addButtonType, ButtonType.CANCEL);

        TextField villeField = new TextField();
        villeField.setPromptText("Ville");
        TextField adresseField = new TextField();
        adresseField.setPromptText("Adresse");
        TextField horairesField = new TextField();
        horairesField.setPromptText("Ex: lun-ven 09:00-18:00");
        TextArea descArea = new TextArea();
        descArea.setPromptText("Description...");
        descArea.setPrefRowCount(3);

        VBox content = new VBox(10);
        content.getChildren().addAll(
            new Label("Ville:"), villeField,
            new Label("Adresse:"), adresseField,
            new Label("Horaires:"), horairesField,
            new Label("Description:"), descArea
        );
        dialog.getDialogPane().setContent(content);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == addButtonType) {
                Cabinet c = new Cabinet();
                c.setVille(villeField.getText());
                c.setAdresse(adresseField.getText());
                c.setHoraires(horairesField.getText());
                c.setDescription(descArea.getText());
                return c;
            }
            return null;
        });

        dialog.showAndWait().ifPresent(cabinet -> {
            saveCabinetRequest(cabinet);
            loadCabinets();
        });
    }

    private void saveCabinetRequest(Cabinet c) {
        String queryCabinet = "INSERT INTO cabinet (ville, adresse, horaires, description, valide, archive) VALUES (?, ?, ?, ?, ?, ?)";
        String queryPsyCabinet = "INSERT INTO psy_cabinet (psychologue_id_user, id_cabinet) VALUES (?, ?)";
        
        try (Connection conn = DatabaseConnection.getConnection()) {
            conn.setAutoCommit(false);
            try (PreparedStatement pstmt = conn.prepareStatement(queryCabinet, Statement.RETURN_GENERATED_KEYS)) {
                pstmt.setString(1, c.getVille());
                pstmt.setString(2, c.getAdresse());
                pstmt.setString(3, c.getHoraires());
                pstmt.setString(4, c.getDescription());
                pstmt.setBoolean(5, false); // Not valid yet
                pstmt.setBoolean(6, false);
                pstmt.executeUpdate();

                ResultSet rs = pstmt.getGeneratedKeys();
                if (rs.next()) {
                    int cabinetId = rs.getInt(1);
                    try (PreparedStatement pstmt2 = conn.prepareStatement(queryPsyCabinet)) {
                        pstmt2.setInt(1, SecurityController.getCurrentUser().getId());
                        pstmt2.setInt(2, cabinetId);
                        pstmt2.executeUpdate();
                    }
                }
                conn.commit();
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
