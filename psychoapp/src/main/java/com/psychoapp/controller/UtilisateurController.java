package com.psychoapp.controller;

import com.psychoapp.model.Utilisateur;
import com.psychoapp.model.Utilisateur.Role;
import com.psychoapp.util.DatabaseConnection;
import com.psychoapp.util.Session;
import com.psychoapp.view.MainApp;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.chart.PieChart;
import javafx.scene.layout.BorderPane;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.mindrot.jbcrypt.BCrypt;

import java.io.IOException;
import java.net.URL;
import java.sql.*;
import java.time.LocalDate;
import java.util.Optional;
import java.util.ResourceBundle;

public class UtilisateurController implements Initializable {

    @FXML private TableView<Utilisateur>              tableView;
    @FXML private TableColumn<Utilisateur, Integer>   colId;
    @FXML private TableColumn<Utilisateur, String>    colNom;
    @FXML private TableColumn<Utilisateur, String>    colPrenom;
    @FXML private TableColumn<Utilisateur, String>    colEmail;
    @FXML private TableColumn<Utilisateur, String>    colRole;
    @FXML private TableColumn<Utilisateur, Boolean>   colActif;
    @FXML private TableColumn<Utilisateur, LocalDate> colDate;
    @FXML private TextField                           searchField;
    @FXML private Label                               statusLabel;
    @FXML private Label                               labelCurrentUser;
    @FXML private Tab                                 tabValidation;
    @FXML private Tab                                 tabLoginHistory;

    // Form fields
    @FXML private TextField      fieldNom;
    @FXML private TextField      fieldPrenom;
    @FXML private TextField      fieldEmail;
    @FXML private PasswordField  fieldPassword;
    @FXML private ComboBox<Role> comboRole;
    @FXML private CheckBox       checkActif;
    @FXML private CheckBox       checkEmailVerifie;
    @FXML private Label          formTitle;
    @FXML private Label          errorLabel;

