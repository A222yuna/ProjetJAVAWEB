package com.psychologie.controller;

import com.psychologie.model.Post;
import com.psychologie.model.User;
import com.psychologie.util.DatabaseConnection;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import java.sql.*;
import java.time.format.DateTimeFormatter;

public class ForumController {

    @FXML private VBox postsContainer;
    @FXML private TextField searchField;
    @FXML private ComboBox<String> categoryComboBox;

    @FXML
    public void initialize() {
        categoryComboBox.getItems().addAll("Tous", "Anxiété", "Dépression", "Relations", "Développement Personnel");
        categoryComboBox.setValue("Tous");
        loadPosts();
    }

    private void loadPosts() {
        postsContainer.getChildren().clear();

        // Use COALESCE so it works whether is_hidden column exists or not
        String query =
            "SELECT p.*, u.prenom, u.nom FROM post p " +
            "LEFT JOIN users u ON p.auteur_id_user = u.id_user";

        String category = categoryComboBox.getValue();
        if (category != null && !"Tous".equals(category)) {
            query += " WHERE p.categorie = ?";
        }
        query += " ORDER BY p.date DESC";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {

            if (category != null && !"Tous".equals(category)) {
                pstmt.setString(1, category);
            }
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                // Skip hidden posts if column exists
                try {
                    if (rs.getInt("is_hidden") == 1) continue;
                } catch (SQLException ignored) { /* column doesn't exist yet — show all */ }

                Post post = new Post();
                post.setId(rs.getInt("id_post"));
                post.setTitre(rs.getString("titre"));
                post.setContenu(rs.getString("contenu"));
                post.setCategorie(rs.getString("categorie") != null ? rs.getString("categorie") : "Général");
                post.setNbLikes(rs.getInt("nb_likes"));
                post.setAuteurRole(rs.getString("auteur_role"));
                User auteur = new User();
                auteur.setId(rs.getInt("auteur_id_user"));
                auteur.setPrenom(rs.getString("prenom") != null ? rs.getString("prenom") : "");
                auteur.setNom(rs.getString("nom") != null ? rs.getString("nom") : "");
                post.setAuteur(auteur);
                Timestamp ts = rs.getTimestamp("date");
                if (ts != null) post.setDate(ts.toLocalDateTime());

                postsContainer.getChildren().add(
                    createPostCard(post, rs.getString("prenom"), rs.getString("nom")));
            }

            if (postsContainer.getChildren().isEmpty()) {
                Label empty = new Label("Aucun post pour le moment. Soyez le premier à publier !");
                empty.setStyle("-fx-text-fill: #5a6560; -fx-font-size: 14px; -fx-padding: 20;");
                postsContainer.getChildren().add(empty);
            }

        } catch (SQLException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible de charger les posts : " + e.getMessage());
        }
    }

    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    private VBox createPostCard(Post post, String prenom, String nom) {
        VBox card = new VBox(10);
        card.getStyleClass().add("stat-card");
        card.setPadding(new Insets(20));

        Label category = new Label(post.getCategorie().toUpperCase());
        category.setStyle("-fx-text-fill: #2a6f5b; -fx-font-weight: bold; -fx-font-size: 10px;");

        Label title = new Label(post.getTitre());
        title.getStyleClass().add("label-header");
        title.setStyle("-fx-font-size: 18px;");
        title.setWrapText(true);

        Label meta = new Label(String.format("Par %s %s · %s · %s",
            prenom != null ? prenom : "Anonyme",
            nom != null ? nom : "",
            post.getAuteurRole() != null ? post.getAuteurRole() : "",
            post.getDate() != null
                ? post.getDate().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"))
                : ""));
        meta.getStyleClass().add("stat-label");

        Label preview = new Label(post.getContenu());
        preview.setWrapText(true);
        preview.setMaxHeight(60);
        preview.setStyle("-fx-text-fill: #5a6560;");

        HBox footer = new HBox(15);
        footer.setAlignment(Pos.CENTER_LEFT);
        Label likes = new Label("❤ " + post.getNbLikes() + " j'aime");
        likes.setStyle("-fx-text-fill: #f1948a; -fx-font-weight: bold;");
        
        Button viewBtn = new Button("Lire la suite →");
        viewBtn.getStyleClass().add("btn-secondary");
        viewBtn.setOnAction(e -> handleViewPost(post));

        // Quick-save button — toggles saved state inline
        boolean isSaved = isPostSaved(post.getId());
        Button saveBtn = new Button(isSaved ? "★" : "☆");
        saveBtn.setTooltip(new Tooltip(isSaved ? "Retirer des enregistrements" : "Enregistrer"));
        saveBtn.setStyle(isSaved
            ? "-fx-background-color: #fff3cd; -fx-text-fill: #856404; -fx-background-radius: 20; -fx-cursor: hand; -fx-font-size: 14px; -fx-padding: 6 12;"
            : "-fx-background-color: white; -fx-border-color: #dcd8d0; -fx-text-fill: #5a6560; -fx-background-radius: 20; -fx-border-radius: 20; -fx-cursor: hand; -fx-font-size: 14px; -fx-padding: 6 12;");
        saveBtn.setOnAction(e -> {
            toggleSave(post.getId());
            loadPosts(); // refresh to update button state
        });

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        footer.getChildren().addAll(likes, spacer, saveBtn, viewBtn);

        card.getChildren().addAll(category, title, meta, preview, footer);
        return card;
    }

    @FXML 
    private void handleNewPost() {
        User user = SecurityController.getCurrentUser();
        if (user == null) return;

        Dialog<Post> dialog = new Dialog<>();
        dialog.setTitle("Nouveau Post");
        dialog.setHeaderText("Créer une nouvelle discussion");

        ButtonType postButtonType = new ButtonType("Publier", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(postButtonType, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));

        TextField titreField = new TextField();
        titreField.setPromptText("Titre");
        TextArea contenuArea = new TextArea();
        contenuArea.setPromptText("Contenu...");
        contenuArea.setPrefRowCount(5);
        ComboBox<String> catCombo = new ComboBox<>(FXCollections.observableArrayList("Anxiété", "Dépression", "Relations", "Développement Personnel"));
        catCombo.setValue("Anxiété");

        grid.add(new Label("Titre:"), 0, 0);
        grid.add(titreField, 1, 0);
        grid.add(new Label("Catégorie:"), 0, 1);
        grid.add(catCombo, 1, 1);
        grid.add(new Label("Contenu:"), 0, 2);
        grid.add(contenuArea, 1, 2);

        dialog.getDialogPane().setContent(grid);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == postButtonType) {
                Post p = new Post();
                p.setTitre(titreField.getText());
                p.setContenu(contenuArea.getText());
                p.setCategorie(catCombo.getValue());
                return p;
            }
            return null;
        });

        dialog.showAndWait().ifPresent(post -> {
            savePost(post);
            loadPosts();
        });
    }

    private void savePost(Post p) {
        User user = SecurityController.getCurrentUser();
        // Ensuring role is valid for DB enum ('Patient','Psychologue')
        String roleToSave = user.getRole();
        if (!roleToSave.equalsIgnoreCase("Patient") && !roleToSave.equalsIgnoreCase("Psychologue")) {
            roleToSave = "Psychologue"; // Fallback for Admin
        } else {
            roleToSave = roleToSave.substring(0, 1).toUpperCase() + roleToSave.substring(1).toLowerCase();
        }

        String query = "INSERT INTO post (auteur_id_user, titre, contenu, categorie, date, nb_likes, auteur_role) VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setInt(1, user.getId());
            pstmt.setString(2, p.getTitre());
            pstmt.setString(3, p.getContenu());
            pstmt.setString(4, p.getCategorie());
            pstmt.setTimestamp(5, new Timestamp(System.currentTimeMillis()));
            pstmt.setInt(6, 0);
            pstmt.setString(7, roleToSave);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible de créer le post: " + e.getMessage());
        }
    }
    @FXML private void handleFilter() { loadPosts(); }
    
    private void handleViewPost(Post post) {
        PostDetailController.setPost(post);
        MainController.getInstance().loadView("/com/psychologie/view/PostDetailView.fxml");
    }

    private boolean isPostSaved(int postId) {
        User user = SecurityController.getCurrentUser();
        if (user == null) return false;
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(
                     "SELECT 1 FROM saved_posts WHERE id_user=? AND id_post=?")) {
            ps.setInt(1, user.getId());
            ps.setInt(2, postId);
            return ps.executeQuery().next();
        } catch (SQLException e) {
            return false;
        }
    }

    private void toggleSave(int postId) {
        User user = SecurityController.getCurrentUser();
        if (user == null) return;
        try (Connection conn = DatabaseConnection.getConnection()) {
            PreparedStatement check = conn.prepareStatement(
                    "SELECT 1 FROM saved_posts WHERE id_user=? AND id_post=?");
            check.setInt(1, user.getId());
            check.setInt(2, postId);
            if (check.executeQuery().next()) {
                PreparedStatement del = conn.prepareStatement(
                        "DELETE FROM saved_posts WHERE id_user=? AND id_post=?");
                del.setInt(1, user.getId()); del.setInt(2, postId);
                del.executeUpdate();
            } else {
                PreparedStatement ins = conn.prepareStatement(
                        "INSERT INTO saved_posts (id_user, id_post) VALUES (?,?)");
                ins.setInt(1, user.getId()); ins.setInt(2, postId);
                ins.executeUpdate();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
