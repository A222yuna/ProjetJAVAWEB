package tn.psy.gestioncabinet.controller;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.web.WebView;
import javafx.stage.Stage;
import tn.psy.gestioncabinet.service.MapService;

/**
 * Contrôleur de la fenêtre "Voir sur la carte".
 * Affiche une carte OpenStreetMap via Leaflet (CDN) avec un marqueur aux coordonnées fournies.
 * 100 % gratuit (Nominatim pour le géocodage, Leaflet + OSM pour la carte).
 */
public class MapViewController {

    @FXML private WebView webView;
    @FXML private Button btnFermer;

    private double lat;
    private double lon;
    private String adresse;
    private boolean coordinatesSet;

    /**
     * Définit les coordonnées du marqueur (obtenues via l'API Nominatim).
     */
    public void setCoordinates(double lat, double lon) {
        this.lat = lat;
        this.lon = lon;
        this.coordinatesSet = true;
    }

    /**
     * Définit le libellé affiché dans le popup du marqueur (ex: adresse du cabinet).
     */
    public void setAdresse(String adresse) {
        this.adresse = adresse != null ? adresse.trim() : null;
    }

    /**
     * Permet d'injecter le WebView depuis l'extérieur (fallback si chargement FXML échoue).
     */
    public void setWebView(WebView webView) {
        this.webView = webView;
    }

    @FXML
    public void initialize() {
        if (btnFermer != null) {
            btnFermer.setOnAction(e -> fermer());
        }
        if (coordinatesSet && webView != null) {
            loadMap();
        }
    }

    /**
     * Charge la carte Leaflet + OpenStreetMap avec un marqueur aux coordonnées définies.
     * Appelé après setCoordinates et setAdresse. Utilise l'API gratuite (pas de clé).
     */
    public void loadMap() {
        if (webView == null) {
            return;
        }
        if (!coordinatesSet) {
            webView.getEngine().loadContent(
                "<html><body style='margin:0;padding:20px;font-family:sans-serif;'>" +
                "<p>Aucune position à afficher.</p>" +
                "</body></html>",
                "text/html");
            return;
        }
        String html = MapService.buildLeafletMapHtml(lat, lon, adresse);
        webView.getEngine().loadContent(html, "text/html");
    }

    /**
     * Charge une URL dans la carte (ex: page de recherche OpenStreetMap en secours si géocodage échoue).
     * 100 % gratuit.
     */
    public void loadMapFromUrl(String url) {
        if (webView == null || url == null || url.isBlank()) {
            return;
        }
        webView.getEngine().load(url);
    }

    private void fermer() {
        Stage stage = null;
        if (btnFermer != null && btnFermer.getScene() != null) {
            stage = (Stage) btnFermer.getScene().getWindow();
        } else if (webView != null && webView.getScene() != null) {
            stage = (Stage) webView.getScene().getWindow();
        }
        if (stage != null) {
            stage.close();
        }
    }
}
