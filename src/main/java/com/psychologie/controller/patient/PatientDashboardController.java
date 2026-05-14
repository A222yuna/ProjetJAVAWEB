package com.psychologie.controller.patient;

import com.psychologie.controller.MainController;
import com.psychologie.controller.SecurityController;
import com.psychologie.model.Appointment;
import com.psychologie.model.User;
import com.psychologie.util.DatabaseConnection;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;

import java.sql.*;

public class PatientDashboardController {

    @FXML private Label welcomeLabel;
    @FXML private Label rdvCountLabel;
    @FXML private Label bienEtreLabel;

    @FXML private TableView<Appointment>              appointmentTable;
    @FXML private TableColumn<Appointment, Appointment> psyColumn;
    @FXML private TableColumn<Appointment, String>    dateColumn;
    @FXML private TableColumn<Appointment, String>    statusColumn;
    @FXML private TableColumn<Appointment, Void>      actionColumn;

    private final ObservableList<Appointment> data = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        User user = SecurityController.getCurrentUser();
        if (user == null) return;

        welcomeLabel.setText("Bienvenue, " + user.getPrenom());
        setupTable();
        loadData(user.getId());
    }

    // ─────────────────────────────────────────────
    //  Table setup
    // ─────────────────────────────────────────────

    private void setupTable() {
        // Praticien column — avatar + name
        psyColumn.setCellValueFactory(cd -> new javafx.beans.property.SimpleObjectProperty<>(cd.getValue()));
        psyColumn.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(Appointment item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null || item.getPlan() == null
                        || item.getPlan().getPsychologue() == null) {
                    setGraphic(null); return;
                }
                User psy = item.getPlan().getPsychologue();
                HBox box = new HBox(8);
                box.setAlignment(Pos.CENTER_LEFT);

                // Avatar circle
                StackPane avatar = new StackPane();
                avatar.setStyle("-fx-background-color: #e6f0ec; -fx-background-radius: 14;" +
                                "-fx-min-width: 28; -fx-min-height: 28;");
                String initials = "";
                if (psy.getPrenom() != null && !psy.getPrenom().isEmpty())
                    initials += psy.getPrenom().charAt(0);
                if (psy.getNom() != null && !psy.getNom().isEmpty())
                    initials += psy.getNom().charAt(0);
                Label init = new Label(initials.toLowerCase());
                init.setStyle("-fx-font-size: 10px; -fx-text-fill: #2a6f5b; -fx-font-weight: bold;");
                avatar.getChildren().add(init);

                Label name = new Label("Psy " + psy.getPrenom() + " " + psy.getNom());
                name.setStyle("-fx-font-size: 12px;");
                box.getChildren().addAll(avatar, name);
                setGraphic(box);
            }
        });

        // Date & Heure column
        dateColumn.setCellValueFactory(cd -> {
            if (cd.getValue().getPlan() != null) {
                return new SimpleStringProperty(
                    cd.getValue().getPlan().getDayOfWeek() + "\n" +
                    cd.getValue().getPlan().getPeriod());
            }
            return new SimpleStringProperty("N/A");
        });
        dateColumn.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) { setGraphic(null); return; }
                String[] parts = item.split("\n");
                javafx.scene.layout.VBox vb = new javafx.scene.layout.VBox(2);
                Label day = new Label("📅 " + (parts.length > 0 ? parts[0] : ""));
                day.setStyle("-fx-font-size: 11px;");
                Label period = new Label("🕒 " + (parts.length > 1 ? parts[1] : ""));
                period.setStyle("-fx-font-size: 11px; -fx-text-fill: #5a6560;");
                vb.getChildren().addAll(day, period);
                setGraphic(vb);
            }
        });

        // Status column — colored badge
        statusColumn.setCellValueFactory(cd -> new SimpleStringProperty(cd.getValue().getStatus()));
        statusColumn.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) { setGraphic(null); return; }
                setGraphic(Appointment.statusBadge(item));
            }
        });

        // Action column — view eye icon
        actionColumn.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) { setGraphic(null); return; }
                Button btn = new Button("👁");
                btn.setStyle("-fx-background-color: transparent; -fx-cursor: hand; -fx-font-size: 14px;");
                btn.setTooltip(new Tooltip("Voir les détails"));
                setGraphic(btn);
            }
        });

        appointmentTable.setItems(data);
    }

    // ─────────────────────────────────────────────
    //  Data loading
    // ─────────────────────────────────────────────

    private void loadData(int patientId) {
        data.clear();

        String q =
            "SELECT a.id, a.status, " +
            "       p.day_of_week, p.period, " +
            "       u.id_user psy_id, u.prenom psy_prenom, u.nom psy_nom " +
            "FROM appointments a " +
            "JOIN psychologue_plans p ON a.plan_id = p.id " +
            "JOIN users u ON p.psychologue_id_user = u.id_user " +
            "WHERE a.patient_id_user = ? " +
            "ORDER BY a.created_at DESC";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(q)) {
            ps.setInt(1, patientId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                Appointment a = new Appointment();
                a.setId(rs.getInt("id"));
                a.setStatus(rs.getString("status"));

                com.psychologie.model.PsychologuePlan plan = new com.psychologie.model.PsychologuePlan();
                plan.setDayOfWeek(rs.getString("day_of_week"));
                plan.setPeriod(rs.getString("period"));

                User psy = new User();
                psy.setId(rs.getInt("psy_id"));
                psy.setPrenom(rs.getString("psy_prenom"));
                psy.setNom(rs.getString("psy_nom"));
                plan.setPsychologue(psy);

                a.setPlan(plan);
                data.add(a);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        rdvCountLabel.setText(String.valueOf(data.size()));

        // Bien-être: check if patient has any active programme
        loadBienEtre(patientId);
    }

    private void loadBienEtre(int patientId) {
        // Show "Actif" if there are active programmes available
        String q = "SELECT COUNT(*) FROM programme_bien_etre " +
                   "WHERE statut IN ('ACTIF','Publié','actif')";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(q)) {
            ResultSet rs = ps.executeQuery();
            if (rs.next() && rs.getInt(1) > 0) {
                bienEtreLabel.setText("Actif");
                bienEtreLabel.setStyle("-fx-font-size: 22px; -fx-font-weight: bold; -fx-text-fill: #2a6f5b;");
            } else {
                bienEtreLabel.setText("Inactif");
                bienEtreLabel.setStyle("-fx-font-size: 22px; -fx-font-weight: bold; -fx-text-fill: #e74c3c;");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // ─────────────────────────────────────────────
    //  Actions
    // ─────────────────────────────────────────────

    @FXML
    private void handleNewConsultation() {
        MainController.getInstance().showAppointments();
    }

    @FXML
    private void handleForum() {
        MainController.getInstance().showForum();
    }
}
