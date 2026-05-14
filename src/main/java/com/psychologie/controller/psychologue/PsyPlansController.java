package com.psychologie.controller.psychologue;

import com.psychologie.controller.MainController;
import com.psychologie.controller.SecurityController;
import com.psychologie.model.PsychologuePlan;
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
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class PsyPlansController {

    @FXML private TableView<PsychologuePlan> plansTable;
    @FXML private TableColumn<PsychologuePlan, String> jourColumn;
    @FXML private TableColumn<PsychologuePlan, String> periodeColumn;
    @FXML private TableColumn<PsychologuePlan, Integer> maxRdvColumn;
    @FXML private TableColumn<PsychologuePlan, String> creeLeColumn;
    @FXML private TableColumn<PsychologuePlan, Void> actionsColumn;

    private ObservableList<PsychologuePlan> plansData = FXCollections.observableArrayList();
    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    @FXML
    public void initialize() {
        setupTable();
        loadPlansData();
    }

    private void setupTable() {
        jourColumn.setCellValueFactory(new PropertyValueFactory<>("dayOfWeek"));
        periodeColumn.setCellValueFactory(new PropertyValueFactory<>("period"));
        maxRdvColumn.setCellValueFactory(new PropertyValueFactory<>("maxAppointments"));
        creeLeColumn.setCellValueFactory(cellData -> {
            LocalDateTime dt = cellData.getValue().getCreatedAt();
            return new SimpleStringProperty(dt != null ? dt.format(formatter) : "N/A");
        });

        actionsColumn.setCellFactory(param -> new TableCell<>() {
            private final Button editBtn = new Button("Edit");
            private final Button deleteBtn = new Button("Delete");
            private final HBox hbox = new HBox(5, editBtn, deleteBtn);

            {
                editBtn.getStyleClass().add("btn-primary");
                editBtn.setStyle("-fx-font-size: 10px; -fx-padding: 3 8;");
                editBtn.setOnAction(event -> handleEdit(getTableView().getItems().get(getIndex())));

                deleteBtn.getStyleClass().add("btn-danger");
                deleteBtn.setStyle("-fx-font-size: 10px; -fx-padding: 3 8; -fx-background-color: #e74c3c; -fx-text-fill: white;");
                deleteBtn.setOnAction(event -> handleDelete(getTableView().getItems().get(getIndex())));
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) setGraphic(null);
                else setGraphic(hbox);
            }
        });

        plansTable.setItems(plansData);
    }

    private void loadPlansData() {
        User user = SecurityController.getCurrentUser();
        if (user == null) return;

        plansData.clear();
        String query = "SELECT * FROM psychologue_plans WHERE psychologue_id_user = ? ORDER BY created_at DESC";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            
            pstmt.setInt(1, user.getId());
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                PsychologuePlan p = new PsychologuePlan();
                p.setId(rs.getInt("id"));
                p.setDayOfWeek(rs.getString("day_of_week"));
                p.setPeriod(rs.getString("period"));
                p.setMaxAppointments(rs.getInt("max_appointments"));
                Timestamp ts = rs.getTimestamp("created_at");
                if (ts != null) p.setCreatedAt(ts.toLocalDateTime());
                plansData.add(p);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @FXML
    public void handleNewPlan() {
        // Implement dialog for new plan
        Dialog<PsychologuePlan> dialog = new Dialog<>();
        dialog.setTitle("Nouveau Plan");
        dialog.setHeaderText("Créer un nouveau créneau de planning");

        ButtonType saveButtonType = new ButtonType("Enregistrer", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveButtonType, ButtonType.CANCEL);

        ComboBox<String> dayCombo = new ComboBox<>(FXCollections.observableArrayList(PsychologuePlan.DAY_OF_WEEK_CHOICES));
        dayCombo.setPromptText("Jour");
        ComboBox<String> periodCombo = new ComboBox<>(FXCollections.observableArrayList(PsychologuePlan.PERIOD_CHOICES));
        periodCombo.setPromptText("Période");
        TextField maxRdvField = new TextField("5");
        maxRdvField.setPromptText("Max RDV");

        javafx.scene.layout.GridPane grid = new javafx.scene.layout.GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new javafx.geometry.Insets(20, 150, 10, 10));

        grid.add(new Label("Jour:"), 0, 0);
        grid.add(dayCombo, 1, 0);
        grid.add(new Label("Période:"), 0, 1);
        grid.add(periodCombo, 1, 1);
        grid.add(new Label("Max RDV:"), 0, 2);
        grid.add(maxRdvField, 1, 2);

        dialog.getDialogPane().setContent(grid);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == saveButtonType) {
                PsychologuePlan p = new PsychologuePlan();
                p.setDayOfWeek(dayCombo.getValue());
                p.setPeriod(periodCombo.getValue());
                p.setMaxAppointments(Integer.parseInt(maxRdvField.getText()));
                return p;
            }
            return null;
        });

        dialog.showAndWait().ifPresent(plan -> {
            savePlan(plan);
            loadPlansData();
        });
    }

    private void savePlan(PsychologuePlan p) {
        String query = "INSERT INTO psychologue_plans (psychologue_id_user, day_of_week, period, max_appointments, created_at) VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setInt(1, SecurityController.getCurrentUser().getId());
            pstmt.setString(2, p.getDayOfWeek());
            pstmt.setString(3, p.getPeriod());
            pstmt.setInt(4, p.getMaxAppointments());
            pstmt.setTimestamp(5, Timestamp.valueOf(LocalDateTime.now()));
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void handleEdit(PsychologuePlan p) {
        Dialog<PsychologuePlan> dialog = new Dialog<>();
        dialog.setTitle("Modifier Plan");
        dialog.setHeaderText("Modifier le créneau de planning");

        ButtonType saveButtonType = new ButtonType("Enregistrer", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveButtonType, ButtonType.CANCEL);

        ComboBox<String> dayCombo = new ComboBox<>(FXCollections.observableArrayList(PsychologuePlan.DAY_OF_WEEK_CHOICES));
        dayCombo.setValue(p.getDayOfWeek());
        ComboBox<String> periodCombo = new ComboBox<>(FXCollections.observableArrayList(PsychologuePlan.PERIOD_CHOICES));
        periodCombo.setValue(p.getPeriod());
        TextField maxRdvField = new TextField(String.valueOf(p.getMaxAppointments()));

        javafx.scene.layout.GridPane grid = new javafx.scene.layout.GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new javafx.geometry.Insets(20, 150, 10, 10));

        grid.add(new Label("Jour:"), 0, 0);
        grid.add(dayCombo, 1, 0);
        grid.add(new Label("Période:"), 0, 1);
        grid.add(periodCombo, 1, 1);
        grid.add(new Label("Max RDV:"), 0, 2);
        grid.add(maxRdvField, 1, 2);

        dialog.getDialogPane().setContent(grid);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == saveButtonType) {
                p.setDayOfWeek(dayCombo.getValue());
                p.setPeriod(periodCombo.getValue());
                p.setMaxAppointments(Integer.parseInt(maxRdvField.getText()));
                return p;
            }
            return null;
        });

        dialog.showAndWait().ifPresent(updatedPlan -> {
            updatePlan(updatedPlan);
            loadPlansData();
        });
    }

    private void updatePlan(PsychologuePlan p) {
        String query = "UPDATE psychologue_plans SET day_of_week = ?, period = ?, max_appointments = ? WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setString(1, p.getDayOfWeek());
            pstmt.setString(2, p.getPeriod());
            pstmt.setInt(3, p.getMaxAppointments());
            pstmt.setInt(4, p.getId());
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void handleDelete(PsychologuePlan p) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION, "Supprimer ce plan ?", ButtonType.YES, ButtonType.NO);
        alert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.YES) {
                String query = "DELETE FROM psychologue_plans WHERE id = ?";
                try (Connection conn = DatabaseConnection.getConnection();
                     PreparedStatement pstmt = conn.prepareStatement(query)) {
                    pstmt.setInt(1, p.getId());
                    pstmt.executeUpdate();
                    loadPlansData();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    @FXML public void showDisponibilites() {
        MainController.getInstance().showAppointments();
    }

    @FXML public void showAppointments() {
        MainController.getInstance().showPsyPlanning();
    }
}
