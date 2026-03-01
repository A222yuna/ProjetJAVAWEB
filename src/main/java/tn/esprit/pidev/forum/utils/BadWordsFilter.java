package tn.esprit.pidev.forum.utils;

import org.json.JSONObject;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

/**
 * ✅ API #1: BAD WORDS FILTER
 *
 * Utilise l'API PurgoMalum (gratuite, sans clé API)
 * https://www.purgomalum.com/
 *
 * Filtre automatiquement les gros mots et les remplace par *****
 */
public class BadWordsFilter {

    // URL de l'API PurgoMalum
    private static final String API_URL = "https://www.purgomalum.com/service/json";
    private static final int TIMEOUT = 5000; // 5 secondes

    /**
     * Filtre les gros mots d'un texte
     *
     * @param text Texte à filtrer
     * @return Texte avec gros mots remplacés par *****
     */
    public static String filterText(String text) {
        // Validation: si texte vide, retourner tel quel
        if (text == null || text.trim().isEmpty()) {
            return text;
        }

        try {
            System.out.println("🔍 Filtrage du texte avec Bad Words API...");

            // ÉTAPE 1: Encoder le texte pour l'URL (remplace espaces par %20, etc.)
            String encodedText = URLEncoder.encode(text, StandardCharsets.UTF_8.toString());

            // ÉTAPE 2: Construire l'URL complète
            String urlString = API_URL + "?text=" + encodedText;
            System.out.println("📡 Appel API: " + API_URL);

            // ÉTAPE 3: Créer la connexion HTTP
            URL url = new URL(urlString);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setConnectTimeout(TIMEOUT);
            conn.setReadTimeout(TIMEOUT);
            conn.setRequestProperty("User-Agent", "ForumApp/1.0");

            // ÉTAPE 4: Vérifier le code de réponse
            int responseCode = conn.getResponseCode();
            if (responseCode != 200) {
                System.err.println("⚠️ API retournée code: " + responseCode);
                return text; // Si erreur, retourner texte original
            }

            // ÉTAPE 5: Lire la réponse de l'API
            BufferedReader in = new BufferedReader(
                    new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8)
            );
            StringBuilder response = new StringBuilder();
            String line;

            while ((line = in.readLine()) != null) {
                response.append(line);
            }
            in.close();
            conn.disconnect();

            // ÉTAPE 6: Parser la réponse JSON
            // Format de réponse: {"result":"texte filtré ici"}
            JSONObject json = new JSONObject(response.toString());
            String filteredText = json.getString("result");

            // ÉTAPE 7: Comparer et afficher le résultat
            if (!filteredText.equals(text)) {
                System.out.println("⚠️ Gros mots détectés et censurés!");
                System.out.println("   Original: " + text.substring(0, Math.min(50, text.length())) + "...");
                System.out.println("   Filtré:   " + filteredText.substring(0, Math.min(50, filteredText.length())) + "...");
            } else {
                System.out.println("✅ Aucun gros mot détecté - texte propre!");
            }

            return filteredText;

        } catch (Exception e) {
            // Si erreur (pas d'internet, API down, etc.), retourner texte original
            System.err.println("❌ Erreur Bad Words Filter: " + e.getMessage());
            return text;
        }
    }

    /**
     * Vérifie si un texte contient des gros mots
     *
     * @param text Texte à vérifier
     * @return true si contient des gros mots, false sinon
     */
    public static boolean containsBadWords(String text) {
        if (text == null || text.trim().isEmpty()) {
            return false;
        }

        String filtered = filterText(text);
        // Si le texte filtré est différent, c'est qu'il contenait des gros mots
        return !filtered.equals(text);
    }

    /**
     * Compte le nombre de gros mots censurés
     *
     * @param text Texte original
     * @return Nombre de gros mots détectés
     */
    public static int countBadWords(String text) {
        String filtered = filterText(text);
        int count = 0;

        // Compter les occurrences de *****
        String marker = "*****";
        int index = filtered.indexOf(marker);

        while (index != -1) {
            count++;
            index = filtered.indexOf(marker, index + marker.length());
        }

        return count;
    }

    /**
     * Filtre avec un texte de remplacement personnalisé
     *
     * @param text Texte à filtrer
     * @param replacement Texte de remplacement (ex: "[CENSURÉ]")
     * @return Texte filtré
     */
    public static String filterWithCustomReplacement(String text, String replacement) {
        if (text == null || text.trim().isEmpty()) {
            return text;
        }

        try {
            String encodedText = URLEncoder.encode(text, StandardCharsets.UTF_8.toString());
            String encodedReplacement = URLEncoder.encode(replacement, StandardCharsets.UTF_8.toString());
            String urlString = API_URL + "?text=" + encodedText + "&fill_text=" + encodedReplacement;

            URL url = new URL(urlString);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setConnectTimeout(TIMEOUT);
            conn.setReadTimeout(TIMEOUT);

            BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            StringBuilder response = new StringBuilder();
            String line;

            while ((line = in.readLine()) != null) {
                response.append(line);
            }
            in.close();
            conn.disconnect();

            JSONObject json = new JSONObject(response.toString());
            return json.getString("result");

        } catch (Exception e) {
            System.err.println("❌ Erreur: " + e.getMessage());
            return text;
        }
    }

    /**
     * Méthode de test
     */
    public static void main(String[] args) {
        System.out.println("=== TEST BAD WORDS FILTER ===\n");

        // Test 1: Texte propre
        String clean = "Ceci est un texte propre et gentil.";
        System.out.println("Test 1 - Texte propre:");
        System.out.println("Original: " + clean);
        System.out.println("Filtré:   " + filterText(clean));
        System.out.println("Contient bad words? " + containsBadWords(clean));
        System.out.println();

        // Test 2: Texte avec gros mot (exemple)
        String dirty = "This is a damn test.";
        System.out.println("Test 2 - Texte avec gros mot:");
        System.out.println("Original: " + dirty);
        System.out.println("Filtré:   " + filterText(dirty));
        System.out.println("Contient bad words? " + containsBadWords(dirty));
        System.out.println("Nombre de bad words: " + countBadWords(dirty));
        System.out.println();

        System.out.println("✅ Bad Words Filter est prêt!");
    }
}