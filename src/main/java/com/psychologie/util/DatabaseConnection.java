package com.psychologie.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class DatabaseConnection {

    private static final String URL      = "jdbc:mysql://127.0.0.1:3306/psychologie_app?serverTimezone=UTC";
    private static final String USER     = "root";
    private static final String PASSWORD = "";

    private static boolean migrationDone = false;

    public static Connection getConnection() throws SQLException {
        Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
        if (!migrationDone) {
            runMigrations(conn);
            migrationDone = true;
        }
        return conn;
    }

    private static void runMigrations(Connection conn) {
        try (Statement st = conn.createStatement()) {

            // 1. Fix appointments.status enum
            String checkSql =
                "SELECT COLUMN_TYPE FROM information_schema.COLUMNS " +
                "WHERE TABLE_SCHEMA = DATABASE() " +
                "  AND TABLE_NAME   = 'appointments' " +
                "  AND COLUMN_NAME  = 'status'";
            try (ResultSet rs = st.executeQuery(checkSql)) {
                if (rs.next()) {
                    String colType = rs.getString("COLUMN_TYPE");
                    if (!colType.contains("CONFIRMED") || !colType.contains("PAID")) {
                        System.out.println("[Migration] Updating appointments.status enum...");
                        st.executeUpdate(
                            "ALTER TABLE `appointments` " +
                            "MODIFY COLUMN `status` " +
                            "ENUM('SCHEDULED','CONFIRMED','PAID','CANCELLED','COMPLETED') " +
                            "NOT NULL DEFAULT 'SCHEDULED'"
                        );
                    }
                }
            }

            // 2. Create saved_posts table if missing
            st.executeUpdate(
                "CREATE TABLE IF NOT EXISTS saved_posts (" +
                "  id_user INT NOT NULL, " +
                "  id_post INT NOT NULL, " +
                "  saved_at DATETIME DEFAULT CURRENT_TIMESTAMP, " +
                "  PRIMARY KEY (id_user, id_post)" +
                ")"
            );

            // 3. Create post_likes table if missing
            st.executeUpdate(
                "CREATE TABLE IF NOT EXISTS post_likes (" +
                "  id_post INT NOT NULL, " +
                "  id_user INT NOT NULL, " +
                "  PRIMARY KEY (id_post, id_user)" +
                ")"
            );

            // 4. Create comment_likes table if missing
            st.executeUpdate(
                "CREATE TABLE IF NOT EXISTS comment_likes (" +
                "  id_comment INT NOT NULL, " +
                "  id_user    INT NOT NULL, " +
                "  PRIMARY KEY (id_comment, id_user)" +
                ")"
            );

            // 5. Create forum_reports table if missing
            st.executeUpdate(
                "CREATE TABLE IF NOT EXISTS forum_reports (" +
                "  id_report        INT AUTO_INCREMENT PRIMARY KEY, " +
                "  id_post          INT, " +
                "  id_comment       INT, " +
                "  id_user_reporter INT, " +
                "  reason           VARCHAR(255), " +
                "  status           VARCHAR(50) DEFAULT 'OPEN', " +
                "  date             DATETIME DEFAULT CURRENT_TIMESTAMP" +
                ")"
            );

            // 6. Add is_hidden column to post if missing
            try {
                st.executeUpdate("ALTER TABLE post ADD COLUMN is_hidden TINYINT(1) DEFAULT 0");
                System.out.println("[Migration] Added is_hidden to post.");
            } catch (SQLException ignored) { /* already exists */ }

        } catch (SQLException e) {
            System.err.println("[Migration] Warning: " + e.getMessage());
        }
    }
}
