package tn.esprit.mindconnect.gui;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import tn.esprit.mindconnect.entities.Avis;
import tn.esprit.mindconnect.entities.ProgrammeBienEtre;
import tn.esprit.mindconnect.services.AvisService;
import tn.esprit.mindconnect.services.ProgrammeService;

import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

/**
 * Contrôleur pour l'affichage de la liste des avis.
 * Récupère les avis du service et les affiche sous forme de cartes.
 */
public class AvisController implements Initializable {

    @FXML
    private FlowPane avisContainer;
    @FXML
    private TextField searchField;
    @FXML
    private ComboBox<String> ratingFilter;
    @FXML
    private ComboBox<String> programFilter;
    @FXML
    private Button clearFiltersBtn;

    private AvisService avisService = new AvisService();
    private ProgrammeService programmeService = new ProgrammeService();
    private boolean isPsychologistMode = false;
    private List<Avis> allAvis;

    /**
     * Définit le mode d'affichage (Psychologue ou Patient) pour contrôler la
     * visibilité des actions.
     * 
     * @param isPsy True pour le mode psychologue, false pour patient.
     */
    public void setPsychologistMode(boolean isPsy) {
        this.isPsychologistMode = isPsy;
        loadAvis(); // Recharger pour appliquer les changements de visibilité si nécessaire
    }

