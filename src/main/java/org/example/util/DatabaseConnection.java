package org.example.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class DatabaseConnection {

    private static final String URL = "jdbc:mysql://localhost:3306/appointments_db";
    private static final String USER = "root";
    private static final String PASSWORD = "";

    private static Connection connection;

    public static Connection getConnection() throws SQLException {
        if (connection == null || connection.isClosed()) {
            try {
                Class.forName("com.mysql.cj.jdbc.Driver");
                connection = DriverManager.getConnection(URL, USER, PASSWORD);
            } catch (Exception e) {
                System.err.println("Failed to connect to MySQL, falling back to H2 memory DB: " + e.getMessage());
                // fallback to H2 in-memory instance
                connection = DriverManager.getConnection("jdbc:h2:mem:appointments_db;DB_CLOSE_DELAY=-1");
                initializeH2Schema(connection);
            }
        }
        return connection;
    }

    private static void initializeH2Schema(Connection conn) {
        try (Statement stmt = conn.createStatement()) {
            // create same schema as MySQL script
            stmt.executeUpdate("CREATE TABLE IF NOT EXISTS users ("
                    + "id INT AUTO_INCREMENT PRIMARY KEY, "
                    + "username VARCHAR(50) NOT NULL UNIQUE, "
                    + "password VARCHAR(100) NOT NULL, "
                    + "full_name VARCHAR(100) NOT NULL, "
                    + "email VARCHAR(100), "
                    + "phone VARCHAR(20), "
                    + "role VARCHAR(20) NOT NULL, "
                    + "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP"
                    + ");");
            stmt.executeUpdate("CREATE TABLE IF NOT EXISTS psychologue_plans ("
                    + "id INT AUTO_INCREMENT PRIMARY KEY, "
                    + "psychologue_id INT NOT NULL, "
                    + "day_of_week VARCHAR(20) NOT NULL, "
                    + "period VARCHAR(20) NOT NULL, "
                    + "max_appointments INT DEFAULT 5, "
                    + "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP, "
                    + "FOREIGN KEY (psychologue_id) REFERENCES users(id)"
                    + ");");
            stmt.executeUpdate("CREATE TABLE IF NOT EXISTS appointments ("
                    + "id INT AUTO_INCREMENT PRIMARY KEY, "
                    + "patient_id INT NOT NULL, "
                    + "plan_id INT NOT NULL, "
                    + "status VARCHAR(20) DEFAULT 'SCHEDULED', "
                    + "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP, "
                    + "FOREIGN KEY (patient_id) REFERENCES users(id), "
                    + "FOREIGN KEY (plan_id) REFERENCES psychologue_plans(id)"
                    + ");");
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }

    public static void closeConnection() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
