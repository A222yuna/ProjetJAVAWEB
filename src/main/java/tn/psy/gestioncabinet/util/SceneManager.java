package tn.psy.gestioncabinet.util;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;

/**
 * Gestionnaire de scènes JavaFX
 */
public class SceneManager {

    private static Stage primaryStage;

    public static void setPrimaryStage(Stage stage) {
        primaryStage = stage;
    }

    public static Stage getPrimaryStage() {
        return primaryStage;
    }

    public static void loadScene(String fxmlPath, String title) throws IOException {
        FXMLLoader loader = new FXMLLoader(SceneManager.class.getResource(fxmlPath));
        Parent root = loader.load();
        Scene scene = new Scene(root);
        scene.getStylesheets().clear();
        scene.getStylesheets().add(SceneManager.class.getResource("/css/modern-theme.css").toExternalForm());
        primaryStage.setScene(scene);
        primaryStage.setTitle(title);
        primaryStage.show();
    }

    public static <T> T loadSceneAndGetController(String fxmlPath, String title) throws IOException {
        FXMLLoader loader = new FXMLLoader(SceneManager.class.getResource(fxmlPath));
        Parent root = loader.load();
        Scene scene = new Scene(root);
        scene.getStylesheets().clear();
        scene.getStylesheets().add(SceneManager.class.getResource("/css/modern-theme.css").toExternalForm());
        primaryStage.setScene(scene);
        primaryStage.setTitle(title);
        primaryStage.show();
        return loader.getController();
    }

    public static void showModal(String fxmlPath, String title, Object data) throws IOException {
        FXMLLoader loader = new FXMLLoader(SceneManager.class.getResource(fxmlPath));
        Parent root = loader.load();
        Object controller = loader.getController();
        if (controller instanceof ModalController) {
            ((ModalController) controller).setData(data);
        }
        Stage stage = new Stage();
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.initOwner(primaryStage);
        Scene scene = new Scene(root);
        scene.getStylesheets().clear();
        scene.getStylesheets().add(SceneManager.class.getResource("/css/modern-theme.css").toExternalForm());
        stage.setScene(scene);
        stage.setTitle(title);
        stage.showAndWait();
    }

    public interface ModalController {
        void setData(Object data);
    }
}
