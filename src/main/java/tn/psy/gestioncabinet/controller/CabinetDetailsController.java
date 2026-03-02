package tn.psy.gestioncabinet.controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;
import tn.psy.gestioncabinet.model.PsyCabinet;
import tn.psy.gestioncabinet.service.CabinetService;
import tn.psy.gestioncabinet.service.MapService;
import tn.psy.gestioncabinet.service.RatingService;

import java.io.IOException;
import java.util.List;

/**
 * Contrôleur des détails du cabinet
 */
public class CabinetDetailsController {

    @FXML private Label lblAdresse;
    @FXML private Label lblVille;
    @FXML private Label lblHoraires;
    @FXML private Label lblDescription;
    @FXML private Label lblStatut;
    @FXML private HBox boxRating;
    @FXML private Label lblRating;
    @FXML private TextArea taPsychologues;
    @FXML private Button btnCarte;
    @FXML private Button btnFermer;

    private final CabinetService cabinetService = new CabinetService();
    private final RatingService ratingService = new RatingService();
    private tn.psy.gestioncabinet.model.Cabinet cabinet;

    /**
     * Définit le cabinet affiché (vue Admin / Psychologue : statut complet).
     */
    public void setCabinet(tn.psy.gestioncabinet.model.Cabinet cabinet) {
        setCabinet(cabinet, false);
    }

    /**
     * Définit le cabinet affiché.
     * @param cabinet le cabinet
     * @param vuePatient si true, n'affiche jamais "Archivé" (statut = "Validé" uniquement)
     */
    public void setCabinet(tn.psy.gestioncabinet.model.Cabinet cabinet, boolean vuePatient) {
        this.cabinet = cabinet;
        if (cabinet != null) {
            lblAdresse.setText(cabinet.getAdresse() != null ? cabinet.getAdresse() : "-");
            lblVille.setText(cabinet.getVille() != null ? cabinet.getVille() : "-");
            lblHoraires.setText(cabinet.getHoraires() != null ? cabinet.getHoraires() : "-");
            lblDescription.setText(cabinet.getDescription() != null ? cabinet.getDescription() : "-");
            lblStatut.setText(vuePatient ? "Validé" : cabinet.getStatutTexte());

            if (vuePatient && boxRating != null && lblRating != null) {
                double moy = ratingService.getMoyenne(cabinet.getIdCabinet());
                int nb = ratingService.getNombreAvis(cabinet.getIdCabinet());
                lblRating.setText(nb == 0 ? "— ★ (0 avis)" : String.format("%.1f ★ (%d avis)", moy, nb));
                boxRating.setVisible(true);
                boxRating.setManaged(true);
            } else if (boxRating != null) {
                boxRating.setVisible(false);
                boxRating.setManaged(false);
            }

            List<PsyCabinet> psychologues = cabinetService.getPsychologuesByCabinet(cabinet.getIdCabinet());
            StringBuilder sb = new StringBuilder();
            for (PsyCabinet pc : psychologues) {
                sb.append("• ").append(pc.getNomPsychologue()).append("\n");
            }
            taPsychologues.setText(sb.length() > 0 ? sb.toString() : "Aucun psychologue associé.");
        }
    }

    @FXML
    public void initialize() {
        btnFermer.setOnAction(e -> fermer());
        if (btnCarte != null) {
            btnCarte.setOnAction(e -> ouvrirCarte());
        }
    }

    private void ouvrirCarte() {
        if (cabinet == null || cabinet.getAdresse() == null || cabinet.getAdresse().isBlank()) {
            return;
        }
        String adresse = cabinet.getAdresse().trim();
<<<<<<< HEAD

        // Adresse plus complète pour le géocodage : adresse + ville (meilleure précision pour Nominatim)
        StringBuilder sbAdresseComplete = new StringBuilder();
        if (cabinet.getAdresse() != null && !cabinet.getAdresse().isBlank()) {
            sbAdresseComplete.append(cabinet.getAdresse().trim());
        }
        if (cabinet.getVille() != null && !cabinet.getVille().isBlank()) {
            if (!sbAdresseComplete.isEmpty()) {
                sbAdresseComplete.append(", ");
            }
            sbAdresseComplete.append(cabinet.getVille().trim());
        }
        String adresseComplete = sbAdresseComplete.length() > 0 ? sbAdresseComplete.toString() : adresse;

        java.util.Optional<double[]> coords = MapService.getCoordinatesFromAddress(adresseComplete);
=======
        java.util.Optional<double[]> coords = MapService.getCoordinatesFromAddress(adresse);
>>>>>>> origin/gestion-cabinet
        boolean useLeaflet = coords.isPresent();
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/map_view.fxml"));
            Parent root = loader.load();
            tn.psy.gestioncabinet.controller.MapViewController ctrl = loader.getController();
            if (ctrl != null) {
                if (useLeaflet) {
                    ctrl.setCoordinates(coords.get()[0], coords.get()[1]);
<<<<<<< HEAD
                    ctrl.setAdresse(adresseComplete);
                    ctrl.loadMap();
                } else {
                    ctrl.loadMapFromUrl(MapService.getOsmSearchUrl(adresseComplete));
=======
                    ctrl.setAdresse(adresse);
                    ctrl.loadMap();
                } else {
                    ctrl.loadMapFromUrl(MapService.getOsmSearchUrl(adresse));
>>>>>>> origin/gestion-cabinet
                }
            }
            Stage stage = new Stage();
            stage.initOwner(btnFermer.getScene().getWindow());
            Scene scene = new Scene(root, 800, 600);
            try {
                java.net.URL css = getClass().getResource("/css/style.css");
                if (css != null) scene.getStylesheets().add(css.toExternalForm());
            } catch (Exception ignored) {}
            stage.setScene(scene);
            stage.setTitle("Localisation du cabinet");
            stage.show();
        } catch (IOException ex) {
            // Silently ignore if map view fails
        }
    }

    private void fermer() {
        Stage stage = (Stage) btnFermer.getScene().getWindow();
        stage.close();
    }
}
