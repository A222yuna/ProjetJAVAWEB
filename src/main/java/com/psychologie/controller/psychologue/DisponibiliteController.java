package com.psychologie.controller.psychologue;

import com.psychologie.controller.MainController;
import com.psychologie.controller.SecurityController;
import com.psychologie.model.Cabinet;
import com.psychologie.model.Disponibilite;
import com.psychologie.model.User;
import com.psychologie.util.DatabaseConnection;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;

import java.sql.*;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

public class DisponibiliteController {

    @FXML private TableView<Disponibilite> dispoTable;
    @FXML private TableColumn<Disponibilite, String> jourColumn;
    @FXML private TableColumn<Disponibilite, LocalTime> debutColumn;
    @FXML private TableColumn<Disponibilite, LocalTime> finColumn;
    @FXML private TableColumn<Disponibilite, Integer> dureeColumn;
    @FXML private TableColumn<Disponibilite, Void> actionsColumn;

    private ObservableList<Disponibilite> dispoData = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        setupTable();
        loadDispoData();
    }

    private void setupTable() {
        jourColumn.setCellValueFactory(cellData -> {
            int jour = cellData.getValue().getJour();
            String[] jours = {"Lundi", "Mardi", "Mercredi", "Jeudi", "Vendredi", "Samedi", "Dimanche"};
            if (jour >= 1 && jour <= 7) return new SimpleStringProperty(jours[jour - 1]);
            return new SimpleStringProperty("Inconnu");
        });
        debutColumn.setCellValueFactory(new PropertyValueFactory<>("heureDebut"));
        finColumn.setCellValueFactory(new PropertyValueFactory<>("heureFin"));
        dureeColumn.setCellValueFactory(new PropertyValueFactory<>("dureeConsultation"));
        
        actionsColumn.setCellFactory(param -> new TableCell<>() {
            private final Button deleteBtn = new Button("Supprimer");
            {
                deleteBtn.setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white; -fx-cursor: hand;");
                deleteBtn.setOnAction(event -> {
                    Disponibilite d = getTableView().getItems().get(getIndex());
                    handleDelete(d);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) setGraphic(null);
                else setGraphic(deleteBtn);
            }
        });

        dispoTable.setItems(dispoData);
    }

    private void loadDispoData() {
        User user = SecurityController.getCurrentUser();
        if (user == null) return;

        dispoData.clear();
        String query = "SELECT d.* FROM disponibilite d " +
                       "JOIN cabinet c ON d.cabinet_id = c.id_cabinet " +
                       "JOIN psy_cabinet pc ON c.id_cabinet = pc.id_cabinet " +
                       "WHERE pc.psychologue_id_user = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            
            pstmt.setInt(1, user.getId());
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                Disponibilite d = new Disponibilite();
                d.setId(rs.getInt("id"));
                d.setJour(rs.getInt("jour"));
                d.setHeureDebut(rs.getTime("heure_debut").toLocalTime());
                d.setHeureFin(rs.getTime("heure_fin").toLocalTime());
                d.setDureeConsultation(rs.getInt("duree_consultation"));
                dispoData.add(d);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void handleDelete(Disponibilite d) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION, "Supprimer cette disponibilité ?", ButtonType.YES, ButtonType.NO);
        alert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.YES) {
                String query = "DELETE FROM disponibilite WHERE id = ?";
                try (Connection conn = DatabaseConnection.getConnection();
                     PreparedStatement pstmt = conn.prepareStatement(query)) {
                    pstmt.setInt(1, d.getId());
                    pstmt.executeUpdate();
                    loadDispoData();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    @FXML 
    public void handleNewDisponibilite() { 
        User user = SecurityController.getCurrentUser();
        if (user == null) return;

        // Fetch user's cabinets
        List<Cabinet> userCabinets = new ArrayList<>();
        String query = "SELECT c.* FROM cabinet c JOIN psy_cabinet pc ON c.id_cabinet = pc.id_cabinet WHERE pc.psychologue_id_user = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setInt(1, user.getId());
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                Cabinet c = new Cabinet();
                c.setId(rs.getInt("id_cabinet"));
                c.setVille(rs.getString("ville"));
                userCabinets.add(c);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        if (userCabinets.isEmpty()) {
            Alert alert = new Alert(Alert.AlertType.WARNING, "Vous devez d'abord ajouter un cabinet.");
            alert.showAndWait();
            return;
        }

        Dialog<Disponibilite> dialog = new Dialog<>();
        dialog.setTitle("Nouvelle Disponibilité");
        dialog.setHeaderText("Ajouter un créneau de disponibilité");

        ButtonType saveButtonType = new ButtonType("Enregistrer", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveButtonType, ButtonType.CANCEL);

        ComboBox<Cabinet> cabinetCombo = new ComboBox<>(FXCollections.observableArrayList(userCabinets));
        cabinetCombo.setCellFactory(param -> new ListCell<>() {
            @Override
            protected void updateItem(Cabinet item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) setText(null);
                else setText(item.getVille());
            }
        });
        cabinetCombo.setButtonCell(new ListCell<>() {
            @Override
            protected void updateItem(Cabinet item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) setText(null);
                else setText(item.getVille());
            }
        });

        ComboBox<String> jourCombo = new ComboBox<>(FXCollections.observableArrayList("Lundi", "Mardi", "Mercredi", "Jeudi", "Vendredi", "Samedi", "Dimanche"));
        TextField debutField = new TextField("09:00");
        TextField finField = new TextField("17:00");
        TextField dureeField = new TextField("45");

        javafx.scene.layout.GridPane grid = new javafx.scene.layout.GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new javafx.geometry.Insets(20, 150, 10, 10));

        grid.add(new Label("Cabinet:"), 0, 0);
        grid.add(cabinetCombo, 1, 0);
        grid.add(new Label("Jour:"), 0, 1);
        grid.add(jourCombo, 1, 1);
        grid.add(new Label("Heure début:"), 0, 2);
        grid.add(debutField, 1, 2);
        grid.add(new Label("Heure fin:"), 0, 3);
        grid.add(finField, 1, 3);
        grid.add(new Label("Durée (min):"), 0, 4);
        grid.add(dureeField, 1, 4);

        dialog.getDialogPane().setContent(grid);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == saveButtonType) {
                Disponibilite d = new Disponibilite();
                d.setCabinet(cabinetCombo.getValue());
                d.setJour(jourCombo.getSelectionModel().getSelectedIndex() + 1);
                d.setHeureDebut(LocalTime.parse(debutField.getText()));
                d.setHeureFin(LocalTime.parse(finField.getText()));
                d.setDureeConsultation(Integer.parseInt(dureeField.getText()));
                return d;
            }
            return null;
        });

        dialog.showAndWait().ifPresent(dispo -> {
            saveDispo(dispo);
            loadDispoData();
        });
    }

    private void saveDispo(Disponibilite d) {
        String query = "INSERT INTO disponibilite (cabinet_id, jour, heure_debut, heure_fin, duree_consultation) VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setInt(1, d.getCabinet().getId());
            pstmt.setInt(2, d.getJour());
            pstmt.setTime(3, Time.valueOf(d.getHeureDebut()));
            pstmt.setTime(4, Time.valueOf(d.getHeureFin()));
            pstmt.setInt(5, d.getDureeConsultation());
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @FXML public void showPlans() { 
        MainController.getInstance().showPsyPlans();
    }

    @FXML public void showAppointments() { 
        MainController.getInstance().showPsyPlanning();
    }
}
