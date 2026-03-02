package tn.esprit.pidev.forum.controllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.input.MouseEvent;
import javafx.stage.Stage;

import java.io.IOException;

public class HomeController {

    @FXML private Button psychologuesBtn;
    @FXML private Button espaceBienEtreBtn;
    @FXML private Button rendezVousBtn;
    @FXML private Button messagerieBtn;
    @FXML private Button forumBtn;
    @FXML private Button deconnexionBtn;

    @FXML
    public void initialize() {
        System.out.println("✅ HomePage chargée!");
    }

    @FXML
    private void onMenuHover(MouseEvent event) {
        Button btn = (Button) event.getSource();
        String currentStyle = btn.getStyle();
        if (!currentStyle.contains("rgba(255,255,255,0.1)")) {
            btn.setStyle(currentStyle + "-fx-background-color: rgba(255,255,255,0.15);");
        }
    }

    @FXML
    private void onMenuExit(MouseEvent event) {
        Button btn = (Button) event.getSource();
        if (btn == forumBtn) {
            btn.setStyle(btn.getStyle().replace("-fx-background-color: rgba(255,255,255,0.15);",
                    "-fx-background-color: rgba(255,255,255,0.1);"));
        } else {
            btn.setStyle(btn.getStyle().replace("-fx-background-color: rgba(255,255,255,0.15);",
                    "-fx-background-color: transparent;"));
        }
    }

    /**
     * ✅ NAVIGATION VERS LE FORUM
     */
    @FXML
    private void handleForum(ActionEvent event) {
        try {
            System.out.println("🚀 Ouverture du Forum...");

            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/ForumView.fxml"));
            Parent root = loader.load();

            Stage forumStage = new Stage();
            forumStage.setTitle("MindConnect - Forum de Discussion");
            forumStage.setScene(new Scene(root, 1400, 900));
            forumStage.setMaximized(true);

            Stage currentStage = (Stage) forumBtn.getScene().getWindow();
            currentStage.close();

            forumStage.show();

            System.out.println("✅ Forum ouvert!");

        } catch (IOException e) {
            System.err.println("❌ Erreur ouverture Forum!");
            e.printStackTrace();
            showError("Erreur", "Impossible d'ouvrir le Forum.\n" + e.getMessage());
        }
    }

    @FXML
    private void handlePsychologues(ActionEvent event) {
        System.out.println("👨‍⚕️ Module Psychologues");
        showInfo("Module à intégrer", "Le module Psychologues sera intégré par l'équipe correspondante.");
    }

    @FXML
    private void handleEspaceBienEtre(ActionEvent event) {
        System.out.println("🧘 Module Bien-être");
        showInfo("Module à intégrer", "Le module Espace Bien-être sera intégré par l'équipe correspondante.");
    }

    @FXML
    private void handleRendezVous(ActionEvent event) {
        System.out.println("📅 Module Rendez-vous");
        showInfo("Module à intégrer", "Le module Rendez-vous sera intégré par l'équipe correspondante.");
    }

    @FXML
    private void handleMessagerie(ActionEvent event) {
        System.out.println("💬 Module Messagerie");
        showInfo("Module à intégrer", "Le module Messagerie sera intégré par l'équipe correspondante.");
    }

    @FXML
    private void handleUrgence(ActionEvent event) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Accès d'urgence");
        alert.setHeaderText("Ressources d'urgence");
        alert.setContentText("📞 Numéro d'urgence: 190\n" +
                "🏥 Centre de crise: Disponible 24/7\n" +
                "💬 Chat: www.mindconnect.tn/urgence");
        alert.showAndWait();
    }

    @FXML
    private void handleContact(ActionEvent event) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Contact");
        alert.setHeaderText("Contactez-nous");
        alert.setContentText("📧 Email: contact@mindconnect.tn\n" +
                "📞 Téléphone: +216 71 123 456\n" +
                "🌐 Site: www.mindconnect.tn");
        alert.showAndWait();
    }

    @FXML
    private void handleNotifications(ActionEvent event) {
        showInfo("Notifications", "Aucune nouvelle notification.");
    }

    @FXML
    private void handleProfile(ActionEvent event) {
        showInfo("Profil", "Fonctionnalité de profil à venir.");
    }

    @FXML
    private void handleDeconnexion(ActionEvent event) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Déconnexion");
        alert.setHeaderText("Voulez-vous vous déconnecter?");

        alert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                Stage stage = (Stage) deconnexionBtn.getScene().getWindow();
                stage.close();
            }
        });
    }

    private void showError(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    private void showInfo(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}
