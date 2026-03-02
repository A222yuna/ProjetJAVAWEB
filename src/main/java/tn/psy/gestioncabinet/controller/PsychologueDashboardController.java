package tn.psy.gestioncabinet.controller;

import javafx.animation.FadeTransition;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Duration;
import tn.psy.gestioncabinet.model.Cabinet;
import tn.psy.gestioncabinet.model.PsyCabinet;
import tn.psy.gestioncabinet.service.CabinetService;
import tn.psy.gestioncabinet.controller.AddDisponibiliteController;
import tn.psy.gestioncabinet.util.SceneManager;
import tn.psy.gestioncabinet.util.SessionManager;
import tn.psy.gestioncabinet.util.AuthGuard;

import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

/**
 * Contrôleur du dashboard Psychologue
 */
public class PsychologueDashboardController implements Initializable {

    @FXML private VBox rootContent;
    @FXML private Label lblWelcome;
    @FXML private Label lblWelcomeCard;
    @FXML private Label lblTotalCabinets;
    @FXML private Label lblCabinetsValides;
    @FXML private Label lblCabinetsEnAttente;
    @FXML private TableView<PsyCabinet> tableCabinets;
    @FXML private TableColumn<PsyCabinet, Integer> colId;
    @FXML private TableColumn<PsyCabinet, String> colAdresse;
    @FXML private TableColumn<PsyCabinet, String> colVille;
    @FXML private TableColumn<PsyCabinet, String> colStatut;
    @FXML private Button btnAjouter;
    @FXML private Button btnModifier;
    @FXML private Button btnPlanifier;
    @FXML private Button btnSupprimer;
    @FXML private Button btnDeconnexion;

    private final CabinetService cabinetService = new CabinetService();
    private int userId;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Contrôle d'accès : uniquement Psychologue
        if (!AuthGuard.check(AuthGuard.RequiredRole.PSYCHOLOGUE)) {
            showAlert(Alert.AlertType.ERROR, "Accès refusé", "Accès non autorisé pour ce rôle.");
            return;
        }

        userId = SessionManager.getInstance().getUserId();
        if (userId > 0) {
            lblWelcome.setText("Espace Psychologue");
            lblWelcomeCard.setText("Bonjour !");
        }

        colId.setCellValueFactory(new PropertyValueFactory<>("idCabinet"));
        colAdresse.setCellValueFactory(new PropertyValueFactory<>("adresseCabinet"));
        colVille.setCellValueFactory(new PropertyValueFactory<>("villeCabinet"));
        colStatut.setCellValueFactory(cb -> {
            PsyCabinet pc = cb.getValue();
            if (pc == null) return new javafx.beans.property.SimpleStringProperty("");
            Cabinet c = cabinetService.findById(pc.getIdCabinet()).orElse(null);
            return new javafx.beans.property.SimpleStringProperty(c != null ? c.getStatutTexte() : "");
        });

        chargerCabinets();

        btnAjouter.setOnAction(e -> ouvrirAjoutCabinet());
        btnModifier.setOnAction(e -> ouvrirModificationCabinet());
        if (btnPlanifier != null) {
            btnPlanifier.setOnAction(e -> ouvrirPlanifierCreneaux());
        }
        btnSupprimer.setOnAction(e -> supprimerCabinet());
        btnDeconnexion.setOnAction(e -> deconnexion());

