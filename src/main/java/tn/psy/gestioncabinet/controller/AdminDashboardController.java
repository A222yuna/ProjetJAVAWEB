package tn.psy.gestioncabinet.controller;

import javafx.animation.FadeTransition;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Duration;
import tn.psy.gestioncabinet.model.Cabinet;
import tn.psy.gestioncabinet.service.CabinetService;
import tn.psy.gestioncabinet.service.EmailService;
import tn.psy.gestioncabinet.util.CabinetEventBus;
import tn.psy.gestioncabinet.util.SessionManager;
import tn.psy.gestioncabinet.util.AuthGuard;

import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

/**
 * Contrôleur du dashboard Administrateur
 */
public class AdminDashboardController implements Initializable {

    @FXML private VBox rootContent;
    @FXML private Label lblWelcome;
    @FXML private Label lblTotalCabinets;
    @FXML private Label lblCabinetsValides;
    @FXML private Label lblCabinetsEnAttente;
    @FXML private TableView<Cabinet> tableCabinets;
    @FXML private TableColumn<Cabinet, Integer> colId;
    @FXML private TableColumn<Cabinet, String> colAdresse;
    @FXML private TableColumn<Cabinet, String> colVille;
    @FXML private TableColumn<Cabinet, String> colStatut;
    @FXML private Button btnValider;
    @FXML private Button btnSupprimer;
    @FXML private Button btnArchiver;
    @FXML private Button btnRafraichir; // peut être absent du FXML
    @FXML private Button btnDeconnexion;

    private final CabinetService cabinetService = new CabinetService();
    private final EmailService emailService = new EmailService();
    private final ObservableList<Cabinet> cabinetsList = FXCollections.observableArrayList();
    private Object admin;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Contrôle d'accès : uniquement Admin
        if (!AuthGuard.check(AuthGuard.RequiredRole.ADMIN)) {
            new Alert(Alert.AlertType.ERROR, "Accès non autorisé pour ce rôle.", ButtonType.OK).showAndWait();
            return;
        }

        admin = SessionManager.getInstance().getUser();
        if (admin != null) {
            lblWelcome.setText("Admin");
        }

        // Lier la TableView principale à l'ObservableList
        tableCabinets.setItems(cabinetsList);

        colId.setCellValueFactory(new PropertyValueFactory<>("idCabinet"));
        colAdresse.setCellValueFactory(new PropertyValueFactory<>("adresse"));
        colVille.setCellValueFactory(new PropertyValueFactory<>("ville"));
        colStatut.setCellValueFactory(new PropertyValueFactory<>("statutArchiveTexte"));

