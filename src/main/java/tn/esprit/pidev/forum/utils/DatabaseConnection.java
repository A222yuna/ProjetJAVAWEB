package tn.esprit.pidev.forum.utils;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseConnection {
    private static final String URL = "jdbc:mysql://localhost:3306/forum_db";
    private static final String USER = "root";
    private static final String PASSWORD = ""; // Changez si vous avez un mot de passe

    private static Connection connection;

    /**
     * Obtenir la connexion à la base de données (Singleton pattern)
     */
    public static Connection getConnection() {
        try {
            if (connection == null || connection.isClosed()) {
                // Charger le driver MySQL
                Class.forName("com.mysql.cj.jdbc.Driver");

                // Établir la connexion
                connection = DriverManager.getConnection(URL, USER, PASSWORD);
                System.out.println("✅ Connexion à la base de données réussie!");
            }
        } catch (ClassNotFoundException e) {
            System.out.println("❌ Driver MySQL introuvable!");
            e.printStackTrace();
        } catch (SQLException e) {
            System.out.println("❌ Échec de la connexion à la base de données!");
            e.printStackTrace();
        }
        return connection;
    }

    /**
     * Fermer la connexion à la base de données
     */
    public static void closeConnection() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
                System.out.println("Connexion à la base de données fermée.");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}