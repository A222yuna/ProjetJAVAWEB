package tn.esprit.mindconnect.gui;

import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import tn.esprit.mindconnect.entities.ActiviteProgramme;
import tn.esprit.mindconnect.entities.ProgrammeBienEtre;
import tn.esprit.mindconnect.services.ActiviteService;
import tn.esprit.mindconnect.services.PDFGenerationService;
import tn.esprit.mindconnect.services.ProgrammeService;

import java.io.File;
import java.net.URL;
import java.util.List;
import javafx.scene.image.Image; // Added this import based on the provided code edit for `new Image()`

/**
 * Contrôleur pour une carte d'item de programme individuel au sein d'une
 * grille.
 * Gère l'affichage des détails du programme et les actions utilisateur basées
 * sur les rôles.
 */
public class ItemProgrammeController {

    @FXML
    private ImageView imgProgramme;
    @FXML
    private Label lblNom, lblObjectif, lblDuree, lblStatut, lblNiveau; // Reordered to match original, but content of
                                                                       // labels is set correctly
    @FXML
    private Button btnEdit, btnDelete, btnViewActivities, btnDonnerAvis, btnDownloadPDF;

    private ProgrammeBienEtre programme;
    private ProgrammeController parentController;
    private ProgrammeService programmeService = new ProgrammeService();
    private ActiviteService activiteService = new ActiviteService();
    private PDFGenerationService pdfService = new PDFGenerationService();

    /**
     * Remplit la carte avec les données du programme et configure la visibilité des
     * boutons selon le rôle.
     * 
     * @param p      L'entité programme à afficher.
     * @param parent Le contrôleur parent pour gérer les rappels d'actions.
     * @param isPsy  Indique si l'utilisateur actuel est un psychologue.
     */
    public void setData(ProgrammeBienEtre p, ProgrammeController parent, boolean isPsy) {
        this.programme = p;
        this.parentController = parent;
        lblNom.setText(p.getNom());
        lblObjectif.setText(p.getObjectif());
        lblDuree.setText(p.getDuree() + " jours");
        lblStatut.setText(p.getStatut());
        lblNiveau.setText(p.getNiveauDifficulte());

        if (p.getStatut().equalsIgnoreCase("Actif")) {
            lblStatut.getStyleClass().setAll("badge-sage");
        } else {
            lblStatut.getStyleClass().setAll("badge-blush");
        }

        // Charger l'image si elle existe
        if (p.getImage() != null && !p.getImage().isEmpty()) {
            try {
                Image img;
                if (p.getImage().startsWith("http") || p.getImage().startsWith("file:")) {
                    img = new Image(p.getImage());
                } else {
                    File file = new File("target/classes/tn/esprit/mindconnect/images/" + p.getImage());
                    if (file.exists()) {
                        img = new Image(file.toURI().toString());
                    } else {
                        URL res = getClass().getResource("/tn/esprit/mindconnect/images/" + p.getImage());
                        if (res != null) {
                            img = new Image(res.toExternalForm());
                        } else {
                            img = null;
                        }
                    }
                }
                if (img != null) {
                    imgProgramme.setImage(img);
                }
            } catch (Exception e) {
                System.err.println("Could not load image for: " + p.getNom());
                // Image par défaut si erreur
            }
        }

        // Visibilité basée sur le rôle
        btnEdit.setVisible(isPsy);
        btnEdit.setManaged(isPsy);
        btnDelete.setVisible(isPsy);
        btnDelete.setManaged(isPsy);

        btnDonnerAvis.setVisible(!isPsy);
        btnDonnerAvis.setManaged(!isPsy);
    }

    /**
     * Handles the edit action by requesting the parent controller to open the edit
     * modal.
     */
    @FXML
    private void handleEdit() {
        if (parentController != null) {
            parentController.openEditModal(programme);
        }
    }

    /**
     * Handles the delete action, asking for confirmation before removing the
     * program.
     */
    @FXML
    private void handleDelete() {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Suppression");
        alert.setHeaderText("Supprimer le programme : " + programme.getNom());
        alert.setContentText("Êtes-vous sûr de vouloir supprimer ce programme ?");

        if (alert.showAndWait().get() == ButtonType.OK) {
            programmeService.delete(programme.getIdProgramme());
            if (parentController != null) {
                parentController.loadProgrammes();
            }
        }
    }

    /**
     * Handles the action to view activities associated with this program.
     */
    @FXML
    private void handleViewActivities() {
        if (parentController != null) {
            parentController.showActivities(programme);
        }
    }

    /**
     * Handles the action for a patient to give a review for this program.
     */
    @FXML
    private void handleGiveAvis() {
        if (parentController != null) {
            parentController.openAvisModal(programme);
        }
    }

    /**
     * Handles the PDF download action for this program.
     */
    @FXML
    private void handleDownloadPDF() {
        try {
            System.out.println("📄 Génération du PDF pour le programme: " + programme.getNom());
            
            // Récupérer les activités du programme
            List<ActiviteProgramme> activities = activiteService.getByProgrammeId(programme.getIdProgramme());
            
            // Générer le PDF
            byte[] pdfContent = pdfService.generateProgramPDF(programme, activities);
            String fileName = pdfService.generateFileName(programme);
            
            // Sauvegarder et ouvrir le PDF
            pdfService.savePDFToFile(pdfContent, fileName);
            
            // Message de confirmation
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("PDF Généré");
            alert.setHeaderText("Programme téléchargé avec succès !");
            alert.setContentText("Le PDF du programme \"" + programme.getNom() + "\" a été généré et ouvert.");
            alert.showAndWait();
            
        } catch (Exception e) {
            System.err.println("❌ Erreur lors de la génération du PDF: " + e.getMessage());
            
            Alert errorAlert = new Alert(Alert.AlertType.ERROR);
            errorAlert.setTitle("Erreur");
            errorAlert.setHeaderText("Erreur lors de la génération du PDF");
            errorAlert.setContentText("Une erreur est survenue: " + e.getMessage() + "\nVeuillez réessayer plus tard.");
            errorAlert.showAndWait();
        }
    }
}
