package org.example.view;

import org.example.dao.UserDAO;
import org.example.model.User;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Separator;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;

public class RegisterView {

    public static Scene createRegisterScene(Stage primaryStage) {
        VBox root = new VBox(20);
        root.setPadding(new Insets(40));
        root.setAlignment(Pos.TOP_CENTER);
        root.setStyle("-fx-background: linear-gradient(from 0% 0% to 100% 100%, #667eea 0%, #764ba2 100%);");

        ScrollPane scrollPane = new ScrollPane();
        scrollPane.setStyle("-fx-background-color: transparent; -fx-border: none;");

        VBox cardBox = new VBox(15);
        cardBox.setPadding(new Insets(40));
        cardBox.setPrefWidth(450);
        cardBox.setMaxWidth(450);
        cardBox.setStyle("-fx-background-color: white; " +
                "-fx-border-radius: 10; " +
                "-fx-background-radius: 10; " +
                "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.3), 15, 0, 0, 5);");

        // Title
        Label titleLabel = new Label("Create Account");
        titleLabel.setFont(Font.font("Segoe UI", FontWeight.BOLD, 24));
        titleLabel.setTextFill(Color.web("#333333"));

        // Username
        Label usernameLabel = new Label("Username");
        usernameLabel.setStyle("-fx-font-size: 11; -fx-font-weight: bold;");
        TextField usernameField = new TextField();
        usernameField.setPromptText("Choose a username");
        usernameField.setStyle("-fx-padding: 8; -fx-font-size: 12;");

        // Password
        Label passwordLabel = new Label("Password");
        passwordLabel.setStyle("-fx-font-size: 11; -fx-font-weight: bold;");
        PasswordField passwordField = new PasswordField();
        passwordField.setPromptText("Enter password");
        passwordField.setStyle("-fx-padding: 8; -fx-font-size: 12;");

        // Full Name
        Label nameLabel = new Label("Full Name");
        nameLabel.setStyle("-fx-font-size: 11; -fx-font-weight: bold;");
        TextField nameField = new TextField();
        nameField.setPromptText("Enter your full name");
        nameField.setStyle("-fx-padding: 8; -fx-font-size: 12;");

        // Email
        Label emailLabel = new Label("Email");
        emailLabel.setStyle("-fx-font-size: 11; -fx-font-weight: bold;");
        TextField emailField = new TextField();
        emailField.setPromptText("Enter your email");
        emailField.setStyle("-fx-padding: 8; -fx-font-size: 12;");

        // Phone
        Label phoneLabel = new Label("Phone");
        phoneLabel.setStyle("-fx-font-size: 11; -fx-font-weight: bold;");
        TextField phoneField = new TextField();
        phoneField.setPromptText("Enter your phone number");
        phoneField.setStyle("-fx-padding: 8; -fx-font-size: 12;");

        // Role
        Label roleLabel = new Label("Role");
        roleLabel.setStyle("-fx-font-size: 11; -fx-font-weight: bold;");
        ComboBox<String> roleCombo = new ComboBox<>();
        roleCombo.getItems().addAll("PATIENT", "PSYCHOLOGUE");
        roleCombo.setValue("PATIENT");
        roleCombo.setStyle("-fx-padding: 8; -fx-font-size: 12;");

        // Error message
        Label errorLabel = new Label();
        errorLabel.setTextFill(Color.RED);
        errorLabel.setWrapText(true);
        errorLabel.setStyle("-fx-font-size: 12;");

        // Buttons
        HBox buttonBox = new HBox(10);
        buttonBox.setAlignment(Pos.CENTER);

        Button registerButton = new Button("Register");
        registerButton.setPrefWidth(130);
        registerButton.setPrefHeight(40);
        registerButton.setStyle("-fx-font-size: 13; -fx-font-weight: bold; " +
                "-fx-background-color: #667eea; -fx-text-fill: white; " +
                "-fx-border-radius: 5;");

        Button backButton = new Button("Back");
        backButton.setPrefWidth(130);
        backButton.setPrefHeight(40);
        backButton.setStyle("-fx-font-size: 13; -fx-font-weight: bold; " +
                "-fx-background-color: #f0f0f0; -fx-text-fill: #333; " +
                "-fx-border-radius: 5;");

        // Register action
        registerButton.setOnAction(e -> {
            String username = usernameField.getText();
            String password = passwordField.getText();
            String fullName = nameField.getText();
            String email = emailField.getText();
            String phone = phoneField.getText();
            String role = roleCombo.getValue();

            if (username.isEmpty() || password.isEmpty() || fullName.isEmpty()) {
                errorLabel.setText("Please fill in all required fields");
                return;
            }

            User newUser = new User(username, password, fullName, email, phone, role);
            if (UserDAO.registerUser(newUser)) {
                errorLabel.setTextFill(Color.GREEN);
                errorLabel.setText("Registration successful! Redirecting to login...");

                new Thread(() -> {
                    try {
                        Thread.sleep(2000);
                        primaryStage.setScene(LoginView.createLoginScene(primaryStage));
                    } catch (InterruptedException ex) {
                        ex.printStackTrace();
                    }
                }).start();
            } else {
                errorLabel.setTextFill(Color.RED);
                errorLabel.setText("Registration failed. Username may already exist.");
            }
        });

        // Back action
        backButton.setOnAction(e -> {
            primaryStage.setScene(LoginView.createLoginScene(primaryStage));
        });

        buttonBox.getChildren().addAll(registerButton, backButton);

        cardBox.getChildren().addAll(
                titleLabel,
                new Separator(),
                usernameLabel,
                usernameField,
                passwordLabel,
                passwordField,
                nameLabel,
                nameField,
                emailLabel,
                emailField,
                phoneLabel,
                phoneField,
                roleLabel,
                roleCombo,
                errorLabel,
                buttonBox
        );

        scrollPane.setContent(cardBox);
        scrollPane.setFitToWidth(true);
        root.getChildren().add(scrollPane);

        return new Scene(root, 800, 700);
    }
}