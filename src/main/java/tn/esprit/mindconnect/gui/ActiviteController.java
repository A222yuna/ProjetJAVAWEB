package tn.esprit.mindconnect.gui;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import tn.esprit.mindconnect.entities.ActiviteProgramme;
import tn.esprit.mindconnect.entities.ProgrammeBienEtre;
import tn.esprit.mindconnect.services.ActiviteService;
import tn.esprit.mindconnect.services.GoogleCalendarService;

import java.io.IOException;
import java.net.URL;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

/**
 * Contrôleur pour la gestion et l'affichage des activités au sein d'un
 * programme.
 * Fournit une vue chronologique des activités groupées par jour.
 */
public class ActiviteController implements Initializable {

    @FXML
    private HBox timelineContainer;
    @FXML
    private Label lblBreadcrumb;
    @FXML
    private Button btnAdd;
    @FXML
    private TextField searchField;
    @FXML
    private ComboBox<String> dayFilter;
    @FXML
    private ComboBox<String> durationFilter;
    @FXML
    private Button clearFiltersBtn;

    private ActiviteService activiteService = new ActiviteService();
    private GoogleCalendarService calendarService = new GoogleCalendarService();
    private ProgrammeBienEtre currentProgramme;
    private MainController mainController;
    private boolean isPsyMode = true;
    private List<ActiviteProgramme> allActivities;

