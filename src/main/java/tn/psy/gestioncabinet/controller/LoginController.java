package tn.psy.gestioncabinet.controller;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import tn.psy.gestioncabinet.model.Administrateur;
import tn.psy.gestioncabinet.model.Patient;
import tn.psy.gestioncabinet.model.Psychologue;
import tn.psy.gestioncabinet.service.AuthService;
import tn.psy.gestioncabinet.util.SceneManager;
import tn.psy.gestioncabinet.util.SessionManager;

import java.io.IOException;

/**
 * Contrôleur de l'écran de connexion
 */
public class LoginController {

    @FXML private TextField tfEmail;
    @FXML private PasswordField pfPassword;
    @FXML private ComboBox<String> cbRole;
    @FXML private Button btnLogin;
    @FXML private Button btnInscription;
    @FXML private Label lblError;

    private final AuthService authService = new AuthService();

    @FXML
    public void initialize() {
        cbRole.getItems().addAll("Psychologue", "Patient", "Administrateur");
        cbRole.setValue("Psychologue");
        lblError.setVisible(false);

        btnLogin.setOnAction(e -> handleLogin());
        btnInscription.setOnAction(e -> ouvrirInscription());
    }

    private void ouvrirInscription() {
        try {
            SceneManager.loadScene("/fxml/inscription.fxml", "Inscription - Gestion Cabinet");
        } catch (IOException ex) {
            showError("Impossible d'ouvrir la page d'inscription.");
        }
    }

    private void handleLogin() {
        String email = tfEmail.getText();
        String password = pfPassword.getText();
        String roleStr = cbRole.getValue();

        if (email == null || email.isBlank()) {
            new Alert(Alert.AlertType.WARNING, "Veuillez entrer votre email.", ButtonType.OK).showAndWait();
            showError("Veuillez entrer votre email.");
            return;
        }
        if (password == null || password.isBlank()) {
            new Alert(Alert.AlertType.WARNING, "Veuillez entrer votre mot de passe.", ButtonType.OK).showAndWait();
            showError("Veuillez entrer votre mot de passe.");
            return;
        }
        if (roleStr == null || roleStr.isBlank()) {
            new Alert(Alert.AlertType.WARNING, "Veuillez sélectionner un rôle.", ButtonType.OK).showAndWait();
            return;
        }

        AuthService.Role role = switch (roleStr) {
            case "Patient" -> AuthService.Role.PATIENT;
            case "Administrateur" -> AuthService.Role.ADMINISTRATEUR;
            default -> AuthService.Role.PSYCHOLOGUE;
        };

        AuthService.AuthResult result = authService.login(email, password, role);

        if (!result.isSuccess()) {
            showError("Email ou mot de passe incorrect.");
            return;
        }

        SessionManager.getInstance().setSession(result.getRole(), result.getUser());
        lblError.setVisible(false);

        try {
            navigateToDashboard();
        } catch (IOException ex) {
            showError("Erreur de navigation.");
        }
    }

    private void navigateToDashboard() throws IOException {
        switch (SessionManager.getInstance().getRole()) {
            case PSYCHOLOGUE -> SceneManager.loadScene("/fxml/psychologue_dashboard.fxml", "Dashboard Psychologue");
            case PATIENT -> SceneManager.loadScene("/fxml/patient_dashboard.fxml", "Dashboard Patient");
            case ADMINISTRATEUR -> SceneManager.loadScene("/fxml/admin_dashboard.fxml", "Dashboard Administrateur");
            default -> showError("Rôle inconnu.");
        }
    }

    private void showError(String message) {
        lblError.setText(message);
        lblError.setVisible(true);
    }

}
