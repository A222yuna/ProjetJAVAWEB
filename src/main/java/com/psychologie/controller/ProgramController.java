package com.psychologie.controller;

import com.psychologie.model.ProgrammeBienEtre;
import com.psychologie.model.User;
import com.psychologie.util.DatabaseConnection;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.*;

import java.sql.*;
import java.time.LocalTime;

public class ProgramController {

    @FXML private FlowPane programsContainer;
    @FXML private Button newProgramBtn;

    @FXML
    public void initialize() {
        User user = SecurityController.getCurrentUser();
        boolean isPsy = user != null && "PSYCHOLOGUE".equalsIgnoreCase(user.getRole());
        newProgramBtn.setVisible(isPsy);
        newProgramBtn.setManaged(isPsy);
        loadPrograms();
    }

    private void loadPrograms() {
        programsContainer.getChildren().clear();

        User user = SecurityController.getCurrentUser();
        String query = "SELECT p.*, u.prenom, u.nom FROM programme_bien_etre p " +
                       "LEFT JOIN users u ON p.psychologue_id_user = u.id_user";
        if (user != null && "PSYCHOLOGUE".equalsIgnoreCase(user.getRole())) {
            query += " WHERE p.psychologue_id_user = ?";
        } else if (user != null && "PATIENT".equalsIgnoreCase(user.getRole())) {
            query += " WHERE p.statut = 'Publié' OR p.statut = 'ACTIF'";
        }
        query += " ORDER BY p.idProgramme DESC";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {

            if (user != null && "PSYCHOLOGUE".equalsIgnoreCase(user.getRole())) {
                pstmt.setInt(1, user.getId());
            }

            ResultSet rs = pstmt.executeQuery();
            boolean hasRows = false;
            while (rs.next()) {
                hasRows = true;
                ProgrammeBienEtre p = new ProgrammeBienEtre();
                p.setId(rs.getInt("idProgramme"));
                p.setNom(rs.getString("nom"));
                p.setObjectif(rs.getString("objectif"));
                p.setDuree(rs.getInt("duree"));
                p.setStatut(rs.getString("statut"));
                p.setNiveauDifficulte(rs.getString("niveauDifficulte"));

                User psy = new User();
                psy.setPrenom(rs.getString("prenom") != null ? rs.getString("prenom") : "Inconnu");
                psy.setNom(rs.getString("nom") != null ? rs.getString("nom") : "");
                p.setPsychologue(psy);

                programsContainer.getChildren().add(createProgramCard(p));
            }

            if (!hasRows) {
                Label empty = new Label("Aucun programme pour le moment.");
                empty.setStyle("-fx-text-fill: #5a6560; -fx-font-size: 14px;");
                programsContainer.getChildren().add(empty);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private VBox createProgramCard(ProgrammeBienEtre p) {
        VBox card = new VBox(14);
        card.getStyleClass().add("stat-card-admin");
        card.setPrefWidth(300);
        card.setMinHeight(260);
        card.setPadding(new Insets(22));

        StackPane imageBox = new StackPane(new Label("Bien-être"));
        imageBox.setStyle("-fx-background-color: #f4f6f7; -fx-background-radius: 12; -fx-min-height: 95;");

        Label title = new Label(p.getNom());
        title.setStyle("-fx-font-weight: bold; -fx-font-size: 18px;");
        title.setWrapText(true);

        Label meta = new Label(p.getDuree() + " jours · " + nullSafe(p.getNiveauDifficulte()));
        meta.setStyle("-fx-text-fill: #5a6560; -fx-font-size: 12px;");

        Label desc = new Label(nullSafe(p.getObjectif()));
        desc.setWrapText(true);
        desc.setMaxHeight(70);
        desc.setStyle("-fx-text-fill: #1c2824; -fx-font-size: 12px;");

        Label status = new Label(nullSafe(p.getStatut()).isBlank() ? "BROUILLON" : p.getStatut());
        status.getStyleClass().add(isActive(p.getStatut()) ? "badge-green" : "badge-yellow");

        HBox actions = new HBox(8);
        Button viewBtn = new Button("Voir");
        viewBtn.getStyleClass().add("btn-outline-primary");
        viewBtn.setOnAction(e -> handleViewDetails(p));
        actions.getChildren().add(viewBtn);

        User user = SecurityController.getCurrentUser();
        boolean isPatient = user != null && "PATIENT".equalsIgnoreCase(user.getRole());

        if (!isPatient) {
            Button activityBtn = new Button("+ Activité");
            activityBtn.getStyleClass().add("btn-outline-success");
            activityBtn.setOnAction(e -> handleNewActivity(p));

            Button editBtn = new Button("Modifier");
            editBtn.getStyleClass().add("btn-outline-muted");
            editBtn.setOnAction(e -> handleEditProgram(p));

            actions.getChildren().addAll(activityBtn, editBtn);
        }
        card.getChildren().addAll(imageBox, title, meta, desc, status, new Region(), actions);
        VBox.setVgrow(card.getChildren().get(5), Priority.ALWAYS);
        return card;
    }

    private boolean isActive(String status) {
        return "ACTIF".equalsIgnoreCase(status) || "Publié".equalsIgnoreCase(status);
    }

    private String nullSafe(String value) {
        return value == null ? "" : value;
    }

    private void handleViewDetails(ProgrammeBienEtre p) {
        ProgramDetailController.setProgram(p);
        MainController.getInstance().loadView("/com/psychologie/view/ProgramDetailView.fxml");
    }

    private void handleNewActivity(ProgrammeBienEtre program) {
        Dialog<Void> dialog = new Dialog<>();
        dialog.setTitle("Nouvelle activite");
        dialog.setHeaderText("Ajouter une activite a " + program.getNom());

        ButtonType saveButtonType = new ButtonType("Enregistrer", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveButtonType, ButtonType.CANCEL);

        TextField titleField = new TextField();
        TextArea descriptionArea = new TextArea();
        descriptionArea.setPrefRowCount(3);
        Spinner<Integer> daySpinner = new Spinner<>(1, Math.max(program.getDuree(), 1), 1);
        TextField timeField = new TextField("09:00");
        TextField durationField = new TextField("30");
        TextField typeField = new TextField();

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 120, 10, 10));
        grid.add(new Label("Titre:"), 0, 0);
        grid.add(titleField, 1, 0);
        grid.add(new Label("Description:"), 0, 1);
        grid.add(descriptionArea, 1, 1);
        grid.add(new Label("Jour:"), 0, 2);
        grid.add(daySpinner, 1, 2);
        grid.add(new Label("Heure debut:"), 0, 3);
        grid.add(timeField, 1, 3);
        grid.add(new Label("Duree minutes:"), 0, 4);
        grid.add(durationField, 1, 4);
        grid.add(new Label("Type:"), 0, 5);
        grid.add(typeField, 1, 5);
        dialog.getDialogPane().setContent(grid);

        Button saveButton = (Button) dialog.getDialogPane().lookupButton(saveButtonType);
        saveButton.addEventFilter(javafx.event.ActionEvent.ACTION, event -> {
            try {
                validateRequired(titleField.getText(), "Le titre est obligatoire.");
                LocalTime.parse(timeField.getText().trim());
                parsePositiveInt(durationField.getText(), "La duree doit etre positive.");
            } catch (IllegalArgumentException ex) {
                event.consume();
                showError(ex.getMessage());
            }
        });

        dialog.setResultConverter(button -> {
            if (button == saveButtonType) {
                saveActivity(program.getId(), daySpinner.getValue(), LocalTime.parse(timeField.getText().trim()),
                        titleField.getText().trim(), descriptionArea.getText().trim(),
                        parsePositiveInt(durationField.getText(), "La duree doit etre positive."),
                        typeField.getText().trim());
            }
            return null;
        });

        dialog.showAndWait();
    }

    @FXML
    private void handleNewProgram() {
        Dialog<ProgrammeBienEtre> dialog = new Dialog<>();
        dialog.setTitle("Nouveau programme");
        dialog.setHeaderText("Créer un programme bien-être");

        ButtonType saveButtonType = new ButtonType("Enregistrer", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveButtonType, ButtonType.CANCEL);

        TextField nameField = new TextField();
        TextArea objectifArea = new TextArea();
        objectifArea.setPrefRowCount(3);
        TextField durationField = new TextField("7");
        ComboBox<String> levelCombo = new ComboBox<>(FXCollections.observableArrayList("facile", "moyen", "difficile"));
        levelCombo.setValue("moyen");
        ComboBox<String> statusCombo = new ComboBox<>(FXCollections.observableArrayList("BROUILLON", "ACTIF"));
        statusCombo.setValue("BROUILLON");

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 120, 10, 10));
        grid.add(new Label("Nom:"), 0, 0);
        grid.add(nameField, 1, 0);
        grid.add(new Label("Objectif:"), 0, 1);
        grid.add(objectifArea, 1, 1);
        grid.add(new Label("Durée:"), 0, 2);
        grid.add(durationField, 1, 2);
        grid.add(new Label("Niveau:"), 0, 3);
        grid.add(levelCombo, 1, 3);
        grid.add(new Label("Statut:"), 0, 4);
        grid.add(statusCombo, 1, 4);
        dialog.getDialogPane().setContent(grid);

