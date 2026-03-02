package tn.esprit.mindconnect.gui;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import tn.esprit.mindconnect.entities.ProgrammeBienEtre;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

/**
 * Contrôleur principal de l'application gérant la navigation globale et les
 * vues basées sur les rôles.
 * Contrôle la disposition principale et le basculement des vues dans la zone de
 * contenu.
 */
public class MainController implements Initializable {

    @FXML
    private Button btnDashboard, btnProgrammes, btnAvis, btnProfil, btnSwitchRole;
    @FXML
    private Label lblPageTitle;
    @FXML
    private StackPane contentArea;

    private Button currentButton;
    private boolean isPsychologistView = true;

    /**
     * Initialise la vue principale, définissant la vue par défaut sur la liste des
     * programmes.
     */
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        currentButton = btnProgrammes;
        loadView("/tn/esprit/mindconnect/fxml/Programmes.fxml");
    }

    /**
     * Bascule le contexte de vue de l'application entre les rôles Psychologue et
     * Patient.
     */
    @FXML
    private void handleSwitchRole() {
        isPsychologistView = !isPsychologistView;
        if (isPsychologistView) {
            btnSwitchRole.setText("Passer à la Vue Patient");
            btnProgrammes.setText("Mes Programmes");
            lblPageTitle.setText("Mes Programmes");
            loadView("/tn/esprit/mindconnect/fxml/Programmes.fxml");
        } else {
            btnSwitchRole.setText("Passer à la Vue Psy");
            btnProgrammes.setText("Catalogues");
            lblPageTitle.setText("Tableau de Bord");
            loadView("/tn/esprit/mindconnect/fxml/PatientDashboard.fxml"); // Load patient dashboard first
        }
    }

    /**
     * Gère les clics sur les boutons de navigation de la barre latérale.
     * 
     * @param event L'événement d'action contenant le bouton source.
     */
    @FXML
    public void handleNavigation(ActionEvent event) {
        Button clickedButton = (Button) event.getSource();

        // Si c'est déjà le bouton actif (par instance), on ignore
        if (clickedButton == currentButton)
            return;

        // Réinitialiser le style du bouton précédent
        currentButton.getStyleClass().remove("sidebar-item-active");

        // Mettre à jour le bouton actuel
        currentButton = clickedButton;
        currentButton.getStyleClass().add("sidebar-item-active");

        String title = clickedButton.getText();
        lblPageTitle.setText(title);

        String fxmlFile = "";
        switch (title) {
            case "Dashboard":
                fxmlFile = isPsychologistView ? "/tn/esprit/mindconnect/fxml/Programmes.fxml"
                        : "/tn/esprit/mindconnect/fxml/PatientDashboard.fxml";
                break;
            case "Mes Programmes":
            case "Catalogues":
                fxmlFile = "/tn/esprit/mindconnect/fxml/Programmes.fxml";
                break;
            case "Avis":
                fxmlFile = "/tn/esprit/mindconnect/fxml/Avis.fxml";
                break;
            // Temporarily disabled to debug search functionality
            // case "Stats":
            //     fxmlFile = "/tn/esprit/mindconnect/fxml/Stats.fxml";
            //     break;
            case "Profil": // Assuming there's a "Profil" button
                fxmlFile = "/tn/esprit/mindconnect/fxml/Profil.fxml"; // Path to Profil FXML
                break;
        }

        if (!fxmlFile.isEmpty()) {
            loadView(fxmlFile);
        }
    }

    /**
     * Charge une vue FXML spécifique dans la zone de contenu et configure son
     * contrôleur.
     * 
     * @param fxmlPath Le chemin vers le fichier FXML.
     */
    private void loadView(String fxmlPath) {
        try {
            System.out.println("Loading FXML: " + fxmlPath);
            URL resourceUrl = getClass().getResource(fxmlPath);
            if (resourceUrl == null) {
                System.err.println("FXML file not found: " + fxmlPath);
                return;
            }
            
            FXMLLoader loader = new FXMLLoader(resourceUrl);
            Parent view = loader.load();

            Object controller = loader.getController();
            if (controller instanceof ProgrammeController) {
                ((ProgrammeController) controller).setPsychologistMode(isPsychologistView);
                ((ProgrammeController) controller).setMainController(this);
            } else if (controller instanceof AvisController) {
                ((AvisController) controller).setPsychologistMode(isPsychologistView);
            }

            contentArea.getChildren().setAll(view);
        } catch (IOException e) {
            System.err.println("Error loading FXML: " + fxmlPath);
            e.printStackTrace();
        }
    }

    // Méthode publique pour que les contrôleurs enfants demandent des changements
    // de vue
    /**
     * Méthode de navigation publique permettant aux contrôleurs enfants de
     * déclencher des changements de vue avec des données optionnelles.
     * 
     * @param fxmlPath Le chemin vers le FXML de destination.
     * @param data     Les données (ex: un Programme) à passer au nouveau
     *                 contrôleur.
     */
    public void navigateTo(String fxmlPath, Object data) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent view = loader.load();

            Object controller = loader.getController();
            if (controller instanceof ActiviteController && data instanceof ProgrammeBienEtre) {
                ((ActiviteController) controller).setMainController(this); // S'assurer que le mainController est défini
                ((ActiviteController) controller).setPsychologistMode(isPsychologistView); // Passer le mode actuel
                ((ActiviteController) controller).setProgramme((ProgrammeBienEtre) data);
            }

            contentArea.getChildren().setAll(view);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
