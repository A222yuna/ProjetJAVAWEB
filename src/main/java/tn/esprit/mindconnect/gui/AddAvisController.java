package tn.esprit.mindconnect.gui;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.stage.Stage;
import org.controlsfx.control.Rating;
import tn.esprit.mindconnect.entities.Avis;
import tn.esprit.mindconnect.entities.ProgrammeBienEtre;
import tn.esprit.mindconnect.services.AvisService;

import java.net.URL;
import java.util.ResourceBundle;

/**
 * Contrôleur pour l'ajout d'un nouvel avis (Avis) pour un programme.
 * Gère la saisie de la note et la validation des commentaires.
 */
public class AddAvisController implements Initializable {

    @FXML
    private Rating rating;
    @FXML
    private TextArea txtCommentaire;
    @FXML
    private Label lblErrorNote, lblErrorCommentaire;

    private ProgrammeBienEtre programme;
    private Avis avisToEdit;
    private AvisService avisService = new AvisService();
    private Runnable onSaveCallback;

    /**
     * Initialise la classe du contrôleur.
     * Configure le composant de notation et ajoute un écouteur pour réinitialiser
     * la validation lors du changement.
     */
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // La notation est déjà configurée dans le FXML avec max=5 et rating=0
        // Ajouter un écouteur pour effacer l'erreur quand la note change
        rating.ratingProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal.doubleValue() > 0) {
                resetValidation();
            }
        });
    }

    /**
     * Définit le programme pour lequel l'avis est ajouté.
     * 
     * @param p L'entité programme.
     */
    public void setProgramme(ProgrammeBienEtre p) {
        this.programme = p;
    }

    /**
     * Définit l'avis à modifier et pré-remplit les champs.
     * 
     * @param a L'entité avis à modifier.
     */
    public void setAvis(Avis a) {
        this.avisToEdit = a;
        if (a != null) {
            rating.setRating(a.getNote());
            txtCommentaire.setText(a.getCommentaire());
        }
    }

    /**
     * Définit un rappel à exécuter après la sauvegarde de l'avis.
     * 
     * @param callback Le Runnable à appeler lors de la sauvegarde.
     */
    public void setOnSaveCallback(Runnable callback) {
        this.onSaveCallback = callback;
    }

    /**
     * Gère l'action de sauvegarde, en validant l'entrée et en persistant le nouvel
     * avis.
     */
    @FXML
    private void handleSave() {
        resetValidation();
        if (!validateInput())
            return;

        Avis avis = (avisToEdit != null) ? avisToEdit : new Avis();
        if (avisToEdit == null) {
            avis.setIdProgramme(programme.getIdProgramme());
            avis.setIdPsychologue(1); // Placeholder pour l'auteur (ID Patient)
            avis.setDateAvis(new java.sql.Date(System.currentTimeMillis()));
        }

        avis.setNote((int) Math.round(rating.getRating()));
        avis.setCommentaire(txtCommentaire.getText());

        if (avisToEdit != null) {
            avisService.update(avis);
        } else {
            avisService.add(avis);
        }

        if (onSaveCallback != null) {
            onSaveCallback.run();
        }
        closeModal();
    }

    /**
     * Valide la note et le commentaire saisi.
     * 
     * @return True si valide, false sinon.
     */
    private boolean validateInput() {
        boolean isValid = true;
        if (rating.getRating() == 0) {
            lblErrorNote.setVisible(true);
            lblErrorNote.setManaged(true);
            isValid = false;
        }
        if (txtCommentaire.getText().trim().isEmpty()) {
            txtCommentaire.getStyleClass().add("text-input-error");
            lblErrorCommentaire.setVisible(true);
            lblErrorCommentaire.setManaged(true);
            isValid = false;
        }
        return isValid;
    }

    /**
     * Réinitialise les erreurs de validation visuelle.
     */
    private void resetValidation() {
        txtCommentaire.getStyleClass().remove("text-input-error");
        lblErrorNote.setVisible(false);
        lblErrorNote.setManaged(false);
        lblErrorCommentaire.setVisible(false);
        lblErrorCommentaire.setManaged(false);
    }

    /**
     * Gère l'action d'annulation en fermant la fenêtre modale.
     */
    @FXML
    private void handleCancel() {
        closeModal();
    }

    /**
     * Ferme la fenêtre modale actuelle.
     */
    private void closeModal() {
        ((Stage) txtCommentaire.getScene().getWindow()).close();
    }
}
