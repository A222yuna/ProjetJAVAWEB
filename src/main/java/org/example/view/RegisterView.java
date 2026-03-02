package org.example.view;

import org.example.dao.UserDAO;
import org.example.model.User;
import org.example.util.ValidationUtil;

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
        root.setStyle("-fx-background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);");

        ScrollPane scrollPane = new ScrollPane();
        scrollPane.setStyle("-fx-background-color: transparent; -fx-border: none;");

        VBox cardBox = new VBox(15);
        cardBox.setPadding(new Insets(40));
        cardBox.setPrefWidth(450);
        cardBox.setMaxWidth(450);
        cardBox.setStyle("-fx-background-color: white; "
                + "-fx-border-radius: 10; "
                + "-fx-background-radius: 10; "
                + "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.3), 15, 0, 0, 5);");

        // ── Title ──────────────────────────────────────────────────────────────
        Label titleLabel = new Label("Create Account");
        titleLabel.setFont(Font.font("Segoe UI", FontWeight.BOLD, 24));
        titleLabel.setTextFill(Color.web("#333333"));

        // ── Fields ────────────────────────────────────────────────────────────
        Label usernameLabel = new Label("Username *");
        usernameLabel.setStyle("-fx-font-size: 11; -fx-font-weight: bold;");
        TextField usernameField = new TextField();
        usernameField.setPromptText("Choose a username (3-50 chars, letters/digits/_)");
        usernameField.setStyle("-fx-padding: 8; -fx-font-size: 12;");
        Label usernameError = makeErrorLabel();

        Label passwordLabel = new Label("Password *");
        passwordLabel.setStyle("-fx-font-size: 11; -fx-font-weight: bold;");
        PasswordField passwordField = new PasswordField();
        passwordField.setPromptText("At least 6 characters");
        passwordField.setStyle("-fx-padding: 8; -fx-font-size: 12;");
        Label passwordError = makeErrorLabel();

        Label nameLabel = new Label("Full Name *");
        nameLabel.setStyle("-fx-font-size: 11; -fx-font-weight: bold;");
        TextField nameField = new TextField();
        nameField.setPromptText("Enter your full name");
        nameField.setStyle("-fx-padding: 8; -fx-font-size: 12;");
        Label nameError = makeErrorLabel();

        Label emailLabel = new Label("Email (optional)");
        emailLabel.setStyle("-fx-font-size: 11; -fx-font-weight: bold;");
        TextField emailField = new TextField();
        emailField.setPromptText("name@example.com");
        emailField.setStyle("-fx-padding: 8; -fx-font-size: 12;");
        Label emailError = makeErrorLabel();

        Label phoneLabel = new Label("Phone (optional)");
        phoneLabel.setStyle("-fx-font-size: 11; -fx-font-weight: bold;");
        TextField phoneField = new TextField();
        phoneField.setPromptText("+216 50 123 456");
        phoneField.setStyle("-fx-padding: 8; -fx-font-size: 12;");
        Label phoneError = makeErrorLabel();

        Label roleLabel = new Label("Role *");
        roleLabel.setStyle("-fx-font-size: 11; -fx-font-weight: bold;");
        ComboBox<String> roleCombo = new ComboBox<>();
        roleCombo.getItems().addAll("PATIENT", "PSYCHOLOGUE");
        roleCombo.setValue("PATIENT");
        roleCombo.setStyle("-fx-padding: 8; -fx-font-size: 12;");

        // ── Inline live validation on focus-lost ──────────────────────────────
        usernameField.focusedProperty().addListener((obs, wasFocused, isNowFocused) -> {
            if (!isNowFocused) {
                showInlineError(usernameField, usernameError,
                        ValidationUtil.validateUsername(usernameField.getText()));
            }
        });

        passwordField.focusedProperty().addListener((obs, wasFocused, isNowFocused) -> {
            if (!isNowFocused) {
                showInlineError(passwordField, passwordError,
                        ValidationUtil.validatePassword(passwordField.getText()));
            }
        });

        nameField.focusedProperty().addListener((obs, wasFocused, isNowFocused) -> {
            if (!isNowFocused) {
                showInlineError(nameField, nameError,
                        ValidationUtil.validateFullName(nameField.getText()));
            }
        });

        emailField.focusedProperty().addListener((obs, wasFocused, isNowFocused) -> {
            if (!isNowFocused) {
                showInlineError(emailField, emailError,
                        ValidationUtil.validateEmail(emailField.getText()));
            }
        });

        phoneField.focusedProperty().addListener((obs, wasFocused, isNowFocused) -> {
            if (!isNowFocused) {
                showInlineError(phoneField, phoneError,
                        ValidationUtil.validatePhone(phoneField.getText()));
            }
        });

        // ── Global status label ───────────────────────────────────────────────
        Label statusLabel = new Label();
        statusLabel.setWrapText(true);
        statusLabel.setStyle("-fx-font-size: 12;");

        // ── Buttons ───────────────────────────────────────────────────────────
        HBox buttonBox = new HBox(10);
        buttonBox.setAlignment(Pos.CENTER);

        Button registerButton = new Button("Register");
        registerButton.setPrefWidth(130);
        registerButton.setPrefHeight(40);
        registerButton.setStyle("-fx-font-size: 13; -fx-font-weight: bold; "
                + "-fx-background-color: #667eea; -fx-text-fill: white; -fx-border-radius: 5;");

        Button backButton = new Button("Back");
        backButton.setPrefWidth(130);
        backButton.setPrefHeight(40);
        backButton.setStyle("-fx-font-size: 13; -fx-font-weight: bold; "
                + "-fx-background-color: #f0f0f0; -fx-text-fill: #333; -fx-border-radius: 5;");

        // ── Register action ───────────────────────────────────────────────────
        registerButton.setOnAction(e -> {
            String username = usernameField.getText().trim();
            String password = passwordField.getText();
            String fullName = nameField.getText().trim();
            String email = emailField.getText().trim();
            String phone = phoneField.getText().trim();
            String role = roleCombo.getValue();

            // Run all validators and collect errors
            boolean hasError = false;

            String uErr = ValidationUtil.validateUsername(username);
            showInlineError(usernameField, usernameError, uErr);
            if (uErr != null) {
                hasError = true;
            }

            String pErr = ValidationUtil.validatePassword(password);
            showInlineError(passwordField, passwordError, pErr);
            if (pErr != null) {
                hasError = true;
            }

            String nErr = ValidationUtil.validateFullName(fullName);
            showInlineError(nameField, nameError, nErr);
            if (nErr != null) {
                hasError = true;
            }

            String eErr = ValidationUtil.validateEmail(email);
            showInlineError(emailField, emailError, eErr);
            if (eErr != null) {
                hasError = true;
            }

            String phErr = ValidationUtil.validatePhone(phone);
            showInlineError(phoneField, phoneError, phErr);
            if (phErr != null) {
                hasError = true;
            }

            if (hasError) {
                statusLabel.setTextFill(Color.RED);
                statusLabel.setText("Please fix the errors above before registering.");
                return;
            }

            // ── All valid → try to persist ─────────────────────────────────
            User newUser = new User(username, password, fullName,
                    email.isEmpty() ? null : email,
                    phone.isEmpty() ? null : phone,
                    role);

            if (UserDAO.registerUser(newUser)) {
                // Disable button to prevent double-submit
                registerButton.setDisable(true);
                statusLabel.setTextFill(Color.GREEN);
                statusLabel.setText("✓ Registration successful! Redirecting to login…");

                new Thread(() -> {
                    try {
                        Thread.sleep(2000);
                        javafx.application.Platform.runLater(()
                                -> primaryStage.setScene(LoginView.createLoginScene(primaryStage)));
                    } catch (InterruptedException ex) {
                        ex.printStackTrace();
                    }
                }).start();
            } else {
                statusLabel.setTextFill(Color.RED);
                statusLabel.setText("Registration failed. Username or email may already exist.");
            }
        });

        backButton.setOnAction(e
                -> primaryStage.setScene(LoginView.createLoginScene(primaryStage)));

        buttonBox.getChildren().addAll(registerButton, backButton);

        // ── Assemble card ─────────────────────────────────────────────────────
        cardBox.getChildren().addAll(
                titleLabel, new Separator(),
                usernameLabel, usernameField, usernameError,
                passwordLabel, passwordField, passwordError,
                nameLabel, nameField, nameError,
                emailLabel, emailField, emailError,
                phoneLabel, phoneField, phoneError,
                roleLabel, roleCombo,
                statusLabel,
                buttonBox
        );

        scrollPane.setContent(cardBox);
        scrollPane.setFitToWidth(true);
        root.getChildren().add(scrollPane);

        return new Scene(root, 800, 750);
    }

    // ── Helpers ───────────────────────────────────────────────────────────────
    /**
     * Creates a small, initially-invisible error label below a field.
     */
    private static Label makeErrorLabel() {
        Label lbl = new Label();
        lbl.setTextFill(Color.RED);
        lbl.setStyle("-fx-font-size: 10;");
        lbl.setWrapText(true);
        lbl.setVisible(false);
        lbl.setManaged(false);
        return lbl;
    }

    /**
     * Shows or hides the inline error label and applies a red border to the
     * field when there is an error.
     *
     * @param field the TextField being validated
     * @param lbl the Label that displays the error message
     * @param error the error string (null = valid)
     */
    private static void showInlineError(TextField field, Label lbl, String error) {
        if (error != null) {
            lbl.setText(error);
            lbl.setVisible(true);
            lbl.setManaged(true);
            field.setStyle("-fx-padding: 8; -fx-font-size: 12; "
                    + "-fx-border-color: red; -fx-border-width: 1.5; -fx-border-radius: 3;");
        } else {
            lbl.setVisible(false);
            lbl.setManaged(false);
            field.setStyle("-fx-padding: 8; -fx-font-size: 12; "
                    + "-fx-border-color: #51cf66; -fx-border-width: 1.5; -fx-border-radius: 3;");
        }
    }
}
