package tn.psy.gestioncabinet.service;

import jakarta.mail.Authenticator;
import jakarta.mail.Message;
import jakarta.mail.PasswordAuthentication;
import jakarta.mail.Session;
import jakarta.mail.Transport;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;

import java.util.Properties;

/**
 * Service d'envoi d'emails via Gmail SMTP (gratuit).
 * Utilisé pour notifier le psychologue lors de la validation d'un cabinet.
 * Credentials : GMAIL_USER (expéditeur), GMAIL_APP_PASSWORD (mot de passe application).
 */
public class EmailService {

    private static final String SMTP_HOST = "smtp.gmail.com";
    private static final int SMTP_PORT = 587;

    private static String getEnvOrProperty(String envName, String propName) {
        String v = System.getenv(envName);
        if (v != null && !v.isBlank()) return v.trim();
        return System.getProperty(propName, "");
    }

    private static String getGmailUser() {
        return getEnvOrProperty("GMAIL_USER", "gmail.user");
    }

    private static String getGmailAppPassword() {
        return getEnvOrProperty("GMAIL_APP_PASSWORD", "gmail.app.password");
    }

    /**
     * Envoie un email au psychologue pour confirmer la validation de son cabinet.
     * Utilise Gmail SMTP (smtp.gmail.com, port 587, STARTTLS).
     *
     * @param emailPsychologue email du psychologue (destinataire)
     * @param nomCabinet       libellé du cabinet (ex: adresse ou "Cabinet X")
     * @return true si l'envoi a réussi, false sinon
     */
    public boolean sendValidationEmail(String emailPsychologue, String nomCabinet) {
        System.out.println("[EmailService] sendValidationEmail - destinataire: " + (emailPsychologue != null ? emailPsychologue : "null") + ", nomCabinet: " + (nomCabinet != null ? nomCabinet : "null"));

        if (emailPsychologue == null || emailPsychologue.isBlank()) {
            System.err.println("[EmailService] Destinataire vide ou null - envoi annulé.");
            return false;
        }

        String user = getGmailUser();
        String password = getGmailAppPassword();
        if (user.isBlank() || password.isBlank()) {
            System.err.println("[EmailService] GMAIL_USER ou GMAIL_APP_PASSWORD absents. Définir les variables d'environnement ou -Dgmail.user et -Dgmail.app.password");
            return false;
        }

        String subject = "Votre cabinet a été validé";
        String content = "Votre cabinet \"" + (nomCabinet != null ? nomCabinet : "Cabinet") + "\" a été validé avec succès.";
        System.out.println("[EmailService] Sujet: " + subject);
        System.out.println("[EmailService] Message: " + content);

        Properties props = new Properties();
        props.put("mail.smtp.host", SMTP_HOST);
        props.put("mail.smtp.port", String.valueOf(SMTP_PORT));
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.starttls.required", "true");

        Authenticator authenticator = new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(user, password);
            }
        };

        Session session = Session.getInstance(props, authenticator);
        try {
            MimeMessage message = new MimeMessage(session);
            message.setFrom(new InternetAddress(user, "Gestion Cabinet"));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(emailPsychologue.trim()));
            message.setSubject(subject, "UTF-8");
            message.setText(content, "UTF-8");
            Transport.send(message);
            System.out.println("[EmailService] Email envoyé avec succès à " + emailPsychologue);
            return true;
        } catch (Exception e) {
            System.err.println("[EmailService] Erreur envoi email: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
}
