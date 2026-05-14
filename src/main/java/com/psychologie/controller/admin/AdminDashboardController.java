package com.psychologie.controller.admin;

import com.psychologie.controller.MainController;
import com.psychologie.controller.SecurityController;
import com.psychologie.util.DatabaseConnection;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class AdminDashboardController {

    @FXML private Label dateLabel;
    @FXML private Label userCountLabel;
    @FXML private Label appointmentCountLabel;
    @FXML private Label cabinetCountLabel;

    @FXML
    public void initialize() {
        dateLabel.setText(LocalDate.now().format(DateTimeFormatter.ofPattern("dd MMMM yyyy")));
        loadStats();
    }

    private void loadStats() {
        try (Connection conn = DatabaseConnection.getConnection()) {
            // User count
            String userQuery = "SELECT COUNT(*) FROM users";
            try (PreparedStatement pstmt = conn.prepareStatement(userQuery);
                 ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) userCountLabel.setText(String.valueOf(rs.getInt(1)));
            }

            // Appointment count (Table name is 'appointments' plural in SQL)
            String apptQuery = "SELECT COUNT(*) FROM appointments";
            try (PreparedStatement pstmt = conn.prepareStatement(apptQuery);
                 ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) appointmentCountLabel.setText(String.valueOf(rs.getInt(1)));
            }

            // Cabinet to validate count (PK is id_cabinet)
            String cabinetQuery = "SELECT COUNT(*) FROM cabinet WHERE valide = 0";
            try (PreparedStatement pstmt = conn.prepareStatement(cabinetQuery);
                 ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) cabinetCountLabel.setText(String.valueOf(rs.getInt(1)));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    public void handleManageUsers() {
        MainController.getInstance().showUsers();
    }

    @FXML
    public void handleViewConsultations() {
        MainController.getInstance().showAppointments();
    }

    @FXML
    public void handleNewPsychologue() {
        MainController.getInstance().showUsers();
        // Ideally open a "new user" dialog here
    }

    @FXML
    public void handleViewForum() {
        MainController.getInstance().showForum();
    }
}
