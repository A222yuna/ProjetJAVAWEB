package tn.esprit.pidev.forum.controllers;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import tn.esprit.pidev.forum.entities.Post;
import tn.esprit.pidev.forum.services.CommentaireService;
import tn.esprit.pidev.forum.services.PostService;
import tn.esprit.pidev.forum.utils.ShareService;

import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class ForumController {

    @FXML private TextField searchField;
    @FXML private ComboBox<String> categoryComboBox;
    @FXML private VBox postsContainer;
    @FXML private VBox categoriesContainer;
    @FXML private VBox topPostsContainer;

    private PostService postService;
    private CommentaireService commentaireService;
    private ObservableList<Post> postsList;
    private int currentUserId = 1;

    private Set<Integer> likedPosts = new HashSet<>();

    @FXML
    public void initialize() {
        postService = new PostService();
        commentaireService = new CommentaireService();
        postsList = FXCollections.observableArrayList();
        loadTopPosts();
        loadCategories();
        loadCategorySidebar();
        loadPosts();
    }

    /**
     * ✅ NOUVEAU: Retour à la HomePage
     */
    @FXML
    private void handleReturnHome(ActionEvent event) {
        try {
            System.out.println("🏠 Retour à la HomePage...");

            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/HomePage.fxml"));
            Parent root = loader.load();

            Stage homeStage = new Stage();
            homeStage.setTitle("MindConnect - Plateforme de Santé Mentale");
            homeStage.setScene(new Scene(root, 1400, 900));
            homeStage.setMaximized(true);

            Stage currentStage = (Stage) searchField.getScene().getWindow();
            currentStage.close();

            homeStage.show();

            System.out.println("✅ Retour à l'accueil réussi!");

        } catch (IOException e) {
            System.err.println("❌ Erreur lors du retour à l'accueil!");
            e.printStackTrace();
            showError("Erreur", "Impossible de retourner à l'accueil.\n" + e.getMessage());
        }
    }

    private void loadCategories() {
        categoryComboBox.getItems().add("Toutes");
        categoryComboBox.getItems().addAll(
                "Discussion Générale",
                "Santé Mentale",
                "Addiction",
                "Social",
                "Discussion sur les Traumatismes"
        );
        categoryComboBox.getSelectionModel().selectFirst();
    }

    /**
     * ✅ CORRIGÉ: Catégories avec TEXTE BLANC
     */
    private void loadCategorySidebar() {
        categoriesContainer.getChildren().clear();

        List<String> categories = Arrays.asList(
                "Discussion Générale",
                "Santé Mentale",
                "Addiction",
                "Social",
                "Discussion sur les Traumatismes"
        );

        for (String category : categories) {
            Button catBtn = new Button(category);
            catBtn.setPrefWidth(280);
            catBtn.setPrefHeight(42);
            // ✅ TEXTE BLANC ICI!
            catBtn.setStyle("-fx-background-color: transparent; " +
                    "-fx-text-fill: white; " +  // ← BLANC!
                    "-fx-font-size: 14px; -fx-font-family: 'Georgia'; -fx-cursor: hand; " +
                    "-fx-alignment: CENTER-LEFT; -fx-padding: 12 25;");
            catBtn.setOnAction(e -> filterByCategory(category));

            catBtn.setOnMouseEntered(e -> catBtn.setStyle(catBtn.getStyle() + "-fx-background-color: rgba(255,255,255,0.1);"));
            catBtn.setOnMouseExited(e -> catBtn.setStyle(catBtn.getStyle().replace("-fx-background-color: rgba(255,255,255,0.1);", "-fx-background-color: transparent;")));

            categoriesContainer.getChildren().add(catBtn);
        }
    }

    private void filterByCategory(String category) {
        postsList.clear();
        List<Post> posts = postService.getPostsByCategory(category);
        postsList.addAll(posts);
        displayPostCards();
    }

    private void loadPosts() {
        postsList.clear();
        List<Post> posts = postService.getAllPosts();
        postsList.addAll(posts);
        displayPostCards();
    }

    private void displayPostCards() {
        postsContainer.getChildren().clear();

        if (postsList.isEmpty()) {
            Label emptyLabel = new Label("Aucun post pour le moment.");
            emptyLabel.setStyle("-fx-text-fill: #816057; -fx-font-size: 17px; -fx-padding: 50; " +
                    "-fx-font-style: italic; -fx-font-family: 'Georgia';");
            postsContainer.getChildren().add(emptyLabel);
            return;
        }

        for (Post post : postsList) {
            postsContainer.getChildren().add(createPostCard(post));
        }
    }

    private VBox createPostCard(Post post) {
        VBox card = new VBox(14);
        card.setMaxWidth(1100);
        card.setStyle("-fx-background-color: white; -fx-background-radius: 12; -fx-padding: 22; " +
                "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.08), 10, 0, 0, 2);");

        VBox innerCard = new VBox(12);
        innerCard.setStyle("-fx-background-color: #e7e6e4; -fx-background-radius: 10; -fx-padding: 20;");

        HBox topRow = new HBox(10);
        topRow.setAlignment(Pos.CENTER_LEFT);

        Label categoryLabel = new Label(post.getCategorie());
        categoryLabel.setStyle("-fx-background-color: #816057; -fx-text-fill: white; " +
                "-fx-padding: 6 14; -fx-background-radius: 6; -fx-font-size: 14px; " +
                "-fx-font-family: 'Georgia';");

        topRow.getChildren().add(categoryLabel);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        topRow.getChildren().add(spacer);

        if (post.getId_auteur() == currentUserId) {
            Button menuBtn = new Button("⋮");
            menuBtn.setStyle("-fx-background-color: transparent; -fx-text-fill: #364e5b; " +
                    "-fx-font-size: 24px; -fx-cursor: hand; -fx-padding: 0 5;");
            menuBtn.setOnAction(e -> showPostMenu(menuBtn, post));
            topRow.getChildren().add(menuBtn);
        }

        Label titleLabel = new Label(post.getTitre());
        titleLabel.setWrapText(true);
        titleLabel.setStyle("-fx-font-size: 19px; -fx-text-fill: #364e5b; -fx-font-family: 'Georgia'; -fx-font-weight: bold;");

        String preview = post.getContenu().length() > 150 ?
                post.getContenu().substring(0, 150) + "..." : post.getContenu();
        Label contentLabel = new Label(preview);
        contentLabel.setWrapText(true);
        contentLabel.setStyle("-fx-text-fill: #364e5b; -fx-font-size: 15px; -fx-font-family: 'Georgia';");

        VBox bottomSection = new VBox(10);

        HBox actionsBox = new HBox(14);
        actionsBox.setAlignment(Pos.CENTER_LEFT);

        Button jaimeBtn = createActionButton("J'aime", "#193764");
        boolean isLiked = likedPosts.contains(post.getId_post());
        if (isLiked) {
            jaimeBtn.setStyle(jaimeBtn.getStyle() + "-fx-opacity: 0.7;");
        }
        jaimeBtn.setOnAction(e -> toggleLike(post, jaimeBtn));

        Button commenterBtn = createActionButton("commenter", "#193764");
        commenterBtn.setOnAction(e -> openPostDetails(post));

        Button plusBtn = createActionButton("plus+", "#193764");
        plusBtn.setOnAction(e -> openPostDetails(post));

        Button shareBtn = createActionButton("🔗 Partager", "#193764");
        shareBtn.setOnAction(e -> handleSharePost(post));

        int commentCount = commentaireService.countCommentsByPost(post.getId_post());
        Label commentCountLabel = new Label("💬 " + commentCount);
        commentCountLabel.setStyle("-fx-text-fill: #364e5b; -fx-font-size: 15px; -fx-font-family: 'Georgia';");

        Label likeCountLabel = new Label("❤️ " + post.getNb_likes());
        likeCountLabel.setStyle("-fx-text-fill: #193764; -fx-font-size: 15px; -fx-font-family: 'Georgia';");

        actionsBox.getChildren().addAll(jaimeBtn, commenterBtn, plusBtn, shareBtn, likeCountLabel, commentCountLabel);
        bottomSection.getChildren().add(actionsBox);

        HBox dateRow = new HBox();
        dateRow.setAlignment(Pos.CENTER_RIGHT);
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
        Label dateLabel = new Label(post.getDate().format(formatter));
        dateLabel.setStyle("-fx-text-fill: #816057; -fx-font-size: 12px; -fx-font-family: 'Georgia'; -fx-font-style: italic;");
        dateRow.getChildren().add(dateLabel);
        bottomSection.getChildren().add(dateRow);

        innerCard.getChildren().addAll(topRow, titleLabel, contentLabel, bottomSection);
        card.getChildren().add(innerCard);

        return card;
    }

    private void handleSharePost(Post post) {
        try {
            String shortUrl = ShareService.generateShareLink(post.getId_post());
            String shareMessage = ShareService.generateShareMessage(post.getTitre(), post.getId_post());
            ShareService.copyToClipboard(shortUrl);

            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Partager ce post");
            alert.setHeaderText("✅ Lien de partage généré!");

            VBox content = new VBox(15);
            content.setStyle("-fx-padding: 20;");

            Label copyInfo = new Label("📋 Le lien a été copié dans votre presse-papier!");
            copyInfo.setStyle("-fx-font-weight: bold; -fx-text-fill: #193764; -fx-font-size: 14px;");

            TextArea textArea = new TextArea(shareMessage);
            textArea.setEditable(false);
            textArea.setWrapText(true);
            textArea.setPrefHeight(120);

            content.getChildren().addAll(copyInfo, textArea);

            try {
                String qrUrl = ShareService.generateQRCodeUrl(post.getId_post());
                javafx.scene.image.Image qrImage = new javafx.scene.image.Image(qrUrl);
                javafx.scene.image.ImageView qrView = new javafx.scene.image.ImageView(qrImage);
                qrView.setFitWidth(200);
                qrView.setFitHeight(200);

                Label qrLabel = new Label("📱 Scannez ce QR code:");
                qrLabel.setStyle("-fx-font-weight: bold;");

                content.getChildren().addAll(qrLabel, qrView);
            } catch (Exception e) {
                // QR code non disponible
            }

            alert.getDialogPane().setContent(content);
            alert.showAndWait();

        } catch (Exception e) {
            showError("Erreur", "Impossible de générer le lien");
        }
    }

    private void showPostMenu(Button menuBtn, Post post) {
        ContextMenu contextMenu = new ContextMenu();

        MenuItem editItem = new MenuItem("✏️ Modifier");
        editItem.setOnAction(e -> editPost(post));

        MenuItem deleteItem = new MenuItem("🗑️ Supprimer");
        deleteItem.setOnAction(e -> deletePost(post));

        contextMenu.getItems().addAll(editItem, deleteItem);
        contextMenu.show(menuBtn, javafx.geometry.Side.BOTTOM, 0, 0);
    }

    private void editPost(Post post) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/AddPostView.fxml"));
            Parent root = loader.load();

            AddPostController controller = loader.getController();
            controller.setForumController(this);
            controller.setCurrentUserId(currentUserId);
            controller.setPostToEdit(post);

            Stage stage = new Stage();
            stage.setTitle("Modifier Post");
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setScene(new Scene(root));
            stage.showAndWait();

        } catch (IOException e) {
            showError("Erreur", "Impossible d'ouvrir le formulaire");
        }
    }

    private void deletePost(Post post) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirmation");
        alert.setHeaderText("Supprimer ce post?");
        alert.setContentText("Cette action est irréversible.");

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            postService.deletePost(post.getId_post());
            loadPosts();
            loadTopPosts();
        }
    }

    private void toggleLike(Post post, Button button) {
        int postId = post.getId_post();

        if (likedPosts.contains(postId)) {
            post.setNb_likes(post.getNb_likes() - 1);
            likedPosts.remove(postId);
            button.setStyle(button.getStyle().replace("-fx-opacity: 0.7;", ""));
        } else {
            postService.likePost(postId);
            post.setNb_likes(post.getNb_likes() + 1);
            likedPosts.add(postId);
            button.setStyle(button.getStyle() + "-fx-opacity: 0.7;");
        }

        displayPostCards();
        loadTopPosts();
    }

    private Button createActionButton(String text, String color) {
        Button btn = new Button(text);
        btn.setStyle("-fx-background-color: " + color + "; -fx-text-fill: white; " +
                "-fx-background-radius: 6; -fx-font-size: 14px; -fx-cursor: hand; " +
                "-fx-padding: 7 14; -fx-font-family: 'Georgia';");
        return btn;
    }

    private void openPostDetails(Post post) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/PostDetailsView.fxml"));
            Parent root = loader.load();

            PostDetailsController controller = loader.getController();
            controller.setPost(post);
            controller.setForumController(this);
            controller.setCurrentUserId(currentUserId);
            controller.setLikedPosts(likedPosts);

            Stage stage = new Stage();
            stage.setTitle("Détails du Post");
            stage.setScene(new Scene(root));
            stage.show();

        } catch (IOException e) {
            showError("Erreur", "Impossible d'ouvrir les détails");
        }
    }

    @FXML
    private void handleNewPost(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/AddPostView.fxml"));
            Parent root = loader.load();

            AddPostController controller = loader.getController();
            controller.setForumController(this);
            controller.setCurrentUserId(currentUserId);

            Stage stage = new Stage();
            stage.setTitle("Nouveau Post");
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setScene(new Scene(root));
            stage.showAndWait();

        } catch (IOException e) {
            showError("Erreur", "Impossible d'ouvrir le formulaire");
        }
    }

    @FXML
    private void handleSearch(ActionEvent event) {
        String keyword = searchField.getText().trim();
        if (!keyword.isEmpty()) {
            postsList.clear();
            List<Post> results = postService.searchPosts(keyword);
            postsList.addAll(results);
            displayPostCards();
        } else {
            loadPosts();
        }
    }

    @FXML
    private void handleCategoryFilter(ActionEvent event) {
        String selected = categoryComboBox.getSelectionModel().getSelectedItem();
        if (selected != null && !selected.equals("Toutes")) {
            filterByCategory(selected);
        } else {
            loadPosts();
        }
    }

    private void loadTopPosts() {
        topPostsContainer.getChildren().clear();

        List<Post> topPosts = postService.getTopPostsThisMonth(5);

        for (int i = 0; i < topPosts.size(); i++) {
            Post post = topPosts.get(i);
            VBox postItem = createTopPostItem(post, i + 1);
            topPostsContainer.getChildren().add(postItem);
        }
    }

    private VBox createTopPostItem(Post post, int rank) {
        VBox item = new VBox(5);
        item.setStyle("-fx-background-color: rgba(255,255,255,0.1); " +
                "-fx-padding: 10; -fx-background-radius: 6; -fx-cursor: hand;");

        Label rankLabel = new Label("#" + rank);
        rankLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #FFD700;");

        String title = post.getTitre().length() > 40 ?
                post.getTitre().substring(0, 40) + "..." :
                post.getTitre();
        Label titleLabel = new Label(title);
        titleLabel.setWrapText(true);
        titleLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: white;");

        Label likesLabel = new Label("❤️ " + post.getNb_likes());
        likesLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: #FFD700;");

        item.getChildren().addAll(rankLabel, titleLabel, likesLabel);

        item.setOnMouseClicked(e -> openPostDetails(post));

        item.setOnMouseEntered(e -> item.setStyle(item.getStyle() + "-fx-background-color: rgba(255,255,255,0.2);"));
        item.setOnMouseExited(e -> item.setStyle(item.getStyle().replace("-fx-background-color: rgba(255,255,255,0.2);", "-fx-background-color: rgba(255,255,255,0.1);")));

        return item;
    }

    @FXML
    private void handleRefresh(ActionEvent event) {
        loadPosts();
        loadTopPosts();
        searchField.clear();
        categoryComboBox.getSelectionModel().selectFirst();
    }

    public void refreshPosts() {
        loadPosts();
        loadTopPosts();
    }

    private void showError(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}
