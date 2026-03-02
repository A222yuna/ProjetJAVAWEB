package tn.psy.gestioncabinet.util;

import tn.psy.gestioncabinet.dao.AdministrateurDAO;
import tn.psy.gestioncabinet.dao.CabinetDAO;
import tn.psy.gestioncabinet.dao.PatientDAO;
import tn.psy.gestioncabinet.dao.PsyCabinetDAO;
import tn.psy.gestioncabinet.dao.PsychologueDAO;
import tn.psy.gestioncabinet.model.Administrateur;
import tn.psy.gestioncabinet.model.Cabinet;
import tn.psy.gestioncabinet.model.Patient;
import tn.psy.gestioncabinet.model.PsyCabinet;
import tn.psy.gestioncabinet.model.Psychologue;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.Statement;
import java.time.LocalDate;

/**
 * Initialise la base de données avec des données de test si les tables sont vides.
 * Utilise BCrypt pour le hash des mots de passe.
 */
public class DbInitializer {

    private static boolean initialized = false;
    private static boolean connectionOk = false;

    /** Retourne true si la base de données est accessible */
    public static boolean isConnectionOk() {
        return connectionOk;
    }

    public static void initialize() {
        if (initialized) return;

        // Vérifier que la connexion fonctionne avant d'initialiser
        Connection conn = Connexion.getConnection();
        if (conn == null) {
            System.err.println("Impossible de se connecter à la base de données.");
            System.err.println(">>> Vérifiez l'existence de la base 'pschologie_app' et de son schéma dans votre SGBD <<<");
            connectionOk = false;
            return;
        }
        connectionOk = true;

        try {
            // Migration : s'assurer que la table cabinet possède les colonnes valide et archive
            ensureCabinetColumns(conn);
            // Migration : tables rating, disponibilite, creneau
            ensureRatingAndPlanningTables(conn);

        PsychologueDAO psychologueDAO = new PsychologueDAO();
        PatientDAO patientDAO = new PatientDAO();
        AdministrateurDAO administrateurDAO = new AdministrateurDAO();
        CabinetDAO cabinetDAO = new CabinetDAO();
        PsyCabinetDAO psyCabinetDAO = new PsyCabinetDAO();

        // Créer les utilisateurs de test uniquement si la table est vide
        if (psychologueDAO.findAll().isEmpty()) {
            Psychologue p1 = new Psychologue("Dr. Amira Ben Salem", "Psychologie clinique", "amira.bensalem@psy.tn", "71 123 456", PasswordUtil.hash("psy123"));
            Psychologue p2 = new Psychologue("Dr. Karim Mezghani", "Psychiatrie", "karim.mezghani@psy.tn", "73 456 789", PasswordUtil.hash("psy123"));
            Psychologue p3 = new Psychologue("Dr. Selma Trabelsi", "Thérapie cognitivo-comportementale", "selma.trabelsi@psy.tn", "72 789 012", PasswordUtil.hash("psy123"));
            psychologueDAO.ajouter(p1);
            psychologueDAO.ajouter(p2);
            psychologueDAO.ajouter(p3);
        }

        if (patientDAO.findAll().isEmpty()) {
            Patient pat1 = new Patient("Mohamed Ali", "mohamed.ali@email.com", PasswordUtil.hash("patient123"));
            Patient pat2 = new Patient("Fatma Chaabane", "fatma.chaabane@email.com", PasswordUtil.hash("patient123"));
            pat1.setTelephone("98 111 222");
            pat2.setTelephone("99 333 444");
            patientDAO.ajouter(pat1);
            patientDAO.ajouter(pat2);
        }

        if (administrateurDAO.findAll().isEmpty()) {
            Administrateur admin = new Administrateur("Admin Principal", "admin@gestioncabinet.tn", PasswordUtil.hash("admin123"));
            administrateurDAO.ajouter(admin);
        }

        if (cabinetDAO.findAll().isEmpty()) {
            Cabinet c1 = new Cabinet("15 Avenue Habib Bourguiba, Tunis", "Tunis", "Lun-Ven: 8h-18h", "Cabinet spécialisé en psychologie clinique et thérapie.");
            Cabinet c2 = new Cabinet("25 Rue de la République, Sousse", "Sousse", "Lun-Sam: 9h-17h", "Cabinet multidisciplinaire avec psychiatres et psychologues.");
            Cabinet c3 = new Cabinet("10 Rue Ibn Khaldoun, Sfax", "Sfax", "Lun-Ven: 10h-19h", "Consultations individuelles et familiales.");
            c1.setValide(true);
            c2.setValide(true);
            c3.setValide(false);
            cabinetDAO.ajouter(c1);
            cabinetDAO.ajouter(c2);
            cabinetDAO.ajouter(c3);
        }

        // Psy_Cabinet - créer les liaisons si vide
        if (psyCabinetDAO.findAll().isEmpty()) {
            psyCabinetDAO.ajouter(new PsyCabinet(1, 1, LocalDate.of(2023, 1, 1), null));
            psyCabinetDAO.ajouter(new PsyCabinet(2, 2, LocalDate.of(2023, 3, 15), null));
            psyCabinetDAO.ajouter(new PsyCabinet(3, 1, LocalDate.of(2022, 6, 1), null));
            psyCabinetDAO.ajouter(new PsyCabinet(3, 3, LocalDate.of(2024, 1, 1), null));
        }

        initialized = true;
        System.out.println("Base de données initialisée avec les données de test.");
        } catch (Exception e) {
            System.err.println("Erreur lors de l'initialisation de la base de données : " + e.getMessage());
            System.err.println(">>> Assurez-vous que la base 'pschologie_app' et les tables nécessaires existent déjà <<<");
        }
    }

