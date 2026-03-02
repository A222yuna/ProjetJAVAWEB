package tn.psy.gestioncabinet.controller;

import javafx.animation.FadeTransition;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Duration;
import tn.psy.gestioncabinet.controller.MapViewController;
import tn.psy.gestioncabinet.controller.PriseRdvController;
import tn.psy.gestioncabinet.model.Cabinet;
import tn.psy.gestioncabinet.model.Patient;
import tn.psy.gestioncabinet.service.CabinetService;
import tn.psy.gestioncabinet.service.MapService;
import tn.psy.gestioncabinet.util.CabinetEventBus;
import tn.psy.gestioncabinet.util.SceneManager;
import tn.psy.gestioncabinet.util.SessionManager;

import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

/**
 * Contrôleur du dashboard Patient
 */
public class PatientDashboardController implements Initializable {

    @FXML private VBox rootContent;
    @FXML private Label lblWelcome;
    @FXML private Label lblWelcomeCard;
    @FXML private Label lblCabinetsDisponibles;
    @FXML private Label lblResultatsRecherche;
    @FXML private TextField tfRecherche;
    @FXML private Button btnRechercher;
    @FXML private FlowPane flowCabinets;
    @FXML private Button btnDeconnexion;

    private final CabinetService cabinetService = new CabinetService();
    private Patient patient;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        patient = (Patient) SessionManager.getInstance().getUser();
        if (patient != null) {
            lblWelcome.setText(patient.getNom());
            lblWelcomeCard.setText("Bonjour, " + patient.getNom() + " !");
        }

        tfRecherche.setOnKeyPressed(e -> {
            if (e.getCode() == KeyCode.ENTER) rechercher();
        });

        btnRechercher.setOnAction(e -> rechercher());
        btnDeconnexion.setOnAction(e -> deconnexion());

        // S'abonner aux événements de changement de cabinet
        CabinetEventBus.getInstance().subscribe(this::chargerCabinets);

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

