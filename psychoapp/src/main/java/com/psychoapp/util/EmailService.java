package com.psychoapp.util;

import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

/**
 * Simple helper for sending transactional emails via Mailgun HTTP API.
 *
 * Required environment variables:
 * - MAILGUN_API_KEY  : your Mailgun private API key
 * - MAILGUN_DOMAIN   : your Mailgun domain (e.g. sandbox123.mailgun.org)
 * - APP_MAIL_FROM    : from address, e.g. no-reply@your-domain.com
 */
public class EmailService {

    private static final String API_KEY   = System.getenv("MAILGUN_API_KEY");
    private static final String DOMAIN    = System.getenv("MAILGUN_DOMAIN");
    private static final String FROM_MAIL = System.getenv("APP_MAIL_FROM") != null
            ? System.getenv("APP_MAIL_FROM")
            : "syramejry@gmail.com";
    private static final String FROM_NAME = "PsychoApp";

    public static void sendWelcomePsychologist(String toEmail, String fullName) {
        if (API_KEY == null || API_KEY.isBlank()) return;
        if (DOMAIN == null || DOMAIN.isBlank()) return;
        if (toEmail == null || toEmail.isBlank()) return;

        String subject = "Votre compte psychologue est approuve";
        String body =
                "Bonjour " + (fullName != null ? fullName : "") + ",\n\n" +
                "Votre compte psychologue a ete approuve par l'administrateur.\n" +
                "Vous pouvez maintenant vous connecter a PsychoApp avec vos identifiants.\n\n" +
                "Cordialement,\n" +
                "L'equipe PsychoApp";

        String fromValue = FROM_NAME + " <" + FROM_MAIL + ">";

        try {
            String form = "from=" + url(fromValue) +
                    "&to=" + url(toEmail) +
                    "&subject=" + url(subject) +
                    "&text=" + url(body);

            String basicToken = Base64.getEncoder()
                    .encodeToString(("api:" + API_KEY).getBytes(StandardCharsets.UTF_8));

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("https://api.mailgun.net/v3/" + DOMAIN + "/messages"))
                    .header("Authorization", "Basic " + basicToken)
                    .header("Content-Type", "application/x-www-form-urlencoded")
                    .POST(HttpRequest.BodyPublishers.ofString(form))
                    .build();

            HttpClient.newHttpClient()
                    .sendAsync(request, HttpResponse.BodyHandlers.discarding());
        } catch (Exception ignored) {
            // Do not block the UI on email errors
        }
    }

    private static String url(String value) {
        return URLEncoder.encode(value, StandardCharsets.UTF_8);
    }
}

