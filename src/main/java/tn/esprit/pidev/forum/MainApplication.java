package tn.esprit.pidev.forum;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

/**
 * Main Application Entry Point
 * Launches the HomePage first (Integration)
 */
public class MainApplication extends Application {

    @Override
    public void start(Stage primaryStage) {
        try {
            System.out.println("🚀 Démarrage de MindConnect...");
            
            // Charger la HomePage
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/HomePage.fxml"));
            Parent root = loader.load();
            
            // Configurer la scène
            Scene scene = new Scene(root, 1400, 900);
            
            // Configurer la fenêtre
            primaryStage.setTitle("MindConnect - Plateforme de Santé Mentale");
            primaryStage.setScene(scene);
            primaryStage.setMaximized(true); // Fullscreen
            
            // Afficher
            primaryStage.show();
            
            System.out.println("✅ MindConnect démarré avec succès!");
            System.out.println("📍 HomePage affichée");
            
        } catch (Exception e) {
            System.err.println("❌ Erreur lors du démarrage de l'application!");
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}
