package tn.esprit.mindconnect.services;

import java.awt.Desktop;
import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Service pour l'intégration avec Google Calendar.
 * Permet d'ajouter des activités au calendrier Google de l'utilisateur.
 */
public class GoogleCalendarService {
    
    private static final String GOOGLE_CALENDAR_URL = "https://calendar.google.com/calendar/render";
    
    /**
     * Ouvre Google Calendar dans le navigateur avec les détails de l'activité pré-remplis.
     */
    public void addToGoogleCalendar(String activityTitle, String description, 
                                   LocalDateTime startTime, LocalDateTime endTime, 
                                   String location) {
        try {
            // Formater les dates pour Google Calendar
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd'T'HHmmss");
            String start = startTime.format(formatter);
            String end = endTime.format(formatter);
            
            // Construire l'URL Google Calendar
            String calendarUrl = String.format(
                "%s?action=TEMPLATE&text=%s&details=%s&location=%s&dates=%s/%s",
                GOOGLE_CALENDAR_URL,
                URLEncoder.encode(activityTitle, StandardCharsets.UTF_8),
                URLEncoder.encode(description, StandardCharsets.UTF_8),
                URLEncoder.encode(location, StandardCharsets.UTF_8),
                start, end
            );
            
            // Ouvrir le navigateur
            Desktop.getDesktop().browse(new URI(calendarUrl));
            
        } catch (Exception e) {
            System.err.println("Erreur lors de l'ouverture de Google Calendar: " + e.getMessage());
            // En cas d'erreur, ouvrir Google Calendar sans pré-remplir
            openGoogleCalendar();
        }
    }
    
    /**
     * Ajoute une activité rapidement avec des valeurs par défaut.
     */
    public void addActivityQuick(String activityTitle, LocalDateTime startTime) {
        LocalDateTime endTime = startTime.plusHours(1); // Durée par défaut: 1 heure
        String description = "Activité de bien-être MindConnect - Prenez un moment pour vous.";
        String location = "Chez vous / En ligne";
        
        addToGoogleCalendar(activityTitle, description, startTime, endTime, location);
    }
    
    /**
     * Ajoute un programme complet sur plusieurs jours.
     */
    public void addProgramToCalendar(String programName, String programDescription, 
                                    LocalDateTime startDate, int durationDays) {
        try {
            // Créer un seul événement récurrent pour tout le programme
            LocalDateTime endDate = startDate.plusDays(durationDays).plusHours(1);
            
            // Construire l'URL pour un événement récurrent
            String calendarUrl = String.format(
                "%s?action=TEMPLATE&text=%s&details=%s&location=%s&dates=%s/%s&recur=RRULE:FREQ=DAILY;COUNT=%d",
                GOOGLE_CALENDAR_URL,
                URLEncoder.encode(programName, StandardCharsets.UTF_8),
                URLEncoder.encode(programDescription + "\n\nProgramme de bien-être MindConnect - Suivez votre planning quotidien.", StandardCharsets.UTF_8),
                URLEncoder.encode("MindConnect Program", StandardCharsets.UTF_8),
                startDate.format(DateTimeFormatter.ofPattern("yyyyMMdd'T'HHmmss")),
                endDate.format(DateTimeFormatter.ofPattern("yyyyMMdd'T'HHmmss")),
                durationDays
            );
            
            // Ouvrir le navigateur (une seule fois)
            Desktop.getDesktop().browse(new URI(calendarUrl));
            
        } catch (Exception e) {
            System.err.println("Erreur lors de l'ajout du programme au calendrier: " + e.getMessage());
            // En cas d'erreur, ouvrir Google Calendar sans pré-remplir
            openGoogleCalendar();
        }
    }
    
    /**
     * Ouvre simplement Google Calendar.
     */
    public void openGoogleCalendar() {
        try {
            Desktop.getDesktop().browse(new URI(GOOGLE_CALENDAR_URL));
        } catch (Exception e) {
            System.err.println("Erreur lors de l'ouverture de Google Calendar: " + e.getMessage());
        }
    }
    
    /**
     * Crée un lien partageable pour l'événement.
     */
    public String createShareableLink(String activityTitle, String description, 
                                    LocalDateTime startTime, LocalDateTime endTime) {
        try {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd'T'HHmmss");
            String start = startTime.format(formatter);
            String end = endTime.format(formatter);
            
            return String.format(
                "%s?action=TEMPLATE&text=%s&details=%s&dates=%s/%s",
                GOOGLE_CALENDAR_URL,
                URLEncoder.encode(activityTitle, StandardCharsets.UTF_8),
                URLEncoder.encode(description, StandardCharsets.UTF_8),
                start, end
            );
        } catch (Exception e) {
            System.err.println("Erreur lors de la création du lien: " + e.getMessage());
            return GOOGLE_CALENDAR_URL;
        }
    }
}
