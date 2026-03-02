package org.example.util;

/**
 * Application-wide constants for the Psychologist Appointment System
 */
public class Constants {

    // User Roles
    public static final String ROLE_USER = "USER";
    public static final String ROLE_PSYCHOLOGIST = "PSY";

    // Window Dimensions
    public static final int WINDOW_WIDTH = 1200;
    public static final int WINDOW_HEIGHT = 800;
    public static final int WINDOW_MIN_WIDTH = 1200;
    public static final int WINDOW_MIN_HEIGHT = 800;

    // FXML File Paths
    public static final String FXML_LOGIN = "login.fxml";
    public static final String FXML_USER_DASHBOARD = "userDashboard.fxml";
    public static final String FXML_PSY_DASHBOARD = "psyDashboard.fxml";
    public static final String FXML_BOOK_APPOINTMENT = "bookAppointment.fxml";
    public static final String FXML_VIEW_SESSIONS = "viewSessions.fxml";
    public static final String FXML_SETTINGS = "settings.fxml";
    public static final String FXML_PSY_PROFILE = "psychologistProfile.fxml";

    // Session Configuration
    public static final long SESSION_TIMEOUT_DURATION = 30 * 60 * 1000; // 30 minutes

    // Login Security
    public static final int MAX_LOGIN_ATTEMPTS = 5;
    public static final long LOGIN_LOCKOUT_DURATION = 15 * 60 * 1000; // 15 minutes
    public static final int MAX_USERNAME_LENGTH = 50;

    // Database Configuration
    public static final String DB_DRIVER = "com.mysql.cj.jdbc.Driver";
    public static final String DB_URL = "jdbc:mysql://localhost:3306/psy_appointment";
    public static final String DB_USER = "root";
    public static final String DB_PASSWORD = "";

    // Error Messages
    public static final String ERROR_INVALID_CREDENTIALS = "Invalid username or password";
    public static final String ERROR_DATABASE = "Database error. Please try again later.";
    public static final String ERROR_FXML_NOT_FOUND = "Cannot find FXML file: ";
    public static final String ERROR_EMPTY_INPUT = "Username and password are required";
    public static final String ERROR_ACCOUNT_LOCKED = "Too many failed attempts. Try again later.";

    // Success Messages
    public static final String SUCCESS_LOGIN = "Login successful";
    public static final String SUCCESS_LOGOUT = "Logout successful";

    // Stage Titles
    public static final String TITLE_LOGIN = "Login";
    public static final String TITLE_USER_DASHBOARD = "User Dashboard";
    public static final String TITLE_PSY_DASHBOARD = "Psychologist Dashboard";

    private Constants() {
        // Private constructor to prevent instantiation
    }
}