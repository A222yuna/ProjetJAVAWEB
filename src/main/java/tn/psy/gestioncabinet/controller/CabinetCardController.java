package tn.psy.gestioncabinet.controller;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import tn.psy.gestioncabinet.model.Cabinet;
<<<<<<< HEAD
import tn.psy.gestioncabinet.model.Patient;
=======
>>>>>>> origin/gestion-cabinet
import tn.psy.gestioncabinet.service.RatingService;

/**
 * Carte visuelle pour l'affichage d'un cabinet côté patient.
 * Utilisée dans un FlowPane dans PatientDashboardController.
 */
public class CabinetCardController {

    private static final String STAR = "★";
    private static final String STAR_EMPTY = "☆";

    @FXML private Label lblTitre;
    @FXML private Label lblBadge;
    @FXML private Label lblDescription;
    @FXML private Label lblVille;
    @FXML private Label lblAdresse;
    @FXML private Label lblHoraires;
    @FXML private Label lblRating;
    @FXML private HBox boxEtoiles;
    @FXML private Button btnDetails;
    @FXML private Button btnCarte;
    @FXML private Button btnRdv;

    private final RatingService ratingService = new RatingService();
    private Cabinet cabinet;
    private PatientDashboardController parent;

    public void setData(Cabinet cabinet, PatientDashboardController parent) {
        this.cabinet = cabinet;
        this.parent = parent;

        // Titre : on utilise l'adresse comme "nom" principal
        lblTitre.setText(cabinet.getAdresse() != null ? cabinet.getAdresse() : "Cabinet");

        // Description
        String desc = cabinet.getDescription();
        lblDescription.setText((desc != null && !desc.isBlank()) ? desc : "Aucune description fournie.");

        // Ville / Adresse / Horaires
        lblVille.setText(cabinet.getVille() != null ? "Ville : " + cabinet.getVille() : "Ville : -");
        lblAdresse.setText(cabinet.getAdresse() != null ? "Adresse : " + cabinet.getAdresse() : "Adresse : -");
        lblHoraires.setText(cabinet.getHoraires() != null ? "Horaires : " + cabinet.getHoraires() : "Horaires : -");

        // Badge statut : côté Patient, on affiche toujours "Validé" (jamais "Archivé").
        lblBadge.setText("Validé");
        lblBadge.setStyle("-fx-background-radius: 999; -fx-padding: 2 8; -fx-font-size: 11px; " +
                "-fx-text-fill: white; -fx-background-color: #2E8B57;");

        // Notation : moyenne + nombre d'avis + étoiles cliquables si pas encore noté
        refreshRatingDisplay();

        btnDetails.setOnAction(e -> {
            if (this.parent != null) {
                parent.afficherDetails(cabinet);
            }
        });

        if (btnCarte != null) {
            btnCarte.setOnAction(e -> {
                if (this.parent != null) {
                    parent.ouvrirCarte(cabinet);
                }
            });
        }

        btnRdv.setOnAction(e -> {
            if (this.parent != null) {
                parent.prendreRendezVous(cabinet);
            }
        });
    }

    private void refreshRatingDisplay() {
        int cabinetId = cabinet.getIdCabinet();
        double moy = ratingService.getMoyenne(cabinetId);
        int nb = ratingService.getNombreAvis(cabinetId);
        if (lblRating != null) {
            lblRating.setText(nb == 0 ? "— ★ (0 avis)" : String.format("%.1f ★ (%d avis)", moy, nb));
        }
        if (boxEtoiles != null) {
            boxEtoiles.getChildren().clear();
<<<<<<< HEAD
            Patient patient = parent != null ? parent.getPatient() : null;
            boolean dejaNote = patient != null && ratingService.aDejaNote(patient.getIdPatient(), cabinetId);
            if (patient != null && !dejaNote) {
=======
            int idUser = parent != null ? parent.getCurrentUserId() : 0;
            boolean dejaNote = idUser > 0 && ratingService.aDejaNote(idUser, cabinetId);
            if (idUser > 0 && !dejaNote) {
>>>>>>> origin/gestion-cabinet
                for (int note = 1; note <= 5; note++) {
                    Button star = new Button(STAR_EMPTY);
                    star.setStyle("-fx-background-color: transparent; -fx-font-size: 18px; -fx-cursor: hand;");
                    int n = note;
                    star.setOnAction(e -> {
<<<<<<< HEAD
                        ratingService.noter(patient.getIdPatient(), cabinetId, n);
=======
                        ratingService.noter(idUser, cabinetId, n);
>>>>>>> origin/gestion-cabinet
                        if (parent != null) {
                            parent.refresh();
                        }
                    });
                    boxEtoiles.getChildren().add(star);
                }
<<<<<<< HEAD
            } else if (patient != null && dejaNote) {
                int notePatient = ratingService.getNotePatient(patient.getIdPatient(), cabinetId).orElse(0);
=======
            } else if (idUser > 0 && dejaNote) {
                int notePatient = ratingService.getNotePatient(idUser, cabinetId).orElse(0);
>>>>>>> origin/gestion-cabinet
                Label vousAvezNote = new Label("Vous avez noté : " + notePatient + " " + STAR);
                vousAvezNote.setStyle("-fx-font-size: 12px;");
                boxEtoiles.getChildren().add(vousAvezNote);
            }
        }
    }
}

