package com.psychologie.controller.admin;

import com.psychologie.model.Appointment;
import com.psychologie.model.PsychologuePlan;
import com.psychologie.model.User;
import com.psychologie.util.DatabaseConnection;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;

import java.sql.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

public class AdminAppointmentController {

    @FXML private TableView<Appointment>           appointmentTable;
    @FXML private TableColumn<Appointment, String> idColumn;
    @FXML private TableColumn<Appointment, String> patientColumn;
    @FXML private TableColumn<Appointment, String> psyColumn;
    @FXML private TableColumn<Appointment, String> dateColumn;
    @FXML private TableColumn<Appointment, String> statusColumn;
    @FXML private TableColumn<Appointment, Void>   actionsColumn;

    @FXML private ComboBox<String> statusFilter;
    @FXML private DatePicker       datePicker;
    @FXML private Label            totalLabel;
    @FXML private Label            dateLabel;

    private final ObservableList<Appointment> allAppointments       = FXCollections.observableArrayList();
    private final ObservableList<Appointment> displayedAppointments = FXCollections.observableArrayList();

    private static final DateTimeFormatter DATE_FMT =
            DateTimeFormatter.ofPattern("dd/MM/yyyy");

    @FXML
    public void initialize() {
        if (dateLabel != null)
            dateLabel.setText(LocalDate.now()
                    .format(DateTimeFormatter.ofPattern("dd MMMM yyyy", java.util.Locale.FRENCH)));

        statusFilter.setItems(FXCollections.observableArrayList(
                "Tous",
                Appointment.STATUS_SCHEDULED,
                Appointment.STATUS_CONFIRMED,
                Appointment.STATUS_PAID,
                Appointment.STATUS_COMPLETED,
                Appointment.STATUS_CANCELLED));
        statusFilter.setValue("Tous");

        setupTable();
        loadAppointments();
    }

    // ─────────────────────────────────────────────
    //  Table setup
    // ─────────────────────────────────────────────

