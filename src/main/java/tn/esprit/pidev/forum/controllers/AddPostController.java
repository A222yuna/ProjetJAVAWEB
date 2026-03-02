package tn.esprit.pidev.forum.controllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.FlowPane;
import javafx.stage.Stage;
import tn.esprit.pidev.forum.entities.Post;
import tn.esprit.pidev.forum.services.PostService;

public class AddPostController {

    @FXML private Label dialogTitle;
    @FXML private TextField titreField;
    @FXML private ComboBox<String> categorieComboBox;
    @FXML private TextArea contenuArea;
    @FXML private Label charCountLabel;
    @FXML private Button emojiButton;  // ✅ NEW: Emoji button (à ajouter dans FXML)

    private PostService postService;
    private ForumController forumController;
    private Post postToEdit;
    private int currentUserId = 1;

    @FXML
    public void initialize() {
        postService = new PostService();

        // French categories
        categorieComboBox.getItems().addAll(
                "Discussion Générale",
                "Santé Mentale",
                "Addiction",
                "Social",
                "Discussion sur les Traumatismes"
        );
        categorieComboBox.getSelectionModel().selectFirst();

        // Character counter
        contenuArea.textProperty().addListener((observable, oldValue, newValue) -> {
            int length = newValue != null ? newValue.length() : 0;
            charCountLabel.setText(length + " caractères");
        });
    }

    public void setForumController(ForumController forumController) {
        this.forumController = forumController;
    }

    public void setCurrentUserId(int userId) {
        this.currentUserId = userId;
    }

    public void setPostToEdit(Post post) {
        this.postToEdit = post;
        dialogTitle.setText("Modifier le post");
        titreField.setText(post.getTitre());
        categorieComboBox.setValue(post.getCategorie());
        contenuArea.setText(post.getContenu());
    }

    /**
     * ✅ NEW: Show Emoji Picker Dialog
     */
    @FXML
    private void handleShowEmojiPicker(ActionEvent event) {
        showEmojiPicker();
    }

    private void showEmojiPicker() {
        // Liste d'emojis populaires
        String[] emojis = {
                "😊", "😂", "😍", "😢", "😭", "😡", "😱", "🥺",
                "💕", "❤️", "💪", "👍", "👎", "🙏", "💬", "🔥",
                "⭐", "✨", "🌟", "💯", "🎉", "🎊", "🤗", "🤔",
                "😴", "🤒", "🤕", "😷", "🤧", "🥴", "😵", "🤯"
        };

        Dialog<String> dialog = new Dialog<>();
        dialog.setTitle("Choisir un emoji");
        dialog.setHeaderText("Cliquez sur un emoji pour l'insérer");

        FlowPane flowPane = new FlowPane();
        flowPane.setHgap(5);
        flowPane.setVgap(5);
        flowPane.setPadding(new Insets(15));
        flowPane.setPrefWrapLength(400); // Largeur maximale

        for (String emoji : emojis) {
            Button btn = new Button(emoji);
            btn.setStyle("-fx-font-size: 24px; -fx-min-width: 50px; -fx-min-height: 50px; " +
                    "-fx-cursor: hand; -fx-background-color: #f1f2f1; " +
                    "-fx-border-color: #193764; -fx-border-radius: 5; -fx-background-radius: 5;");

            // Hover effect
            btn.setOnMouseEntered(e -> btn.setStyle(btn.getStyle() + "-fx-background-color: #193764; -fx-text-fill: white;"));
            btn.setOnMouseExited(e -> btn.setStyle(btn.getStyle().replace("-fx-background-color: #193764; -fx-text-fill: white;", "-fx-background-color: #f1f2f1;")));

            btn.setOnAction(e -> {
                insertEmoji(emoji);
                dialog.close();
            });
            flowPane.getChildren().add(btn);
        }

        ScrollPane scrollPane = new ScrollPane(flowPane);
        scrollPane.setFitToWidth(true);
        scrollPane.setPrefHeight(300);

        dialog.getDialogPane().setContent(scrollPane);
        dialog.getDialogPane().getButtonTypes().add(ButtonType.CLOSE);
        dialog.show();
    }

    private void insertEmoji(String emoji) {
        // Insérer l'emoji à la position du curseur
        int caretPosition = contenuArea.getCaretPosition();
        String currentText = contenuArea.getText();
        String newText = currentText.substring(0, caretPosition) + emoji + currentText.substring(caretPosition);
        contenuArea.setText(newText);
        contenuArea.positionCaret(caretPosition + emoji.length());
    }

    @FXML
    private void handleSave(ActionEvent event) {
        if (!validateForm()) {
            return;
        }

        try {
            if (postToEdit == null) {
                Post newPost = new Post(
                        currentUserId,
                        titreField.getText().trim(),
                        contenuArea.getText().trim(),
                        categorieComboBox.getValue()
                );

                if (postService.addPost(newPost)) {
                    showSuccess("Post publié avec succès!");
                    closeDialog();
                    if (forumController != null) {
                        forumController.refreshPosts();
                    }
                }
            } else {
                postToEdit.setTitre(titreField.getText().trim());
                postToEdit.setContenu(contenuArea.getText().trim());
                postToEdit.setCategorie(categorieComboBox.getValue());

                if (postService.updatePost(postToEdit)) {
                    showSuccess("Post modifié avec succès!");
                    closeDialog();
                    if (forumController != null) {
                        forumController.refreshPosts();
                    }
                }
            }
        } catch (Exception e) {
            showError("Erreur", "Une erreur s'est produite");
            e.printStackTrace();
        }
    }

    @FXML
    private void handleCancel(ActionEvent event) {
        closeDialog();
    }

    private boolean validateForm() {
        if (titreField.getText().trim().isEmpty()) {
            showError("Validation", "Le titre ne peut pas être vide.");
            return false;
        }

        // Nouveau: au moins 10 lettres (A‑Z, a‑z, accents inclus) et pas d'autres caractères obligatoires
        String title = titreField.getText().trim();
        if (title.length() < 10) {
            showError("Validation", "Le titre doit contenir au moins 10 lettres.");
            return false;
        }

        // Autoriser uniquement les lettres et espaces
        if (!title.matches("[\\p{L} ]+")) {
            showError("Validation", "Le titre ne doit contenir que des lettres et des espaces.");
            return false;
        }

        if (categorieComboBox.getValue() == null) {
            showError("Validation", "Veuillez sélectionner une catégorie.");
            return false;
        }

        if (contenuArea.getText().trim().isEmpty()) {
            showError("Validation", "Le contenu ne peut pas être vide.");
            return false;
        }

        if (contenuArea.getText().trim().length() < 10) {
            showError("Validation", "Le contenu doit contenir au moins 10 caractères.");
            return false;
        }

        return true;
    }

    private void closeDialog() {
        Stage stage = (Stage) titreField.getScene().getWindow();
        stage.close();
    }

    private void showError(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    private void showSuccess(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Succès");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}