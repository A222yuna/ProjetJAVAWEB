package tn.psy.gestioncabinet.service;

import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Service de géolocalisation et affichage carte.
 * Utilise Nominatim (OpenStreetMap) pour le géocodage et Leaflet + tuiles OSM pour la carte.
 * 100 % gratuit, aucune clé API payante.
 */
public class MapService {

    private static final String NOMINATIM_URL = "https://nominatim.openstreetmap.org/search";
    private static final String USER_AGENT = "GestionCabinet/1.0 (application JavaFX)";
    private static final Duration REQUEST_TIMEOUT = Duration.ofSeconds(15);

    /** Pattern pour extraire "lat":"xx.xx" et "lon":"yy.yy" du JSON Nominatim. */
    private static final Pattern LAT_PATTERN = Pattern.compile("\"lat\":\"([^\"]+)\"");
    private static final Pattern LON_PATTERN = Pattern.compile("\"lon\":\"([^\"]+)\"");

    /**
     * Géocode une adresse via l'API Nominatim (OpenStreetMap), gratuite et sans clé.
     * @param adresse adresse complète (ex: "Tunisia, Mednine 4100")
     * @return Optional avec [latitude, longitude] ou vide si non trouvé / erreur
     */
    public static Optional<double[]> getCoordinatesFromAddress(String adresse) {
        if (adresse == null || adresse.isBlank()) {
            return Optional.empty();
        }
        try {
            String encoded = URLEncoder.encode(adresse.trim(), StandardCharsets.UTF_8);
            String url = NOMINATIM_URL + "?q=" + encoded + "&format=json&limit=1";
            HttpClient client = HttpClient.newBuilder().connectTimeout(REQUEST_TIMEOUT).build();
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .timeout(REQUEST_TIMEOUT)
                    .header("User-Agent", USER_AGENT)
                    .GET()
                    .build();
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
            String body = response.body();
            if (body == null || body.isEmpty() || "[]".equals(body.trim())) {
                return Optional.empty();
            }
            Matcher latM = LAT_PATTERN.matcher(body);
            Matcher lonM = LON_PATTERN.matcher(body);
            if (latM.find() && lonM.find()) {
                double lat = Double.parseDouble(latM.group(1));
                double lon = Double.parseDouble(lonM.group(1));
                return Optional.of(new double[]{lat, lon});
            }
        } catch (Exception e) {
            System.err.println("MapService Nominatim: " + e.getMessage());
        }
        return Optional.empty();
    }

    /**
     * URL de recherche OpenStreetMap (gratuite). À utiliser en secours si Nominatim ne renvoie rien :
     * la carte s'ouvre quand même et OSM affiche la recherche.
     */
    public static String getOsmSearchUrl(String adresse) {
        if (adresse == null || adresse.isBlank()) return "https://www.openstreetmap.org";
        try {
            String encoded = URLEncoder.encode(adresse.trim(), StandardCharsets.UTF_8);
            return "https://www.openstreetmap.org/search?query=" + encoded;
        } catch (Exception e) {
            return "https://www.openstreetmap.org";
        }
    }

    /**
     * Génère une page HTML avec Leaflet + OpenStreetMap et un marqueur aux coordonnées données.
     * À charger dans un WebView (loadContent). 100 % gratuit (CDN Leaflet + tuiles OSM).
     */
    public static String buildLeafletMapHtml(double lat, double lon, String markerLabel) {
        String label = escapeHtml(markerLabel != null ? markerLabel : "Cabinet");
        String latStr = Double.toString(lat);
        String lonStr = Double.toString(lon);

        StringBuilder sb = new StringBuilder();
        sb.append("<!DOCTYPE html>\n")
                .append("<html>\n")
                .append("<head>\n")
                .append("  <meta charset=\"utf-8\"/>\n")
                .append("  <link rel=\"stylesheet\" href=\"https://unpkg.com/leaflet@1.9.4/dist/leaflet.css\"/>\n")
                .append("  <script src=\"https://unpkg.com/leaflet@1.9.4/dist/leaflet.js\"></script>\n")
                .append("  <style>\n")
                .append("    body { margin: 0; padding: 0; height: 100vh; }\n")
                .append("    #map { width: 100%; height: 100%; }\n")
                .append("  </style>\n")
                .append("</head>\n")
                .append("<body>\n")
                .append("  <div id=\"map\"></div>\n")
                .append("  <script>\n")
                .append("    var map = L.map('map').setView([")
                .append(latStr).append(", ").append(lonStr).append("], 16);\n")
                .append("    L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {\n")
                .append("      attribution: '&copy; <a href=\"https://www.openstreetmap.org/copyright\">OpenStreetMap</a>'\n")
                .append("    }).addTo(map);\n")
                .append("    var marker = L.marker([")
                .append(latStr).append(", ").append(lonStr).append("]).addTo(map);\n")
                .append("    marker.bindPopup(\"").append(label).append("\").openPopup();\n")
                .append("  </script>\n")
                .append("</body>\n")
                .append("</html>\n");

        return sb.toString();
    }

    private static String escapeHtml(String s) {
        if (s == null) return "";
        return s.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("&", "&amp;");
    }

    // --- Ancienne API Google (conservée pour isAdresseValide si besoin) ---
    private static final String GEOCODE_URL = "https://maps.googleapis.com/maps/api/geocode/json";

    private static String getGoogleApiKey() {
        String key = System.getenv("GOOGLE_MAPS_API_KEY");
        if (key == null || key.isBlank()) {
            key = System.getProperty("google.maps.api.key", "");
        }
        return key;
    }

    /**
     * Vérifie que l'adresse existe (Nominatim gratuit en priorité, sinon Google si clé configurée).
     */
    public boolean isAdresseValide(String adresse) {
        if (adresse == null || adresse.isBlank()) return false;
        if (getCoordinatesFromAddress(adresse).isPresent()) return true;
        String key = getGoogleApiKey();
        if (key.isBlank()) return true;
        try {
            String encoded = URLEncoder.encode(adresse.trim(), StandardCharsets.UTF_8);
            String url = GEOCODE_URL + "?address=" + encoded + "&key=" + key;
            HttpClient client = HttpClient.newBuilder().connectTimeout(REQUEST_TIMEOUT).build();
            HttpRequest request = HttpRequest.newBuilder().uri(URI.create(url)).timeout(REQUEST_TIMEOUT).GET().build();
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
            String body = response.body();
            return body != null && body.contains("\"status\"") && body.contains("OK") && body.contains("\"results\"");
        } catch (Exception e) {
            return false;
        }
    }
}
