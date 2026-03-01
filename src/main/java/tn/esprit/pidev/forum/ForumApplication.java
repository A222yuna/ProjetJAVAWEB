package tn.esprit.pidev.forum;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import tn.esprit.pidev.forum.utils.DatabaseConnection;

import java.io.IOException;

/**
 * Main JavaFX application for the Forum Module (psychological consultation platform).
 */
public class ForumApplication extends Application {

    @Override
    public void start(Stage stage) throws IOException {
        // Test database connection
        try {
            if (DatabaseConnection.getConnection() == null) {
                System.err.println("Could not connect to database. Check MySQL is running and forum_db exists.");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/ForumView.fxml"));
        Parent root = loader.load();
        Scene scene = new Scene(root, 1000, 700);
        stage.setTitle("Forum - Consultation Psychologique");
        stage.setScene(scene);
        stage.setMaximized(true);
        stage.show();
    }

    @Override
    public void stop() {
        DatabaseConnection.closeConnection();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
