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
        SceneManager.setPrimaryStage(primaryStage);
        // L'initialisation de la base et la gestion des comptes sont assurées
        // par le module central (pschologie_app). Ici, on démarre sur la Home.

        FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/home.fxml"));
        Parent root = loader.load();

        Scene scene = new Scene(root, 500, 500);
        scene.getStylesheets().clear();
        scene.getStylesheets().add(getClass().getResource("/css/modern-theme.css").toExternalForm());

        primaryStage.setTitle("Plateforme Psychologie - Accueil");
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
