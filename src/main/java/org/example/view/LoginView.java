package org.example.view;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;
import org.example.controller.MainController;
import org.example.dao.UserDAO;
import org.example.model.User;
import org.example.util.session.SessionManager;

public class LoginView {

    public static Scene createLoginScene(Stage primaryStage) {
        VBox root = new VBox(20);
        root.setPadding(new Insets(40));
        root.setAlignment(Pos.CENTER);
        // use angle-free gradient syntax to avoid parser warnings
        root.setStyle("-fx-background: linear-gradient(from 0% 0% to 100% 100%, #667eea 0%, #764ba2 100%);");

        VBox cardBox = new VBox(20);
        cardBox.setPadding(new Insets(40));
        cardBox.setStyle("-fx-background-color: white; -fx-border-radius: 10; -fx-background-radius: 10;");
        cardBox.setPrefWidth(400);
        cardBox.setMaxWidth(400);
        cardBox.setStyle("-fx-background-color: white; "
                + "-fx-border-radius: 10; "
                + "-fx-background-radius: 10; "
                + "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.3), 15, 0, 0, 5);");

        // Title
        Label titleLabel = new Label("Appointment System");
        titleLabel.setFont(Font.font("Segoe UI", FontWeight.BOLD, 28));
        titleLabel.setTextFill(Color.web("#333333"));

        Label subtitleLabel = new Label("Login to your account");
        subtitleLabel.setFont(Font.font("Segoe UI", 14));
        subtitleLabel.setTextFill(Color.web("#666666"));

        // Username
        Label usernameLabel = new Label("Username");
        usernameLabel.setStyle("-fx-font-size: 12; -fx-font-weight: bold; -fx-text-fill: #333;");
        TextField usernameField = new TextField();
        usernameField.setPromptText("Enter your username");
        usernameField.setStyle("-fx-padding: 10; -fx-font-size: 14; -fx-border-radius: 5;");

        // Password
        Label passwordLabel = new Label("Password");
        passwordLabel.setStyle("-fx-font-size: 12; -fx-font-weight: bold; -fx-text-fill: #333;");
        PasswordField passwordField = new PasswordField();
        passwordField.setPromptText("Enter your password");
        passwordField.setStyle("-fx-padding: 10; -fx-font-size: 14; -fx-border-radius: 5;");

        // Error message
        Label errorLabel = new Label();
        errorLabel.setTextFill(Color.RED);
        errorLabel.setWrapText(true);

        // Buttons
        HBox buttonBox = new HBox(15);
        buttonBox.setAlignment(Pos.CENTER);

        Button loginButton = new Button("Login");
        loginButton.setPrefWidth(150);
        loginButton.setPrefHeight(45);
        loginButton.setStyle("-fx-font-size: 14; -fx-font-weight: bold; "
                + "-fx-background-color: #667eea; -fx-text-fill: white; "
                + "-fx-border-radius: 5; -fx-padding: 10;");

        Button registerButton = new Button("Register");
        registerButton.setPrefWidth(150);
        registerButton.setPrefHeight(45);
        registerButton.setStyle("-fx-font-size: 14; -fx-font-weight: bold; "
                + "-fx-background-color: #f0f0f0; -fx-text-fill: #333; "
                + "-fx-border-radius: 5; -fx-padding: 10;");

        // Login button action
        loginButton.setOnAction(e -> {
            String username = usernameField.getText();
            String password = passwordField.getText();

            if (username.isEmpty() || password.isEmpty()) {
                errorLabel.setText("Please enter username and password");
                return;
            }

            User user = UserDAO.authenticateUser(username, password);
            if (user != null) {
                SessionManager.setCurrentUser(user);
                MainController.showDashboard(primaryStage);
            } else {
                errorLabel.setText("Invalid username or password");
                passwordField.clear();
            }
        });

        // Register button action
        registerButton.setOnAction(e -> {
            primaryStage.setScene(RegisterView.createRegisterScene(primaryStage));
        });

        buttonBox.getChildren().addAll(loginButton, registerButton);

        cardBox.getChildren().addAll(
                titleLabel,
                subtitleLabel,
                new Separator(),
                usernameLabel,
                usernameField,
                passwordLabel,
                passwordField,
                errorLabel,
                buttonBox
        );

        root.getChildren().add(cardBox);

        return new Scene(root, 800, 600);
    }
}