    private void setupTable() {
        idColumn.setCellValueFactory(cd ->
                new SimpleStringProperty("#" + cd.getValue().getId()));

        patientColumn.setCellValueFactory(cd -> {
            User p = cd.getValue().getPatient();
            return new SimpleStringProperty(p != null
                    ? p.getPrenom() + " " + p.getNom() : "—");
        });

        // Real psychologue name from the plan
        psyColumn.setCellValueFactory(cd -> {
            PsychologuePlan plan = cd.getValue().getPlan();
            if (plan != null && plan.getPsychologue() != null) {
                User psy = plan.getPsychologue();
                return new SimpleStringProperty(
                        psy.getPrenom() + " " + psy.getNom());
            }
            return new SimpleStringProperty("—");
        });

        // created_at formatted as dd/MM/yyyy
        dateColumn.setCellValueFactory(cd -> {
            if (cd.getValue().getCreatedAt() != null)
                return new SimpleStringProperty(
                        cd.getValue().getCreatedAt().toLocalDate().format(DATE_FMT));
            return new SimpleStringProperty("—");
        });

        // Status badge
        statusColumn.setCellValueFactory(cd ->
                new SimpleStringProperty(cd.getValue().getStatus()));
        statusColumn.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null || item.isBlank()) { setGraphic(null); return; }
                setGraphic(Appointment.statusBadge(item));
            }
        });

        // Actions: Voir | status combo | MAJ
        actionsColumn.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) { setGraphic(null); return; }

                Appointment appt = getTableView().getItems().get(getIndex());
                HBox box = new HBox(6);
                box.setAlignment(Pos.CENTER_LEFT);

                Button viewBtn = new Button("Voir");
                viewBtn.setStyle("-fx-font-size:10px; -fx-background-color:#f8f9fa;" +
                                 "-fx-border-color:#dee2e6; -fx-cursor:hand; -fx-background-radius:4;");

                ComboBox<String> combo = new ComboBox<>(
                        FXCollections.observableArrayList(Appointment.allStatuses()));
                combo.setValue(appt.getStatus() != null ? appt.getStatus() : Appointment.STATUS_SCHEDULED);
                combo.setStyle("-fx-font-size:10px;");
                combo.setPrefWidth(130);

                Button majBtn = new Button("MAJ");
                majBtn.setStyle("-fx-font-size:10px; -fx-background-color:#3498db;" +
                                "-fx-text-fill:white; -fx-cursor:hand; -fx-background-radius:4;");
                majBtn.setOnAction(e -> handleUpdateStatus(appt, combo.getValue()));

                box.getChildren().addAll(viewBtn, combo, majBtn);
                setGraphic(box);
            }
        });

        appointmentTable.setItems(displayedAppointments);
    }

    // ─────────────────────────────────────────────
    //  Data loading — JOIN psy user for real name
    // ─────────────────────────────────────────────

    private void loadAppointments() {
        allAppointments.clear();

        String query =
            "SELECT a.id, a.status, a.created_at, " +
            "       pat.prenom pat_prenom, pat.nom pat_nom, " +
            "       psy.prenom psy_prenom, psy.nom psy_nom, " +
            "       p.day_of_week, p.period " +
            "FROM appointments a " +
            "LEFT JOIN users pat ON a.patient_id_user = pat.id_user " +
            "LEFT JOIN psychologue_plans p ON a.plan_id = p.id " +
            "LEFT JOIN users psy ON p.psychologue_id_user = psy.id_user " +
            "ORDER BY a.created_at DESC";

        try (Connection conn = DatabaseConnection.getConnection();
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(query)) {

            while (rs.next()) {
                Appointment a = new Appointment();
                a.setId(rs.getInt("id"));
                a.setStatus(rs.getString("status") != null
                        ? rs.getString("status") : Appointment.STATUS_SCHEDULED);

                // created_at → LocalDateTime
                Timestamp ts = rs.getTimestamp("created_at");
                if (ts != null) a.setCreatedAt(ts.toLocalDateTime());

                // Patient
                User patient = new User();
                patient.setPrenom(safe(rs.getString("pat_prenom")));
                patient.setNom(safe(rs.getString("pat_nom")));
                a.setPatient(patient);

                // Plan + real psy name
                PsychologuePlan plan = new PsychologuePlan();
                plan.setDayOfWeek(safe(rs.getString("day_of_week")));
                plan.setPeriod(safe(rs.getString("period")));
                User psy = new User();
                psy.setPrenom(safe(rs.getString("psy_prenom")));
                psy.setNom(safe(rs.getString("psy_nom")));
                plan.setPsychologue(psy);
                a.setPlan(plan);

                allAppointments.add(a);
            }
            applyFilters();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // ─────────────────────────────────────────────
    //  Filters — status + date
    // ─────────────────────────────────────────────

    @FXML
    public void applyFilters() {
        String status      = statusFilter.getValue();
        LocalDate dateFilter = datePicker != null ? datePicker.getValue() : null;

        List<Appointment> filtered = allAppointments.stream().filter(a -> {
            // Status filter
            boolean matchStatus = "Tous".equals(status)
                    || (a.getStatus() != null && a.getStatus().equals(status));

            // Date filter — match on created_at date
            boolean matchDate = dateFilter == null
                    || (a.getCreatedAt() != null
                        && a.getCreatedAt().toLocalDate().equals(dateFilter));

            return matchStatus && matchDate;
        }).collect(Collectors.toList());

        displayedAppointments.setAll(filtered);
        if (totalLabel != null) totalLabel.setText(String.valueOf(filtered.size()));
    }

    // ─────────────────────────────────────────────
    //  Update status
    // ─────────────────────────────────────────────

    private void handleUpdateStatus(Appointment appt, String newStatus) {
        if (newStatus == null || newStatus.equals(appt.getStatus())) return;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(
                     "UPDATE appointments SET status=? WHERE id=?")) {
            ps.setString(1, newStatus);
            ps.setInt(2, appt.getId());
            ps.executeUpdate();
            loadAppointments();
        } catch (Exception e) {
            e.printStackTrace();
            new Alert(Alert.AlertType.ERROR, "Erreur mise à jour : " + e.getMessage()).showAndWait();
        }
    }

    @FXML
    public void handleReset() {
        statusFilter.setValue("Tous");
        if (datePicker != null) datePicker.setValue(null);
        applyFilters();
    }

    private String safe(String s) { return s != null ? s : ""; }
}