        chargerCabinets();
    }

    /**
     * Charge uniquement les cabinets validés depuis la base de données
     * Utilise findAllValides() pour filtrer les cabinets non validés
     */
    private void chargerCabinets() {
        List<Cabinet> cabinets = cabinetService.findAllValides();
        populateCards(cabinets);
        
        // Mettre à jour les statistiques
        lblCabinetsDisponibles.setText(String.valueOf(cabinets.size()));
        lblResultatsRecherche.setText(String.valueOf(cabinets.size()));
    }

    /**
     * Méthode publique pour rafraîchir la liste depuis l'extérieur
     */
    public void refresh() {
        chargerCabinets();
    }

    /**
     * Retourne le patient connecté (pour la notation des cabinets).
     */
    public Patient getPatient() {
        return patient;
    }

    private void rechercher() {
        String critere = tfRecherche.getText();
        List<Cabinet> cabinets = cabinetService.rechercher(critere);
        populateCards(cabinets);
        
        // Mettre à jour les statistiques de recherche
        lblResultatsRecherche.setText(String.valueOf(cabinets.size()));
    }

    /**
     * Ouvre une fenêtre avec la carte OpenStreetMap (Leaflet) et un marqueur à l'emplacement du cabinet.
     * Utilise l'API gratuite Nominatim pour géocoder l'adresse, puis Leaflet + tuiles OSM pour la carte.
     * Aucune clé API payante. Gestion des erreurs : adresse vide ou API sans résultat.
     */
    public void ouvrirCarte(Cabinet cabinet) {
        if (cabinet == null || cabinet.getAdresse() == null || cabinet.getAdresse().isBlank()) {
            new Alert(Alert.AlertType.WARNING, "Aucune adresse à afficher.", ButtonType.OK).showAndWait();
            return;
        }
        String adresse = cabinet.getAdresse().trim();

        // Géocodage gratuit via Nominatim (OpenStreetMap)
        java.util.Optional<double[]> coords = MapService.getCoordinatesFromAddress(adresse);
        boolean useLeaflet = coords.isPresent();
        double lat = useLeaflet ? coords.get()[0] : 0;
        double lon = useLeaflet ? coords.get()[1] : 0;
        String mapHtml = useLeaflet ? MapService.buildLeafletMapHtml(lat, lon, adresse) : null;
        String osmSearchUrl = MapService.getOsmSearchUrl(adresse);

        try {
            java.net.URL fxmlUrl = getClass().getResource("/fxml/map_view.fxml");
            if (fxmlUrl == null) {
                fxmlUrl = getClass().getClassLoader().getResource("fxml/map_view.fxml");
            }
            if (fxmlUrl != null) {
                FXMLLoader loader = new FXMLLoader(fxmlUrl);
                Parent root = loader.load();
                MapViewController ctrl = loader.getController();
                if (ctrl != null) {
                    if (useLeaflet) {
                        ctrl.setCoordinates(lat, lon);
                        ctrl.setAdresse(adresse);
                        ctrl.loadMap();
                    } else {
                        ctrl.loadMapFromUrl(osmSearchUrl);
                    }
                }
                Stage stage = new Stage();
                stage.initModality(Modality.NONE);
                try {
                    stage.initOwner(SceneManager.getPrimaryStage());
                } catch (Exception ignored) {}
                Scene scene = new Scene(root, 800, 600);
                try {
                    java.net.URL css = getClass().getResource("/css/style.css");
                    if (css != null) {
                        scene.getStylesheets().add(css.toExternalForm());
                    }
                } catch (Exception ignored) {}
                stage.setScene(scene);
                stage.setTitle("Localisation du cabinet");
                stage.show();
                return;
            }
        } catch (Exception ex) {
            System.err.println("Chargement map_view.fxml échoué, fallback WebView direct: " + ex.getMessage());
        }

        // Fallback : fenêtre construite en code (WebView occupe toute la fenêtre)
        try {
            javafx.scene.web.WebView webView = new javafx.scene.web.WebView();
            webView.setMinSize(500, 400);
            if (useLeaflet && mapHtml != null) {
                webView.getEngine().loadContent(mapHtml, "text/html");
            } else {
                webView.getEngine().load(osmSearchUrl);
            }
            javafx.scene.control.Button btnFermer = new javafx.scene.control.Button("Fermer");
            javafx.scene.layout.StackPane stack = new javafx.scene.layout.StackPane(webView, btnFermer);
            javafx.scene.layout.StackPane.setAlignment(btnFermer, javafx.geometry.Pos.BOTTOM_RIGHT);
            javafx.scene.layout.StackPane.setMargin(btnFermer, new javafx.geometry.Insets(0, 15, 15, 0));
            Stage stage = new Stage();
            stage.initModality(Modality.NONE);
            try {
                stage.initOwner(SceneManager.getPrimaryStage());
            } catch (Exception ignored) {}
            Scene scene = new Scene(stack, 800, 600);
            try {
                java.net.URL css = getClass().getResource("/css/style.css");
                if (css != null) {
                    scene.getStylesheets().add(css.toExternalForm());
                }
            } catch (Exception ignored) {}
            btnFermer.setOnAction(e -> stage.close());
            stage.setScene(scene);
            stage.setTitle("Localisation du cabinet");
            stage.show();
        } catch (Exception ex) {
            System.err.println("Fallback carte: " + ex.getMessage());
            ex.printStackTrace();
            new Alert(Alert.AlertType.ERROR,
                    "Impossible d'ouvrir la carte. Vérifiez que le module JavaFX Web (javafx-web) est disponible.\nDétail: " + ex.getMessage(),
                    ButtonType.OK).showAndWait();
        }
    }

    public void afficherDetails(Cabinet selected) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/cabinet_details.fxml"));
            Parent root = loader.load();
            CabinetDetailsController ctrl = loader.getController();
            ctrl.setCabinet(selected, true);

            Stage stage = new Stage();
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.initOwner(SceneManager.getPrimaryStage());
            Scene scene = new Scene(root);
            scene.getStylesheets().add(getClass().getResource("/css/style.css").toExternalForm());
            stage.setScene(scene);
            stage.setTitle("Détails du cabinet");
            stage.showAndWait();
        } catch (IOException ex) {
            new Alert(Alert.AlertType.ERROR, "Impossible d'afficher les détails.", ButtonType.OK).showAndWait();
        }
    }

    /**
     * Ouvre le dialogue de prise de rendez-vous : créneaux libres et réservation.
     */
    public void prendreRendezVous(Cabinet cabinet) {
        if (cabinet == null || patient == null) {
            new Alert(Alert.AlertType.WARNING, "Impossible d'ouvrir la réservation.", ButtonType.OK).showAndWait();
            return;
        }
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/prise_rdv.fxml"));
            Parent root = loader.load();
            PriseRdvController ctrl = loader.getController();
            ctrl.setData(cabinet, patient, this::refresh);

            Stage stage = new Stage();
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.initOwner(SceneManager.getPrimaryStage());
            Scene scene = new Scene(root);
            scene.getStylesheets().add(getClass().getResource("/css/style.css").toExternalForm());
            stage.setScene(scene);
            stage.setTitle("Prendre rendez-vous");
            stage.showAndWait();
        } catch (IOException ex) {
            new Alert(Alert.AlertType.ERROR, "Impossible d'ouvrir la réservation.", ButtonType.OK).showAndWait();
        }
    }

    /**
     * Construit dynamiquement les cartes de cabinets dans le FlowPane.
     */
    private void populateCards(List<Cabinet> cabinets) {
        if (flowCabinets == null) return;
        flowCabinets.getChildren().clear();

        for (Cabinet cabinet : cabinets) {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/cabinet_card.fxml"));
                Parent cardRoot = loader.load();
                CabinetCardController controller = loader.getController();
                controller.setData(cabinet, this);
                flowCabinets.getChildren().add(cardRoot);
            } catch (IOException e) {
                new Alert(Alert.AlertType.ERROR,
                        "Impossible de charger la carte du cabinet.",
                        ButtonType.OK).showAndWait();
            }
        }
    }

    @FXML
    private void deconnexion() {
        // Se désabonner de l'EventBus avant de quitter
        CabinetEventBus.getInstance().unsubscribe(this::chargerCabinets);
        SessionManager.getInstance().clearSession();
        try {
            SceneManager.loadScene("/fxml/login.fxml", "Connexion - Gestion Cabinet");
        } catch (IOException ex) {
            new Alert(Alert.AlertType.ERROR, "Impossible de se déconnecter.", ButtonType.OK).showAndWait();
        }
    }
}