    /**
     * Initialise la classe du contrôleur.
     * 
     * @param location  L'emplacement utilisé pour résoudre les chemins relatifs de
     *                  l'objet racine, ou null.
     * @param resources Les ressources utilisées pour localiser l'objet racine, ou
     *                  null.
     */
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        setupFilters();
        setupSearchListeners();
    }

    /**
     * Configure les filtres disponibles.
     */
    private void setupFilters() {
        // Configuration du filtre de jours
        dayFilter.getItems().addAll("Tous les jours", "Jour 1", "Jour 2", "Jour 3", "Jour 4", "Jour 5", "Jour 6", "Jour 7");
        dayFilter.setValue("Tous les jours");

        // Configuration du filtre de durée
        durationFilter.getItems().addAll("Toutes durées", "0-30 min", "30-60 min", "60-90 min", "90+ min");
        durationFilter.setValue("Toutes durées");
    }

    /**
     * Configure les écouteurs pour la recherche et les filtres en temps réel.
     */
    private void setupSearchListeners() {
        // Recherche en temps réel
        searchField.textProperty().addListener((observable, oldValue, newValue) -> {
            filterAndDisplayActivities();
        });

        // Filtres en temps réel
        dayFilter.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            filterAndDisplayActivities();
        });

        durationFilter.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            filterAndDisplayActivities();
        });

        // Bouton pour effacer les filtres
        clearFiltersBtn.setOnAction(e -> clearFilters());
    }

    /**
     * Définit le contrôleur principal pour la navigation et la communication entre
     * les vues.
     * 
     * @param mainController L'instance du contrôleur principal.
     */
    public void setMainController(MainController mainController) {
        this.mainController = mainController;
    }

    /**
     * Définit si la vue est en mode psychologue, activant les fonctionnalités de
     * modification et de suppression.
     * 
     * @param isPsy True si le mode psychologue est actif, false sinon.
     */
    public void setPsychologistMode(boolean isPsy) {
        this.isPsyMode = isPsy;
        if (btnAdd != null) {
            btnAdd.setVisible(isPsy);
            btnAdd.setManaged(isPsy);
        }
    }

    /**
     * Définit le programme actuel et charge ses activités associées.
     * 
     * @param p Le programme à afficher.
     */
    public void setProgramme(ProgrammeBienEtre p) {
        this.currentProgramme = p;
        if (lblBreadcrumb != null) {
            lblBreadcrumb.setText((isPsyMode ? "Mes Programmes > " : "Catalogues > ") + p.getNom());
        }
        loadActivities();
    }

    /**
     * Charge et affiche les activités du programme actuel, groupées par jour.
     */
    private void loadActivities() {
        if (currentProgramme == null) {
            return;
        }
        
        allActivities = activiteService.getByProgrammeId(currentProgramme.getIdProgramme());
        filterAndDisplayActivities();
    }

    /**
     * Filtre et affiche les activités selon les critères de recherche et de filtres.
     */
    private void filterAndDisplayActivities() {
        // Clear the container first
        timelineContainer.getChildren().clear();
        
        if (allActivities == null || currentProgramme == null) {
            // Show loading message or empty state
            Label lblEmpty = new Label("Chargement des activités...");
            lblEmpty.setStyle("-fx-text-fill: #8B8680; -fx-font-style: italic;");
            timelineContainer.getChildren().add(lblEmpty);
            return;
        }

        List<ActiviteProgramme> filteredActivities = allActivities.stream()
                .filter(this::matchesSearch)
                .filter(this::matchesDayFilter)
                .filter(this::matchesDurationFilter)
                .collect(Collectors.toList());

        displayFilteredActivities(filteredActivities);
    }

    /**
     * Vérifie si l'activité correspond au texte de recherche.
     */
    private boolean matchesSearch(ActiviteProgramme activity) {
        String searchText = searchField.getText().toLowerCase().trim();
        if (searchText.isEmpty()) return true;
        
        return activity.getTitre().toLowerCase().contains(searchText) ||
               activity.getDescription().toLowerCase().contains(searchText);
    }

    /**
     * Vérifie si l'activité correspond au filtre de jour.
     */
    private boolean matchesDayFilter(ActiviteProgramme activity) {
        String selectedDay = dayFilter.getValue();
        if (selectedDay == null || selectedDay.equals("Tous les jours")) return true;
        
        try {
            int dayNum = Integer.parseInt(selectedDay.replace("Jour ", ""));
            return activity.getJour() == dayNum;
        } catch (NumberFormatException e) {
            return true;
        }
    }

    /**
     * Vérifie si l'activité correspond au filtre de durée.
     */
    private boolean matchesDurationFilter(ActiviteProgramme activity) {
        String selectedDuration = durationFilter.getValue();
        if (selectedDuration == null || selectedDuration.equals("Toutes durées")) return true;
        
        int duration = activity.getDureeMinutes();
        
        switch (selectedDuration) {
            case "0-30 min":
                return duration >= 0 && duration <= 30;
            case "30-60 min":
                return duration > 30 && duration <= 60;
            case "60-90 min":
                return duration > 60 && duration <= 90;
            case "90+ min":
                return duration > 90;
            default:
                return true;
        }
    }

    /**
     * Affiche les activités filtrées groupées par jour.
     */
    private void displayFilteredActivities(List<ActiviteProgramme> filteredActivities) {
        timelineContainer.getChildren().clear();
        
        if (filteredActivities.isEmpty()) {
            Label lblEmpty = new Label("Aucune activité trouvée pour les critères sélectionnés.");
            lblEmpty.setStyle("-fx-text-fill: #8B8680; -fx-font-style: italic;");
            timelineContainer.getChildren().add(lblEmpty);
            return;
        }

        // Groupement par jour pour les activités filtrées
        Map<Integer, List<ActiviteProgramme>> groupedByDay = filteredActivities.stream()
                .collect(Collectors.groupingBy(ActiviteProgramme::getJour));

        // Afficher uniquement les jours qui ont des activités filtrées
        for (Map.Entry<Integer, List<ActiviteProgramme>> entry : groupedByDay.entrySet()) {
            int dayNum = entry.getKey();
            List<ActiviteProgramme> dayActivities = entry.getValue();
            
            VBox dayColumn = createDayColumn(dayNum, dayActivities);
            timelineContainer.getChildren().add(dayColumn);
        }
    }

    /**
     * Efface tous les filtres et réinitialise la recherche.
     */
    private void clearFilters() {
        searchField.clear();
        dayFilter.setValue("Tous les jours");
        durationFilter.setValue("Toutes durées");
        filterAndDisplayActivities();
    }

    /**
     * Crée une colonne VBox représentant un seul jour et ses activités.
     * 
     * @param dayNum       Le numéro du jour.
     * @param dayActivites La liste des activités pour ce jour.
     * @return Une VBox contenant la chronologie du jour.
     */
    private VBox createDayColumn(int dayNum, List<ActiviteProgramme> dayActivites) {
        VBox column = new VBox(15);
        column.setPrefWidth(250);
        column.setStyle("-fx-background-color: #F7F4EE; -fx-background-radius: 15; -fx-padding: 15;");

        Label lblDay = new Label("Jour " + dayNum);
        lblDay.setStyle("-fx-font-weight: bold; -fx-text-fill: #7C9A85; -fx-font-size: 16px;");
        column.getChildren().add(lblDay);

        if (dayActivites.isEmpty()) {
            Label lblEmpty = new Label("Aucune activité");
            lblEmpty.setStyle("-fx-text-fill: #8B8680; -fx-font-style: italic;");
            column.getChildren().add(lblEmpty);
        } else {
            for (ActiviteProgramme a : dayActivites) {
                VBox activityCard = createActivityCard(a);
                column.getChildren().add(activityCard);
            }
        }

        return column;
    }

    /**
     * Crée une représentation sous forme de carte d'une activité.
     * 
     * @param a L'activité à afficher.
     * @return Une VBox stylisée comme une carte d'activité.
     */
    private VBox createActivityCard(ActiviteProgramme a) {
        VBox card = new VBox(5);
        card.setStyle(
                "-fx-background-color: white; -fx-background-radius: 10; -fx-padding: 12; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.05), 10, 0, 0, 5);");

        HBox header = new HBox(10);
        Label lblTime = new Label(a.getHeureDebut().toString().substring(0, 5));
        lblTime.setStyle("-fx-text-fill: #C9A96E; -fx-font-weight: bold; -fx-font-size: 12px;");
        header.getChildren().add(lblTime);

        Region spacer = new Region();
        HBox.setHgrow(spacer, javafx.scene.layout.Priority.ALWAYS);
        header.getChildren().add(spacer);

        if (isPsyMode) {
            Button btnEdit = new Button("✏");
            btnEdit.setStyle(
                    "-fx-background-color: transparent; -fx-text-fill: #7C9A85; -fx-font-weight: bold; -fx-padding: 0 5 0 0; -fx-cursor: hand;");
            btnEdit.setOnAction(e -> handleEditActivity(a));

            Button btnDel = new Button("×");
            btnDel.setStyle(
                    "-fx-background-color: transparent; -fx-text-fill: #D4A5A0; -fx-font-weight: bold; -fx-padding: 0; -fx-cursor: hand;");
            btnDel.setOnAction(e -> handleDeleteActivity(a));
            header.getChildren().addAll(btnEdit, btnDel);
        }

        Label lblTitle = new Label(a.getTitre());
        lblTitle.setStyle("-fx-font-weight: bold; -fx-text-fill: #2A2A2A;");
        lblTitle.setWrapText(true);

        Label lblDesc = new Label(a.getDescription());
        lblDesc.setStyle("-fx-text-fill: #8B8680; -fx-font-size: 11px;");
        lblDesc.setWrapText(true);

        HBox footer = new HBox(10);
        Label lblDuration = new Label(a.getDureeMinutes() + " min");
        lblDuration.setStyle(
                "-fx-background-color: #E8E4DC; -fx-background-radius: 5; -fx-padding: 2 5; -fx-font-size: 9px;");
        footer.getChildren().add(lblDuration);

        // Add calendar button for all users (both psychologists and patients)
        Button btnCalendar = new Button("📅");
        btnCalendar.setStyle(
                "-fx-background-color: #7C9A85; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 2 6; -fx-background-radius: 5; -fx-cursor: hand; -fx-font-size: 10px;");
        btnCalendar.setOnAction(e -> handleAddActivityToCalendar(a));
        footer.getChildren().add(btnCalendar);

        card.getChildren().addAll(header, lblTitle, lblDesc, footer);
        return card;
    }

    /**
     * Gère l'action de modification d'une activité existante en ouvrant une boîte
     * de dialogue modale.
     * 
     * @param a L'activité à modifier.
     */
    private void handleEditActivity(ActiviteProgramme a) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/tn/esprit/mindconnect/fxml/AddActivite.fxml"));
            Parent root = loader.load();

            AddActiviteController controller = loader.getController();
            controller.setProgramme(currentProgramme);
            controller.setEditMode(a);
            controller.setOnSaveCallback(this::loadActivities);

            Stage stage = new Stage();
            stage.setTitle("Modifier l'Activité");
            stage.initModality(Modality.APPLICATION_MODAL);
            Scene scene = new Scene(root);
            scene.getStylesheets().add(getClass().getResource("/tn/esprit/mindconnect/style.css").toExternalForm());
            stage.setScene(scene);
            stage.showAndWait();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Gère l'action de suppression d'une activité après confirmation de
     * l'utilisateur.
     * 
     * @param a L'activité à supprimer.
     */
    private void handleDeleteActivity(ActiviteProgramme a) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Suppression");
        alert.setHeaderText("Supprimer l'activité : " + a.getTitre());
        alert.setContentText("Êtes-vous sûr ?");

        if (alert.showAndWait().get() == ButtonType.OK) {
            activiteService.delete(a.getIdActivite());
            loadActivities();
        }
    }

    /**
     * Retourne à la vue principale des programmes.
     */
    @FXML
    private void handleBack() {
        if (mainController != null) {
            // Utiliser navigateTo pour recharger la vue des programmes avec le bon mode
            mainController.navigateTo("/tn/esprit/mindconnect/fxml/Programmes.fxml", null);
        }
    }

    /**
     * Gère l'action d'ajout d'une nouvelle activité en ouvrant une boîte de
     * dialogue modale.
     */
    @FXML
    private void handleAddActivity() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/tn/esprit/mindconnect/fxml/AddActivite.fxml"));
            Parent root = loader.load();

            AddActiviteController controller = loader.getController();
            controller.setProgramme(currentProgramme);
            controller.setOnSaveCallback(this::loadActivities);

            Stage stage = new Stage();
            stage.setTitle("Nouvelle Activité");
            stage.initModality(Modality.APPLICATION_MODAL);
            Scene scene = new Scene(root);
            scene.getStylesheets().add(getClass().getResource("/tn/esprit/mindconnect/style.css").toExternalForm());
            stage.setScene(scene);
            stage.showAndWait();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Gère l'ajout d'une activité spécifique au calendrier Google.
     * 
     * @param activity L'activité à ajouter au calendrier.
     */
    private void handleAddActivityToCalendar(ActiviteProgramme activity) {
        try {
            // Calculer la date de l'activité (jour X à partir de demain)
            LocalDateTime activityDate = LocalDateTime.now()
                .plusDays(1)
                .plusDays(activity.getJour() - 1)
                .withHour(activity.getHeureDebut().toLocalTime().getHour())
                .withMinute(activity.getHeureDebut().toLocalTime().getMinute());
            
            LocalDateTime endTime = activityDate.plusMinutes(activity.getDureeMinutes());
            
            // Créer le titre avec le nom du programme et de l'activité
            String eventTitle = currentProgramme.getNom() + " - " + activity.getTitre();
            
            // Créer la description avec tous les détails
            String description = String.format(
                "Programme: %s\n" +
                "Jour: %d\n" +
                "Activité: %s\n" +
                "Durée: %d minutes\n" +
                "Description: %s\n\n" +
                "Programme de bien-être MindConnect",
                currentProgramme.getNom(),
                activity.getJour(),
                activity.getTitre(),
                activity.getDureeMinutes(),
                activity.getDescription()
            );
            
            // Ajouter au calendrier
            calendarService.addToGoogleCalendar(
                eventTitle,
                description,
                activityDate,
                endTime,
                "MindConnect - " + currentProgramme.getNom()
            );
            
            // Message de confirmation
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Calendrier Google");
            alert.setHeaderText("Activité ajoutée avec succès !");
            alert.setContentText("L'activité \"" + activity.getTitre() + "\" a été ajoutée à votre calendrier Google. Votre navigateur devrait s'ouvrir automatiquement.");
            alert.showAndWait();
            
        } catch (Exception e) {
            System.err.println("Erreur lors de l'ajout de l'activité au calendrier: " + e.getMessage());
            Alert errorAlert = new Alert(Alert.AlertType.ERROR);
            errorAlert.setTitle("Erreur");
            errorAlert.setHeaderText("Erreur lors de l'ajout au calendrier");
            errorAlert.setContentText("Une erreur est survenue: " + e.getMessage() + "\nVeuillez réessayer plus tard.");
            errorAlert.showAndWait();
        }
    }
}
