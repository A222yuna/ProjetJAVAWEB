package com.psychoapp.controller;

import com.psychoapp.model.Utilisateur;
import com.psychoapp.util.DatabaseConnection;
import com.psychoapp.util.Session;
import com.psychoapp.view.MainApp;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import org.mindrot.jbcrypt.BCrypt;

import java.sql.*;

public class LoginController {

    @FXML private TextField     fieldEmail;
    @FXML private PasswordField fieldPassword;
    @FXML private Label         errorLabel;

    @FXML
    private void handleLogin() {
        String email    = fieldEmail.getText().trim();
        String password = fieldPassword.getText();

        errorLabel.setText("");

        if (email.isBlank() || password.isBlank()) {
            errorLabel.setText("Veuillez remplir tous les champs.");
            return;
        }

        Utilisateur user = findUserByEmail(email);

        if (user == null) {
            errorLabel.setText("Email introuvable.");
            return;
        }

        // Support both BCrypt hashed passwords AND plain-text (for old test data)
        boolean passwordOk = checkPassword(password, user.getMotDePasse());
        if (!passwordOk) {
            errorLabel.setText("Mot de passe incorrect.");
            return;
        }

        if (!user.isEstActif()) {
            errorLabel.setText("Compte désactivé. Contactez l'administrateur.");
            return;
        }

        // Psychologue must be approved (only if column exists)
        if (user.getRole() == Utilisateur.Role.Psychologue
                && "en_attente".equals(user.getStatutValidation())) {
            errorLabel.setText("Votre compte est en attente de validation par l'administrateur.");
            return;
        }

        updateLastLogin(user.getIdUser());
        Session.setCurrentUser(user);

        try {
            MainApp.showDashboard();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /** Supports both BCrypt hashes and plain-text legacy passwords */
    private boolean checkPassword(String input, String stored) {
        if (stored == null) return false;
        // BCrypt hashes start with $2a$ or $2b$
        if (stored.startsWith("$2")) {
            try {
                return BCrypt.checkpw(input, stored);
            } catch (Exception e) {
                return false;
            }
        }
        // Plain-text fallback (old test data)
        return input.equals(stored);
    }

    @FXML
    private void handleGoRegister() {
        try { MainApp.showRegister(); } catch (Exception e) { e.printStackTrace(); }
    }

    // ── DB helpers ────────────────────────────────────────────────────────────

    private Utilisateur findUserByEmail(String email) {
        String sql = "SELECT * FROM users WHERE email = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, email);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return mapRow(rs);
        } catch (SQLException e) {
            errorLabel.setText("Erreur DB : " + e.getMessage());
        }
        return null;
    }

    private void updateLastLogin(int id) {
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(
                     "UPDATE users SET derniere_connexion = NOW() WHERE id_user = ?")) {
            ps.setInt(1, id);
            ps.executeUpdate();
        } catch (SQLException ignored) {}
    }

    private Utilisateur mapRow(ResultSet rs) throws SQLException {
        Utilisateur u = new Utilisateur();
        u.setIdUser(rs.getInt("id_user"));
        u.setNom(rs.getString("nom"));
        u.setPrenom(rs.getString("prenom"));
        u.setEmail(rs.getString("email"));
        u.setMotDePasse(rs.getString("mot_de_passe"));

        // Role might differ from enum - handle safely
        try {
            u.setRole(Utilisateur.Role.valueOf(rs.getString("role")));
        } catch (Exception e) {
            u.setRole(Utilisateur.Role.Patient);
        }

        u.setEstActif(rs.getBoolean("est_actif"));
        u.setEmailVerifie(rs.getBoolean("email_verifie"));

        // statut_validation - safe read (column may not exist yet)
        try {
            u.setStatutValidation(rs.getString("statut_validation"));
        } catch (SQLException e) {
            u.setStatutValidation("approuve"); // default if column missing
        }

        Date d = rs.getDate("date_inscription");
        if (d != null) u.setDateInscription(d.toLocalDate());

        Timestamp ts = rs.getTimestamp("derniere_connexion");
        if (ts != null) u.setDerniereConnexion(ts.toLocalDateTime());

        return u;
    }
}
