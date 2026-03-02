package com.psychoapp.controller;

import com.psychoapp.model.Utilisateur;
import com.psychoapp.util.DatabaseConnection;
import com.psychoapp.view.MainApp;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import org.mindrot.jbcrypt.BCrypt;

import java.net.URL;
import java.sql.*;
import java.util.ResourceBundle;

public class RegisterController implements Initializable {

    @FXML private TextField     fieldNom;
    @FXML private TextField     fieldPrenom;
    @FXML private TextField     fieldEmail;
    @FXML private PasswordField fieldPassword;
    @FXML private PasswordField fieldConfirm;
    @FXML private ComboBox<Utilisateur.Role> comboRole;
    @FXML private Label         errorLabel;
    @FXML private Label         infoLabel;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        comboRole.setItems(FXCollections.observableArrayList(
                Utilisateur.Role.Patient, Utilisateur.Role.Psychologue));
        comboRole.setValue(Utilisateur.Role.Patient);
        comboRole.valueProperty().addListener((obs, old, newVal) -> {
            if (newVal == Utilisateur.Role.Psychologue) {
                infoLabel.setText("Info : votre compte sera soumis à validation par l'administrateur.");
            } else {
                infoLabel.setText("");
            }
        });
    }

    @FXML
    private void handleRegister() {
        errorLabel.setText("");
        if (fieldNom.getText().isBlank() || fieldPrenom.getText().isBlank()
                || fieldEmail.getText().isBlank() || fieldPassword.getText().isBlank()) {
            errorLabel.setText("Tous les champs sont obligatoires."); return;
        }
        if (!fieldEmail.getText().matches("^[\\w.+-]+@[\\w-]+\\.[a-zA-Z]{2,}$")) {
            errorLabel.setText("Format d'email invalide."); return;
        }
        if (fieldPassword.getText().length() < 6) {
            errorLabel.setText("Mot de passe : minimum 6 caractères."); return;
        }
        if (!fieldPassword.getText().equals(fieldConfirm.getText())) {
            errorLabel.setText("Les mots de passe ne correspondent pas."); return;
        }
        if (emailExists(fieldEmail.getText().trim())) {
            errorLabel.setText("Cet email est déjà utilisé."); return;
        }
        insertUser();
    }

    private void insertUser() {
        Utilisateur.Role role = comboRole.getValue();
        String statut = (role == Utilisateur.Role.Psychologue) ? "en_attente" : "approuve";

        // Try with statut_validation column first, fallback without it
        String sql = "INSERT INTO users (nom,prenom,email,mot_de_passe,role,date_inscription,est_actif,email_verifie) VALUES (?,?,?,?,?,CURDATE(),1,0)";
        try (Connection conn = DatabaseConnection.getConnection()) {
            // Check if statut_validation column exists
            boolean hasStatut = columnExists(conn, "statut_validation");
            if (hasStatut) {
                sql = "INSERT INTO users (nom,prenom,email,mot_de_passe,role,date_inscription,est_actif,email_verifie,statut_validation) VALUES (?,?,?,?,?,CURDATE(),1,0,?)";
            }
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setString(1, fieldNom.getText().trim());
            ps.setString(2, fieldPrenom.getText().trim());
            ps.setString(3, fieldEmail.getText().trim());
            ps.setString(4, BCrypt.hashpw(fieldPassword.getText(), BCrypt.gensalt()));
            ps.setString(5, role.name());
            if (hasStatut) ps.setString(6, statut);
            ps.executeUpdate();

            String msg = (role == Utilisateur.Role.Psychologue)
                    ? "Inscription envoyée !\nEn attente de validation par l'administrateur."
                    : "Compte créé ! Vous pouvez maintenant vous connecter.";
            Alert a = new Alert(Alert.AlertType.INFORMATION);
            a.setHeaderText(null); a.setContentText(msg); a.showAndWait();
            MainApp.showLogin();
        } catch (Exception e) {
            errorLabel.setText("Erreur : " + e.getMessage());
        }
    }

    @FXML private void handleGoLogin() {
        try { MainApp.showLogin(); } catch (Exception e) { e.printStackTrace(); }
    }

    private boolean emailExists(String email) {
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement("SELECT id_user FROM users WHERE email=?")) {
            ps.setString(1, email);
            return ps.executeQuery().next();
        } catch (SQLException e) { return false; }
    }

    private boolean columnExists(Connection conn, String columnName) {
        try {
            ResultSet rs = conn.getMetaData().getColumns(null, null, "users", columnName);
            return rs.next();
        } catch (SQLException e) { return false; }
    }
}