    /**
     * Vérifie que la table cabinet contient les colonnes valide et archive.
     * Les ajoute si elles sont absentes (migration pour bases existantes).
     */
    private static void ensureCabinetColumns(Connection conn) {
        if (conn == null) return;
        try {
            DatabaseMetaData meta = conn.getMetaData();
            String catalog = conn.getCatalog();
            if (catalog == null) return;
            try (ResultSet rs = meta.getColumns(catalog, null, "cabinet", null)) {
                boolean hasValide = false;
                boolean hasArchive = false;
                while (rs.next()) {
                    String col = rs.getString("COLUMN_NAME");
                    if ("valide".equalsIgnoreCase(col)) hasValide = true;
                    if ("archive".equalsIgnoreCase(col)) hasArchive = true;
                }
                try (Statement st = conn.createStatement()) {
                    if (!hasValide) {
                        st.executeUpdate("ALTER TABLE cabinet ADD COLUMN valide BOOLEAN DEFAULT FALSE");
                        System.out.println("Migration : colonne 'valide' ajoutée à la table cabinet.");
                    }
                    if (!hasArchive) {
                        st.executeUpdate("ALTER TABLE cabinet ADD COLUMN archive BOOLEAN DEFAULT FALSE");
                        System.out.println("Migration : colonne 'archive' ajoutée à la table cabinet.");
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("Migration table cabinet : " + e.getMessage());
        }
    }

    /**
     * Vérifie la présence logique des tables rating, disponibilite, creneau.
     * IMPORTANT : la base 'pschologie_app' et ces tables doivent être créées
     * manuellement (ou via un script SQL dédié). Aucune création/modification
     * de structure n'est effectuée ici pour respecter le schéma unifié.
     */
    private static void ensureRatingAndPlanningTables(Connection conn) {
        if (conn == null) return;
        try {
            DatabaseMetaData meta = conn.getMetaData();
            String catalog = conn.getCatalog();
            if (catalog == null) return;
            String[] tables = {"rating", "disponibilite", "creneau"};
            for (String table : tables) {
                try (ResultSet rs = meta.getTables(catalog, null, table, null)) {
                    if (!rs.next()) {
                        System.err.println("ATTENTION: la table '" + table + "' est absente de la base '"
                                + catalog + "'. Créez-la via votre script SQL (schéma pschologie_app).");
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("Vérification rating/planning : " + e.getMessage());
        }
    }
}
