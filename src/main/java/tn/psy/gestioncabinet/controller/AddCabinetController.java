package tn.psy.gestioncabinet.controller;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import tn.psy.gestioncabinet.model.Cabinet;
import tn.psy.gestioncabinet.service.CabinetService;
import tn.psy.gestioncabinet.service.MapService;
import tn.psy.gestioncabinet.util.CabinetEventBus;

/**
 * Contrôleur du formulaire d'ajout de cabinet
 */
public class AddCabinetController {

    @FXML private TextField tfAdresse;
    @FXML private TextField tfVille;
    @FXML private TextField tfHoraires;
    @FXML private TextArea taDescription;
    @FXML private Button btnValider;
    @FXML private Button btnAnnuler;
    @FXML private Label lblError;

    private final CabinetService cabinetService = new CabinetService();
    private final MapService mapService = new MapService();
    private int idPsychologue;

    public void setIdPsychologue(int idPsychologue) {
        this.idPsychologue = idPsychologue;
    }

    @FXML
    public void initialize() {
        lblError.setVisible(false);
        btnValider.setOnAction(e -> valider());
        btnAnnuler.setOnAction(e -> annuler());
    }

    /**
     * Valide le champ Adresse : format "Pays, Ville CodePostal".
     * Règles : virgule, au moins 4 chiffres, des lettres, longueur min 10.
     */
    private boolean validateAdresse(String adresse) {
        if (adresse == null || adresse.isBlank()) {
            return false;
        }
        String s = adresse.trim();
        if (s.length() < 10) {
            return false;
        }
        if (!s.contains(",")) {
            return false;
        }
        long digitCount = s.chars().filter(Character::isDigit).count();
        if (digitCount < 4) {
            return false;
        }
        boolean hasLetter = s.chars().anyMatch(Character::isLetter);
        return hasLetter;
    }

    /**
     * Valide le champ Horaire : au moins un chiffre, au moins une lettre ou espace, min 4 caractères,
     * pas uniquement lettres ni uniquement chiffres.
     */
    private boolean validateHoraire(String horaire) {
        if (horaire == null || horaire.isBlank()) {
            return true; // champ optionnel
        }
        String s = horaire.trim();
        if (s.length() < 4) {
            return false;
        }
        boolean hasDigit = s.chars().anyMatch(Character::isDigit);
        boolean hasLetterOrSpace = s.chars().anyMatch(c -> Character.isLetter(c) || Character.isSpaceChar(c));
        if (!hasDigit || !hasLetterOrSpace) {
            return false;
        }
        boolean onlyLetters = s.chars().allMatch(c -> Character.isLetter(c) || Character.isSpaceChar(c));
        boolean onlyDigits = s.chars().allMatch(Character::isDigit);
        return !onlyLetters && !onlyDigits;
    }

    private void valider() {
        lblError.setVisible(false);
        String adresse = tfAdresse.getText();
        String ville = tfVille.getText();
        String horaires = tfHoraires.getText();
        String description = taDescription.getText();

        if (adresse == null || adresse.isBlank()) {
            showError("L'adresse est obligatoire.");
            return;
        }
        if (!validateAdresse(adresse)) {
            new Alert(Alert.AlertType.WARNING, "Veuillez entrer une adresse complète (Pays, Ville CodePostal)", ButtonType.OK).showAndWait();
            lblError.setText("Adresse invalide : format Pays, Ville CodePostal (ex: Tunisia, Mednine 4100)");
            lblError.setVisible(true);
            return;
        }
        if (!mapService.isAdresseValide(adresse.trim())) {
            new Alert(Alert.AlertType.WARNING, "Cette adresse n'a pas été reconnue. Vérifiez qu'elle existe (géolocalisation).", ButtonType.OK).showAndWait();
            lblError.setText("Adresse non trouvée sur la carte.");
            lblError.setVisible(true);
            return;
        }
        if (ville == null || ville.isBlank()) {
            showError("La ville est obligatoire.");
            return;
        }
        if (!validateHoraire(horaires != null ? horaires : "")) {
            new Alert(Alert.AlertType.WARNING, "Horaire invalide (ex: 10 17 ou LUN Ven 10 17)", ButtonType.OK).showAndWait();
            lblError.setText("Horaire invalide : au moins un chiffre et des lettres/espaces, min 4 caractères.");
            lblError.setVisible(true);
            return;
        }

        Cabinet cabinet = new Cabinet(adresse.trim(), ville.trim(),
                horaires != null ? horaires.trim() : "",
                description != null ? description.trim() : "");

        int id = cabinetService.ajouterCabinet(cabinet, idPsychologue);
        if (id > 0) {
            new Alert(Alert.AlertType.INFORMATION, "Cabinet ajouté avec succès. Il sera visible après validation par l'administrateur.", ButtonType.OK).showAndWait();
            CabinetEventBus.getInstance().notifyCabinetChanged();
            fermer();
        } else {
            showError("Erreur lors de l'ajout du cabinet.");
        }
    }

    private void annuler() {
        fermer();
    }

    private void fermer() {
        Stage stage = (Stage) btnAnnuler.getScene().getWindow();
        stage.close();
    }

    private void showError(String message) {
        lblError.setText(message);
        lblError.setVisible(true);
    }
}