    /**
     * Initialise la classe du contrôleur.
     * Charge les avis depuis la base de données après l'initialisation.
     */
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        setupFilters();
        setupSearchListeners();
        loadAvis();
    }

    /**
     * Configure les filtres disponibles.
     */
    private void setupFilters() {
        // Configuration du filtre de notes
        ratingFilter.getItems().addAll("Toutes notes", "5 étoiles", "4 étoiles", "3 étoiles", "2 étoiles", "1 étoile");
        ratingFilter.setValue("Toutes notes");

        // Configuration du filtre de programmes
        programFilter.getItems().addAll("Tous programmes");
        programFilter.setValue("Tous programmes");
        loadProgramNames();
    }

    /**
     * Charge les noms des programmes dans le filtre.
     */
    private void loadProgramNames() {
        try {
            List<ProgrammeBienEtre> programmes = programmeService.getAll();
            for (ProgrammeBienEtre programme : programmes) {
                programFilter.getItems().add(programme.getNom());
            }
        } catch (Exception e) {
            System.err.println("Erreur lors du chargement des programmes: " + e.getMessage());
        }
    }

    /**
     * Configure les écouteurs pour la recherche et les filtres en temps réel.
     */
    private void setupSearchListeners() {
        // Recherche en temps réel
        searchField.textProperty().addListener((observable, oldValue, newValue) -> {
            filterAndDisplayAvis();
        });

        // Filtres en temps réel
        ratingFilter.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            filterAndDisplayAvis();
        });

        programFilter.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            filterAndDisplayAvis();
        });

        // Bouton pour effacer les filtres
        clearFiltersBtn.setOnAction(e -> clearFilters());
    }

    /**
     * Récupère tous les avis du service et remplit le conteneur FlowPane.
     */
    private void loadAvis() {
        allAvis = avisService.getAll();
        filterAndDisplayAvis();
    }

    /**
     * Filtre et affiche les avis selon les critères de recherche et de filtres.
     */
    private void filterAndDisplayAvis() {
        if (allAvis == null) return;

        avisContainer.getChildren().clear();
        
        List<Avis> filteredAvis = allAvis.stream()
                .filter(this::matchesSearch)
                .filter(this::matchesRatingFilter)
                .filter(this::matchesProgramFilter)
                .collect(Collectors.toList());

        if (filteredAvis.isEmpty()) {
            Label lblEmpty = new Label("Aucun avis trouvé pour les critères sélectionnés.");
            lblEmpty.setStyle("-fx-text-fill: #8B8680;");
            avisContainer.getChildren().add(lblEmpty);
            return;
        }

        for (Avis a : filteredAvis) {
            avisContainer.getChildren().add(createAvisCard(a));
        }
    }

    /**
     * Vérifie si l'avis correspond au texte de recherche.
     */
    private boolean matchesSearch(Avis avis) {
        String searchText = searchField.getText().toLowerCase().trim();
        if (searchText.isEmpty()) return true;
        
        return avis.getCommentaire().toLowerCase().contains(searchText);
    }

    /**
     * Vérifie si l'avis correspond au filtre de note.
     */
    private boolean matchesRatingFilter(Avis avis) {
        String selectedRating = ratingFilter.getValue();
        if (selectedRating == null || selectedRating.equals("Toutes notes")) return true;
        
        try {
            int rating = Integer.parseInt(selectedRating.replace(" étoiles", ""));
            return avis.getNote() == rating;
        } catch (NumberFormatException e) {
            return true;
        }
    }

    /**
     * Vérifie si l'avis correspond au filtre de programme.
     */
    private boolean matchesProgramFilter(Avis avis) {
        String selectedProgram = programFilter.getValue();
        if (selectedProgram == null || selectedProgram.equals("Tous programmes")) return true;
        
        try {
            ProgrammeBienEtre programme = programmeService.getById(avis.getIdProgramme());
            return programme != null && programme.getNom().equals(selectedProgram);
        } catch (Exception e) {
            return true;
        }
    }

    /**
     * Efface tous les filtres et réinitialise la recherche.
     */
    private void clearFilters() {
        searchField.clear();
        ratingFilter.setValue("Toutes notes");
        programFilter.setValue("Tous programmes");
        filterAndDisplayAvis();
    }

    /**
     * Crée un composant VBox représentant un avis individuel.
     * 
     * @param a L'entité avis à afficher.
     * @return Une VBox formatée contenant les détails de l'avis.
     */
    private VBox createAvisCard(Avis a) {
        VBox card = new VBox(10);
        card.getStyleClass().add("card");
        card.setPadding(new Insets(20));
        card.setPrefWidth(300);
        card.setStyle(
                "-fx-background-color: white; -fx-background-radius: 15; -fx-padding: 20; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.05), 10, 0, 0, 5);");

        // Header avec étoiles
        HBox header = new HBox(5);
        header.setAlignment(Pos.CENTER_LEFT);
        Label lblStars = new Label("★".repeat(a.getNote()) + "☆".repeat(5 - a.getNote()));
        lblStars.setStyle("-fx-text-fill: #C9A96E; -fx-font-size: 18px;");
        header.getChildren().add(lblStars);

        // Nom du programme
        String programmeName = "Programme inconnu";
        try {
            ProgrammeBienEtre p = programmeService.getById(a.getIdProgramme());
            if (p != null)
                programmeName = p.getNom();
        } catch (Exception e) {
            System.err.println("Erreur lors de la récupération du programme: " + e.getMessage());
        }
        Label lblProg = new Label(" sur " + programmeName);
        lblProg.setStyle("-fx-text-fill: #8B8680; -fx-font-size: 13px;");
        header.getChildren().add(lblProg);

        Label lblComment = new Label(a.getCommentaire());
        lblComment.setWrapText(true);
        lblComment.setStyle("-fx-font-size: 14px; -fx-text-fill: #2A2A2A;");

        Label lblDate = new Label("Posté le " + a.getDateAvis().toString());
        lblDate.setStyle("-fx-text-fill: #8B8680; -fx-font-size: 11px;");

        card.getChildren().addAll(header, lblComment, new Separator(), lblDate);

        // Ajout des boutons Modifier et Supprimer (uniquement pour les patients)
        if (!isPsychologistMode) {
            HBox actions = new HBox(10);
            actions.setAlignment(Pos.CENTER_RIGHT);

            Button btnEdit = new Button("Modifier");
            btnEdit.getStyleClass().add("btn-secondary");
            btnEdit.setOnAction(e -> handleEditAvis(a));

            Button btnDelete = new Button("Supprimer");
            btnDelete.getStyleClass().add("btn-danger");
            btnDelete.setOnAction(e -> handleDeleteAvis(a));

            actions.getChildren().addAll(btnEdit, btnDelete);
            card.getChildren().add(actions);
        }

        return card;
    }

    /**
     * Ouvre la modale d'édition pour un avis existant.
     * 
     * @param a L'avis à modifier.
     */
    private void handleEditAvis(Avis a) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/tn/esprit/mindconnect/fxml/AddAvis.fxml"));
            Parent root = loader.load();

            AddAvisController controller = loader.getController();
            controller.setAvis(a);
            controller.setOnSaveCallback(this::loadAvis);

            Stage stage = new Stage();
            stage.setTitle("Modifier votre avis");
            stage.initModality(javafx.stage.Modality.APPLICATION_MODAL);
            Scene scene = new Scene(root);
            scene.getStylesheets().add(getClass().getResource("/tn/esprit/mindconnect/style.css").toExternalForm());
            stage.setScene(scene);
            stage.showAndWait();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Supprime un avis après confirmation de l'utilisateur.
     * 
     * @param a L'avis à supprimer.
     */
    private void handleDeleteAvis(Avis a) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirmation de suppression");
        alert.setHeaderText("Supprimer cet avis ?");
        alert.setContentText("Cette action est irréversible.");

        if (alert.showAndWait().get() == ButtonType.OK) {
            avisService.delete(a.getIdAvis());
            loadAvis();
        }
    }
}
