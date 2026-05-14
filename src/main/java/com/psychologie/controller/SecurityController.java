package com.psychologie.controller;

import com.psychologie.model.User;
import com.psychologie.util.DatabaseConnection;
import org.mindrot.jbcrypt.BCrypt;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;

public class SecurityController {

    // Login fields
    @FXML private TextField emailField;
    @FXML private PasswordField passwordField;

    // Register fields
    @FXML private TextField regPrenomField;
    @FXML private TextField regNomField;
    @FXML private TextField regEmailField;
    @FXML private TextField regTelField;
    @FXML private ComboBox<String> roleComboBox;
    @FXML private PasswordField regPasswordField;

    private static User currentUser;

    @FXML
    public void initialize() {
        if (roleComboBox != null) {
            roleComboBox.setItems(FXCollections.observableArrayList("Patient", "Psychologue"));
            roleComboBox.setValue("Patient");
        }
    }

    public static User getCurrentUser() {
        return currentUser;
    }

    @FXML
    public void handleLogin() {
        String email = emailField.getText();
        String password = passwordField.getText();

        if (email.isEmpty() || password.isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Veuillez remplir tous les champs.");
            return;
        }

        try (Connection conn = DatabaseConnection.getConnection()) {
            System.out.println("Attempting login for: " + email);
            
            // Debug: Check what's in DB for this email
            String debugQuery = "SELECT id_user, email, role, mot_de_passe FROM users WHERE email = ?";
            try (PreparedStatement debugPstmt = conn.prepareStatement(debugQuery)) {
                debugPstmt.setString(1, email);
                try (ResultSet rsDebug = debugPstmt.executeQuery()) {
                    if (rsDebug.next()) {
                        System.out.println("User found in DB!");
                        System.out.println("DB Role: [" + rsDebug.getString("role") + "]");
                        System.out.println("DB Password: [" + rsDebug.getString("mot_de_passe") + "]");
                    } else {
                        System.out.println("User NOT found in DB with email: " + email);
                    }
                }
            }

            String query = "SELECT * FROM users WHERE email = ?";
            try (PreparedStatement pstmt = conn.prepareStatement(query)) {
                pstmt.setString(1, email);
                
                try (ResultSet rs = pstmt.executeQuery()) {
                    if (rs.next()) {
                        String hashedDBPassword = rs.getString("mot_de_passe");
                        
                        // Symfony uses $2y$ prefix, Java jBCrypt expects $2a$
                        // They are functionally the same, so we replace it for compatibility
                        String normalizedHash = hashedDBPassword.replaceFirst("^\\$2y\\$", "\\$2a\\$");
                        
                        if (BCrypt.checkpw(password, normalizedHash)) {
                            boolean estActif = rs.getBoolean("est_actif");
                            if (!estActif) {
                                showAlert(Alert.AlertType.WARNING, "Compte inactif", "Votre compte n'est pas encore activé par l'administrateur.");
                                return;
                            }
                            
                            currentUser = new User();
                            currentUser.setId(rs.getInt("id_user"));
                            currentUser.setNom(rs.getString("nom"));
                            currentUser.setPrenom(rs.getString("prenom"));
                            currentUser.setEmail(rs.getString("email"));
                            currentUser.setRole(rs.getString("role"));
                            currentUser.setEstActif(estActif);
                            
                            System.out.println("Login successful! Role: " + currentUser.getRole());
                            switchToMain();
                        } else {
                            System.out.println("Login failed: Password mismatch.");
                            showAlert(Alert.AlertType.ERROR, "Erreur", "Email ou mot de passe incorrect.");
                        }
                    } else {
                        System.out.println("Login failed: User not found.");
                        showAlert(Alert.AlertType.ERROR, "Erreur", "Email ou mot de passe incorrect.");
                    }
                }
            }
        } catch (SQLException | IOException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Erreur", "Une erreur est survenue lors de la connexion.");
        }
    }

    @FXML
    public void handleRegister() {
        String prenom = regPrenomField.getText();
        String nom = regNomField.getText();
        String email = regEmailField.getText();
        String tel = regTelField.getText();
        String role = roleComboBox.getValue();
        String password = regPasswordField.getText();

        if (prenom.isEmpty() || nom.isEmpty() || email.isEmpty() || password.isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Veuillez remplir les champs obligatoires.");
            return;
        }

        try (Connection conn = DatabaseConnection.getConnection()) {
            String query = "INSERT INTO users (prenom, nom, email, telephone, role, mot_de_passe, date_inscription, est_actif, email_verifie, statut_validation) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
            try (PreparedStatement pstmt = conn.prepareStatement(query)) {
                pstmt.setString(1, prenom);
                pstmt.setString(2, nom);
                pstmt.setString(3, email);
                pstmt.setString(4, tel);
                pstmt.setString(5, role);
                pstmt.setString(6, BCrypt.hashpw(password, BCrypt.gensalt()));
                pstmt.setDate(7, java.sql.Date.valueOf(LocalDate.now()));
                
                // If it's a Psychologist, start as inactive (est_actif = 0)
                // Patients can be active by default, or you can make them inactive too
                boolean shouldBeActive = role.equalsIgnoreCase("Patient");
                pstmt.setBoolean(8, shouldBeActive);
                
                pstmt.setBoolean(9, false); // email_verifie
                pstmt.setString(10, role.equalsIgnoreCase("Psychologue") ? "en_attente" : "approuve");
                
                pstmt.executeUpdate();
                showAlert(Alert.AlertType.INFORMATION, "Succès", "Compte créé avec succès." + 
                    (role.equalsIgnoreCase("Psychologue") ? " Un administrateur doit valider votre compte avant la connexion." : ""));
                showLogin();
            }
        } catch (SQLException | IOException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Erreur", "Une erreur est survenue lors de l'inscription.");
        }
    }

    @FXML
    public void showRegister() throws IOException {
        loadView("/com/psychologie/view/RegisterView.fxml");
    }

    @FXML
    public void showLogin() throws IOException {
        loadView("/com/psychologie/view/LoginView.fxml");
    }

    private void loadView(String fxmlPath) throws IOException {
        Stage stage = (Stage) (emailField != null ? emailField.getScene().getWindow() : regPrenomField.getScene().getWindow());
        FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
        Parent root = loader.load();
        Scene scene = new Scene(root, 800, 600);
        stage.setScene(scene);
    }

    private void switchToMain() throws IOException {
        Stage stage = (Stage) emailField.getScene().getWindow();
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/psychologie/view/MainView.fxml"));
        Parent root = loader.load();
        Scene scene = new Scene(root, 1000, 700);
        stage.setScene(scene);
    }

    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}
