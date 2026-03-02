package tn.psy.gestioncabinet.controller;

import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import tn.psy.gestioncabinet.model.Disponibilite;
import tn.psy.gestioncabinet.service.PlanningService;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

/**
 * Contrôleur pour ajouter une disponibilité (plage récurrente) et générer les créneaux.
 */
public class AddDisponibiliteController {

    private static final DateTimeFormatter TIME_FMT = DateTimeFormatter.ofPattern("H:mm");

    @FXML private Label lblCabinet;
    @FXML private TextField tfJour;
    @FXML private TextField tfHeureDebut;
    @FXML private TextField tfHeureFin;
    @FXML private TextField tfDuree;
    @FXML private Label lblError;
    @FXML private Button btnGenerer;
    @FXML private Button btnFermer;

    private final PlanningService planningService = new PlanningService();
    private int cabinetId;
    private String cabinetLabel;

    public void setCabinet(int cabinetId, String cabinetLabel) {
        this.cabinetId = cabinetId;
        this.cabinetLabel = cabinetLabel != null ? cabinetLabel : "Cabinet " + cabinetId;
        if (lblCabinet != null) {
            lblCabinet.setText("Cabinet : " + this.cabinetLabel);
        }
    }

    @FXML
    public void initialize() {
        lblError.setVisible(false);
        btnGenerer.setOnAction(e -> generer());
        btnFermer.setOnAction(e -> fermer());
    }

    private void generer() {
        lblError.setVisible(false);
        int jour;
        try {
            jour = Integer.parseInt(tfJour.getText() == null ? "" : tfJour.getText().trim());
            if (jour < 1 || jour > 7) {
                throw new NumberFormatException("Jour entre 1 et 7");
            }
        } catch (NumberFormatException e) {
            afficherErreur("Jour invalide (1 = Lundi … 7 = Dimanche).");
            return;
        }
        LocalTime debut = parseTime(tfHeureDebut.getText(), "Heure début");
        LocalTime fin = parseTime(tfHeureFin.getText(), "Heure fin");
        if (debut == null || fin == null) return;
        if (!fin.isAfter(debut)) {
            afficherErreur("L'heure de fin doit être après l'heure de début.");
            return;
        }
        int duree;
        try {
            duree = Integer.parseInt(tfDuree.getText() == null ? "30" : tfDuree.getText().trim());
            if (duree <= 0 || duree > 120) {
                throw new NumberFormatException("Durée entre 1 et 120");
            }
        } catch (NumberFormatException e) {
            afficherErreur("Durée invalide (minutes, ex: 30).");
            return;
        }

        Disponibilite d = new Disponibilite();
        d.setCabinetId(cabinetId);
        d.setJour(jour);
        d.setHeureDebut(debut);
        d.setHeureFin(fin);
        d.setDureeConsultation(duree);
        int idDisp = planningService.ajouterDisponibilite(d);
        if (idDisp <= 0) {
            afficherErreur("Erreur lors de l'ajout de la disponibilité.");
            return;
        }
        LocalDate aujourdhui = LocalDate.now();
        LocalDate finPeriod = aujourdhui.plusWeeks(4);
        int nb = planningService.generateCreneaux(idDisp, aujourdhui, finPeriod);
        new Alert(Alert.AlertType.INFORMATION, nb + " créneaux générés pour les 4 prochaines semaines.", ButtonType.OK).showAndWait();
        fermer();
    }

    private LocalTime parseTime(String s, String field) {
        if (s == null || s.isBlank()) {
            afficherErreur(field + " non renseigné (ex: 08:00).");
            return null;
        }
        try {
            return LocalTime.parse(s.trim(), TIME_FMT);
        } catch (DateTimeParseException e) {
            try {
                return LocalTime.parse(s.trim(), DateTimeFormatter.ofPattern("HH:mm"));
            } catch (DateTimeParseException e2) {
                afficherErreur(field + " invalide (format HH:mm).");
                return null;
            }
        }
    }

    private void afficherErreur(String msg) {
        lblError.setText(msg);
        lblError.setVisible(true);
    }

    private void fermer() {
        Stage stage = (Stage) btnFermer.getScene().getWindow();
        stage.close();
    }
}