        // Coloration des lignes archivées (gris clair)
        tableCabinets.setRowFactory(tv -> new TableRow<>() {
            @Override
            protected void updateItem(Cabinet item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setStyle("");
                } else if (item.isArchive()) {
                    setStyle("-fx-background-color: #ecf0f1;");
                } else {
                    setStyle("");
                }
            }
        });

        btnValider.setOnAction(e -> validerCabinet());
        if (btnSupprimer != null) {
            btnSupprimer.setOnAction(e -> supprimerCabinet());
        }
        if (btnArchiver != null) {
            btnArchiver.setOnAction(e -> toggleArchiveCabinet());
        }
        if (btnRafraichir != null) {
            btnRafraichir.setOnAction(e -> chargerCabinets());
        }
        btnDeconnexion.setOnAction(e -> SessionManager.getInstance().clearSession());

        // Gestion des états des boutons selon la sélection
        tableCabinets.getSelectionModel().selectedItemProperty().addListener((obs, oldSel, newSel) -> updateButtonsState(newSel));

        // S'abonner aux événements de changement de cabinet
        CabinetEventBus.getInstance().subscribe(this::chargerCabinets);

        // Animation fade-in sur le contenu principal
        Platform.runLater(() -> {
            if (rootContent != null) {
                rootContent.setOpacity(0);
                FadeTransition ft = new FadeTransition(Duration.millis(450), rootContent);
                ft.setFromValue(0);
                ft.setToValue(1);
                ft.play();
            }
        });

        chargerCabinets();
    }

    /**
     * Charge tous les cabinets depuis la base de données (validés ou non, archivés ou non).
     */
    private void chargerCabinets() {
        List<Cabinet> cabinets = cabinetService.findAll();

        cabinetsList.clear();
        cabinetsList.addAll(cabinets);

        // Mettre à jour les statistiques (sur tous les cabinets)
        long total = cabinets.size();
        long valides = cabinets.stream().filter(Cabinet::isValide).count();
        long enAttente = total - valides;

        lblTotalCabinets.setText(String.valueOf(total));
        lblCabinetsValides.setText(String.valueOf(valides));
        lblCabinetsEnAttente.setText(String.valueOf(enAttente));

        // Réinitialiser l'état des boutons
        updateButtonsState(null);
    }

    /**
     * Méthode publique pour rafraîchir la liste depuis l'extérieur
     */
    public void refresh() {
        chargerCabinets();
    }

    private void validerCabinet() {
        Cabinet selected = tableCabinets.getSelectionModel().getSelectedItem();
        if (selected == null) {
            new Alert(Alert.AlertType.WARNING, "Veuillez sélectionner un cabinet.", ButtonType.OK).showAndWait();
            return;
        }
        if (selected.isArchive()) {
            new Alert(Alert.AlertType.INFORMATION, "Ce cabinet est archivé et ne peut plus être validé.", ButtonType.OK).showAndWait();
            return;
        }
        if (selected.isValide()) {
            new Alert(Alert.AlertType.INFORMATION, "Ce cabinet est déjà validé.", ButtonType.OK).showAndWait();
            return;
        }

        if (cabinetService.validerCabinet(selected.getIdCabinet())) {
            String nomCabinet = selected.getAdresse() != null ? selected.getAdresse() : "Cabinet " + selected.getIdCabinet();
            List<String> emails = cabinetService.getEmailsPsychologuesPourCabinet(selected.getIdCabinet());
            if (emails != null && !emails.isEmpty()) {
                for (String email : emails) {
                    if (email != null && !email.isBlank()) {
                        boolean ok = emailService.sendValidationEmail(email.trim(), nomCabinet);
                        if (!ok) {
                            System.err.println("[AdminDashboard] Échec envoi email à " + email);
                        }
                    }
                }
            } else {
                System.out.println("[AdminDashboard] Aucun psychologue associé à ce cabinet - aucun email envoyé.");
            }

            new Alert(Alert.AlertType.INFORMATION, "Cabinet validé avec succès.", ButtonType.OK).showAndWait();
            chargerCabinets();
            CabinetEventBus.getInstance().notifyCabinetChanged();
        } else {
            new Alert(Alert.AlertType.ERROR, "Erreur lors de la validation.", ButtonType.OK).showAndWait();
        }
    }

    /**
     * Bouton dynamique Archiver / Désarchiver : bascule l'état d'archivage et rafraîchit la TableView.
     */
    private void toggleArchiveCabinet() {
        Cabinet selected = tableCabinets.getSelectionModel().getSelectedItem();
        if (selected == null) {
            new Alert(Alert.AlertType.WARNING, "Veuillez sélectionner un cabinet.", ButtonType.OK).showAndWait();
            return;
        }

        if (!selected.isValide()) {
            new Alert(Alert.AlertType.INFORMATION, "Vous ne pouvez archiver/désarchiver que des cabinets validés.", ButtonType.OK).showAndWait();
            return;
        }

        String action = selected.isArchive() ? "désarchiver" : "archiver";
        ButtonType result = new Alert(Alert.AlertType.CONFIRMATION,
                "Voulez-vous " + action + " ce cabinet ?",
                ButtonType.YES, ButtonType.NO).showAndWait().orElse(ButtonType.NO);

        if (result == ButtonType.YES && cabinetService.toggleArchiveCabinet(selected.getIdCabinet())) {
            new Alert(Alert.AlertType.INFORMATION, "Cabinet " + action + " avec succès.", ButtonType.OK).showAndWait();
            chargerCabinets();
            CabinetEventBus.getInstance().notifyCabinetChanged();
        } else if (result == ButtonType.YES) {
            new Alert(Alert.AlertType.ERROR, "Impossible de " + action + " le cabinet.", ButtonType.OK).showAndWait();
        }
    }

    /**
     * Suppression d'un cabinet non validé (DELETE réel).
     * Les cabinets validés ne peuvent plus être supprimés.
     */
    private void supprimerCabinet() {
        Cabinet selected = tableCabinets.getSelectionModel().getSelectedItem();
        if (selected == null) {
            new Alert(Alert.AlertType.WARNING, "Veuillez sélectionner un cabinet.", ButtonType.OK).showAndWait();
            return;
        }

        if (selected.isValide()) {
            new Alert(Alert.AlertType.INFORMATION, "Un cabinet validé ne peut plus être supprimé.", ButtonType.OK).showAndWait();
            return;
        }

        ButtonType result = new Alert(Alert.AlertType.CONFIRMATION,
                "Êtes-vous sûr de vouloir supprimer ce cabinet ? Cette action est définitive.",
                ButtonType.YES, ButtonType.NO).showAndWait().orElse(ButtonType.NO);

        if (result == ButtonType.YES) {
            if (cabinetService.supprimerCabinet(selected.getIdCabinet(), true)) {
                new Alert(Alert.AlertType.INFORMATION, "Cabinet supprimé.", ButtonType.OK).showAndWait();
                chargerCabinets();
                CabinetEventBus.getInstance().notifyCabinetChanged();
            } else {
                new Alert(Alert.AlertType.ERROR, "Impossible de supprimer le cabinet.", ButtonType.OK).showAndWait();
            }
        }
    }

    /**
     * Met à jour l'état des boutons en fonction du cabinet sélectionné.
     * Bouton dynamique : "Archiver" si archive == false, "Désarchiver" si archive == true.
     */
    private void updateButtonsState(Cabinet selected) {
        boolean hasSelection = selected != null;

        if (!hasSelection) {
            btnValider.setDisable(true);
            if (btnSupprimer != null) {
                btnSupprimer.setDisable(true);
            }
            if (btnArchiver != null) {
                btnArchiver.setDisable(true);
                btnArchiver.setText("Archiver");
            }
            return;
        }

        boolean isValide = selected.isValide();
        boolean isArchive = selected.isArchive();

        // Validation : seulement pour cabinets non validés et non archivés
        btnValider.setDisable(isValide || isArchive);

        // Suppression uniquement si non validé
        if (btnSupprimer != null) {
            btnSupprimer.setDisable(isValide);
        }

        // Archiver / Désarchiver uniquement si validé
        if (btnArchiver != null) {
            btnArchiver.setDisable(!isValide);
            btnArchiver.setText(isArchive ? "Désarchiver" : "Archiver");
        }
    }

    @FXML
    private void deconnexion() {
        // Se désabonner de l'EventBus avant de quitter
        CabinetEventBus.getInstance().unsubscribe(this::chargerCabinets);
        SessionManager.getInstance().clearSession();
    }
}
