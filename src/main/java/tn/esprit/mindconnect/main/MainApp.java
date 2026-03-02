package tn.esprit.mindconnect.main;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class MainApp extends Application {

    @Override
    public void start(Stage stage) throws Exception {
        // Charger le FXML de MindConnect
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/tn/esprit/mindconnect/fxml/MainLayout.fxml"));

        Scene scene = new Scene(loader.load());
        stage.setTitle("MindConnect - Wellness Platform");
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
