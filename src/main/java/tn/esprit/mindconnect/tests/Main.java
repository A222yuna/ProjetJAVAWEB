package tn.esprit.mindconnect.tests;

import tn.esprit.mindconnect.entities.ActiviteProgramme;
import tn.esprit.mindconnect.entities.Avis;
import tn.esprit.mindconnect.entities.ProgrammeBienEtre;
import tn.esprit.mindconnect.services.ActiviteService;
import tn.esprit.mindconnect.services.AvisService;
import tn.esprit.mindconnect.services.ProgrammeService;

import java.sql.Date;
import java.sql.Time;
import java.time.LocalDate;
import java.time.LocalTime;

public class Main {
    public static void main(String[] args) {
        // --- Programme Service Test ---
        ProgrammeService ps = new ProgrammeService();
        ProgrammeBienEtre p1 = new ProgrammeBienEtre(1, "Programme Zen", "Relaxation totale", 30, "Actif", "image.png",
                "Facile");

        System.out.println("--- Ajout Programme ---");
        ps.add(p1);

        System.out.println("--- Affichage Programmes ---");
        System.out.println(ps.getAll());

        // Assuming ID 1 for update/delete as generated ID logic isn't fully implemented
        // in the test object (ID is usually auto-increment in DB)
        // For testing purposes we'll use a fetched object or assume an ID that exists
        // after add.
        // p1.setIdProgramme(1); // Manually setting ID for update test if needed
        // p1.setNom("Programme Zen Updated");
        // ps.update(p1);

        // --- Activite Service Test ---
        ActiviteService as = new ActiviteService();
        // Assuming Programme ID 1 exists
        ActiviteProgramme ap1 = new ActiviteProgramme(1, 1, Time.valueOf(LocalTime.of(10, 0)), "Yoga Matinal",
                "Session de yoga", 45, "Physique");

        System.out.println("--- Ajout Activité ---");
        as.add(ap1);

        System.out.println("--- Affichage Activités ---");
        System.out.println(as.getAll());

        // --- Avis Service Test ---
        AvisService avs = new AvisService();
        // Assuming Programme ID 1 and Psychologue ID 1 exist
        Avis avis1 = new Avis(1, 1, 5, "Excellent programme !", Date.valueOf(LocalDate.now()));

        System.out.println("--- Ajout Avis ---");
        avs.add(avis1);

        System.out.println("--- Affichage Avis ---");
        System.out.println(avs.getAll());
    }
}
