package com.psychologie.controller.psychologue;

import com.psychologie.controller.MainController;
import com.psychologie.controller.SecurityController;
import com.psychologie.model.Appointment;
import com.psychologie.model.User;
import com.psychologie.util.DatabaseConnection;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import java.sql.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class PsyDashboardController {

    @FXML private Label dateLabel;
    @FXML private Label appointmentCountLabel;
    @FXML private Label dispoStatusLabel;
    @FXML private TableView<Appointment> requestTable;
    @FXML private TableColumn<Appointment, Appointment> patientColumn;
    @FXML private TableColumn<Appointment, String> dateColumn;
    @FXML private TableColumn<Appointment, String> statusColumn;
    @FXML private TableColumn<Appointment, Void> actionColumn;

    private ObservableList<Appointment> appointmentData = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        dateLabel.setText(LocalDate.now().format(DateTimeFormatter.ofPattern("dd MMMM yyyy")));
        setupTable();
        loadDashboardData();
    }

    private void setupTable() {
        patientColumn.setCellValueFactory(cellData -> new SimpleObjectProperty<>(cellData.getValue()));
        patientColumn.setCellFactory(param -> new TableCell<>() {
            @Override
            protected void updateItem(Appointment item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null || item.getPatient() == null) {
                    setGraphic(null);
                } else {
                    HBox hbox = new HBox(10);
                    hbox.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
                    
                    StackPane avatar = new StackPane();
                    avatar.setStyle("-fx-background-color: #f2f4f4; -fx-background-radius: 15; -fx-min-width: 30; -fx-min-height: 30;");
                    String initials = "";
                    if (item.getPatient().getPrenom() != null && !item.getPatient().getPrenom().isEmpty()) {
                        initials += item.getPatient().getPrenom().charAt(0);
                    }
                    if (item.getPatient().getNom() != null && !item.getPatient().getNom().isEmpty()) {
                        initials += item.getPatient().getNom().charAt(0);
                    }
                    Label initialLabel = new Label(initials.toLowerCase());
                    initialLabel.setStyle("-fx-font-size: 10px; -fx-text-fill: #95a5a6;");
                    avatar.getChildren().add(initialLabel);

                    Label name = new Label(item.getPatient().getPrenom() + " " + item.getPatient().getNom());
                    hbox.getChildren().addAll(avatar, name);
                    setGraphic(hbox);
                }
            }
        });

        dateColumn.setCellValueFactory(cellData -> {
            if (cellData.getValue().getPlan() != null) {
                return new SimpleStringProperty(cellData.getValue().getPlan().getDayOfWeek() + "\n" + cellData.getValue().getPlan().getPeriod());
            }
            return new SimpleStringProperty("N/A");
        });

        statusColumn.setCellValueFactory(new javafx.scene.control.cell.PropertyValueFactory<>("status"));
        statusColumn.setCellFactory(param -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) { setGraphic(null); return; }
                setGraphic(Appointment.statusBadge(item));
            }
        });

        actionColumn.setCellFactory(param -> new TableCell<>() {
            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    HBox box = new HBox(5);
                    box.setAlignment(javafx.geometry.Pos.CENTER);
                    
                    Button acceptBtn = new Button("✓");
                    acceptBtn.setStyle("-fx-background-color: #27ae60; -fx-text-fill: white; -fx-cursor: hand; -fx-background-radius: 10; -fx-font-size: 10;");
                    acceptBtn.setOnAction(e -> handleUpdateStatus(getTableView().getItems().get(getIndex()), Appointment.STATUS_CONFIRMED));

                    Button rejectBtn = new Button("✕");
                    rejectBtn.setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white; -fx-cursor: hand; -fx-background-radius: 10; -fx-font-size: 10;");
                    rejectBtn.setOnAction(e -> handleUpdateStatus(getTableView().getItems().get(getIndex()), Appointment.STATUS_CANCELLED));

                    Appointment appt = getTableView().getItems().get(getIndex());
                    if (Appointment.STATUS_SCHEDULED.equalsIgnoreCase(appt.getStatus())) {
                        box.getChildren().addAll(acceptBtn, rejectBtn);
                    } else {
                        Button viewBtn = new Button("👁");
                        viewBtn.setStyle("-fx-background-color: transparent; -fx-cursor: hand; -fx-opacity: 0.6;");
                        box.getChildren().add(viewBtn);
                    }
                    
                    setGraphic(box);
                }
            }
        });

        requestTable.setItems(appointmentData);
    }

    private void handleUpdateStatus(Appointment appointment, String newStatus) {
        String query = "UPDATE appointments SET status = ? WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setString(1, newStatus);
            pstmt.setInt(2, appointment.getId());
            pstmt.executeUpdate();
            loadDashboardData();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @FXML
    public void handleAddPlanning() {
        MainController.getInstance().showPsyPlans();
    }

    @FXML
    public void handleShowHoraires() {
        MainController.getInstance().showAppointments();
    }

    private void loadDashboardData() {
        User psy = SecurityController.getCurrentUser();
        if (psy == null) return;

        appointmentData.clear();
        String query = "SELECT a.*, p.day_of_week, p.period, u.prenom, u.nom " +
                       "FROM appointments a " +
                       "JOIN psychologue_plans p ON a.plan_id = p.id " +
                       "JOIN users u ON a.patient_id_user = u.id_user " +
                       "WHERE p.psychologue_id_user = ? " +
                       "ORDER BY a.created_at DESC LIMIT 5";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            
            pstmt.setInt(1, psy.getId());
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                Appointment a = new Appointment();
                a.setId(rs.getInt("id"));
                a.setStatus(rs.getString("status"));
                
                User patient = new User();
                patient.setPrenom(rs.getString("prenom"));
                patient.setNom(rs.getString("nom"));
                a.setPatient(patient);

                com.psychologie.model.PsychologuePlan plan = new com.psychologie.model.PsychologuePlan();
                plan.setDayOfWeek(rs.getString("day_of_week"));
                plan.setPeriod(rs.getString("period"));
                a.setPlan(plan);

                appointmentData.add(a);
            }
            
            // Total appointment count
            String countQuery = "SELECT COUNT(*) FROM appointments a " +
                                "JOIN psychologue_plans p ON a.plan_id = p.id " +
                                "WHERE p.psychologue_id_user = ?";
            PreparedStatement pstmtCount = conn.prepareStatement(countQuery);
            pstmtCount.setInt(1, psy.getId());
            ResultSet rsCount = pstmtCount.executeQuery();
            if (rsCount.next()) {
                appointmentCountLabel.setText(String.valueOf(rsCount.getInt(1)));
            }
            
            // Check dispo status — Active if psy has at least one disponibilite linked to their cabinets
            String dispoQuery = "SELECT COUNT(*) FROM disponibilite d " +
                                "JOIN psy_cabinet pc ON d.cabinet_id = pc.id_cabinet " +
                                "WHERE pc.psychologue_id_user = ?";
            PreparedStatement pstmtDispo = conn.prepareStatement(dispoQuery);
            pstmtDispo.setInt(1, psy.getId());
            ResultSet rsDispo = pstmtDispo.executeQuery();
            if (rsDispo.next() && rsDispo.getInt(1) > 0) {
                dispoStatusLabel.setText("Active");
                dispoStatusLabel.setStyle("-fx-text-fill: #27ae60; -fx-font-weight: bold;");
            } else {
                dispoStatusLabel.setText("Inactive");
                dispoStatusLabel.setStyle("-fx-text-fill: #e74c3c; -fx-font-weight: bold;");
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
