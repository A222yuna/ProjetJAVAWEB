package tn.psy.gestioncabinet.controller;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import tn.psy.gestioncabinet.service.AuthService;
import tn.psy.gestioncabinet.service.InscriptionService;
import tn.psy.gestioncabinet.util.SceneManager;

import java.io.IOException;

/**
 * Contrôleur du formulaire d'inscription
 */
public class InscriptionController {

    @FXML private TextField tfNomComplet;
    @FXML private TextField tfEmail;
    @FXML private PasswordField pfMotDePasse;
    @FXML private PasswordField pfConfirmerMotDePasse;
    @FXML private ComboBox<String> cbRole;
    @FXML private Button btnInscrire;
    @FXML private Button btnRetour;
    @FXML private Label lblError;

    private final InscriptionService inscriptionService = new InscriptionService();

    @FXML
    public void initialize() {
        cbRole.getItems().addAll("Patient", "Psychologue");
        cbRole.setValue("Patient");
        lblError.setVisible(false);

        btnInscrire.setOnAction(e -> handleInscription());
        btnRetour.setOnAction(e -> retourLogin());
    }

    private void handleInscription() {
        String nomComplet = tfNomComplet.getText();
        String email = tfEmail.getText();
        String motDePasse = pfMotDePasse.getText();
        String confirmerMotDePasse = pfConfirmerMotDePasse.getText();
        String roleStr = cbRole.getValue();

        AuthService.Role role = "Psychologue".equals(roleStr) ? AuthService.Role.PSYCHOLOGUE : AuthService.Role.PATIENT;

        InscriptionService.InscriptionResult result = inscriptionService.inscrire(
                nomComplet, email, motDePasse, confirmerMotDePasse, role);

        switch (result) {
            case SUCCESS:
                new Alert(Alert.AlertType.INFORMATION,
                        "Inscription réussie ! Vous pouvez maintenant vous connecter.",
                        ButtonType.OK).showAndWait();
                retourLogin();
                break;

            case EMAIL_EXISTS:
                showError("Cet email est déjà utilisé. Veuillez en choisir un autre.");
                break;

            case PASSWORDS_DO_NOT_MATCH:
                showError("Les mots de passe ne correspondent pas.");
                break;

            case INVALID_DATA:
                showError("Veuillez remplir tous les champs correctement (nom, email valide, mot de passe).");
                break;

            case ERROR:
            default:
                showError("Une erreur s'est produite lors de l'inscription. Veuillez réessayer.");
                break;
        }
    }

    private void retourLogin() {
        try {
            SceneManager.loadScene("/fxml/login.fxml", "Connexion - Gestion Cabinet");
        } catch (IOException ex) {
            showError("Impossible de retourner à l'écran de connexion.");
        }
    }

    private void showError(String message) {
        lblError.setText(message);
        lblError.setVisible(true);
    }
}
