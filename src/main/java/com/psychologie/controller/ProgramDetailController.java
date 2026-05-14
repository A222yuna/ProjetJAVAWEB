package com.psychologie.controller;

import com.psychologie.model.ActiviteProgramme;
import com.psychologie.model.Avis;
import com.psychologie.model.ProgrammeBienEtre;
import com.psychologie.model.User;
import com.psychologie.util.DatabaseConnection;
import com.psychologie.util.PdfExportService;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.FileChooser;
import javafx.stage.Window;

import java.io.File;
import java.io.IOException;
import java.sql.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class ProgramDetailController {

    // Top bar
    @FXML private Label programTitleLabel;

    // Info card
    @FXML private Label programTitleCard;
    @FXML private Label psyNameLabel;
    @FXML private Label objectifLabel;
    @FXML private Label dureeLabel;
    @FXML private Label difficulteLabel;
    @FXML private Label statutLabel;

    // Activities
    @FXML private Label activitiesHeader;
    @FXML private VBox  activitiesContainer;
    @FXML private Label noActivitiesLabel;

    // Avis
    @FXML private Label avisHeader;
    @FXML private VBox  avisContainer;
    @FXML private Label noAvisLabel;

    private static ProgrammeBienEtre currentProgram;

    public static void setProgram(ProgrammeBienEtre p) {
        currentProgram = p;
    }

    // ─────────────────────────────────────────────
    //  Lifecycle
    // ─────────────────────────────────────────────

    @FXML
    public void initialize() {
        if (currentProgram != null) {
            loadProgramDetails();
            loadActivities();
            loadAvis();
        }
    }

    // ─────────────────────────────────────────────
    //  Data loading
    // ─────────────────────────────────────────────

    private void loadProgramDetails() {
        programTitleLabel.setText(currentProgram.getNom());
        programTitleCard.setText(currentProgram.getNom());
        objectifLabel.setText(currentProgram.getObjectif() != null ? currentProgram.getObjectif() : "");
        dureeLabel.setText("📅 " + currentProgram.getDuree() + " jours");
        difficulteLabel.setText("📶 " + nullSafe(currentProgram.getNiveauDifficulte()));

        String statut = nullSafe(currentProgram.getStatut());
        statutLabel.setText(statut.isBlank() ? "BROUILLON" : statut.toUpperCase());
        if ("ACTIF".equalsIgnoreCase(statut) || "Publié".equalsIgnoreCase(statut)) {
            statutLabel.getStyleClass().setAll("badge-green");
        } else {
            statutLabel.getStyleClass().setAll("badge-yellow");
        }

        if (currentProgram.getPsychologue() != null) {
            User psy = currentProgram.getPsychologue();
            psyNameLabel.setText("Par Dr. " + nullSafe(psy.getPrenom()) + " " + nullSafe(psy.getNom()));
        }
    }

    private void loadActivities() {
        activitiesContainer.getChildren().clear();
        List<ActiviteProgramme> activities = new ArrayList<>();

        String query = "SELECT * FROM activite_programme WHERE idProgramme = ? ORDER BY jour, heureDebut";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(query)) {
            ps.setInt(1, currentProgram.getId());
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                ActiviteProgramme act = new ActiviteProgramme();
                act.setTitre(rs.getString("titre"));
                act.setDescription(rs.getString("description"));
                act.setJour(rs.getInt("jour"));
                Time t = rs.getTime("heureDebut");
                act.setHeureDebut(t != null ? t.toLocalTime() : java.time.LocalTime.of(9, 0));
                act.setDureeMinutes(rs.getInt("dureeMinutes"));
                act.setTypeActivite(rs.getString("typeActivite"));
                activities.add(act);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        activitiesHeader.setText("Activités (" + activities.size() + ")");

        if (activities.isEmpty()) {
            noActivitiesLabel.setVisible(true);
            noActivitiesLabel.setManaged(true);
        } else {
            noActivitiesLabel.setVisible(false);
            noActivitiesLabel.setManaged(false);
            for (ActiviteProgramme act : activities) {
                activitiesContainer.getChildren().add(buildActivityRow(act));
            }
        }
    }

    private void loadAvis() {
        avisContainer.getChildren().clear();
        List<Avis> avisList = new ArrayList<>();

        String query = "SELECT a.*, u.prenom, u.nom FROM avis a " +
                       "LEFT JOIN users u ON a.psychologue_id_user = u.id_user " +
                       "WHERE a.idProgramme = ? ORDER BY a.dateAvis DESC";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(query)) {
            ps.setInt(1, currentProgram.getId());
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                Avis avis = new Avis();
                avis.setId(rs.getInt("idAvis"));
                avis.setNote(rs.getInt("note"));
                avis.setCommentaire(rs.getString("commentaire"));
                java.sql.Date d = rs.getDate("dateAvis");
                avis.setDateAvis(d != null ? d.toLocalDate() : null);

                User author = new User();
                author.setPrenom(nullSafe(rs.getString("prenom")));
                author.setNom(nullSafe(rs.getString("nom")));
                avis.setPsychologue(author);
                avisList.add(avis);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        avisHeader.setText("Avis (" + avisList.size() + ")");

        if (avisList.isEmpty()) {
            noAvisLabel.setVisible(true);
            noAvisLabel.setManaged(true);
        } else {
            noAvisLabel.setVisible(false);
            noAvisLabel.setManaged(false);
            for (Avis avis : avisList) {
                avisContainer.getChildren().add(buildAvisRow(avis));
            }
        }
    }

    // ─────────────────────────────────────────────
    //  UI builders
    // ─────────────────────────────────────────────

    private HBox buildActivityRow(ActiviteProgramme act) {
        HBox row = new HBox(16);
        row.setPadding(new Insets(12));
        row.setAlignment(Pos.CENTER_LEFT);
        row.setStyle("-fx-background-color: #f9f9f9; -fx-background-radius: 8; -fx-border-color: #e8e8e8; -fx-border-radius: 8;");

        // Day badge
        VBox dayBox = new VBox(2);
        dayBox.setAlignment(Pos.CENTER);
        dayBox.setStyle("-fx-background-color: #e6f0ec; -fx-padding: 8 12; -fx-background-radius: 8; -fx-min-width: 60;");
        Label dayLbl = new Label("JOUR");
        dayLbl.setStyle("-fx-font-size: 9px; -fx-text-fill: #2a6f5b; -fx-font-weight: bold;");
        Label dayNum = new Label(String.valueOf(act.getJour()));
        dayNum.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #2a6f5b;");
        dayBox.getChildren().addAll(dayLbl, dayNum);

        // Content
        VBox content = new VBox(4);
        HBox.setHgrow(content, Priority.ALWAYS);

        Label title = new Label(nullSafe(act.getTitre()));
        title.setStyle("-fx-font-weight: bold; -fx-font-size: 13px;");

        HBox meta = new HBox(10);
        meta.setAlignment(Pos.CENTER_LEFT);
        Label time = new Label("🕒 " + act.getHeureDebut().format(DateTimeFormatter.ofPattern("HH:mm")));
        time.setStyle("-fx-text-fill: #5a6560; -fx-font-size: 12px;");
        Label dur = new Label("⏱ " + act.getDureeMinutes() + " min");
        dur.setStyle("-fx-text-fill: #5a6560; -fx-font-size: 12px;");

        if (act.getTypeActivite() != null && !act.getTypeActivite().isBlank()) {
            Label type = new Label(act.getTypeActivite());
            type.setStyle("-fx-background-color: #e6f0ec; -fx-text-fill: #2a6f5b; -fx-padding: 2 8; -fx-background-radius: 5; -fx-font-size: 10px;");
            meta.getChildren().addAll(time, dur, type);
        } else {
            meta.getChildren().addAll(time, dur);
        }

        content.getChildren().addAll(title, meta);

        if (act.getDescription() != null && !act.getDescription().isBlank()) {
            Label desc = new Label(act.getDescription());
            desc.setStyle("-fx-text-fill: #5a6560; -fx-font-size: 12px;");
            desc.setWrapText(true);
            content.getChildren().add(desc);
        }

        row.getChildren().addAll(dayBox, content);
        return row;
    }

    private HBox buildAvisRow(Avis avis) {
        HBox row = new HBox();
        row.setPadding(new Insets(14));
        row.setAlignment(Pos.CENTER_LEFT);
        row.setStyle("-fx-background-color: #ffffff; -fx-background-radius: 8; " +
                     "-fx-border-color: #e8e8e8; -fx-border-radius: 8;");

        // Left: author info + comment
        VBox left = new VBox(4);
        HBox.setHgrow(left, Priority.ALWAYS);

        User author = avis.getPsychologue();
        String fullName = (author != null)
                ? (nullSafe(author.getPrenom()) + " " + nullSafe(author.getNom())).trim()
                : "Anonyme";
        Label nameLbl = new Label(fullName);
        nameLbl.setStyle("-fx-font-weight: bold; -fx-font-size: 13px;");

        if (avis.getCommentaire() != null && !avis.getCommentaire().isBlank()) {
            Label commentLbl = new Label(avis.getCommentaire());
            commentLbl.setStyle("-fx-text-fill: #5a6560; -fx-font-size: 12px;");
            commentLbl.setWrapText(true);
            left.getChildren().addAll(nameLbl, commentLbl);
        } else {
            left.getChildren().add(nameLbl);
        }

        if (avis.getDateAvis() != null) {
            Label dateLbl = new Label(avis.getDateAvis().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
            dateLbl.setStyle("-fx-text-fill: #9aa09a; -fx-font-size: 11px;");
            left.getChildren().add(dateLbl);
        }

        // Right: star rating
        HBox stars = buildStarDisplay(avis.getNote());
        stars.setAlignment(Pos.CENTER_RIGHT);

        row.getChildren().addAll(left, stars);
        return row;
    }

    /**
     * Renders filled/empty stars for a given note (1–5).
     */
    private HBox buildStarDisplay(int note) {
        HBox box = new HBox(3);
        box.setAlignment(Pos.CENTER);
        for (int i = 1; i <= 5; i++) {
            Label star = new Label(i <= note ? "★" : "☆");
            star.setStyle(i <= note
                    ? "-fx-text-fill: #f5a623; -fx-font-size: 18px;"
                    : "-fx-text-fill: #cccccc; -fx-font-size: 18px;");
            box.getChildren().add(star);
        }
        return box;
    }

    // ─────────────────────────────────────────────
    //  Actions
    // ─────────────────────────────────────────────

    @FXML
    private void handleLeaveReview() {
        User user = SecurityController.getCurrentUser();
        if (user == null) {
            showAlert(Alert.AlertType.WARNING, "Non connecté", "Vous devez être connecté pour laisser un avis.");
            return;
        }

        // Check if user already left a review for this program
        if (hasAlreadyReviewed(user.getId(), currentProgram.getId())) {
            showAlert(Alert.AlertType.INFORMATION, "Avis existant",
                    "Vous avez déjà laissé un avis pour ce programme.");
            return;
        }

        Dialog<Avis> dialog = new Dialog<>();
        dialog.setTitle("Laisser un avis");
        dialog.setHeaderText("Votre avis sur : " + currentProgram.getNom());

        ButtonType saveBtn = new ButtonType("Envoyer", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveBtn, ButtonType.CANCEL);

        // Star rating widget
        final int[] selectedNote = {0};
        HBox starBox = new HBox(6);
        starBox.setAlignment(Pos.CENTER_LEFT);
        Label[] starLabels = new Label[5];
        for (int i = 0; i < 5; i++) {
            final int starIndex = i + 1;
            Label star = new Label("☆");
            star.setStyle("-fx-font-size: 28px; -fx-text-fill: #cccccc; -fx-cursor: hand;");
            star.setOnMouseEntered(e -> highlightStars(starLabels, starIndex));
            star.setOnMouseExited(e -> highlightStars(starLabels, selectedNote[0]));
            star.setOnMouseClicked(e -> {
                selectedNote[0] = starIndex;
                highlightStars(starLabels, starIndex);
            });
            starLabels[i] = star;
            starBox.getChildren().add(star);
        }

        TextArea commentArea = new TextArea();
        commentArea.setPromptText("Votre commentaire (optionnel)...");
        commentArea.setPrefRowCount(4);
        commentArea.setWrapText(true);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(12);
        grid.setPadding(new Insets(20, 30, 10, 10));
        grid.add(new Label("Note :"), 0, 0);
        grid.add(starBox, 1, 0);
        grid.add(new Label("Commentaire :"), 0, 1);
        grid.add(commentArea, 1, 1);
        GridPane.setHgrow(commentArea, Priority.ALWAYS);
        dialog.getDialogPane().setContent(grid);
        dialog.getDialogPane().setPrefWidth(420);

        // Validate before closing
        Button okButton = (Button) dialog.getDialogPane().lookupButton(saveBtn);
        okButton.addEventFilter(javafx.event.ActionEvent.ACTION, event -> {
            if (selectedNote[0] == 0) {
                event.consume();
                showAlert(Alert.AlertType.WARNING, "Note manquante", "Veuillez sélectionner une note (1 à 5 étoiles).");
            }
        });

        dialog.setResultConverter(btn -> {
            if (btn == saveBtn && selectedNote[0] > 0) {
                Avis avis = new Avis();
                avis.setNote(selectedNote[0]);
                avis.setCommentaire(commentArea.getText().trim());
                avis.setDateAvis(LocalDate.now());
                return avis;
            }
            return null;
        });

        dialog.showAndWait().ifPresent(avis -> {
            saveAvis(avis, user, currentProgram.getId());
            loadAvis(); // refresh the list
        });
    }

    @FXML
    private void handleDownloadPdf() {
        if (currentProgram == null) return;

        FileChooser chooser = new FileChooser();
        chooser.setTitle("Enregistrer le PDF");
        chooser.setInitialFileName("programme_" + currentProgram.getId() + ".pdf");
        chooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Fichiers PDF (*.pdf)", "*.pdf"));

        // Get the current window from any node in the scene
        Window window = avisHeader.getScene().getWindow();
        File dest = chooser.showSaveDialog(window);

        if (dest != null) {
            try {
                PdfExportService.export(currentProgram, dest);
                showAlert(Alert.AlertType.INFORMATION, "PDF généré",
                        "Le fichier a été enregistré :\n" + dest.getAbsolutePath());
            } catch (IOException e) {
                e.printStackTrace();
                showAlert(Alert.AlertType.ERROR, "Erreur PDF",
                        "Impossible de générer le PDF : " + e.getMessage());
            }
        }
    }

    @FXML
    private void handleBack() {
        User user = SecurityController.getCurrentUser();
        if (user != null && "ADMIN".equalsIgnoreCase(user.getRole())) {
            MainController.getInstance().loadView("/com/psychologie/view/admin/AdminWellbeing.fxml");
        } else {
            MainController.getInstance().loadView("/com/psychologie/view/ProgramListView.fxml");
        }
    }

    // ─────────────────────────────────────────────
    //  DB helpers
    // ─────────────────────────────────────────────

    private boolean hasAlreadyReviewed(int userId, int programId) {
        String q = "SELECT COUNT(*) FROM avis WHERE psychologue_id_user = ? AND idProgramme = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(q)) {
            ps.setInt(1, userId);
            ps.setInt(2, programId);
            ResultSet rs = ps.executeQuery();
            return rs.next() && rs.getInt(1) > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    private void saveAvis(Avis avis, User user, int programId) {
        String q = "INSERT INTO avis (idProgramme, psychologue_id_user, note, commentaire, dateAvis) VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(q)) {
            ps.setInt(1, programId);
            ps.setInt(2, user.getId());
            ps.setInt(3, avis.getNote());
            ps.setString(4, avis.getCommentaire());
            ps.setDate(5, avis.getDateAvis() != null
                    ? java.sql.Date.valueOf(avis.getDateAvis())
                    : java.sql.Date.valueOf(LocalDate.now()));
            ps.executeUpdate();
            showAlert(Alert.AlertType.INFORMATION, "Merci !", "Votre avis a été enregistré.");
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible d'enregistrer l'avis.");
        }
    }

    // ─────────────────────────────────────────────
    //  Utility
    // ─────────────────────────────────────────────

    /** Highlights stars up to `count` in the interactive star widget. */
    private void highlightStars(Label[] stars, int count) {
        for (int i = 0; i < stars.length; i++) {
            if (i < count) {
                stars[i].setText("★");
                stars[i].setStyle("-fx-font-size: 28px; -fx-text-fill: #f5a623; -fx-cursor: hand;");
            } else {
                stars[i].setText("☆");
                stars[i].setStyle("-fx-font-size: 28px; -fx-text-fill: #cccccc; -fx-cursor: hand;");
            }
        }
    }

    private String nullSafe(String s) {
        return s == null ? "" : s;
    }

    private void showAlert(Alert.AlertType type, String title, String msg) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(msg);
        alert.showAndWait();
    }
}