    private final ObservableList<Utilisateur> utilisateurs = FXCollections.observableArrayList();
    private Utilisateur selectedUser = null;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        if (tableView != null) {
            setupTable();
            loadAllUsers();
            Utilisateur current = Session.getCurrentUser();
            if (labelCurrentUser != null && current != null) {
                labelCurrentUser.setText(
                        "Utilisateur : " + current.getPrenom() + " " + current.getNom()
                                + "  |  " + current.getRole()
                );
            }
            if (!Session.isAdmin()) {
                if (tabValidation != null) {
                    tabValidation.getTabPane().getTabs().remove(tabValidation);
                }
                if (tabLoginHistory != null) {
                    tabLoginHistory.getTabPane().getTabs().remove(tabLoginHistory);
                }
            }
        }
        if (comboRole != null) {
            comboRole.setItems(FXCollections.observableArrayList(Role.values()));
            comboRole.setValue(Role.Patient);
        }
    }

    private void setupTable() {
        colId.setCellValueFactory(new PropertyValueFactory<>("idUser"));
        colNom.setCellValueFactory(new PropertyValueFactory<>("nom"));
        colPrenom.setCellValueFactory(new PropertyValueFactory<>("prenom"));
        colEmail.setCellValueFactory(new PropertyValueFactory<>("email"));
        colRole.setCellValueFactory(new PropertyValueFactory<>("role"));
        colActif.setCellValueFactory(new PropertyValueFactory<>("estActif"));
        colDate.setCellValueFactory(new PropertyValueFactory<>("dateInscription"));

        colActif.setCellFactory(col -> new TableCell<>() {
            @Override protected void updateItem(Boolean item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : (item ? "✔" : "✘"));
                setStyle(empty || item == null ? "" :
                        (item ? "-fx-text-fill:#38a169;" : "-fx-text-fill:#e53e3e;"));
            }
        });
        tableView.setItems(utilisateurs);
        if (searchField != null)
            searchField.textProperty().addListener((obs, o, n) -> filterTable(n));
    }

    public void loadAllUsers() {
        utilisateurs.clear();
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs   = stmt.executeQuery("SELECT * FROM users ORDER BY id_user")) {
            while (rs.next()) utilisateurs.add(mapRow(rs));
            setStatus("✔  " + utilisateurs.size() + " utilisateurs chargés.");
        } catch (SQLException e) {
            showError("Erreur chargement : " + e.getMessage());
        }
    }

    @FXML private void handleRefresh() { loadAllUsers(); }

    @FXML private void handleShowStats() {
        Stage stage = new Stage();
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.setTitle("Statistiques des utilisateurs par role");

        PieChart chart = new PieChart();
        chart.setTitle("Utilisateurs par role");

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(
                     "SELECT role, COUNT(*) AS total FROM users GROUP BY role");
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                String role = rs.getString("role");
                int count   = rs.getInt("total");
                String label = role + " (" + count + ")";
                chart.getData().add(new PieChart.Data(label, count));
            }
        } catch (SQLException e) {
            showError("Erreur statistiques : " + e.getMessage());
        }

        BorderPane root = new BorderPane(chart);
        root.setPrefSize(500, 400);

        stage.setScene(new Scene(root, 500, 400));
        stage.showAndWait();
    }

    private void filterTable(String kw) {
        if (kw == null || kw.isBlank()) { tableView.setItems(utilisateurs); return; }
        String k = kw.toLowerCase();
        tableView.setItems(utilisateurs.filtered(u ->
                u.getNom().toLowerCase().contains(k)
                || u.getPrenom().toLowerCase().contains(k)
                || u.getEmail().toLowerCase().contains(k)
                || u.getRole().name().toLowerCase().contains(k)));
    }

    @FXML private void handleAdd()  { openForm(null); }

    @FXML private void handleEdit() {
        Utilisateur u = tableView.getSelectionModel().getSelectedItem();
        if (u == null) { showAlert("Sélectionnez un utilisateur à modifier."); return; }
        openForm(u);
    }

    private void openForm(Utilisateur userToEdit) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/UtilisateurForm.fxml"));
            Parent root = loader.load();
            UtilisateurController fc = loader.getController();
            fc.prepareForm(userToEdit);
            Stage stage = new Stage();
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setTitle(userToEdit == null ? "Ajouter" : "Modifier");
            stage.setScene(new Scene(root));
            stage.showAndWait();
            loadAllUsers();
        } catch (IOException e) { e.printStackTrace(); }
    }

    public void prepareForm(Utilisateur u) {
        if (u == null) {
            selectedUser = null;
            formTitle.setText("Nouvel utilisateur");
            checkActif.setSelected(true);
        } else {
            selectedUser = u;
            formTitle.setText("Modifier l'utilisateur");
            fieldNom.setText(u.getNom());
            fieldPrenom.setText(u.getPrenom());
            fieldEmail.setText(u.getEmail());
            comboRole.setValue(u.getRole());
            checkActif.setSelected(u.isEstActif());
            checkEmailVerifie.setSelected(u.isEmailVerifie());
        }
    }

    @FXML private void handleSave() {
        if (!validateForm()) return;
        if (selectedUser == null) insertUser(); else updateUser();
    }

    private void insertUser() {
        Role role = comboRole.getValue();
        String statut = (role == Role.Psychologue) ? "en_attente" : "approuve";

        String sql = "INSERT INTO users (nom,prenom,email,mot_de_passe,role,date_inscription,est_actif,email_verifie) " +
                     "VALUES (?,?,?,?,?,CURDATE(),?,?)";
        try (Connection conn = DatabaseConnection.getConnection()) {
            boolean hasStatut = columnExists(conn, "statut_validation");
            if (hasStatut) {
                sql = "INSERT INTO users (nom,prenom,email,mot_de_passe,role,date_inscription,est_actif,email_verifie,statut_validation) " +
                      "VALUES (?,?,?,?,?,CURDATE(),?,?,?)";
            }
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setString(1, fieldNom.getText().trim());
            ps.setString(2, fieldPrenom.getText().trim());
            ps.setString(3, fieldEmail.getText().trim());
            ps.setString(4, BCrypt.hashpw(fieldPassword.getText(), BCrypt.gensalt()));
            ps.setString(5, role.name());
            ps.setBoolean(6, checkActif.isSelected());
            ps.setBoolean(7, checkEmailVerifie.isSelected());
            if (hasStatut) {
                ps.setString(8, statut);
            }
            ps.executeUpdate();
            closeForm();
        } catch (SQLException e) { errorLabel.setText("Erreur : " + e.getMessage()); }
    }

    private void updateUser() {
        boolean changePw = !fieldPassword.getText().isBlank();
        String sql = changePw
                ? "UPDATE users SET nom=?,prenom=?,email=?,mot_de_passe=?,role=?,est_actif=?,email_verifie=? WHERE id_user=?"
                : "UPDATE users SET nom=?,prenom=?,email=?,role=?,est_actif=?,email_verifie=? WHERE id_user=?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, fieldNom.getText().trim());
            ps.setString(2, fieldPrenom.getText().trim());
            ps.setString(3, fieldEmail.getText().trim());
            if (changePw) {
                ps.setString(4, BCrypt.hashpw(fieldPassword.getText(), BCrypt.gensalt()));
                ps.setString(5, comboRole.getValue().name());
                ps.setBoolean(6, checkActif.isSelected());
                ps.setBoolean(7, checkEmailVerifie.isSelected());
                ps.setInt(8, selectedUser.getIdUser());
            } else {
                ps.setString(4, comboRole.getValue().name());
                ps.setBoolean(5, checkActif.isSelected());
                ps.setBoolean(6, checkEmailVerifie.isSelected());
                ps.setInt(7, selectedUser.getIdUser());
            }
            ps.executeUpdate();
            closeForm();
        } catch (SQLException e) { errorLabel.setText("Erreur : " + e.getMessage()); }
    }

    @FXML private void handleDelete() {
        Utilisateur u = tableView.getSelectionModel().getSelectedItem();
        if (u == null) { showAlert("Sélectionnez un utilisateur à supprimer."); return; }
        Alert c = new Alert(Alert.AlertType.CONFIRMATION);
        c.setHeaderText("Supprimer " + u.getPrenom() + " " + u.getNom() + " ?");
        c.setContentText("Cette action est irréversible.");
        c.showAndWait().filter(r -> r == ButtonType.OK).ifPresent(r -> {
            try (Connection conn = DatabaseConnection.getConnection();
                 PreparedStatement ps = conn.prepareStatement("DELETE FROM users WHERE id_user=?")) {
                ps.setInt(1, u.getIdUser());
                ps.executeUpdate();
                loadAllUsers();
            } catch (SQLException e) { showError(e.getMessage()); }
        });
    }

    @FXML private void handleToggleActif() {
        Utilisateur u = tableView.getSelectionModel().getSelectedItem();
        if (u == null) { showAlert("Sélectionnez un utilisateur."); return; }
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement("UPDATE users SET est_actif=? WHERE id_user=?")) {
            ps.setBoolean(1, !u.isEstActif());
            ps.setInt(2, u.getIdUser());
            ps.executeUpdate();
            loadAllUsers();
        } catch (SQLException e) { showError(e.getMessage()); }
    }

    @FXML private void handleLogout() {
        Session.clear();
        try { MainApp.showLogin(); } catch (Exception e) { e.printStackTrace(); }
    }

    private Utilisateur mapRow(ResultSet rs) throws SQLException {
        Utilisateur u = new Utilisateur();
        u.setIdUser(rs.getInt("id_user"));
        u.setNom(rs.getString("nom"));
        u.setPrenom(rs.getString("prenom"));
        u.setEmail(rs.getString("email"));
        u.setMotDePasse(rs.getString("mot_de_passe"));
        try { u.setRole(Role.valueOf(rs.getString("role"))); }
        catch (Exception e) { u.setRole(Role.Patient); }
        try { u.setStatutValidation(rs.getString("statut_validation")); }
        catch (SQLException e) { u.setStatutValidation("approuve"); }
        Date d = rs.getDate("date_inscription");
        if (d != null) u.setDateInscription(d.toLocalDate());
        u.setEstActif(rs.getBoolean("est_actif"));
        u.setEmailVerifie(rs.getBoolean("email_verifie"));
        Timestamp ts = rs.getTimestamp("derniere_connexion");
        if (ts != null) u.setDerniereConnexion(ts.toLocalDateTime());
        return u;
    }

    private boolean validateForm() {
        if (fieldNom.getText().isBlank() || fieldPrenom.getText().isBlank() || fieldEmail.getText().isBlank()) {
            errorLabel.setText("Nom, prénom et email sont obligatoires."); return false;
        }
        // Création : mot de passe obligatoire
        if (selectedUser == null && fieldPassword.getText().isBlank()) {
            errorLabel.setText("Mot de passe obligatoire."); return false;
        }
        // Création ou modification : si un mot de passe est saisi, il doit avoir au moins 6 caractères
        if (!fieldPassword.getText().isBlank() && fieldPassword.getText().length() < 6) {
            errorLabel.setText("Mot de passe : minimum 6 caractères."); return false;
        }
        if (!fieldEmail.getText().matches("^[\\w.+-]+@[\\w-]+\\.[a-zA-Z]{2,}$")) {
            errorLabel.setText("Format email invalide."); return false;
        }
        errorLabel.setText(""); return true;
    }

    @FXML private void handleCancel() { closeForm(); }
    private void closeForm() { ((Stage) fieldNom.getScene().getWindow()).close(); }
    private void setStatus(String m) { if (statusLabel != null) statusLabel.setText(m); }
    private void showError(String m) { if (statusLabel != null) statusLabel.setText(" " + m); }
    private void showAlert(String m) {
        Alert a = new Alert(Alert.AlertType.WARNING); a.setHeaderText(null); a.setContentText(m); a.showAndWait();
    }

    private boolean columnExists(Connection conn, String columnName) {
        try {
            ResultSet rs = conn.getMetaData().getColumns(null, null, "users", columnName);
            return rs.next();
        } catch (SQLException e) {
            return false;
        }
    }
}
