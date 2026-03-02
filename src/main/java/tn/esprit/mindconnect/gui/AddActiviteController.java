package tn.esprit.mindconnect.gui;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.stage.Stage;
import tn.esprit.mindconnect.entities.ActiviteProgramme;
import tn.esprit.mindconnect.entities.ProgrammeBienEtre;
import tn.esprit.mindconnect.services.ActiviteService;

import java.net.URL;
import java.sql.Time;
import java.util.ResourceBundle;

/**
 * Contrôleur pour la boîte de dialogue d'ajout/modification d'activité.
 * Gère la validation des entrées et la persistance des activités dans un
 * programme.
 */
public class AddActiviteController implements Initializable {

    @FXML
    private Label lblHeader, lblErrorTitre, lblErrorHeure;
    @FXML
    private Spinner<Integer> spinnerJour, spinnerDuree;
    @FXML
    private TextField txtTitre, txtHeure;
    @FXML
    private ComboBox<String> comboType;
    @FXML
    private TextArea txtDescription;

    private ProgrammeBienEtre programme;
    private ActiviteService activiteService = new ActiviteService();
    private Runnable onSaveCallback;

    private ActiviteProgramme currentActivite;
    private boolean isEditMode = false;

    /**
     * Initialise la classe du contrôleur.
     * Configure les usines de valeurs pour les spinners et les éléments de la boîte
     * combinée.
     */
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        spinnerJour.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 40, 1));
        spinnerDuree.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(5, 120, 15, 5));
        comboType.setItems(
                FXCollections.observableArrayList("Méditation", "Exercice", "Journaling", "Respiration", "Autre"));
        comboType.setValue("Méditation");
    }

    /**
     * Définit le programme associé pour l'activité.
     * 
     * @param p L'entité programme.
     */
    public void setProgramme(ProgrammeBienEtre p) {
        this.programme = p;
        ((SpinnerValueFactory.IntegerSpinnerValueFactory) spinnerJour.getValueFactory()).setMax(p.getDuree());
    }

    /**
     * Configure le contrôleur pour modifier une activité existante et remplit les
     * champs.
     * 
     * @param a L'activité à modifier.
     */
    public void setEditMode(ActiviteProgramme a) {
        this.currentActivite = a;
        this.isEditMode = true;

        if (lblHeader != null)
            lblHeader.setText("Modifier l'Étape");

        spinnerJour.getValueFactory().setValue(a.getJour());
        spinnerDuree.getValueFactory().setValue(a.getDureeMinutes());
        txtTitre.setText(a.getTitre());
        txtDescription.setText(a.getDescription());
        comboType.setValue(a.getTypeActivite());
        txtHeure.setText(a.getHeureDebut().toString().substring(0, 5));
    }

    /**
     * Définit un rappel à exécuter après une sauvegarde réussie.
     * 
     * @param callback Le Runnable à appeler lors de la sauvegarde.
     */
    public void setOnSaveCallback(Runnable callback) {
        this.onSaveCallback = callback;
    }

    /**
     * Gère l'action de sauvegarde, en validant les entrées et en ajoutant ou
     * mettant à jour l'activité.
     */
    @FXML
    private void handleSave() {
        resetValidation();
        if (!validateInput())
            return;

        try {
            ActiviteProgramme a = isEditMode ? currentActivite : new ActiviteProgramme();
            a.setIdProgramme(programme.getIdProgramme());
            a.setJour(spinnerJour.getValue());
            a.setDureeMinutes(spinnerDuree.getValue());
            a.setTitre(txtTitre.getText());
            a.setDescription(txtDescription.getText());
            a.setTypeActivite(comboType.getValue());

            String timeStr = txtHeure.getText();
            if (!timeStr.contains(":"))
                timeStr += ":00:00";
            else if (timeStr.split(":").length == 2)
                timeStr += ":00";

            a.setHeureDebut(Time.valueOf(timeStr));

            if (isEditMode) {
                activiteService.update(a);
            } else {
                activiteService.add(a);
            }

            if (onSaveCallback != null) {
                onSaveCallback.run();
            }
            closeModal();
        } catch (Exception e) {
            txtHeure.getStyleClass().add("text-input-error");
            lblErrorHeure.setText("Format invalide (HH:mm)");
            lblErrorHeure.setVisible(true);
            lblErrorHeure.setManaged(true);
        }
    }

    /**
     * Valide que les champs requis ne sont pas vides.
     * 
     * @return True si l'entrée est valide, false sinon.
     */
    private boolean validateInput() {
        boolean isValid = true;
        if (txtTitre.getText().trim().isEmpty()) {
            txtTitre.getStyleClass().add("text-input-error");
            lblErrorTitre.setVisible(true);
            lblErrorTitre.setManaged(true);
            isValid = false;
        }
        if (txtHeure.getText().trim().isEmpty()) {
            txtHeure.getStyleClass().add("text-input-error");
            lblErrorHeure.setVisible(true);
            lblErrorHeure.setManaged(true);
            isValid = false;
        }
        return isValid;
    }

    /**
     * Réinitialise toutes les erreurs de validation visuelle sur le formulaire.
     */
    private void resetValidation() {
        txtTitre.getStyleClass().remove("text-input-error");
        txtHeure.getStyleClass().remove("text-input-error");
        lblErrorTitre.setVisible(false);
        lblErrorTitre.setManaged(false);
        lblErrorHeure.setVisible(false);
        lblErrorHeure.setManaged(false);
    }

    /**
     * Gère l'action du bouton d'annulation en fermant la fenêtre modale.
     */
    @FXML
    private void handleCancel() {
        closeModal();
    }

    /**
     * Ferme la fenêtre modale actuelle.
     */
    private void closeModal() {
        ((Stage) txtTitre.getScene().getWindow()).close();
    }
}
