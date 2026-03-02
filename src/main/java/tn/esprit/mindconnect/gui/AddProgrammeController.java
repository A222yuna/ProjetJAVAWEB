package tn.esprit.mindconnect.gui;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import tn.esprit.mindconnect.entities.ActiviteProgramme;
import tn.esprit.mindconnect.entities.ProgrammeBienEtre;
import tn.esprit.mindconnect.services.ActiviteService;
import tn.esprit.mindconnect.services.EmailService;
import tn.esprit.mindconnect.services.ProgrammeService;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.ResourceBundle;

/**
 * Contrôleur pour la boîte de dialogue de création et de modification de
 * programme.
 * Gère les détails du programme, le téléchargement d'images et la validation.
 */
public class AddProgrammeController implements Initializable {

    @FXML
    private Label lblTitle, lblErrorNom, lblErrorObjectif, lblErrorDuree;
    @FXML
    private TextField txtNom, txtDuree;
    @FXML
    private TextArea txtObjectif;
    @FXML
    private ComboBox<String> comboNiveau, comboStatut;
    @FXML
    private ImageView imgPreview;
    @FXML
    private VBox placeholderArea;

    private ProgrammeService programmeService = new ProgrammeService();
    private ActiviteService activiteService = new ActiviteService();
    private EmailService emailService = new EmailService();
    private ProgrammeBienEtre programmeToEdit;
    private boolean isEditMode = false;
    private Runnable onSaveCallback;
    private String selectedImagePath = "";

    /**
     * Initialise la classe du contrôleur.
     * Configure les boîtes combinées pour les niveaux et le statut.
     */
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        comboNiveau.setItems(FXCollections.observableArrayList("Facile", "Moyen", "Difficile"));
        comboStatut.setItems(FXCollections.observableArrayList("Actif", "Inactif", "Archivé"));

