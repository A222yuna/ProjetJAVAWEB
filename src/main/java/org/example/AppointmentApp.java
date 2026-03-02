package org.example;
import javafx.application.Application;
import javafx.stage.Stage;
import org.example.controller.MainController;
import org.example.util.DatabaseConnection;

public class AppointmentApp extends Application {

    @Override
    public void start(Stage primaryStage) {
        try {
            MainController.setPrimaryStage(primaryStage);

            primaryStage.setTitle("Appointment Management System");
            primaryStage.setWidth(1200);
            primaryStage.setHeight(800);

            // Show login screen
            MainController.showLogin(primaryStage);

            primaryStage.setOnCloseRequest(e -> {
                DatabaseConnection.closeConnection();
                System.exit(0);
            });

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}