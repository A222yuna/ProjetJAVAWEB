package com.psychologie.controller.admin;

import com.psychologie.model.Appointment;
import com.psychologie.model.User;
import com.psychologie.model.PsychologuePlan;
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
import java.util.List;
import java.util.stream.Collectors;

public class AdminAppointmentController {

    @FXML private TableView<Appointment> appointmentTable;
    @FXML private TableColumn<Appointment, String> idColumn;
    @FXML private TableColumn<Appointment, String> patientColumn;
    @FXML private TableColumn<Appointment, String> psyColumn;
    @FXML private TableColumn<Appointment, String> dateColumn;
    @FXML private TableColumn<Appointment, String> statusColumn;
    @FXML private TableColumn<Appointment, Void> actionsColumn;

    @FXML private ComboBox<String> statusFilter;
    @FXML private DatePicker datePicker;
    @FXML private Label totalLabel;

    private ObservableList<Appointment> allAppointments = FXCollections.observableArrayList();
    private ObservableList<Appointment> displayedAppointments = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        setupTable();
        loadAppointments();
        
        statusFilter.setItems(FXCollections.observableArrayList("Tous", Appointment.STATUS_SCHEDULED, Appointment.STATUS_CONFIRMED, Appointment.STATUS_PAID, Appointment.STATUS_COMPLETED, Appointment.STATUS_CANCELLED));
        statusFilter.setValue("Tous");
    }

    private void setupTable() {
        idColumn.setCellValueFactory(cellData -> new SimpleStringProperty("#" + cellData.getValue().getId()));
        patientColumn.setCellValueFactory(cellData -> new SimpleStringProperty(
            cellData.getValue().getPatient().getPrenom() + " " + cellData.getValue().getPatient().getNom()));
        
        psyColumn.setCellValueFactory(cellData -> {
            // In a real app, you'd fetch the psy name. For now we use the plan's psychologue if available.
            return new SimpleStringProperty(cellData.getValue().getPlan() != null ? 
                "Psy #" + cellData.getValue().getPlan().getPsychologue().getId() : "N/A");
        });

        dateColumn.setCellValueFactory(cellData -> {
            if (cellData.getValue().getPlan() != null) {
                return new SimpleStringProperty(cellData.getValue().getPlan().getDayOfWeek() + " (" + cellData.getValue().getPlan().getPeriod() + ")");
            }
            return new SimpleStringProperty("N/A");
        });

        statusColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getStatus()));
        statusColumn.setCellFactory(param -> new TableCell<Appointment, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) { setGraphic(null); return; }
                setGraphic(Appointment.statusBadge(item));
            }
        });

        actionsColumn.setCellFactory(param -> new TableCell<Appointment, Void>() {
            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    Appointment appt = getTableView().getItems().get(getIndex());
                    HBox box = new HBox(5);
                    box.setAlignment(javafx.geometry.Pos.CENTER);
                    
                    Button viewBtn = new Button("Voir");
                    viewBtn.setStyle("-fx-font-size: 10px;");
                    
                    ComboBox<String> statusCombo = new ComboBox<>(FXCollections.observableArrayList(Appointment.allStatuses()));
                    statusCombo.setValue(appt.getStatus());
                    statusCombo.setStyle("-fx-font-size: 10px;");
                    
                    Button majBtn = new Button("MAJ");
                    majBtn.setStyle("-fx-font-size: 10px; -fx-background-color: white; -fx-border-color: #3498db; -fx-text-fill: #3498db;");
                    majBtn.setOnAction(e -> handleUpdateStatus(appt, statusCombo.getValue()));

                    box.getChildren().addAll(viewBtn, statusCombo, majBtn);
                    setGraphic(box);
                }
            }
        });

        appointmentTable.setItems(displayedAppointments);
    }

    private void loadAppointments() {
        allAppointments.clear();
        String query = "SELECT a.*, p.day_of_week, p.period, p.psychologue_id_user, u.prenom, u.nom " +
                       "FROM appointments a " +
                       "LEFT JOIN psychologue_plans p ON a.plan_id = p.id " +
                       "LEFT JOIN users u ON a.patient_id_user = u.id_user " +
                       "ORDER BY a.created_at DESC";
        
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            
            while (rs.next()) {
                Appointment a = new Appointment();
                a.setId(rs.getInt("id"));
                a.setStatus(rs.getString("status"));
                
                User patient = new User();
                patient.setPrenom(rs.getString("prenom") != null ? rs.getString("prenom") : "Inconnu");
                patient.setNom(rs.getString("nom") != null ? rs.getString("nom") : "");
                a.setPatient(patient);

                PsychologuePlan plan = new PsychologuePlan();
                plan.setDayOfWeek(rs.getString("day_of_week") != null ? rs.getString("day_of_week") : "N/A");
                plan.setPeriod(rs.getString("period") != null ? rs.getString("period") : "N/A");
                User psy = new User();
                psy.setId(rs.getInt("psychologue_id_user"));
                plan.setPsychologue(psy);
                a.setPlan(plan);

                allAppointments.add(a);
            }
            applyFilters();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    public void applyFilters() {
        String status = statusFilter.getValue();
        List<Appointment> filtered = allAppointments.stream()
            .filter(a -> "Tous".equals(status) || a.getStatus().equals(status))
            .collect(Collectors.toList());
        
        displayedAppointments.setAll(filtered);
        totalLabel.setText(String.valueOf(displayedAppointments.size()));
    }

    private void handleUpdateStatus(Appointment appt, String newStatus) {
        String query = "UPDATE appointments SET status = ? WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setString(1, newStatus);
            pstmt.setInt(2, appt.getId());
            pstmt.executeUpdate();
            loadAppointments();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
