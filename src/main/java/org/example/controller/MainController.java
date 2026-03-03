package org.example.controller;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.TextInputDialog;
import org.example.model.User;
import org.example.dao.UserDAO;
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

        // ensure that patient has contact info
        if ("PATIENT".equals(currentUser.getRole())) {
            ensureContactInfo(); // will prompt if missing
        }

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

    /**
     * Prompt the current user to provide at least one contact method if missing.
     * This mirrors the logic that was previously in PatientDashboard.
     *
     * @return true once the user has contact information, false if they declined.
     */
    public static boolean ensureContactInfo() {
        User user = SessionManager.getCurrentUser();
        if (user == null) {
            return false;
        }
        boolean hasPhone = user.getPhone() != null && !user.getPhone().isBlank();
        boolean hasEmail = user.getEmail() != null && !user.getEmail().isBlank();
        if (hasPhone || hasEmail) {
            return true;
        }

        TextInputDialog phoneDialog = new TextInputDialog();
        phoneDialog.setTitle("Contact Information Required");
        phoneDialog.setHeaderText("Please provide a phone number or email to receive notifications.");
        phoneDialog.setContentText("Phone (leave blank to use email):");
        String phone = phoneDialog.showAndWait().orElse("").trim();

        TextInputDialog emailDialog = new TextInputDialog();
        emailDialog.setTitle("Contact Information Required");
        emailDialog.setHeaderText("Please provide a phone number or email to receive notifications.");
        emailDialog.setContentText("Email (leave blank to use phone):");
        String email = emailDialog.showAndWait().orElse("").trim();

        if (phone.isEmpty() && email.isEmpty()) {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("Contact Required");
            alert.setHeaderText(null);
            alert.setContentText("At least one contact method is required to receive appointment notifications.");
            alert.showAndWait();
            return false;
        }

        if (UserDAO.updateContactInfo(user.getId(),
                email.isEmpty() ? null : email,
                phone.isEmpty() ? null : phone)) {
            User updated = UserDAO.getUserById(user.getId());
            if (updated != null) {
                SessionManager.setCurrentUser(updated);
            }
            return true;
        } else {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Update Failed");
            alert.setHeaderText(null);
            alert.setContentText("Failed to save contact information. Please try again.");
            alert.showAndWait();
            return false;
        }
    }
}
