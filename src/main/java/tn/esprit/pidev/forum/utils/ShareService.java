package tn.esprit.pidev.forum.utils;

import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

/**
 * API #2: SHARE BUTTON avec TinyURL
 *
 * Génère des liens courts pour partager les posts
 * API: https://tinyurl.com/api-create.php
 * Gratuite, sans inscription!
 */
public class ShareService {

    private static final String TINYURL_API = "https://tinyurl.com/api-create.php";
    private static final String BASE_URL = "http://localhost:8080/post/"; // À adapter

    /**
     * Génère un lien court pour un post
     *
     * @param postId ID du post
     * @return URL courte (ex: https://tinyurl.com/abc123)
     */
    public static String generateShareLink(int postId) {
        try {
            System.out.println("Génération du lien de partage...");

            String fullUrl = BASE_URL + postId;
            System.out.println("URL originale: " + fullUrl);

            String encodedUrl = URLEncoder.encode(fullUrl, StandardCharsets.UTF_8.toString());
            String apiUrl = TINYURL_API + "?url=" + encodedUrl;
            System.out.println("Appel TinyURL API...");

            URL url = new URL(apiUrl);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setConnectTimeout(5000);
            conn.setReadTimeout(5000);

            BufferedReader in = new BufferedReader(
                    new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8)
            );
            String shortUrl = in.readLine();
            in.close();
            conn.disconnect();

            System.out.println("Lien court généré: " + shortUrl);
            return shortUrl != null ? shortUrl : BASE_URL + postId;

        } catch (Exception e) {
            System.err.println("Erreur génération lien: " + e.getMessage());
            return BASE_URL + postId;
        }
    }

    /**
     * Copie le lien dans le presse-papier
     */
    public static boolean copyToClipboard(String link) {
        try {
            StringSelection selection = new StringSelection(link);
            Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
            clipboard.setContents(selection, selection);
            System.out.println("Lien copié dans le presse-papier!");
            return true;
        } catch (Exception e) {
            System.err.println("Erreur copie presse-papier: " + e.getMessage());
            return false;
        }
    }

    /**
     * Génère un message de partage formaté
     */
    public static String generateShareMessage(String postTitle, int postId) {
        String shortUrl = generateShareLink(postId);
        return "📝 " + postTitle + "\n\n" +
                "🔗 Lire sur le forum: " + shortUrl + "\n\n" +
                "#ForumSantéMentale #Partage";
    }

    /**
     * Génère l'URL d'un QR code pour le lien
     */
    public static String generateQRCodeUrl(int postId) {
        try {
            String shortUrl = generateShareLink(postId);
            String encodedUrl = URLEncoder.encode(shortUrl, StandardCharsets.UTF_8.toString());
            return "https://api.qrserver.com/v1/create-qr-code/?size=200x200&data=" + encodedUrl;
        } catch (Exception e) {
            return "";
        }
    }
}
