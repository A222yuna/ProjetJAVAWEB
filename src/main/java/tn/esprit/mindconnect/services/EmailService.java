package tn.esprit.mindconnect.services;

import tn.esprit.mindconnect.entities.ActiviteProgramme;
import tn.esprit.mindconnect.entities.ProgrammeBienEtre;

import java.util.List;
import java.util.Properties;
import javax.mail.*;
import javax.mail.internet.*;

/**
 * Service pour l'envoi d'emails de notification.
 * Envoie des emails lorsqu'un nouveau programme de bien-être est ajouté.
 */
public class EmailService {
    
    private static final String SENDER_EMAIL = "hassanjebri99@gmail.com";
    private static final String SENDER_PASSWORD = "annshqysvyicmsts";
    private static final String RECIPIENT_EMAIL = "ahssanjebri66@gmail.com";
    
    /**
     * Envoie un email de notification lorsqu'un nouveau programme est ajouté.
     */
    public void sendProgramNotification(ProgrammeBienEtre programme, List<ActiviteProgramme> activities) {
        try {
            System.out.println("📧 Envoi d'email pour le nouveau programme: " + programme.getNom());
            
            Properties props = new Properties();
            props.put("mail.smtp.auth", "true");
            props.put("mail.smtp.starttls.enable", "true");
            props.put("mail.smtp.host", "smtp.gmail.com");
            props.put("mail.smtp.port", "587");
            
            Session session = Session.getInstance(props, new Authenticator() {
                protected PasswordAuthentication getPasswordAuthentication() {
                    return new PasswordAuthentication(SENDER_EMAIL, SENDER_PASSWORD);
                }
            });
            
            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(SENDER_EMAIL));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(RECIPIENT_EMAIL));
            message.setSubject("🌟 Nouveau Programme de Bien-Être Ajouté - " + programme.getNom());
            
            // Créer le contenu HTML de l'email
            String emailContent = createProgramEmailContent(programme, activities);
            message.setContent(emailContent, "text/html; charset=utf-8");
            
