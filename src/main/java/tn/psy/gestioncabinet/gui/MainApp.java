package tn.psy.gestioncabinet.gui;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.stage.Stage;
import tn.psy.gestioncabinet.util.Connexion;
import tn.psy.gestioncabinet.util.DbInitializer;
import tn.psy.gestioncabinet.util.SceneManager;

import java.io.IOException;


public class MainApp extends Application {

    @Override
    public void start(Stage primaryStage) throws IOException {
<<<<<<< HEAD
        // Initialiser les données de test
        DbInitializer.initialize();

        SceneManager.setPrimaryStage(primaryStage);

        // Afficher une alerte si la base de données n'existe pas
        if (!DbInitializer.isConnectionOk()) {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("Base de données");
            alert.setHeaderText("Connexion impossible");
            alert.setContentText("La base de données 'gestion_cabinet_db' n'existe pas.\n\n" +
                    "Pour la créer :\n" +
                    "1. Ouvrez phpMyAdmin (http://localhost/phpmyadmin)\n" +
                    "2. Onglet SQL\n" +
                    "3. Copiez et exécutez le contenu du fichier database/schema.sql\n\n" +
                    "Redémarrez l'application ensuite.");
            alert.showAndWait();
        }

        FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/login.fxml"));
=======
        SceneManager.setPrimaryStage(primaryStage);
        // L'initialisation de la base et la gestion des comptes sont assurées
        // par le module central (pschologie_app). Ici, on démarre sur la Home.

        FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/home.fxml"));
>>>>>>> origin/gestion-cabinet
        Parent root = loader.load();

        Scene scene = new Scene(root, 500, 500);
        scene.getStylesheets().clear();
        scene.getStylesheets().add(getClass().getResource("/css/modern-theme.css").toExternalForm());

<<<<<<< HEAD
        primaryStage.setTitle("Gestion Cabinet - Connexion");
=======
        primaryStage.setTitle("Plateforme Psychologie - Accueil");
>>>>>>> origin/gestion-cabinet
        primaryStage.setScene(scene);
        primaryStage.setMinWidth(500);
        primaryStage.setMinHeight(500);
        primaryStage.centerOnScreen();

        primaryStage.setOnCloseRequest(event -> Connexion.fermerConnexion());

        primaryStage.show();
    }

    @Override
    public void stop() {
        Connexion.fermerConnexion();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
