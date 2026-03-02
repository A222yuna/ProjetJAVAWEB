package com.psychoapp.view;

import com.psychoapp.util.DatabaseConnection;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class MainApp extends Application {

    private static Stage primaryStage;

    @Override
    public void start(Stage stage) throws Exception {
        primaryStage = stage;
        showLogin();
        primaryStage.setResizable(false);
        primaryStage.show();
    }

    public static void showLogin() throws Exception {
        Parent root = FXMLLoader.load(
                MainApp.class.getResource("/fxml/Login.fxml"));
        Scene scene = new Scene(root, 420, 520);
        scene.getStylesheets().add(
                MainApp.class.getResource("/css/style.css").toExternalForm());
        primaryStage.setTitle("PsychoApp — Connexion");
        primaryStage.setScene(scene);
    }

    public static void showRegister() throws Exception {
        Parent root = FXMLLoader.load(
                MainApp.class.getResource("/fxml/Register.fxml"));
        Scene scene = new Scene(root, 420, 620);
        scene.getStylesheets().add(
                MainApp.class.getResource("/css/style.css").toExternalForm());
        primaryStage.setTitle("PsychoApp — Inscription");
        primaryStage.setScene(scene);
    }

    public static void showDashboard() throws Exception {
        Parent root = FXMLLoader.load(
                MainApp.class.getResource("/fxml/UtilisateurList.fxml"));
        Scene scene = new Scene(root, 960, 640);
        scene.getStylesheets().add(
                MainApp.class.getResource("/css/style.css").toExternalForm());
        primaryStage.setTitle("PsychoApp — Gestion des Utilisateurs");
        primaryStage.setResizable(true);
        primaryStage.setScene(scene);
    }

    @Override
    public void stop() {
        DatabaseConnection.closeConnection();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