            // Envoyer l'email
            Transport.send(message);
            System.out.println("✅ Email envoyé avec succès à " + RECIPIENT_EMAIL);
            
        } catch (Exception e) {
            System.err.println("❌ Erreur lors de l'envoi de l'email: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Crée le contenu HTML de l'email pour le programme.
     */
    private String createProgramEmailContent(ProgrammeBienEtre programme, List<ActiviteProgramme> activities) {
        StringBuilder content = new StringBuilder();
        
        content.append("<!DOCTYPE html>");
        content.append("<html><head>");
        content.append("<meta charset='UTF-8'>");
        content.append("<title>Nouveau Programme MindConnect</title>");
        content.append("<style>");
        content.append("body { font-family: 'Segoe UI', Arial, sans-serif; margin: 0; padding: 20px; background-color: #f5f7fa; }");
        content.append(".container { max-width: 600px; margin: 0 auto; background-color: white; border-radius: 15px; overflow: hidden; box-shadow: 0 4px 15px rgba(0,0,0,0.1); }");
        content.append(".header { background: linear-gradient(135deg, #7C9A85 0%, #5A7A6B 100%); color: white; padding: 40px 30px; text-align: center; }");
        content.append(".header h1 { margin: 0; font-size: 28px; font-weight: 300; }");
        content.append(".header p { margin: 10px 0 0 0; opacity: 0.9; font-size: 16px; }");
        content.append(".content { padding: 30px; }");
        content.append(".program-info { background-color: #F7F4EE; border-radius: 10px; padding: 20px; margin-bottom: 25px; }");
        content.append(".info-row { display: flex; margin-bottom: 10px; }");
        content.append(".info-label { font-weight: bold; color: #7C9A85; width: 120px; }");
        content.append(".info-value { color: #2A2A2A; }");
        content.append(".activities { margin-top: 25px; }");
        content.append(".activities h3 { color: #7C9A85; border-bottom: 2px solid #F7F4EE; padding-bottom: 10px; }");
        content.append(".activity { background-color: white; border-left: 4px solid #C9A96E; margin: 15px 0; padding: 15px; border-radius: 5px; box-shadow: 0 2px 5px rgba(0,0,0,0.05); }");
        content.append(".activity-title { font-weight: bold; color: #2A2A2A; margin-bottom: 5px; }");
        content.append(".activity-details { color: #8B8680; font-size: 14px; }");
        content.append(".footer { background-color: #F7F4EE; padding: 20px; text-align: center; color: #8B8680; font-size: 14px; }");
        content.append("</style>");
        content.append("</head><body>");
        
        // Header
        content.append("<div class='container'>");
        content.append("<div class='header'>");
        content.append("<h1>🌟 Nouveau Programme de Bien-Être</h1>");
        content.append("<p>MindConnect - Votre partenaire bien-être</p>");
        content.append("</div>");
        
        // Content
        content.append("<div class='content'>");
        content.append("<h2>🎯 Détails du Programme</h2>");
        content.append("<div class='program-info'>");
        content.append("<div class='info-row'><span class='info-label'>Nom:</span><span class='info-value'>").append(programme.getNom()).append("</span></div>");
        content.append("<div class='info-row'><span class='info-label'>Objectif:</span><span class='info-value'>").append(programme.getObjectif()).append("</span></div>");
        content.append("<div class='info-row'><span class='info-label'>Durée:</span><span class='info-value'>").append(programme.getDuree()).append(" jours</span></div>");
        content.append("<div class='info-row'><span class='info-label'>Niveau:</span><span class='info-value'>").append(programme.getNiveauDifficulte()).append("</span></div>");
        content.append("<div class='info-row'><span class='info-label'>Statut:</span><span class='info-value'>").append(programme.getStatut()).append("</span></div>");
        content.append("</div>");
        
        // Activities
        content.append("<div class='activities'>");
        content.append("<h3>📅 Activités du Programme</h3>");
        
        if (activities != null && !activities.isEmpty()) {
            // Group activities by day
            activities.stream()
                .collect(java.util.stream.Collectors.groupingBy(ActiviteProgramme::getJour))
                .entrySet().stream()
                .sorted(java.util.Map.Entry.comparingByKey())
                .forEach(entry -> {
                    int day = entry.getKey();
                    List<ActiviteProgramme> dayActivities = entry.getValue();
                    
                    content.append("<h4>📆 Jour ").append(day).append("</h4>");
                    for (ActiviteProgramme activity : dayActivities) {
                        content.append("<div class='activity'>");
                        content.append("<div class='activity-title'>").append(activity.getTitre()).append("</div>");
                        content.append("<div class='activity-details'>");
                        content.append("⏰ ").append(activity.getHeureDebut().toString().substring(0, 5));
                        content.append(" | ⏱️ ").append(activity.getDureeMinutes()).append(" minutes");
                        content.append("<br>").append(activity.getDescription());
                        content.append("</div>");
                        content.append("</div>");
                    }
                });
        } else {
            content.append("<p>Aucune activité définie pour ce programme.</p>");
        }
        
        content.append("</div>");
        content.append("</div>");
        
        // Footer
        content.append("<div class='footer'>");
        content.append("<p>📧 Cet email a été généré automatiquement par MindConnect</p>");
        content.append("<p>© 2024 MindConnect - Tous droits réservés</p>");
        content.append("</div>");
        content.append("</div>");
        
        content.append("</body></html>");
        
        return content.toString();
    }
    
    /**
     * Test la configuration email.
     */
    public boolean testEmailConfiguration() {
        try {
            Properties props = new Properties();
            props.put("mail.smtp.auth", "true");
            props.put("mail.smtp.starttls.enable", "true");
            props.put("mail.smtp.host", "smtp.gmail.com");
            props.put("mail.smtp.port", "587");
            
            Session session = Session.getInstance(props, new Authenticator() {
                protected PasswordAuthentication getPasswordAuthentication() {
                    return new PasswordAuthentication(SENDER_EMAIL, SENDER_PASSWORD);
                }
            });
            
            Transport transport = session.getTransport("smtp");
            transport.connect();
            transport.close();
            
            System.out.println("✅ Configuration email validée avec succès");
            return true;
            
        } catch (Exception e) {
            System.err.println("❌ Erreur de configuration email: " + e.getMessage());
            return false;
        }
    }
}
