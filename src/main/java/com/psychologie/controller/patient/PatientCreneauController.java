package com.psychologie.controller.patient;

import com.psychologie.model.Cabinet;
import com.psychologie.model.Disponibilite;
import com.psychologie.model.User;
import com.psychologie.controller.SecurityController;
import com.psychologie.util.DatabaseConnection;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;

import java.sql.*;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

public class PatientCreneauController {

    @FXML private ComboBox<Disponibilite> disponibiliteComboBox;
    @FXML private DatePicker datePicker;
    @FXML private TextField heureField;

    @FXML private TableView<Disponibilite> dispoTable;
    @FXML private TableColumn<Disponibilite, Integer> idColumn;
    @FXML private TableColumn<Disponibilite, String>  cabinetColumn;
    @FXML private TableColumn<Disponibilite, Integer> jourColumn;
    @FXML private TableColumn<Disponibilite, String>  fenetreColumn;
    @FXML private TableColumn<Disponibilite, Integer> dureeColumn;

    private final ObservableList<Disponibilite> dispoData = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        setupTable();
        loadDisponibilites();
        disponibiliteComboBox.setItems(dispoData);
        disponibiliteComboBox.setConverter(new javafx.util.StringConverter<>() {
            private final String[] DAYS = {"", "Lundi", "Mardi", "Mercredi", "Jeudi", "Vendredi", "Samedi", "Dimanche"};
            @Override
            public String toString(Disponibilite d) {
                if (d == null) return "";
                Cabinet c = d.getCabinet();
                String cab = (c != null && c.getVille() != null && !c.getVille().isBlank())
                        ? c.getVille() : (c != null ? "Cabinet #" + c.getId() : "?");
                int j = d.getJour();
                String day = (j >= 1 && j <= 7) ? DAYS[j] : "Jour " + j;
                return cab + " – " + day + " " + fmt(d.getHeureDebut()) + "-" + fmt(d.getHeureFin());
            }
            @Override public Disponibilite fromString(String s) { return null; }
        });
    }

    private void setupTable() {
        idColumn.setCellValueFactory(new PropertyValueFactory<>("id"));

        cabinetColumn.setCellValueFactory(cd -> {
            Cabinet c = cd.getValue().getCabinet();
            if (c == null) return new SimpleStringProperty("—");
            String v = c.getVille()   != null ? c.getVille()   : "";
            String a = c.getAdresse() != null ? c.getAdresse() : "";
            return new SimpleStringProperty(v.isBlank() ? a : v + (a.isBlank() ? "" : " – " + a));
        });

        jourColumn.setCellValueFactory(new PropertyValueFactory<>("jour"));
        jourColumn.setCellFactory(col -> new TableCell<>() {
            private final String[] DAYS = {"", "Lundi", "Mardi", "Mercredi", "Jeudi", "Vendredi", "Samedi", "Dimanche"};
            @Override
            protected void updateItem(Integer item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) { setText(null); return; }
                setText(item >= 1 && item <= 7 ? DAYS[item] : "Jour " + item);
            }
        });

        fenetreColumn.setCellValueFactory(cd -> new SimpleStringProperty(
                fmt(cd.getValue().getHeureDebut()) + " - " + fmt(cd.getValue().getHeureFin())));

        dureeColumn.setCellValueFactory(new PropertyValueFactory<>("dureeConsultation"));
        dispoTable.setItems(dispoData);
    }

    private void loadDisponibilites() {
        dispoData.clear();
        String q =
            "SELECT d.id, d.jour, d.heure_debut, d.heure_fin, d.duree_consultation, " +
            "       c.id_cabinet, c.adresse, c.ville " +
            "FROM disponibilite d " +
            "JOIN cabinet c ON d.cabinet_id = c.id_cabinet " +
            "ORDER BY d.id";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(q);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                Disponibilite d = new Disponibilite();
                d.setId(rs.getInt("id"));
                d.setJour(rs.getInt("jour"));
                d.setHeureDebut(rs.getTime("heure_debut").toLocalTime());
                d.setHeureFin(rs.getTime("heure_fin").toLocalTime());
                d.setDureeConsultation(rs.getInt("duree_consultation"));
                Cabinet c = new Cabinet();
                c.setId(rs.getInt("id_cabinet"));
                c.setAdresse(rs.getString("adresse"));
                c.setVille(rs.getString("ville"));
                d.setCabinet(c);
                dispoData.add(d);
            }
        } catch (SQLException e) { e.printStackTrace(); }
    }

    @FXML
    public void handleBooking() {
        Disponibilite sel = disponibiliteComboBox.getValue();
        LocalDate date    = datePicker.getValue();
        String heureStr   = heureField.getText();
        if (sel == null || date == null || heureStr.isBlank()) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Veuillez remplir tous les champs.");
            return;
        }
        try {
            LocalTime t = LocalTime.parse(heureStr, DateTimeFormatter.ofPattern("HH:mm"));
            if (date.isBefore(LocalDate.now())) {
                showAlert(Alert.AlertType.ERROR, "Erreur", "Vous ne pouvez pas réserver dans le passé.");
                return;
            }
            if (java.time.LocalDateTime.of(date, t)
                    .isBefore(java.time.LocalDateTime.now().plusHours(2))) {
                showAlert(Alert.AlertType.ERROR, "Erreur", "Réservez au moins 2 heures à l'avance.");
                return;
            }
            if (t.isBefore(sel.getHeureDebut()) || t.isAfter(sel.getHeureFin())) {
                showAlert(Alert.AlertType.ERROR, "Erreur",
                        "L'heure doit être entre " + fmt(sel.getHeureDebut()) + " et " + fmt(sel.getHeureFin()));
                return;
            }
            User patient = SecurityController.getCurrentUser();
            if (patient == null) return;
            try (Connection conn = DatabaseConnection.getConnection()) {
                PreparedStatement c1 = conn.prepareStatement(
                        "SELECT COUNT(*) FROM creneau WHERE patient_id_user=? AND date_creneau=?");
                c1.setInt(1, patient.getId()); c1.setDate(2, Date.valueOf(date));
                ResultSet r1 = c1.executeQuery();
                if (r1.next() && r1.getInt(1) > 0) {
                    showAlert(Alert.AlertType.ERROR, "Erreur", "Vous avez déjà un rendez-vous ce jour.");
                    return;
                }
                PreparedStatement c2 = conn.prepareStatement(
                        "SELECT COUNT(*) FROM creneau WHERE disponibilite_id=? AND date_creneau=? AND heure=? AND statut='RESERVE'");
                c2.setInt(1, sel.getId()); c2.setDate(2, Date.valueOf(date)); c2.setTime(3, Time.valueOf(t));
                ResultSet r2 = c2.executeQuery();
                if (r2.next() && r2.getInt(1) > 0) {
                    showAlert(Alert.AlertType.ERROR, "Erreur", "Ce créneau est déjà réservé.");
                    return;
                }
                PreparedStatement ins = conn.prepareStatement(
                        "INSERT INTO creneau (disponibilite_id,patient_id_user,date_creneau,heure,statut) VALUES(?,?,?,?,?)");
                ins.setInt(1, sel.getId()); ins.setInt(2, patient.getId());
                ins.setDate(3, Date.valueOf(date)); ins.setTime(4, Time.valueOf(t));
                ins.setString(5, "RESERVE");
                ins.executeUpdate();
                showAlert(Alert.AlertType.INFORMATION, "Succès", "Créneau réservé avec succès ✓");
                loadDisponibilites();
            }
        } catch (Exception e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Erreur", "Format d'heure invalide (HH:mm) ou erreur base de données.");
        }
    }

    @FXML public void showMyCreneaux() { System.out.println("Mes créneaux"); }

    private String fmt(LocalTime t) {
        return t != null ? t.format(DateTimeFormatter.ofPattern("HH:mm")) : "--";
    }

    private void showAlert(Alert.AlertType type, String title, String msg) {
        Alert a = new Alert(type); a.setTitle(title); a.setHeaderText(null);
        a.setContentText(msg); a.showAndWait();
    }
}
