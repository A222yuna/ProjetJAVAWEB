package tn.psy.gestioncabinet.controller;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import tn.psy.gestioncabinet.model.Cabinet;
import tn.psy.gestioncabinet.service.CabinetService;
import tn.psy.gestioncabinet.util.CabinetEventBus;

public class ModifyCabinetController {

    @FXML private TextField tfAdresse;
    @FXML private TextField tfVille;
    @FXML private TextField tfHoraires;
    @FXML private TextArea taDescription;
    @FXML private Button btnValider;
    @FXML private Button btnAnnuler;
    @FXML private Label lblError;

    private final CabinetService cabinetService = new CabinetService();
    private Cabinet cabinet;

    @FXML
    public void initialize() {
        lblError.setVisible(false);

        btnValider.setOnAction(e -> valider());
        btnAnnuler.setOnAction(e -> fermer());
    }

    // 🔥 Réception du cabinet
    public void setCabinet(Cabinet cabinet) {
        this.cabinet = cabinet;

        if (cabinet != null) {
            tfAdresse.setText(cabinet.getAdresse());
            tfVille.setText(cabinet.getVille());
            tfHoraires.setText(cabinet.getHoraires() != null ? cabinet.getHoraires() : "");
            taDescription.setText(cabinet.getDescription() != null ? cabinet.getDescription() : "");
        }
    }

    private void valider() {

        if (cabinet == null) {
            showError("Erreur interne : aucun cabinet reçu.");
            return;
        }

        String adresse = tfAdresse.getText();
        String ville = tfVille.getText();
        String horaires = tfHoraires.getText();
        String description = taDescription.getText();

        if (adresse == null || adresse.isBlank()) {
            showError("L'adresse est obligatoire.");
            return;
        }

        if (ville == null || ville.isBlank()) {
            showError("La ville est obligatoire.");
            return;
        }

        // 🔥 Mise à jour de l'objet
        cabinet.setAdresse(adresse.trim());
        cabinet.setVille(ville.trim());
        cabinet.setHoraires(horaires != null ? horaires.trim() : "");
        cabinet.setDescription(description != null ? description.trim() : "");

        boolean success = cabinetService.modifierCabinet(cabinet);

        if (success) {
            new Alert(Alert.AlertType.INFORMATION,
                    "Cabinet modifié avec succès.",
                    ButtonType.OK).showAndWait();
            // Notifier tous les controllers du changement
            CabinetEventBus.getInstance().notifyCabinetChanged();
            fermer();
        } else {
            showError("Erreur lors de la modification en base.");
        }
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
