package tn.psy.gestioncabinet.controller;

import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.stage.Stage;
import tn.psy.gestioncabinet.model.Cabinet;
import tn.psy.gestioncabinet.model.Creneau;
import tn.psy.gestioncabinet.service.PlanningService;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Contrôleur du dialogue "Prendre rendez-vous" : affiche les créneaux libres et permet la réservation.
 */
public class PriseRdvController {

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("EEEE d MMMM yyyy", java.util.Locale.FRENCH);
    private static final DateTimeFormatter HEURE_FMT = DateTimeFormatter.ofPattern("HH'h'mm");

    @FXML private Label lblTitre;
    @FXML private ListView<String> listCreneaux;
    @FXML private Label lblMessage;
    @FXML private Button btnReserver;
    @FXML private Button btnAnnuler;

    private final PlanningService planningService = new PlanningService();
    private Cabinet cabinet;
    /**
     * Identifiant utilisateur du patient (users.id_user).
     */
    private int patientUserId;
    private List<Creneau> creneaux;
    private Runnable onReserve;

    public void setData(Cabinet cabinet, int patientUserId, Runnable onReserve) {
        this.cabinet = cabinet;
        this.patientUserId = patientUserId;
        this.onReserve = onReserve;
        if (lblTitre != null && cabinet != null) {
            lblTitre.setText("Rendez-vous : " + (cabinet.getAdresse() != null ? cabinet.getAdresse() : "Cabinet"));
        }
        chargerCreneaux();
    }

    private void chargerCreneaux() {
        if (cabinet == null || listCreneaux == null) return;
        LocalDate aujourdhui = LocalDate.now();
        LocalDate fin = aujourdhui.plusWeeks(2);
        creneaux = planningService.getCreneauxLibres(cabinet.getIdCabinet(), aujourdhui, fin);
        listCreneaux.getItems().clear();
        if (creneaux.isEmpty()) {
            listCreneaux.getItems().add("Aucun créneau disponible. Le cabinet n'a peut-être pas encore défini de plages.");
        } else {
            for (Creneau c : creneaux) {
                String line = String.format("%s à %s",
                        c.getDateCreneau() != null ? c.getDateCreneau().format(DATE_FMT) : "?",
                        c.getHeure() != null ? c.getHeure().format(HEURE_FMT) : "?");
                listCreneaux.getItems().add(line);
            }
        }
        listCreneaux.getSelectionModel().selectedIndexProperty().addListener((o, oldVal, newVal) -> {
            btnReserver.setDisable(newVal == null || newVal.intValue() < 0 || creneaux.isEmpty());
        });
    }

    @FXML
    public void initialize() {
        if (btnReserver != null) {
            btnReserver.setOnAction(e -> reserver());
        }
        if (btnAnnuler != null) {
            btnAnnuler.setOnAction(e -> fermer());
        }
    }

    private void reserver() {
        int idx = listCreneaux.getSelectionModel().getSelectedIndex();
        if (patientUserId <= 0 || cabinet == null || idx < 0 || creneaux == null || idx >= creneaux.size()) {
            return;
        }
        Creneau c = creneaux.get(idx);
        if (!c.isLibre()) {
            afficherMessage("Ce créneau n'est plus disponible.");
            return;
        }
        if (planningService.reserverCreneau(c.getId(), patientUserId)) {
            new Alert(Alert.AlertType.INFORMATION, "Créneau réservé avec succès.", ButtonType.OK).showAndWait();
            if (onReserve != null) {
                onReserve.run();
            }
            fermer();
        } else {
            afficherMessage("Impossible de réserver (créneau peut-être déjà pris).");
        }
    }

    private void afficherMessage(String msg) {
        if (lblMessage != null) {
            lblMessage.setText(msg);
            lblMessage.setVisible(true);
        }
    }

    private void fermer() {
        Stage stage = (Stage) (btnAnnuler != null ? btnAnnuler.getScene().getWindow() : listCreneaux.getScene().getWindow());
        if (stage != null) {
            stage.close();
        }
    }
}
