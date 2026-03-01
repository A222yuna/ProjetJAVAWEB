package tn.esprit.pidev.forum.controllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import tn.esprit.pidev.forum.entities.Commentaire;
import tn.esprit.pidev.forum.entities.Post;
import tn.esprit.pidev.forum.services.CommentaireService;
import tn.esprit.pidev.forum.services.PostService;

import java.time.format.DateTimeFormatter;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class PostDetailsController {

    @FXML private Label titleHeaderLabel;
    @FXML private Label categorieLabel;
    @FXML private Label dateLabel;
    @FXML private Label titreLabel;
    @FXML private Label auteurLabel;
    @FXML private Label contenuLabel;
    @FXML private Label editedLabel;
    @FXML private Button likeButton;
    @FXML private Label likesLabel;
    @FXML private Label commentsCountLabel;
    @FXML private TextArea commentArea;
    @FXML private VBox commentsContainer;

    private Post currentPost;
    private PostService postService;
    private CommentaireService commentaireService;
    private ForumController forumController;
    private int currentUserId = 1;

    private Set<Integer> likedPosts = new HashSet<>();
    private Set<Integer> likedComments = new HashSet<>();

    @FXML
    public void initialize() {
        postService = new PostService();
        commentaireService = new CommentaireService();
    }

    public void setPost(Post post) {
        this.currentPost = post;
        displayPostDetails();
        loadComments();
    }

    public void setForumController(ForumController forumController) {
        this.forumController = forumController;
    }

    public void setCurrentUserId(int userId) {
        this.currentUserId = userId;
    }

    public void setLikedPosts(Set<Integer> likedPosts) {
        this.likedPosts = likedPosts;
    }

    private void displayPostDetails() {
        if (currentPost != null) {
            titleHeaderLabel.setText(currentPost.getTitre());
            categorieLabel.setText(currentPost.getCategorie());

            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
            dateLabel.setText(currentPost.getDate().format(formatter));

            titreLabel.setText(currentPost.getTitre());
            auteurLabel.setText("Auteur #" + currentPost.getId_auteur());
            contenuLabel.setText(currentPost.getContenu());
            likesLabel.setText("❤️ " + currentPost.getNb_likes());

            if (likedPosts.contains(currentPost.getId_post())) {
                likeButton.setStyle(likeButton.getStyle() + "-fx-opacity: 0.7;");
            }
        }
    }

    /**
     * ✅ NEW: Load comments with threading support
     */
    private void loadComments() {
        commentsContainer.getChildren().clear();

        // Get only top-level comments
        List<Commentaire> topLevelComments = commentaireService.getTopLevelComments(currentPost.getId_post());

        // Count ALL comments (including replies)
        int totalComments = commentaireService.getCommentairesByPost(currentPost.getId_post()).size();
        commentsCountLabel.setText("💬 " + totalComments);

        if (topLevelComments.isEmpty()) {
            Label noComments = new Label("Aucun commentaire pour le moment.");
            noComments.setStyle("-fx-text-fill: #816057; -fx-font-style: italic; -fx-font-size: 15px; " +
                    "-fx-padding: 20; -fx-font-family: 'Georgia';");
            commentsContainer.getChildren().add(noComments);
        } else {
            for (Commentaire comment : topLevelComments) {
                VBox commentWithReplies = createCommentWithReplies(comment);
                commentsContainer.getChildren().add(commentWithReplies);
            }
        }
    }

    /**
     * ✅ NEW: Create comment card with its replies (threading)
     */
    private VBox createCommentWithReplies(Commentaire comment) {
        VBox container = new VBox(10);
        container.setMaxWidth(700);

        // Main comment
        VBox commentCard = createCommentCard(comment);
        container.getChildren().add(commentCard);

        // Replies (indented)
        List<Commentaire> replies = commentaireService.getReplies(comment.getId_comment(), currentPost.getId_post());
        if (!replies.isEmpty()) {
            VBox repliesBox = new VBox(8);
            repliesBox.setStyle("-fx-padding: 0 0 0 40;"); // Indent left

            for (Commentaire reply : replies) {
                VBox replyCard = createReplyCard(reply);
                repliesBox.getChildren().add(replyCard);
            }

            container.getChildren().add(repliesBox);
        }

        return container;
    }

    private VBox createCommentCard(Commentaire comment) {
        VBox commentCard = new VBox(12);
        commentCard.setMaxWidth(700);
        commentCard.setStyle("-fx-background-color: #e7e6e4; -fx-background-radius: 10; -fx-padding: 20;");

        Label authorLabel = new Label("Auteur #" + comment.getId_auteur());
        authorLabel.setStyle("-fx-text-fill: #816057; -fx-font-size: 14px; -fx-font-family: 'Georgia'; -fx-font-weight: 600;");

        Label contentLabel = new Label(comment.getContenu());
        contentLabel.setWrapText(true);
        contentLabel.setStyle("-fx-text-fill: #364e5b; -fx-font-size: 16px; -fx-font-family: 'Georgia';");

        HBox actions = new HBox(12);
        actions.setAlignment(Pos.CENTER_LEFT);

        // Like button
        Button likeBtn = new Button("❤️");
        likeBtn.setStyle("-fx-background-color: #193764; -fx-text-fill: white; -fx-background-radius: 6; " +
                "-fx-font-size: 15px; -fx-cursor: hand; -fx-padding: 6 12; -fx-font-family: 'Georgia';");

        boolean isLiked = likedComments.contains(comment.getId_comment());
        if (isLiked) {
            likeBtn.setStyle(likeBtn.getStyle() + "-fx-opacity: 0.7;");
        }

        likeBtn.setOnAction(e -> toggleCommentLike(comment, likeBtn));

        Label likeCount = new Label(String.valueOf(comment.getNb_likes()));
        likeCount.setStyle("-fx-text-fill: #193764; -fx-font-size: 15px; -fx-font-family: 'Georgia'; -fx-font-weight: 600;");

        // ✅ NEW: Reply button
        Button replyBtn = new Button("💬 Répondre");
        replyBtn.setStyle("-fx-background-color: #193764; -fx-text-fill: white; -fx-background-radius: 6; " +
                "-fx-font-size: 13px; -fx-cursor: hand; -fx-padding: 6 12; -fx-font-family: 'Georgia';");
        replyBtn.setOnAction(e -> showReplyBox(comment, commentCard));

        actions.getChildren().addAll(likeBtn, likeCount, replyBtn);

        // Delete button (if owner)
        if (comment.getId_auteur() == currentUserId) {
            Button deleteBtn = new Button("🗑️");
            deleteBtn.setStyle("-fx-background-color: #737373; -fx-text-fill: white; -fx-background-radius: 6; " +
                    "-fx-font-size: 15px; -fx-cursor: hand; -fx-padding: 6 12; -fx-font-family: 'Georgia';");
            deleteBtn.setOnAction(e -> deleteComment(comment));
            actions.getChildren().add(deleteBtn);
        }

        commentCard.getChildren().addAll(authorLabel, contentLabel, actions);

        return commentCard;
    }

    /**
     * ✅ NEW: Create reply card (indented, lighter color)
     */
    private VBox createReplyCard(Commentaire reply) {
        VBox replyCard = new VBox(8);
        replyCard.setStyle("-fx-background-color: #d5d7d5; -fx-padding: 12; -fx-background-radius: 8;");

        Label authorLabel = new Label("↳ Auteur #" + reply.getId_auteur());
        authorLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 12px; -fx-text-fill: #193764; -fx-font-family: 'Georgia';");

        Label contentLabel = new Label(reply.getContenu());
        contentLabel.setWrapText(true);
        contentLabel.setStyle("-fx-font-size: 14px; -fx-font-family: 'Georgia';");

        HBox actions = new HBox(10);
        actions.setAlignment(Pos.CENTER_LEFT);

        Button likeBtn = new Button("❤️ " + reply.getNb_likes());
        likeBtn.setStyle("-fx-background-color: #193764; -fx-text-fill: white; -fx-font-size: 12px; " +
                "-fx-cursor: hand; -fx-padding: 4 10; -fx-background-radius: 5; -fx-font-family: 'Georgia';");

        boolean isLiked = likedComments.contains(reply.getId_comment());
        if (isLiked) {
            likeBtn.setStyle(likeBtn.getStyle() + "-fx-opacity: 0.7;");
        }

        likeBtn.setOnAction(e -> toggleCommentLike(reply, likeBtn));

        actions.getChildren().add(likeBtn);

        // Delete if owner
        if (reply.getId_auteur() == currentUserId) {
            Button deleteBtn = new Button("🗑️");
            deleteBtn.setStyle("-fx-background-color: #737373; -fx-text-fill: white; -fx-font-size: 12px; " +
                    "-fx-cursor: hand; -fx-padding: 4 10; -fx-background-radius: 5;");
            deleteBtn.setOnAction(e -> deleteComment(reply));
            actions.getChildren().add(deleteBtn);
        }

        replyCard.getChildren().addAll(authorLabel, contentLabel, actions);

        return replyCard;
    }

    /**
     * ✅ NEW: Show reply input box
     */
    private void showReplyBox(Commentaire parentComment, VBox parentCard) {
        // Check if reply box already exists
        if (parentCard.getChildren().stream().anyMatch(node -> node.getUserData() != null && node.getUserData().equals("replyBox"))) {
            return; // Already showing
        }

        VBox replyBox = new VBox(10);
        replyBox.setUserData("replyBox"); // Mark it
        replyBox.setStyle("-fx-background-color: #f1f2f1; -fx-padding: 15; -fx-background-radius: 8;");

        Label replyLabel = new Label("Répondre à Auteur #" + parentComment.getId_auteur());
        replyLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 13px; -fx-font-family: 'Georgia';");

        HBox textBox = new HBox(10);
        textBox.setAlignment(Pos.CENTER_LEFT);

        TextArea replyArea = new TextArea();
        replyArea.setPromptText("Écrivez votre réponse...");
        replyArea.setPrefHeight(70);
        replyArea.setPrefWidth(500);
        replyArea.setWrapText(true);
        replyArea.setStyle("-fx-font-family: 'Georgia'; -fx-font-size: 14px;");

        // ✅ Emoji button for reply
        Button emojiBtn = new Button("😊");
        emojiBtn.setStyle("-fx-font-size: 20px; -fx-cursor: hand; -fx-background-color: white; " +
                "-fx-border-color: #193764; -fx-border-radius: 5; -fx-background-radius: 5;");
        emojiBtn.setOnAction(e -> showEmojiPickerForTextArea(replyArea));

        textBox.getChildren().addAll(replyArea, emojiBtn);

        HBox buttonBox = new HBox(10);
        buttonBox.setAlignment(Pos.CENTER_RIGHT);

        Button cancelBtn = new Button("Annuler");
        cancelBtn.setStyle("-fx-background-color: #737373; -fx-text-fill: white; -fx-cursor: hand; -fx-font-family: 'Georgia';");
        cancelBtn.setOnAction(e -> parentCard.getChildren().remove(replyBox));

        Button submitBtn = new Button("Publier");
        submitBtn.setStyle("-fx-background-color: #193764; -fx-text-fill: white; -fx-cursor: hand; -fx-font-family: 'Georgia';");
        submitBtn.setOnAction(e -> {
            if (handleAddReply(parentComment.getId_comment(), replyArea.getText())) {
                parentCard.getChildren().remove(replyBox);
            }
        });

        buttonBox.getChildren().addAll(cancelBtn, submitBtn);
        replyBox.getChildren().addAll(replyLabel, textBox, buttonBox);

        parentCard.getChildren().add(replyBox);
    }

    /**
     * ✅ NEW: Add a reply to a comment
     */
    private boolean handleAddReply(int parentCommentId, String content) {
        if (content.trim().isEmpty()) {
            showError("Validation", "La réponse ne peut pas être vide.");
            return false;
        }

        if (content.trim().length() < 2) {
            showError("Validation", "La réponse doit contenir au moins 2 caractères.");
            return false;
        }

        try {
            // ✅ Create reply with parent_comment_id
            Commentaire reply = new Commentaire(
                    currentPost.getId_post(),
                    currentUserId,
                    content.trim(),
                    parentCommentId  // ← IMPORTANT: Parent comment ID
            );

            if (commentaireService.addCommentaire(reply)) {
                showSuccess("Réponse ajoutée!");
                loadComments(); // Reload to show the new reply
                return true;
            }

        } catch (Exception e) {
            showError("Erreur", "Impossible d'ajouter la réponse");
            e.printStackTrace();
        }

        return false;
    }

    /**
     * ✅ NEW: Show emoji picker for a TextArea
     */
    private void showEmojiPickerForTextArea(TextArea textArea) {
        String[] emojis = {
                "😊", "😂", "😍", "😢", "😭", "😡", "😱", "🥺",
                "💕", "❤️", "💪", "👍", "👎", "🙏", "💬", "🔥",
                "⭐", "✨", "🌟", "💯", "🎉", "🎊", "🤗", "🤔"
        };

        Dialog<String> dialog = new Dialog<>();
        dialog.setTitle("Choisir un emoji");
        dialog.setHeaderText("Cliquez sur un emoji pour l'insérer");

        FlowPane flowPane = new FlowPane();
        flowPane.setHgap(5);
        flowPane.setVgap(5);
        flowPane.setPadding(new Insets(10));

        for (String emoji : emojis) {
            Button btn = new Button(emoji);
            btn.setStyle("-fx-font-size: 24px; -fx-min-width: 50px; -fx-min-height: 50px; -fx-cursor: hand;");
            btn.setOnAction(e -> {
                int pos = textArea.getCaretPosition();
                String text = textArea.getText();
                textArea.setText(text.substring(0, pos) + emoji + text.substring(pos));
                textArea.positionCaret(pos + emoji.length());
                dialog.close();
            });
            flowPane.getChildren().add(btn);
        }

        dialog.getDialogPane().setContent(flowPane);
        dialog.getDialogPane().getButtonTypes().add(ButtonType.CLOSE);
        dialog.show();
    }

    private void deleteComment(Commentaire comment) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirmation");
        alert.setHeaderText("Supprimer ce commentaire?");
        alert.setContentText("Cette action est irréversible.");

        alert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                commentaireService.deleteCommentaire(comment.getId_comment());
                loadComments();
            }
        });
    }

    private void toggleCommentLike(Commentaire comment, Button button) {
        int commentId = comment.getId_comment();

        if (likedComments.contains(commentId)) {
            comment.setNb_likes(comment.getNb_likes() - 1);
            likedComments.remove(commentId);
            button.setStyle(button.getStyle().replace("-fx-opacity: 0.7;", ""));
        } else {
            commentaireService.likeCommentaire(commentId);
            comment.setNb_likes(comment.getNb_likes() + 1);
            likedComments.add(commentId);
            button.setStyle(button.getStyle() + "-fx-opacity: 0.7;");
        }

        loadComments();
    }

    @FXML
    private void handleLikePost(ActionEvent event) {
        int postId = currentPost.getId_post();

        if (likedPosts.contains(postId)) {
            currentPost.setNb_likes(currentPost.getNb_likes() - 1);
            likedPosts.remove(postId);
            likeButton.setStyle(likeButton.getStyle().replace("-fx-opacity: 0.7;", ""));
        } else {
            postService.likePost(postId);
            currentPost.setNb_likes(currentPost.getNb_likes() + 1);
            likedPosts.add(postId);
            likeButton.setStyle(likeButton.getStyle() + "-fx-opacity: 0.7;");
        }

        likesLabel.setText("❤️ " + currentPost.getNb_likes());

        if (forumController != null) {
            forumController.refreshPosts();
        }
    }

    @FXML
    private void handleAddComment(ActionEvent event) {
        if (commentArea.getText().trim().isEmpty()) {
            showError("Validation", "Le commentaire ne peut pas être vide.");
            return;
        }

        if (commentArea.getText().trim().length() < 2) {
            showError("Validation", "Le commentaire doit contenir au moins 2 caractères.");
            return;
        }

        try {
            Commentaire newComment = new Commentaire(
                    currentPost.getId_post(),
                    currentUserId,
                    commentArea.getText().trim()
            );

            if (commentaireService.addCommentaire(newComment)) {
                showSuccess("Commentaire ajouté!");
                commentArea.clear();
                loadComments();
            }

        } catch (Exception e) {
            showError("Erreur", "Impossible d'ajouter le commentaire");
            e.printStackTrace();
        }
    }

    /**
     * ✅ NEW: Emoji picker for main comment area
     */
    @FXML
    private void handleShowEmojiPicker(ActionEvent event) {
        showEmojiPickerForTextArea(commentArea);
    }

    @FXML
    private void handleBack(ActionEvent event) {
        Stage stage = (Stage) categorieLabel.getScene().getWindow();
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
        alert.setTitle("Info");
        alert.setHeaderText(null);
        alert.setContentText(message);

        new Thread(() -> {
            try {
                Thread.sleep(1500);
                javafx.application.Platform.runLater(() -> alert.close());
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }).start();

        alert.show();
    }
}