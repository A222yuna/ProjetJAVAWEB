package org.example.controller;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.scene.Scene;
import org.example.model.User;
import org.example.util.session.SessionManager;
import org.example.view.LoginView;

public class MainController {
    private static Stage primaryStage;

    public static void setPrimaryStage(Stage stage) {
        primaryStage = stage;
    }

    public static Stage getPrimaryStage() {
        return primaryStage;
    }

    public static void showLogin(Stage stage) {
        SessionManager.logout();
        Scene loginScene = LoginView.createLoginScene(stage);
        stage.setScene(loginScene);
        stage.show();
    }

    public static void showDashboard(Stage stage) {
        if (!SessionManager.isLoggedIn()) {
            showLogin(stage);
            return;
        }

        User currentUser = SessionManager.getCurrentUser();

        VBox dashboardVBox;
        if ("PATIENT".equals(currentUser.getRole())) {
            dashboardVBox = PatientDashboard.createDashboard();
        } else if ("PSYCHOLOGUE".equals(currentUser.getRole())) {
            dashboardVBox = PsychologueDashboard.createDashboard();
        } else {
            showLogin(stage);
            return;
        }

        Scene dashboardScene = new Scene(dashboardVBox, 1000, 700);
        stage.setScene(dashboardScene);
        stage.setTitle("Appointment System - " + currentUser.getFullName());
        stage.show();
    }
}
