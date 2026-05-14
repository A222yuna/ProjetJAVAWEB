package com.psychologie.controller;

import com.psychologie.model.Post;
import com.psychologie.model.User;
import com.psychologie.util.DatabaseConnection;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;

import java.sql.*;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class SavedPostsController {

    @FXML private FlowPane postsContainer;
    @FXML private VBox     emptyState;
    @FXML private Label    countLabel;

    @FXML
    public void initialize() {
        loadSavedPosts();
    }

    private void loadSavedPosts() {
        postsContainer.getChildren().clear();
        User user = SecurityController.getCurrentUser();
        if (user == null) return;

        List<Post> posts = new ArrayList<>();

        String query =
            "SELECT p.id_post, p.titre, p.contenu, p.categorie, p.nb_likes, p.date, " +
            "       u.prenom, u.nom " +
            "FROM post p " +
            "JOIN saved_posts s ON p.id_post = s.id_post " +
            "LEFT JOIN users u ON p.auteur_id_user = u.id_user " +
            "WHERE s.id_user = ? " +
            "ORDER BY p.date DESC";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(query)) {
            ps.setInt(1, user.getId());
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                Post p = new Post();
                p.setId(rs.getInt("id_post"));
                p.setTitre(rs.getString("titre"));
                p.setContenu(rs.getString("contenu"));
                p.setCategorie(rs.getString("categorie"));
                p.setNbLikes(rs.getInt("nb_likes"));
                Timestamp ts = rs.getTimestamp("date");
                if (ts != null) p.setDate(ts.toLocalDateTime());

                User author = new User();
                author.setPrenom(safe(rs.getString("prenom")));
                author.setNom(safe(rs.getString("nom")));
                p.setAuteur(author);
                posts.add(p);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        if (posts.isEmpty()) {
            emptyState.setVisible(true);
            emptyState.setManaged(true);
            postsContainer.setVisible(false);
            postsContainer.setManaged(false);
            countLabel.setText("");
        } else {
            emptyState.setVisible(false);
            emptyState.setManaged(false);
            postsContainer.setVisible(true);
            postsContainer.setManaged(true);
            countLabel.setText(posts.size() + " publication" + (posts.size() > 1 ? "s" : ""));
            for (Post p : posts) postsContainer.getChildren().add(createPostCard(p));
        }
    }

    private VBox createPostCard(Post p) {
        VBox card = new VBox(12);
        card.setPrefWidth(320);
        card.setPadding(new Insets(20));
        card.setStyle("-fx-background-color: white; -fx-background-radius: 12; " +
                      "-fx-effect: dropshadow(three-pass-box,rgba(0,0,0,0.06),10,0,0,5);");

        // Category badge
        Label cat = new Label(safe(p.getCategorie()).toUpperCase());
        cat.setStyle("-fx-background-color: #e6f0ec; -fx-text-fill: #2a6f5b; " +
                     "-fx-padding: 3 10; -fx-background-radius: 8; -fx-font-size: 10px; -fx-font-weight: bold;");

        // Title
        Label title = new Label(p.getTitre());
        title.setStyle("-fx-font-weight: bold; -fx-font-size: 16px; -fx-text-fill: #1c2824;");
        title.setWrapText(true);

        // Author + date
        String dateStr = p.getDate() != null
                ? p.getDate().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")) : "";
        Label meta = new Label("Par " + p.getAuteur().getPrenom() + " " + p.getAuteur().getNom()
                + (dateStr.isBlank() ? "" : " · " + dateStr));
        meta.setStyle("-fx-text-fill: #5a6560; -fx-font-size: 11px;");

        // Content preview
        Label content = new Label(p.getContenu());
        content.setWrapText(true);
        content.setMaxHeight(60);
        content.setStyle("-fx-text-fill: #5a6560; -fx-font-size: 12px;");

        // Footer
        HBox footer = new HBox(8);
        footer.setAlignment(Pos.CENTER_LEFT);

        Label likes = new Label("❤ " + p.getNbLikes());
        likes.setStyle("-fx-text-fill: #e74c3c; -fx-font-size: 12px;");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button viewBtn = new Button("Lire →");
        viewBtn.setStyle("-fx-background-color: #2a6f5b; -fx-text-fill: white; " +
                         "-fx-background-radius: 8; -fx-cursor: hand; -fx-padding: 6 14; -fx-font-size: 11px;");
        viewBtn.setOnAction(e -> handleViewDetail(p));

        Button unsaveBtn = new Button("★ Enregistré");
        unsaveBtn.setStyle("-fx-background-color: #fff3cd; -fx-text-fill: #856404; " +
                           "-fx-background-radius: 8; -fx-cursor: hand; -fx-padding: 6 14; -fx-font-size: 11px;");
        unsaveBtn.setTooltip(new Tooltip("Cliquer pour retirer des enregistrements"));
        unsaveBtn.setOnAction(e -> {
            removeSaved(p.getId());
            loadSavedPosts(); // refresh
        });

        footer.getChildren().addAll(likes, spacer, unsaveBtn, viewBtn);
        card.getChildren().addAll(cat, title, meta, content, footer);
        return card;
    }

    private void removeSaved(int postId) {
        User user = SecurityController.getCurrentUser();
        if (user == null) return;
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(
                     "DELETE FROM saved_posts WHERE id_user=? AND id_post=?")) {
            ps.setInt(1, user.getId());
            ps.setInt(2, postId);
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void handleViewDetail(Post p) {
        PostDetailController.setPost(p);
        MainController.getInstance().loadView("/com/psychologie/view/PostDetailView.fxml");
    }

    @FXML
    private void handleGoToForum() {
        MainController.getInstance().showForum();
    }

    private String safe(String s) { return s == null ? "" : s; }
}
