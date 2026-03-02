package tn.esprit.mindconnect.gui;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import tn.esprit.mindconnect.entities.ProgrammeBienEtre;
import tn.esprit.mindconnect.services.ProgrammeService;

import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

/**
 * Contrôleur pour la vue de la liste des programmes.
 * Affiche une grille de programmes de bien-être avec des capacités de filtrage
 * et de gestion.
 */
public class ProgrammeController implements Initializable {

    @FXML
    private GridPane programmesGrid;
    @FXML
    private Button btnAddProgramme;
    @FXML
    private TextField searchField;
    @FXML
    private ComboBox<String> filterStatut;
    @FXML
    private Button clearFiltersBtn;

    private ProgrammeService programmeService = new ProgrammeService();
    private MainController mainController;
    private boolean isPsychologistMode = true;
    private List<ProgrammeBienEtre> allProgrammes;

    /**
     * Initialise la classe du contrôleur.
     * Charge la liste initiale des programmes.
     */
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        setupFilters();
        setupSearchListeners();
        // loadProgrammes() sera appelé après setPsychologistMode si nécessaire,
        // mais ici nous l'appelons par défaut et il s'actualisera quand le mode sera
        // défini.
        loadProgrammes();
    }

    /**
     * Configure les filtres disponibles.
     */
    private void setupFilters() {
        // Configuration du filtre de statut
        filterStatut.getItems().addAll("Tous les statuts", "Actif", "Inactif", "En cours");
        filterStatut.setValue("Tous les statuts");
    }

    /**
     * Configure les écouteurs pour la recherche et les filtres en temps réel.
     */
    private void setupSearchListeners() {
        // Recherche en temps réel
        searchField.textProperty().addListener((observable, oldValue, newValue) -> {
            filterAndDisplayProgrammes();
        });

        // Filtres en temps réel
        filterStatut.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            filterAndDisplayProgrammes();
        });

        // Bouton pour effacer les filtres
        clearFiltersBtn.setOnAction(e -> clearFilters());
    }

    /**
     * Efface tous les filtres et réinitialise la recherche.
     */
    private void clearFilters() {
        searchField.clear();
        filterStatut.setValue("Tous les statuts");
        filterAndDisplayProgrammes();
    }

    /**
     * Définit le contrôleur principal pour permettre la navigation entre les vues.
     * 
     * @param mainController L'instance du contrôleur principal.
     */
    public void setMainController(MainController mainController) {
        this.mainController = mainController;
    }

    /**
     * Configure le mode de vue (Psychologue ou Patient) et actualise les labels ou
     * boutons en conséquence.
     * 
     * @param isPsy True pour le mode psychologue, false sinon.
     */
    public void setPsychologistMode(boolean isPsy) {
        this.isPsychologistMode = isPsy;
        if (btnAddProgramme != null) {
            btnAddProgramme.setVisible(isPsy);
            btnAddProgramme.setManaged(isPsy);
        }
        loadProgrammes(); // Actualise avec la bonne visibilité des boutons
    }

    /**
     * Charge tous les programmes du service et les stocke pour le filtrage.
     */
    public void loadProgrammes() {
        allProgrammes = programmeService.getAll();
        filterAndDisplayProgrammes();
    }

    /**
     * Filtre et affiche les programmes selon les critères de recherche et de filtres.
     */
    private void filterAndDisplayProgrammes() {
        programmesGrid.getChildren().clear();
        
        if (allProgrammes == null) {
            return;
        }

        List<ProgrammeBienEtre> filteredProgrammes = allProgrammes.stream()
                .filter(this::matchesSearch)
                .filter(this::matchesStatusFilter)
                .collect(Collectors.toList());

        int column = 0;
        int row = 0;

        try {
            for (ProgrammeBienEtre p : filteredProgrammes) {
                FXMLLoader fxmlLoader = new FXMLLoader();
                fxmlLoader.setLocation(getClass().getResource("/tn/esprit/mindconnect/fxml/ItemProgramme.fxml"));
                VBox card = fxmlLoader.load();

                ItemProgrammeController itemController = fxmlLoader.getController();
                itemController.setData(p, this, isPsychologistMode);

                if (column == 3) {
                    column = 0;
                    row++;
                }

                programmesGrid.add(card, column++, row);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Vérifie si le programme correspond au texte de recherche.
     */
    private boolean matchesSearch(ProgrammeBienEtre programme) {
        String searchText = searchField.getText().toLowerCase().trim();
        if (searchText.isEmpty()) return true;
        
        return programme.getNom().toLowerCase().contains(searchText) ||
               programme.getObjectif().toLowerCase().contains(searchText);
    }

    /**
     * Vérifie si le programme correspond au filtre de statut.
     */
    private boolean matchesStatusFilter(ProgrammeBienEtre programme) {
        String selectedStatus = filterStatut.getValue();
        if (selectedStatus == null || selectedStatus.equals("Tous les statuts")) return true;
        
        return selectedStatus.equalsIgnoreCase(programme.getStatut());
    }

    /**
     * Navigue vers la vue de la chronologie des activités pour un programme
     * spécifique.
     * 
     * @param p L'entité programme.
     */
    public void showActivities(ProgrammeBienEtre p) {
        if (mainController != null) {
            mainController.navigateTo("/tn/esprit/mindconnect/fxml/Activities.fxml", p);
        }
    }

    /**
     * Ouvre une boîte de dialogue modale pour qu'un patient puisse soumettre un
     * avis sur un programme.
     * 
     * @param p Le programme à évaluer.
     */
    public void openAvisModal(ProgrammeBienEtre p) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/tn/esprit/mindconnect/fxml/AddAvis.fxml"));
            Parent root = loader.load();

            AddAvisController controller = loader.getController();
            controller.setProgramme(p);
            controller.setOnSaveCallback(this::loadProgrammes); // Actualise si nécessaire (par exemple pour mettre à
                                                                // jour la note moyenne)

            Stage stage = new Stage();
            stage.setTitle("Donner votre avis");
            stage.initModality(javafx.stage.Modality.APPLICATION_MODAL);
            Scene scene = new javafx.scene.Scene(root);
            scene.getStylesheets().add(getClass().getResource("/tn/esprit/mindconnect/style.css").toExternalForm());
            stage.setScene(scene);
            stage.showAndWait();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Gère l'action d'ajout d'un nouveau programme en ouvrant une boîte de dialogue
     * modale.
     */
    @FXML
    private void handleAddProgramme() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/tn/esprit/mindconnect/fxml/AddProgramme.fxml"));
            Parent root = loader.load();

            AddProgrammeController controller = loader.getController();
            controller.setOnSaveCallback(this::loadProgrammes);

            Stage stage = new Stage();
            stage.setTitle("Nouveau Programme");
            stage.initModality(javafx.stage.Modality.APPLICATION_MODAL);
            Scene scene = new javafx.scene.Scene(root);
            scene.getStylesheets().add(getClass().getResource("/tn/esprit/mindconnect/style.css").toExternalForm());
            stage.setScene(scene);
            stage.showAndWait();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Ouvre une boîte de dialogue modale pour modifier un programme existant.
     * 
     * @param p Le programme à modifier.
     */
    public void openEditModal(ProgrammeBienEtre p) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/tn/esprit/mindconnect/fxml/AddProgramme.fxml"));
            Parent root = loader.load();

            AddProgrammeController controller = loader.getController();
            controller.setEditMode(p);
            controller.setOnSaveCallback(this::loadProgrammes);

            Stage stage = new Stage();
            stage.setTitle("Modifier le Programme");
            stage.initModality(javafx.stage.Modality.APPLICATION_MODAL);
            Scene scene = new javafx.scene.Scene(root);
            scene.getStylesheets().add(getClass().getResource("/tn/esprit/mindconnect/style.css").toExternalForm());
            stage.setScene(scene);
            stage.showAndWait();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
