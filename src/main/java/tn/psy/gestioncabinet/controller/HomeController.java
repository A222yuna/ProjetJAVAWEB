package tn.psy.gestioncabinet.controller;

import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import tn.psy.gestioncabinet.util.AuthGuard;
import tn.psy.gestioncabinet.util.SceneManager;
import tn.psy.gestioncabinet.util.SessionManager;

import java.io.IOException;

/**
 * Page d'accueil principale avec 3 boutons :
 *  - Admin
 *  - Psychologue
 *  - Patient
 *
 * Cette page ne gère PAS le login / register.
 * Elle suppose que le module central de gestion des users
 * a déjà positionné la session (SessionManager).
 */
public class HomeController {

    // MODE TEST TEMPORAIRE – A désactiver après intégration Gestion User
    // Si true : on simule un utilisateur connecté pour chaque espace
    // et on ignore la vérification de session/role.
    private static final boolean TEST_MODE = true;

    @FXML
    private Button btnAdmin;
    @FXML
    private Button btnPsychologue;
    @FXML
    private Button btnPatient;

    @FXML
    public void initialize() {
        if (btnAdmin != null) {
            btnAdmin.setOnAction(e -> openAdmin());
        }
        if (btnPsychologue != null) {
            btnPsychologue.setOnAction(e -> openPsychologue());
        }
        if (btnPatient != null) {
            btnPatient.setOnAction(e -> openPatient());
        }
    }

    private boolean ensureLoggedIn() {
        if (TEST_MODE) {
            // En mode test, on considère toujours que l'utilisateur est connecté.
            return true;
        }
        if (SessionManager.getInstance().getUser() == null ||
                SessionManager.getInstance().getRole() == null) {
            new Alert(Alert.AlertType.INFORMATION,
                    "Vous devez être connecté via le module de gestion des utilisateurs.",
                    ButtonType.OK).showAndWait();
            // Ici, on pourrait déclencher une redirection vers le module central (URL externe, autre appli, etc.)
            return false;
        }
        return true;
    }

    private void openAdmin() {
        if (TEST_MODE) {
            // MODE TEST TEMPORAIRE – simulation Admin
            SessionManager.getInstance().setSession(SessionManager.Role.ADMIN, 1, null);
        } else {
            if (!ensureLoggedIn()) return;
            if (!AuthGuard.check(AuthGuard.RequiredRole.ADMIN)) {
                showAccessDenied();
                return;
            }
        }
        try {
            SceneManager.loadScene("/fxml/admin_dashboard.fxml", "Dashboard Admin");
        } catch (IOException e) {
            showError("Impossible d'ouvrir le dashboard Admin.");
        }
    }

    private void openPsychologue() {
        if (TEST_MODE) {
            // MODE TEST TEMPORAIRE – simulation Psychologue
            SessionManager.getInstance().setSession(SessionManager.Role.PSYCHOLOGUE, 3, null);
        } else {
            if (!ensureLoggedIn()) return;
            if (!AuthGuard.check(AuthGuard.RequiredRole.PSYCHOLOGUE)) {
                showAccessDenied();
                return;
            }
        }
        try {
            SceneManager.loadScene("/fxml/psychologue_dashboard.fxml", "Dashboard Psychologue");
        } catch (IOException e) {
            showError("Impossible d'ouvrir le dashboard Psychologue.");
        }
    }

    private void openPatient() {
        if (TEST_MODE) {
            // MODE TEST TEMPORAIRE – simulation Patient
            SessionManager.getInstance().setSession(SessionManager.Role.PATIENT, 2, null);
        } else {
            if (!ensureLoggedIn()) return;
            if (!AuthGuard.check(AuthGuard.RequiredRole.PATIENT)) {
                showAccessDenied();
                return;
            }
        }
        try {
            SceneManager.loadScene("/fxml/patient_dashboard.fxml", "Dashboard Patient");
        } catch (IOException e) {
            showError("Impossible d'ouvrir le dashboard Patient.");
        }
    }

    private void showAccessDenied() {
        new Alert(Alert.AlertType.WARNING,
                "Accès non autorisé pour ce rôle.",
                ButtonType.OK).showAndWait();
    }

    private void showError(String msg) {
        new Alert(Alert.AlertType.ERROR, msg, ButtonType.OK).showAndWait();
    }
}