        // Valeurs par défaut
        if (!isEditMode) {
            comboNiveau.setValue("Facile");
            comboStatut.setValue("Actif");
        }
    }

    /**
     * Configure le contrôleur pour la modification d'un programme existant.
     * 
     * @param p L'entité programme à modifier.
     */
    public void setEditMode(ProgrammeBienEtre p) {
        this.programmeToEdit = p;
        this.isEditMode = true;
        this.lblTitle.setText("Modifier le Programme");

        txtNom.setText(p.getNom());
        txtObjectif.setText(p.getObjectif());
        txtDuree.setText(String.valueOf(p.getDuree()));
        comboNiveau.setValue(p.getNiveauDifficulte());
        comboStatut.setValue(p.getStatut());

        if (p.getImage() != null && !p.getImage().isEmpty()) {
            displayImage(p.getImage());
            this.selectedImagePath = p.getImage();
        }
    }

    /**
     * Affiche un aperçu d'image à partir de divers types de chemins (URL, Fichier,
     * Ressource).
     * 
     * @param path Le chemin de l'image.
     */
    private void displayImage(String path) {
        try {
            Image img;
            if (path.startsWith("http") || path.startsWith("file:")) {
                img = new Image(path);
            } else {
                File file = new File(path);
                if (file.exists()) {
                    img = new Image(file.toURI().toString());
                } else {
                    // Essayer les ressources
                    URL res = getClass().getResource("/tn/esprit/mindconnect/images/" + path);
                    if (res != null) {
                        img = new Image(res.toExternalForm());
                    } else {
                        return;
                    }
                }
            }
            imgPreview.setImage(img);
            imgPreview.setVisible(true);
            placeholderArea.setVisible(false);
        } catch (Exception e) {
            System.err.println("Could not load image preview: " + e.getMessage());
        }
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
     * Gère l'action de téléchargement d'image, permettant à l'utilisateur de
     * choisir une image et de la copier dans le dossier cible.
     */
    @FXML
    private void handleUploadImage() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Choisir une image de couverture");
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Images", "*.png", "*.jpg", "*.jpeg"));
        File selectedFile = fileChooser.showOpenDialog(txtNom.getScene().getWindow());

        if (selectedFile != null) {
            try {
                // Déterminer la destination
                String fileName = System.currentTimeMillis() + "_" + selectedFile.getName();
                Path dest = Paths.get("target/classes/tn/esprit/mindconnect/images/" + fileName);
                Files.createDirectories(dest.getParent());
                Files.copy(selectedFile.toPath(), dest, StandardCopyOption.REPLACE_EXISTING);

                // Garder pour l'aperçu et la sauvegarde
                this.selectedImagePath = fileName; // Nous stockons le chemin relatif pour la portabilité
                displayImage(selectedFile.getAbsolutePath());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Gère l'action d'annulation en fermant la scène.
     */
    @FXML
    private void handleCancel() {
        closeStage();
    }

    /**
     * Gère l'action de sauvegarde, en validant l'entrée et en persistant le
     * programme.
     */
    @FXML
    private void handleSave() {
        resetValidation();
        if (!validateInput())
            return;

        String nom = txtNom.getText();
        String objectif = txtObjectif.getText();
        int duree = Integer.parseInt(txtDuree.getText());
        String niveau = comboNiveau.getValue();
        String statut = comboStatut.getValue();
        String image = selectedImagePath;

        if (isEditMode) {
            programmeToEdit.setNom(nom);
            programmeToEdit.setObjectif(objectif);
            programmeToEdit.setDuree(duree);
            programmeToEdit.setNiveauDifficulte(niveau);
            programmeToEdit.setStatut(statut);
            programmeToEdit.setImage(image);
            programmeService.update(programmeToEdit);
        } else {
            ProgrammeBienEtre newP = new ProgrammeBienEtre(1, nom, objectif, duree, statut, image, niveau);
            programmeService.add(newP);
            
            // Envoyer un email de notification pour le nouveau programme
            try {
                // Récupérer les activités du programme (s'il y en a)
                List<ActiviteProgramme> activities = activiteService.getByProgrammeId(newP.getIdProgramme());
                emailService.sendProgramNotification(newP, activities);
                System.out.println("📧 Email de notification envoyé pour le nouveau programme: " + nom);
            } catch (Exception e) {
                System.err.println("⚠️ Erreur lors de l'envoi de l'email: " + e.getMessage());
                // Ne pas bloquer la sauvegarde si l'email échoue
            }
        }

        if (onSaveCallback != null)
            onSaveCallback.run();
        closeStage();
    }

    /**
     * Valide les champs de saisie du programme.
     * 
     * @return True si valide, false sinon.
     */
    private boolean validateInput() {
        boolean isValid = true;

        if (txtNom.getText().trim().isEmpty()) {
            txtNom.getStyleClass().add("text-input-error");
            lblErrorNom.setVisible(true);
            lblErrorNom.setManaged(true);
            isValid = false;
        }

        if (txtObjectif.getText().trim().isEmpty()) {
            txtObjectif.getStyleClass().add("text-input-error");
            lblErrorObjectif.setVisible(true);
            lblErrorObjectif.setManaged(true);
            isValid = false;
        }

        try {
            int d = Integer.parseInt(txtDuree.getText());
            if (d <= 0 || d > 90)
                throw new Exception();
        } catch (Exception e) {
            txtDuree.getStyleClass().add("text-input-error");
            lblErrorDuree.setVisible(true);
            lblErrorDuree.setManaged(true);
            isValid = false;
        }

        return isValid;
    }

    /**
     * Réinitialise les erreurs de validation visuelle.
     */
    private void resetValidation() {
        txtNom.getStyleClass().remove("text-input-error");
        txtObjectif.getStyleClass().remove("text-input-error");
        txtDuree.getStyleClass().remove("text-input-error");

        lblErrorNom.setVisible(false);
        lblErrorNom.setManaged(false);
        lblErrorObjectif.setVisible(false);
        lblErrorObjectif.setManaged(false);
        lblErrorDuree.setVisible(false);
        lblErrorDuree.setManaged(false);
    }

    /**
     * Ferme la fenêtre de la scène actuelle.
     */
    private void closeStage() {
        Stage stage = (Stage) txtNom.getScene().getWindow();
        stage.close();
    }
}