        // Animation fade-in sur le contenu principal
        Platform.runLater(() -> {
            if (rootContent != null) {
                rootContent.setOpacity(0);
                FadeTransition ft = new FadeTransition(Duration.millis(450), rootContent);
                ft.setFromValue(0);
                ft.setToValue(1);
                ft.play();
            }
        });
    }

    private void chargerCabinets() {
        if (userId <= 0) return;
        List<PsyCabinet> cabinets = cabinetService.getCabinetsByPsychologue(userId);
        tableCabinets.getItems().clear();
        tableCabinets.getItems().addAll(cabinets);
        
        // Calculer les statistiques
        long total = cabinets.size();
        long valides = cabinets.stream()
                .mapToLong(pc -> {
                    Cabinet c = cabinetService.findById(pc.getIdCabinet()).orElse(null);
                    return (c != null && c.isValide()) ? 1 : 0;
                })
                .sum();
        long enAttente = total - valides;
        
        lblTotalCabinets.setText(String.valueOf(total));
        lblCabinetsValides.setText(String.valueOf(valides));
        lblCabinetsEnAttente.setText(String.valueOf(enAttente));
    }

    private void ouvrirAjoutCabinet() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/add_cabinet.fxml"));
            Parent root = loader.load();
            AddCabinetController ctrl = loader.getController();
            ctrl.setIdPsychologue(userId);

            Stage stage = new Stage();
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.initOwner(SceneManager.getPrimaryStage());
            Scene scene = new Scene(root);
            scene.getStylesheets().add(getClass().getResource("/css/style.css").toExternalForm());
            stage.setScene(scene);
            stage.setTitle("Ajouter un cabinet");
            stage.showAndWait();
            chargerCabinets();
        } catch (IOException ex) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible d'ouvrir le formulaire d'ajout.");
        }
    }

    private void ouvrirModificationCabinet() {
        PsyCabinet selected = tableCabinets.getSelectionModel().getSelectedItem();

        if (selected == null) {
            showAlert(Alert.AlertType.WARNING, "Attention", "Veuillez sélectionner un cabinet.");
            return;
        }

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/modify_cabinet.fxml"));
            Parent root = loader.load();

            // 🔥 Création directe de l'objet Cabinet depuis la sélection
            Cabinet cabinet = new Cabinet();
            cabinet.setIdCabinet(selected.getIdCabinet());
            cabinet.setAdresse(selected.getAdresseCabinet());
            cabinet.setVille(selected.getVilleCabinet());
            cabinet.setHoraires(""); // si PsyCabinet ne contient pas horaires
            cabinet.setDescription(""); // idem

            ModifyCabinetController ctrl = loader.getController();
            ctrl.setCabinet(cabinet);

            Stage stage = new Stage();
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.initOwner(SceneManager.getPrimaryStage());

            Scene scene = new Scene(root);
            scene.getStylesheets().add(getClass().getResource("/css/style.css").toExternalForm());

            stage.setScene(scene);
            stage.setTitle("Modifier le cabinet");
            stage.showAndWait();

            // 🔄 Rafraîchir après modification
            chargerCabinets();

        } catch (IOException ex) {
            ex.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible d'ouvrir le formulaire de modification.");
        }
    }

    private void ouvrirPlanifierCreneaux() {
        PsyCabinet selected = tableCabinets.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert(Alert.AlertType.WARNING, "Attention", "Veuillez sélectionner un cabinet.");
            return;
        }
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/add_disponibilite.fxml"));
            Parent root = loader.load();
            AddDisponibiliteController ctrl = loader.getController();
            String label = selected.getAdresseCabinet() != null ? selected.getAdresseCabinet() : selected.getVilleCabinet();
            ctrl.setCabinet(selected.getIdCabinet(), label != null ? label : "Cabinet " + selected.getIdCabinet());

            Stage stage = new Stage();
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.initOwner(SceneManager.getPrimaryStage());
            Scene scene = new Scene(root);
            scene.getStylesheets().add(getClass().getResource("/css/style.css").toExternalForm());
            stage.setScene(scene);
            stage.setTitle("Planifier les créneaux");
            stage.showAndWait();
        } catch (IOException ex) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible d'ouvrir la planification.");
        }
    }

    private void supprimerCabinet() {
        PsyCabinet selected = tableCabinets.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert(Alert.AlertType.WARNING, "Attention", "Veuillez sélectionner un cabinet.");
            return;
        }

        ButtonType result = new Alert(Alert.AlertType.CONFIRMATION,
                "Êtes-vous sûr de vouloir supprimer ce cabinet ?",
                ButtonType.YES, ButtonType.NO).showAndWait().orElse(ButtonType.NO);

        if (result == ButtonType.YES) {
            if (cabinetService.supprimerCabinet(selected.getIdCabinet(), false)) {
                showAlert(Alert.AlertType.INFORMATION, "Succès", "Cabinet supprimé.");
                chargerCabinets();
            } else {
                showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible de supprimer le cabinet.");
            }
        }
    }

    @FXML
    private void deconnexion() {
        SessionManager.getInstance().clearSession();
    }

    private void showAlert(Alert.AlertType type, String title, String message) {
        new Alert(type, message, ButtonType.OK).showAndWait();
    }
}
