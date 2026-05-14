package com.psychologie.controller;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.layout.StackPane;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import java.io.IOException;

import com.psychologie.model.User;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class MainController {

    @FXML
    private StackPane contentArea;

    @FXML
    private Label userGreetingLabel;

    @FXML private VBox adminMenu;
    @FXML private VBox psychologueMenu;
    @FXML private VBox patientMenu;
    @FXML private Label sidebarTitle;

    private static MainController instance;

    public static MainController getInstance() {
        return instance;
    }

    @FXML
    public void initialize() {
        instance = this;
        if (SecurityController.getCurrentUser() != null) {
            User user = SecurityController.getCurrentUser();
            userGreetingLabel.setText("Bonjour, " + user.getPrenom() + " " + user.getNom() + " (" + user.getRole() + ")");
            
            // Hide all menus first
            adminMenu.setVisible(false);
            adminMenu.setManaged(false);
            psychologueMenu.setVisible(false);
            psychologueMenu.setManaged(false);
            patientMenu.setVisible(false);
            patientMenu.setManaged(false);

            // Show menu based on role and load specific welcome view
            String role = user.getRole().toUpperCase();
            if (role.equals("ADMIN")) {
                sidebarTitle.setText("ADMIN PANEL");
                adminMenu.setVisible(true);
                adminMenu.setManaged(true);
                showDashboard(); // Admin sees Dashboard by default now
            } else if (role.equals("PSYCHOLOGUE")) {
                sidebarTitle.setText("ESPACE PSY");
                psychologueMenu.setVisible(true);
                psychologueMenu.setManaged(true);
                showDashboard(); // Psychologist sees Dashboard by default
            } else {
                sidebarTitle.setText("ESPACE PATIENT");
                patientMenu.setVisible(true);
                patientMenu.setManaged(true);
                showDashboard(); // Patient sees default welcome/dashboard
            }
        }
    }

    @FXML
    public void handleLogout() {
        try {
            Stage stage = (Stage) contentArea.getScene().getWindow();
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/psychologie/view/LoginView.fxml"));
            Parent root = loader.load();
            Scene scene = new Scene(root, 800, 600);
            stage.setScene(scene);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void loadView(String fxmlPath) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent view = loader.load();
            contentArea.getChildren().setAll(view);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    public void handleExit() {
        Platform.exit();
    }

    @FXML
    public void handleAbout() {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("About");
        alert.setHeaderText("Psychologie App JavaFX Template");
        alert.setContentText("A template based on the Symfony project idea.");
        alert.showAndWait();
    }

    @FXML
    public void showDashboard() {
        User user = SecurityController.getCurrentUser();
        if (user != null) {
            String role = user.getRole().toUpperCase();
            if (role.equals("ADMIN")) {
                loadView("/com/psychologie/view/admin/AdminDashboard.fxml");
            } else if (role.equals("PSYCHOLOGUE")) {
                loadView("/com/psychologie/view/psychologue/PsyDashboard.fxml");
            } else {
                // Patient dashboard
                loadView("/com/psychologie/view/patient/PatientDashboard.fxml");
            }
        }
    }

    @FXML
    public void showStats() {
        User user = SecurityController.getCurrentUser();
        if (user != null && "PSYCHOLOGUE".equalsIgnoreCase(user.getRole())) {
            loadView("/com/psychologie/view/psychologue/PsyStats.fxml");
        } else {
            loadView("/com/psychologie/view/admin/Stats.fxml");
        }
    }

    @FXML
    public void showPsyPlans() {
        loadView("/com/psychologie/view/psychologue/PsyPlans.fxml");
    }

    @FXML
    public void showPsyPlanning() {
        loadView("/com/psychologie/view/psychologue/PsyPlanning.fxml");
    }

    @FXML
    public void showPsyCabinets() {
        loadView("/com/psychologie/view/psychologue/PsyCabinets.fxml");
    }

    @FXML
    public void showAppointments() {
        User user = SecurityController.getCurrentUser();
        if (user != null) {
            String role = user.getRole().toUpperCase();
            if (role.equals("ADMIN")) {
                loadView("/com/psychologie/view/admin/AdminAppointments.fxml");
            } else if (role.equals("PATIENT")) {
                loadView("/com/psychologie/view/patient/BookCreneau.fxml");
            } else {
                loadView("/com/psychologie/view/psychologue/Disponibilites.fxml");
            }
        }
    }

    @FXML
    public void showCabinets() {
        User user = SecurityController.getCurrentUser();
        if (user != null && "ADMIN".equalsIgnoreCase(user.getRole())) {
            loadView("/com/psychologie/view/admin/AdminCabinets.fxml");
        }
    }

    @FXML
    public void showUsers() {
        loadView("/com/psychologie/view/admin/UserAdmin.fxml");
    }

    @FXML
    public void showChat() {
        loadView("/com/psychologie/view/ChatView.fxml");
    }

    @FXML
    public void showForum() {
        User user = SecurityController.getCurrentUser();
        if (user != null && "ADMIN".equalsIgnoreCase(user.getRole())) {
            loadView("/com/psychologie/view/admin/AdminForum.fxml");
        } else {
            loadView("/com/psychologie/view/ForumView.fxml");
        }
    }

    @FXML
    public void showSavedPosts() {
        loadView("/com/psychologie/view/SavedPostsView.fxml");
    }

    @FXML
    public void showReports() {
        loadView("/com/psychologie/view/admin/AdminSignalement.fxml");
    }

    @FXML
    public void showCalendrier() {
        User user = SecurityController.getCurrentUser();
        if (user != null && "PATIENT".equalsIgnoreCase(user.getRole())) {
            loadView("/com/psychologie/view/patient/PatientCalendar.fxml");
        } else {
            loadView("/com/psychologie/view/psychologue/PsyCalendar.fxml");
        }
    }

    @FXML
    public void showPrograms() {
        User user = SecurityController.getCurrentUser();
        if (user != null && "ADMIN".equalsIgnoreCase(user.getRole())) {
            loadView("/com/psychologie/view/admin/AdminWellbeing.fxml");
        } else {
            loadView("/com/psychologie/view/ProgramListView.fxml");
        }
    }
}
