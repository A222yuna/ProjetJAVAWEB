package com.psychologie.controller.psychologue;

import com.psychologie.controller.MainController;
import com.psychologie.controller.SecurityController;
import com.psychologie.model.Appointment;
import com.psychologie.model.User;
import com.psychologie.util.DatabaseConnection;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import java.sql.*;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

public class PsyPlanningController {

    @FXML private Label plansCountLabel;
    @FXML private Label dispoCountLabel;
    @FXML private Label rdvCountLabel;
    @FXML private Label pageLabel;
    @FXML private VBox plansList;
    @FXML private VBox disposList;
    @FXML private VBox appointmentsList;

    @FXML
    public void initialize() {
        loadPlanning();
    }

    private void loadPlanning() {
        User user = SecurityController.getCurrentUser();
        if (user == null) return;

        plansList.getChildren().clear();
        disposList.getChildren().clear();
        appointmentsList.getChildren().clear();

        try (Connection conn = DatabaseConnection.getConnection()) {
            loadPlans(conn, user.getId());
            loadDisponibilites(conn, user.getId());
            loadAppointments(conn, user.getId());
            pageLabel.setText("1/1");
        } catch (SQLException e) {
            e.printStackTrace();
            showError("Impossible de charger le planning.");
        }
    }

    private void loadPlans(Connection conn, int psyId) throws SQLException {
        String query = "SELECT day_of_week, period, max_appointments FROM psychologue_plans WHERE psychologue_id_user = ? ORDER BY id DESC";
        int count = 0;
        try (PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setInt(1, psyId);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                count++;
                plansList.getChildren().add(new Label("• " + rs.getString("day_of_week") + " - " + rs.getString("period") + " (" + rs.getInt("max_appointments") + " max)"));
            }
        }
        plansCountLabel.setText(String.valueOf(count));
        if (count == 0) {
            plansList.getChildren().add(new Label("• Aucun plan."));
        }
    }

    private void loadDisponibilites(Connection conn, int psyId) throws SQLException {
        String query = "SELECT d.jour, d.heure_debut, d.heure_fin, d.duree_consultation " +
                       "FROM disponibilite d " +
                       "JOIN psy_cabinet pc ON d.cabinet_id = pc.id_cabinet " +
                       "WHERE pc.psychologue_id_user = ? ORDER BY d.jour, d.heure_debut";
        int count = 0;
        try (PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setInt(1, psyId);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                count++;
                String item = "• " + dayName(rs.getInt("jour")) + " - " +
                        formatTime(rs.getTime("heure_debut")) + " / " +
                        formatTime(rs.getTime("heure_fin")) + " (" +
                        rs.getInt("duree_consultation") + " min)";
                disposList.getChildren().add(new Label(item));
            }
        }
        dispoCountLabel.setText(String.valueOf(count));
        if (count == 0) {
            disposList.getChildren().add(new Label("• Aucune disponibilité."));
        }
    }

    private void loadAppointments(Connection conn, int psyId) throws SQLException {
        String query = "SELECT a.id, a.status, u.prenom, u.nom " +
                       "FROM appointments a " +
                       "JOIN psychologue_plans p ON a.plan_id = p.id " +
                       "JOIN users u ON a.patient_id_user = u.id_user " +
                       "WHERE p.psychologue_id_user = ? ORDER BY a.created_at DESC";
        int count = 0;
        try (PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setInt(1, psyId);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                count++;
                appointmentsList.getChildren().add(createAppointmentRow(
                        rs.getInt("id"),
                        rs.getString("prenom") + " " + rs.getString("nom"),
                        rs.getString("status")
                ));
            }
        }
        rdvCountLabel.setText(String.valueOf(count));
        if (count == 0) {
            appointmentsList.getChildren().add(new Label("• Aucun rendez-vous pour vos plans."));
        }
    }

    private HBox createAppointmentRow(int id, String patientName, String status) {
        HBox row = new HBox(8);
        row.setAlignment(javafx.geometry.Pos.CENTER_LEFT);

        Label text = new Label("• #" + id + " - " + patientName);
        Label badge = Appointment.statusBadge(status);
        row.getChildren().addAll(text, badge);

        if (Appointment.STATUS_SCHEDULED.equalsIgnoreCase(status)) {
            Button complete = new Button("Compléter");
            complete.setText("Confirmer");
            complete.getStyleClass().add("btn-outline-primary");
            complete.setOnAction(e -> updateAppointmentStatus(id, Appointment.STATUS_CONFIRMED));

            Button cancel = new Button("Annuler");
            cancel.getStyleClass().add("btn-outline-danger");
            cancel.setOnAction(e -> updateAppointmentStatus(id, Appointment.STATUS_CANCELLED));
            row.getChildren().addAll(complete, cancel);
        } else if (Appointment.STATUS_CONFIRMED.equalsIgnoreCase(status) || Appointment.STATUS_PAID.equalsIgnoreCase(status)) {
            Button complete = new Button("Compléter");
            complete.getStyleClass().add("btn-outline-success");
            complete.setOnAction(e -> updateAppointmentStatus(id, Appointment.STATUS_COMPLETED));

            Button cancel = new Button("Annuler");
            cancel.getStyleClass().add("btn-outline-danger");
            cancel.setOnAction(e -> updateAppointmentStatus(id, Appointment.STATUS_CANCELLED));
            row.getChildren().addAll(complete, cancel);
        }

        return row;
    }

    private void updateAppointmentStatus(int appointmentId, String status) {
        User user = SecurityController.getCurrentUser();
        if (user == null) return;

        String query = "UPDATE appointments a " +
                       "JOIN psychologue_plans p ON a.plan_id = p.id " +
                       "SET a.status = ? " +
                       "WHERE a.id = ? AND p.psychologue_id_user = ? AND a.status IN ('SCHEDULED', 'CONFIRMED', 'PAID')";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setString(1, status);
            pstmt.setInt(2, appointmentId);
            pstmt.setInt(3, user.getId());
            pstmt.executeUpdate();
            loadPlanning();
        } catch (SQLException e) {
            e.printStackTrace();
            showError("Impossible de mettre à jour le rendez-vous.");
        }
    }

    @FXML public void handleNewPlan() {
        MainController.getInstance().showPsyPlans();
    }

    @FXML public void handleNewDisponibilite() {
        MainController.getInstance().showAppointments();
    }

    private String dayName(int day) {
        String[] days = {"Lundi", "Mardi", "Mercredi", "Jeudi", "Vendredi", "Samedi", "Dimanche"};
        return day >= 1 && day <= 7 ? days[day - 1] : "Jour " + day;
    }

    private String formatTime(Time time) {
        if (time == null) return "--:--";
        LocalTime localTime = time.toLocalTime();
        return localTime.format(DateTimeFormatter.ofPattern("HH:mm"));
    }

    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR, message);
        alert.showAndWait();
    }
}