        dialog.setResultConverter(button -> {
            if (button == saveButtonType) {
                ProgrammeBienEtre p = new ProgrammeBienEtre();
                p.setNom(nameField.getText());
                p.setObjectif(objectifArea.getText());
                p.setDuree(Integer.parseInt(durationField.getText()));
                p.setNiveauDifficulte(levelCombo.getValue());
                p.setStatut(statusCombo.getValue());
                return p;
            }
            return null;
        });

        dialog.showAndWait().ifPresent(program -> {
            saveProgram(program);
            loadPrograms();
        });
    }

    private void handleEditProgram(ProgrammeBienEtre program) {
        Dialog<ProgrammeBienEtre> dialog = new Dialog<>();
        dialog.setTitle("Modifier programme");
        dialog.setHeaderText("Modifier le programme bien-etre");

        ButtonType saveButtonType = new ButtonType("Enregistrer", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveButtonType, ButtonType.CANCEL);

        TextField nameField = new TextField(nullSafe(program.getNom()));
        TextArea objectifArea = new TextArea(nullSafe(program.getObjectif()));
        objectifArea.setPrefRowCount(3);
        TextField durationField = new TextField(String.valueOf(program.getDuree()));
        ComboBox<String> levelCombo = new ComboBox<>(FXCollections.observableArrayList("facile", "moyen", "difficile", "Facile", "Intermediaire", "Difficile"));
        levelCombo.setValue(nullSafe(program.getNiveauDifficulte()).isBlank() ? "moyen" : program.getNiveauDifficulte());
        ComboBox<String> statusCombo = new ComboBox<>(FXCollections.observableArrayList("BROUILLON", "ACTIF", "Brouillon", "Publie"));
        statusCombo.setValue(nullSafe(program.getStatut()).isBlank() ? "BROUILLON" : program.getStatut());

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 120, 10, 10));
        grid.add(new Label("Nom:"), 0, 0);
        grid.add(nameField, 1, 0);
        grid.add(new Label("Objectif:"), 0, 1);
        grid.add(objectifArea, 1, 1);
        grid.add(new Label("Duree:"), 0, 2);
        grid.add(durationField, 1, 2);
        grid.add(new Label("Niveau:"), 0, 3);
        grid.add(levelCombo, 1, 3);
        grid.add(new Label("Statut:"), 0, 4);
        grid.add(statusCombo, 1, 4);
        dialog.getDialogPane().setContent(grid);

        Button saveButton = (Button) dialog.getDialogPane().lookupButton(saveButtonType);
        saveButton.addEventFilter(javafx.event.ActionEvent.ACTION, event -> {
            try {
                validateRequired(nameField.getText(), "Le nom est obligatoire.");
                parsePositiveInt(durationField.getText(), "La duree doit etre positive.");
            } catch (IllegalArgumentException ex) {
                event.consume();
                showError(ex.getMessage());
            }
        });

        dialog.setResultConverter(button -> {
            if (button == saveButtonType) {
                program.setNom(nameField.getText().trim());
                program.setObjectif(objectifArea.getText().trim());
                program.setDuree(parsePositiveInt(durationField.getText(), "La duree doit etre positive."));
                program.setNiveauDifficulte(levelCombo.getValue());
                program.setStatut(statusCombo.getValue());
                return program;
            }
            return null;
        });

        dialog.showAndWait().ifPresent(updatedProgram -> {
            updateProgram(updatedProgram);
            loadPrograms();
        });
    }

    private void saveProgram(ProgrammeBienEtre p) {
        User user = SecurityController.getCurrentUser();
        if (user == null) return;

        String query = "INSERT INTO programme_bien_etre (psychologue_id_user, nom, objectif, duree, statut, niveauDifficulte) VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setInt(1, user.getId());
            pstmt.setString(2, p.getNom());
            pstmt.setString(3, p.getObjectif());
            pstmt.setInt(4, p.getDuree());
            pstmt.setString(5, p.getStatut());
            pstmt.setString(6, p.getNiveauDifficulte());
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
            showError("Impossible d'enregistrer le programme.");
        }
    }

    private void updateProgram(ProgrammeBienEtre p) {
        User user = SecurityController.getCurrentUser();
        if (user == null) return;

        String query = "UPDATE programme_bien_etre SET nom = ?, objectif = ?, duree = ?, statut = ?, niveauDifficulte = ? " +
                "WHERE idProgramme = ? AND psychologue_id_user = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setString(1, p.getNom());
            pstmt.setString(2, p.getObjectif());
            pstmt.setInt(3, p.getDuree());
            pstmt.setString(4, p.getStatut());
            pstmt.setString(5, p.getNiveauDifficulte());
            pstmt.setInt(6, p.getId());
            pstmt.setInt(7, user.getId());
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
            showError("Impossible de modifier le programme.");
        }
    }

    private void saveActivity(int programId, int day, LocalTime startTime, String title, String description, int durationMinutes, String type) {
        User user = SecurityController.getCurrentUser();
        if (user == null) return;

        String query = "INSERT INTO activite_programme (idProgramme, jour, heureDebut, titre, description, dureeMinutes, typeActivite) " +
                "SELECT ?, ?, ?, ?, ?, ?, ? FROM programme_bien_etre " +
                "WHERE idProgramme = ? AND psychologue_id_user = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setInt(1, programId);
            pstmt.setInt(2, day);
            pstmt.setTime(3, Time.valueOf(startTime));
            pstmt.setString(4, title);
            pstmt.setString(5, description);
            pstmt.setInt(6, durationMinutes);
            pstmt.setString(7, type);
            pstmt.setInt(8, programId);
            pstmt.setInt(9, user.getId());
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
            showError("Impossible d'ajouter l'activite.");
        }
    }

    private void validateRequired(String value, String message) {
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException(message);
        }
    }

    private int parsePositiveInt(String value, String message) {
        try {
            int number = Integer.parseInt(value.trim());
            if (number <= 0) throw new NumberFormatException();
            return number;
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException(message);
        }
    }

    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR, message);
        alert.showAndWait();
    }
}
