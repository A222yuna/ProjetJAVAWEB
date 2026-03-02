package tn.psy.gestioncabinet.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Classe de gestion de la connexion à MySQL.
 * Pattern Singleton : une seule instance de connexion partagée.
 */
public class Connexion {

    // MODE PROJET : base unifiée "pschologie_app"
    private static final String URL = "jdbc:mysql://localhost:3306/pschologie_app?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true";
    private static final String USER = "root";
    private static final String PASSWORD = "";

    private static Connection connection = null;

    private Connexion() {
    }

    public static Connection getConnection() {
        try {
            if (connection == null || connection.isClosed()) {
                Class.forName("com.mysql.cj.jdbc.Driver");
                connection = DriverManager.getConnection(URL, USER, PASSWORD);
                System.out.println("Connexion MySQL réussie.");
            }
        } catch (ClassNotFoundException e) {
            System.err.println("Pilote MySQL non trouvé : " + e.getMessage());
        } catch (SQLException e) {
            System.err.println("Erreur connexion MySQL : " + e.getMessage());
        }
        return connection;
    }

    public static void fermerConnexion() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
                connection = null;
                System.out.println("Connexion fermée.");
            }
        } catch (SQLException e) {
            System.err.println("Erreur fermeture : " + e.getMessage());
        }
    }
}
