package com.psychologie.controller;

import com.psychologie.model.Commentaire;
import com.psychologie.model.Post;
import com.psychologie.model.User;
import com.psychologie.util.DatabaseConnection;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import java.sql.*;
import java.time.format.DateTimeFormatter;

public class PostDetailController {

    @FXML private Label titleLabel;
    @FXML private Label authorLabel;
    @FXML private Label contentLabel;
    @FXML private Label likesLabel;
    @FXML private Label commentCountBadge;
    @FXML private Button likeBtn;
    @FXML private Button saveBtn;
    @FXML private Button editPostBtn;
    @FXML private Button deletePostBtn;
    @FXML private VBox commentsContainer;
    @FXML private TextArea commentTextArea;
    @FXML private HBox replyIndicator;
    @FXML private Label replyLabel;

    private static Post currentPost;
    private Integer replyingToCommentId = null;

    public static void setPost(Post p) {
        currentPost = p;
    }

    @FXML
    public void initialize() {
        createLikeTables();
        createSaveTable();
        if (currentPost != null) {
            loadPostDetails();
            loadComments();
            updateLikeButtonState();
            updateSaveButtonState();
        }
    }

    private void createSaveTable() {
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement()) {
            stmt.execute("CREATE TABLE IF NOT EXISTS saved_posts (id_user INT, id_post INT, PRIMARY KEY (id_user, id_post))");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void createLikeTables() {
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement()) {
            stmt.execute("CREATE TABLE IF NOT EXISTS post_likes (id_post INT, id_user INT, PRIMARY KEY (id_post, id_user))");
            stmt.execute("CREATE TABLE IF NOT EXISTS comment_likes (id_comment INT, id_user INT, PRIMARY KEY (id_comment, id_user))");
            stmt.execute("CREATE TABLE IF NOT EXISTS forum_reports (" +
                        "id_report INT AUTO_INCREMENT PRIMARY KEY, " +
                        "id_post INT, " +
                        "id_comment INT, " +
                        "id_user_reporter INT, " +
                        "reason VARCHAR(255), " +
                        "status VARCHAR(50) DEFAULT 'OPEN', " +
                        "date DATETIME DEFAULT CURRENT_TIMESTAMP)");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void loadPostDetails() {
        ensurePostAuthorLoaded();
        titleLabel.setText(currentPost.getTitre());
        contentLabel.setText(currentPost.getContenu());
        likesLabel.setText(currentPost.getNbLikes() + " j'aime");
        
        User currentUser = SecurityController.getCurrentUser();
        if (currentUser != null) {
            boolean isOwner = currentPost.getAuteur() != null && currentUser.getId().equals(currentPost.getAuteur().getId());
            boolean isAdmin = "ADMIN".equalsIgnoreCase(currentUser.getRole());
            
            // Only owner can edit
            editPostBtn.setVisible(isOwner);
            editPostBtn.setManaged(isOwner);
            
            // Owner or Admin can delete
            deletePostBtn.setVisible(isOwner || isAdmin);
            deletePostBtn.setManaged(isOwner || isAdmin);
        } else {
            editPostBtn.setVisible(false);
            deletePostBtn.setVisible(false);
        }

        if (currentPost.getAuteur() != null) {
            authorLabel.setText(nullSafe(currentPost.getAuteur().getPrenom()) + " " + nullSafe(currentPost.getAuteur().getNom()));
        } else {
            authorLabel.setText("Auteur inconnu");
        }
    }

    private void ensurePostAuthorLoaded() {
        if (currentPost == null || currentPost.getAuteur() != null) return;

        String query = "SELECT p.auteur_id_user, u.prenom, u.nom " +
                       "FROM post p LEFT JOIN users u ON p.auteur_id_user = u.id_user " +
                       "WHERE p.id_post = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setInt(1, currentPost.getId());
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                User auteur = new User();
                auteur.setId(rs.getInt("auteur_id_user"));
                auteur.setPrenom(rs.getString("prenom"));
                auteur.setNom(rs.getString("nom"));
                currentPost.setAuteur(auteur);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private String nullSafe(String value) {
        return value == null ? "" : value;
    }

    private void updateLikeButtonState() {
        User user = SecurityController.getCurrentUser();
        if (user == null) return;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement("SELECT 1 FROM post_likes WHERE id_post = ? AND id_user = ?")) {
            pstmt.setInt(1, currentPost.getId());
            pstmt.setInt(2, user.getId());
            ResultSet rs = pstmt.executeQuery();
            
            Label btnLabel = (Label) likeBtn.getGraphic();
            if (rs.next()) {
                likeBtn.setStyle("-fx-background-color: #f1948a; -fx-text-fill: white; -fx-background-radius: 20; -fx-padding: 8 20; -fx-cursor: hand;");
                if (btnLabel != null) {
                    btnLabel.setText("♥ Aimé");
                    btnLabel.setStyle("-fx-text-fill: white; -fx-font-size: 13px; -fx-font-weight: bold;");
                }
            } else {
                likeBtn.setStyle("-fx-background-color: white; -fx-border-color: #dcd8d0; -fx-border-radius: 20; -fx-background-radius: 20; -fx-padding: 8 20; -fx-cursor: hand;");
                if (btnLabel != null) {
                    btnLabel.setText("♥ J'aime");
                    btnLabel.setStyle("-fx-text-fill: #34495e; -fx-font-size: 13px;");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void updateSaveButtonState() {
        User user = SecurityController.getCurrentUser();
        if (user == null || saveBtn == null) return;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement("SELECT 1 FROM saved_posts WHERE id_user = ? AND id_post = ?")) {
            pstmt.setInt(1, user.getId());
            pstmt.setInt(2, currentPost.getId());
            ResultSet rs = pstmt.executeQuery();
            
            Label btnLabel = (Label) saveBtn.getGraphic();
            if (rs.next()) {
                saveBtn.setStyle("-fx-background-color: #2ecc71; -fx-text-fill: white; -fx-background-radius: 20; -fx-padding: 8 20; -fx-cursor: hand;");
                if (btnLabel != null) {
                    btnLabel.setText("★ Enregistré");
                    btnLabel.setStyle("-fx-text-fill: white; -fx-font-size: 13px; -fx-font-weight: bold;");
                }
            } else {
                saveBtn.setStyle("-fx-background-color: white; -fx-border-color: #dcd8d0; -fx-border-radius: 20; -fx-background-radius: 20; -fx-padding: 8 20; -fx-cursor: hand;");
                if (btnLabel != null) {
                    btnLabel.setText("☆ Enregistrer");
                    btnLabel.setStyle("-fx-text-fill: #34495e; -fx-font-size: 13px;");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @FXML
    public void handleSavePost() {
        User user = SecurityController.getCurrentUser();
        if (user == null) return;

        try (Connection conn = DatabaseConnection.getConnection()) {
            PreparedStatement checkPstmt = conn.prepareStatement("SELECT 1 FROM saved_posts WHERE id_user = ? AND id_post = ?");
            checkPstmt.setInt(1, user.getId());
            checkPstmt.setInt(2, currentPost.getId());
            
            if (checkPstmt.executeQuery().next()) {
                // Unsave
                PreparedStatement delPstmt = conn.prepareStatement("DELETE FROM saved_posts WHERE id_user = ? AND id_post = ?");
                delPstmt.setInt(1, user.getId());
                delPstmt.setInt(2, currentPost.getId());
                delPstmt.executeUpdate();
            } else {
                // Save
                PreparedStatement insPstmt = conn.prepareStatement("INSERT INTO saved_posts (id_user, id_post) VALUES (?, ?)");
                insPstmt.setInt(1, user.getId());
                insPstmt.setInt(2, currentPost.getId());
                insPstmt.executeUpdate();
            }
            updateSaveButtonState();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void loadComments() {
        commentsContainer.getChildren().clear();
        String query = "SELECT c.*, u.prenom, u.nom FROM commentaire c LEFT JOIN users u ON c.auteur_id_user = u.id_user WHERE c.id_post = ? ORDER BY c.date ASC";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setInt(1, currentPost.getId());
            ResultSet rs = pstmt.executeQuery();

            java.util.List<Commentaire> allComments = new java.util.ArrayList<>();
            while (rs.next()) {
                Commentaire c = new Commentaire();
                c.setId(rs.getInt("id_comment"));
                c.setContenu(rs.getString("contenu"));
                c.setAuteurRole(rs.getString("auteur_role"));
                c.setNbLikes(rs.getInt("nb_likes"));
                
                int parentId = rs.getInt("parent_comment_id");
                if (parentId > 0) {
                    Commentaire parent = new Commentaire();
                    parent.setId(parentId);
                    c.setParent(parent);
                }

                Timestamp ts = rs.getTimestamp("date");
                if (ts != null) c.setDate(ts.toLocalDateTime());

                User author = new User();
                author.setId(rs.getInt("auteur_id_user"));
                author.setPrenom(rs.getString("prenom"));
                author.setNom(rs.getString("nom"));
                c.setAuteur(author);

                allComments.add(c);
            }

            // Filter top-level comments and render them with their children
            for (Commentaire c : allComments) {
                if (c.getParent() == null) {
                    renderCommentAndReplies(c, allComments, 0);
                }
            }
            commentCountBadge.setText(String.valueOf(allComments.size()));
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void renderCommentAndReplies(Commentaire parent, java.util.List<Commentaire> allComments, int depth) {
        VBox card = createCommentCard(parent, parent.getAuteur().getPrenom(), parent.getAuteur().getNom());
        if (depth > 0) {
            card.setTranslateX(30 * depth);
            card.setMaxWidth(card.getMaxWidth() - (30 * depth));
        }
        commentsContainer.getChildren().add(card);

        for (Commentaire child : allComments) {
            if (child.getParent() != null && child.getParent().getId().equals(parent.getId())) {
                renderCommentAndReplies(child, allComments, depth + 1);
            }
        }
    }

    private VBox createCommentCard(Commentaire c, String prenom, String nom) {
        VBox card = new VBox(10);
        card.setStyle("-fx-background-color: white; -fx-padding: 15; -fx-background-radius: 10; -fx-border-color: #f4f2ee; -fx-border-width: 1;");

        HBox header = new HBox(10);
        header.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
        
        StackPane avatar = new StackPane(new Label(prenom != null ? prenom.substring(0, 1).toUpperCase() : "A"));
        avatar.setStyle("-fx-background-color: #e8f8f5; -fx-padding: 8; -fx-background-radius: 20; -fx-min-width: 35; -fx-min-height: 35;");
        
        VBox authorInfo = new VBox(2);
        Label author = new Label(prenom != null ? prenom + " " + nom : "Anonyme");
        author.setStyle("-fx-font-weight: bold; -fx-font-size: 14px;");
        Label date = new Label(c.getDate().format(DateTimeFormatter.ofPattern("dd/MM/yyyy · HH:mm")));
        date.setStyle("-fx-font-size: 11px; -fx-text-fill: #95a5a6;");
        authorInfo.getChildren().addAll(author, date);
        
        header.getChildren().addAll(avatar, authorInfo);

        Label content = new Label(c.getContenu());
        content.setWrapText(true);
        content.setStyle("-fx-padding: 5 0 10 0; -fx-text-fill: #34495e;");

        HBox actions = new HBox(15);
        actions.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
        
        Label likesLabelComm = new Label(c.getNbLikes() + " j'aime");
        likesLabelComm.setStyle("-fx-text-fill: #95a5a6; -fx-font-size: 12px;");
        
        Button likeCommBtn = new Button("♥ J'aime");
        likeCommBtn.setStyle("-fx-background-color: transparent; -fx-text-fill: #7f8c8d; -fx-font-size: 12px; -fx-cursor: hand; -fx-padding: 0;");
        
        // Check if user already liked this comment
        User user = SecurityController.getCurrentUser();
        if (user != null) {
            try (Connection conn = DatabaseConnection.getConnection();
                 PreparedStatement pstmt = conn.prepareStatement("SELECT 1 FROM comment_likes WHERE id_comment = ? AND id_user = ?")) {
                pstmt.setInt(1, c.getId());
                pstmt.setInt(2, user.getId());
                if (pstmt.executeQuery().next()) {
                    likeCommBtn.setStyle("-fx-background-color: transparent; -fx-text-fill: #f1948a; -fx-font-size: 12px; -fx-cursor: hand; -fx-padding: 0; -fx-font-weight: bold;");
                }
            } catch (SQLException e) { e.printStackTrace(); }
        }
        likeCommBtn.setOnAction(e -> handleLikeComment(c));

        Button replyBtn = new Button("↶ Répondre");
        replyBtn.setStyle("-fx-background-color: transparent; -fx-text-fill: #7f8c8d; -fx-font-size: 12px; -fx-cursor: hand; -fx-padding: 0;");
        replyBtn.setOnAction(e -> handleReply(c, prenom, nom));

        Button signalBtn = new Button("🚩 Signaler");
        signalBtn.setStyle("-fx-background-color: transparent; -fx-text-fill: #7f8c8d; -fx-font-size: 12px; -fx-cursor: hand; -fx-padding: 0;");
        signalBtn.setOnAction(e -> handleSignalComment(c));

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        
        actions.getChildren().addAll(likesLabelComm, likeCommBtn, replyBtn, signalBtn, spacer);

        if (user != null) {
            boolean isOwner = user.getId().equals(c.getAuteur().getId());
            boolean isAdmin = "ADMIN".equalsIgnoreCase(user.getRole());

            if (isOwner) {
                Button editBtn = new Button("✎ Modifier");
                editBtn.setStyle("-fx-background-color: transparent; -fx-text-fill: #f39c12; -fx-font-size: 12px; -fx-cursor: hand; -fx-padding: 0;");
                editBtn.setOnAction(e -> handleEditComment(c));
                actions.getChildren().add(editBtn);
            }

            if (isOwner || isAdmin) {
                Button deleteBtn = new Button("🗑");
                deleteBtn.setStyle("-fx-background-color: transparent; -fx-text-fill: #95a5a6; -fx-font-size: 14px; -fx-cursor: hand; -fx-padding: 0;");
                deleteBtn.setOnAction(e -> handleDeleteComment(c));
                actions.getChildren().add(deleteBtn);
            }
        }

        card.getChildren().addAll(header, content, actions);
        return card;
    }

    @FXML
    public void handleLikePost() {
        User user = SecurityController.getCurrentUser();
        if (user == null) return;

        try (Connection conn = DatabaseConnection.getConnection()) {
            // Check if already liked
            PreparedStatement checkPstmt = conn.prepareStatement("SELECT 1 FROM post_likes WHERE id_post = ? AND id_user = ?");
            checkPstmt.setInt(1, currentPost.getId());
            checkPstmt.setInt(2, user.getId());
            
            if (checkPstmt.executeQuery().next()) {
                // Unlike
                PreparedStatement delPstmt = conn.prepareStatement("DELETE FROM post_likes WHERE id_post = ? AND id_user = ?");
                delPstmt.setInt(1, currentPost.getId());
                delPstmt.setInt(2, user.getId());
                delPstmt.executeUpdate();
                
                PreparedStatement updatePstmt = conn.prepareStatement("UPDATE post SET nb_likes = nb_likes - 1 WHERE id_post = ?");
                updatePstmt.setInt(1, currentPost.getId());
                updatePstmt.executeUpdate();
                currentPost.setNbLikes(currentPost.getNbLikes() - 1);
            } else {
                // Like
                PreparedStatement insPstmt = conn.prepareStatement("INSERT INTO post_likes (id_post, id_user) VALUES (?, ?)");
                insPstmt.setInt(1, currentPost.getId());
                insPstmt.setInt(2, user.getId());
                insPstmt.executeUpdate();
                
                PreparedStatement updatePstmt = conn.prepareStatement("UPDATE post SET nb_likes = nb_likes + 1 WHERE id_post = ?");
                updatePstmt.setInt(1, currentPost.getId());
                updatePstmt.executeUpdate();
                currentPost.setNbLikes(currentPost.getNbLikes() + 1);
            }
            
            likesLabel.setText(currentPost.getNbLikes() + " j'aime");
            updateLikeButtonState();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void handleLikeComment(Commentaire c) {
        User user = SecurityController.getCurrentUser();
        if (user == null) return;

        try (Connection conn = DatabaseConnection.getConnection()) {
            PreparedStatement checkPstmt = conn.prepareStatement("SELECT 1 FROM comment_likes WHERE id_comment = ? AND id_user = ?");
            checkPstmt.setInt(1, c.getId());
            checkPstmt.setInt(2, user.getId());
            
            if (checkPstmt.executeQuery().next()) {
                // Unlike
                PreparedStatement delPstmt = conn.prepareStatement("DELETE FROM comment_likes WHERE id_comment = ? AND id_user = ?");
                delPstmt.setInt(1, c.getId());
                delPstmt.setInt(2, user.getId());
                delPstmt.executeUpdate();
                
                PreparedStatement updatePstmt = conn.prepareStatement("UPDATE commentaire SET nb_likes = nb_likes - 1 WHERE id_comment = ?");
                updatePstmt.setInt(1, c.getId());
                updatePstmt.executeUpdate();
            } else {
                // Like
                PreparedStatement insPstmt = conn.prepareStatement("INSERT INTO comment_likes (id_comment, id_user) VALUES (?, ?)");
                insPstmt.setInt(1, c.getId());
                insPstmt.setInt(2, user.getId());
                insPstmt.executeUpdate();
                
                PreparedStatement updatePstmt = conn.prepareStatement("UPDATE commentaire SET nb_likes = nb_likes + 1 WHERE id_comment = ?");
                updatePstmt.setInt(1, c.getId());
                updatePstmt.executeUpdate();
            }
            loadComments();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @FXML
    public void handleEditPost() {
        TextInputDialog dialog = new TextInputDialog(currentPost.getContenu());
        dialog.setTitle("Modifier la publication");
        dialog.setHeaderText("Modifier le contenu de votre publication");
        dialog.setContentText("Contenu:");
        dialog.getDialogPane().setPrefWidth(400);

        dialog.showAndWait().ifPresent(newContent -> {
            if (!newContent.trim().isEmpty()) {
                try (Connection conn = DatabaseConnection.getConnection();
                     PreparedStatement pstmt = conn.prepareStatement("UPDATE post SET contenu = ? WHERE id_post = ?")) {
                    pstmt.setString(1, newContent);
                    pstmt.setInt(2, currentPost.getId());
                    pstmt.executeUpdate();
                    currentPost.setContenu(newContent);
                    contentLabel.setText(newContent);
                } catch (SQLException e) { e.printStackTrace(); }
            }
        });
    }

    private void handleEditComment(Commentaire c) {
        TextInputDialog dialog = new TextInputDialog(c.getContenu());
        dialog.setTitle("Modifier le commentaire");
        dialog.setHeaderText("Modifier votre commentaire");
        dialog.setContentText("Contenu:");

        dialog.showAndWait().ifPresent(newContent -> {
            if (!newContent.trim().isEmpty()) {
                try (Connection conn = DatabaseConnection.getConnection();
                     PreparedStatement pstmt = conn.prepareStatement("UPDATE commentaire SET contenu = ? WHERE id_comment = ?")) {
                    pstmt.setString(1, newContent);
                    pstmt.setInt(2, c.getId());
                    pstmt.executeUpdate();
                    loadComments();
                } catch (SQLException e) { e.printStackTrace(); }
            }
        });
    }

    @FXML
    public void handleSignalPost() {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Signaler la publication");
        dialog.setHeaderText("Pourquoi signalez-vous cette publication ?");
        dialog.setContentText("Raison:");

        dialog.showAndWait().ifPresent(reason -> {
            if (!reason.trim().isEmpty()) {
                saveReport(currentPost.getId(), null, reason);
            }
        });
    }

    private void handleSignalComment(Commentaire c) {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Signaler le commentaire");
        dialog.setHeaderText("Pourquoi signalez-vous ce commentaire ?");
        dialog.setContentText("Raison:");

        dialog.showAndWait().ifPresent(reason -> {
            if (!reason.trim().isEmpty()) {
                saveReport(null, c.getId(), reason);
            }
        });
    }

    private void saveReport(Integer postId, Integer commentId, String reason) {
        User user = SecurityController.getCurrentUser();
        if (user == null) return;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement("INSERT INTO forum_reports (id_post, id_comment, id_user_reporter, reason) VALUES (?, ?, ?, ?)")) {
            if (postId != null) pstmt.setInt(1, postId); else pstmt.setNull(1, Types.INTEGER);
            if (commentId != null) pstmt.setInt(2, commentId); else pstmt.setNull(2, Types.INTEGER);
            pstmt.setInt(3, user.getId());
            pstmt.setString(4, reason);
            pstmt.executeUpdate();
            
            Alert alert = new Alert(Alert.AlertType.INFORMATION, "Signalement envoyé. Merci de votre contribution.");
            alert.showAndWait();
        } catch (SQLException e) {
            e.printStackTrace();
            Alert alert = new Alert(Alert.AlertType.ERROR, "Erreur lors de l'envoi du signalement.");
            alert.showAndWait();
        }
    }

    @FXML
    public void handleDeletePost() {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION, "Supprimer cette publication ?", ButtonType.YES, ButtonType.NO);
        alert.showAndWait().ifPresent(res -> {
            if (res == ButtonType.YES) {
                try (Connection conn = DatabaseConnection.getConnection();
                     PreparedStatement pstmt = conn.prepareStatement("DELETE FROM post WHERE id_post = ?")) {
                    pstmt.setInt(1, currentPost.getId());
                    pstmt.executeUpdate();
                    handleBack();
                } catch (SQLException e) { e.printStackTrace(); }
            }
        });
    }

    private void handleDeleteComment(Commentaire c) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION, "Supprimer ce commentaire ?", ButtonType.YES, ButtonType.NO);
        alert.showAndWait().ifPresent(res -> {
            if (res == ButtonType.YES) {
                try (Connection conn = DatabaseConnection.getConnection();
                     PreparedStatement pstmt = conn.prepareStatement("DELETE FROM commentaire WHERE id_comment = ?")) {
                    pstmt.setInt(1, c.getId());
                    pstmt.executeUpdate();
                    loadComments();
                } catch (SQLException e) { e.printStackTrace(); }
            }
        });
    }

    @FXML
    public void handleFocusComment() {
        commentTextArea.requestFocus();
    }

    private void handleReply(Commentaire c, String prenom, String nom) {
        replyingToCommentId = c.getId();
        replyLabel.setText("Réponse à " + prenom + " " + nom);
        replyIndicator.setVisible(true);
        replyIndicator.setManaged(true);
        commentTextArea.requestFocus();
    }

    @FXML
    private void handleCancelReply() {
        replyingToCommentId = null;
        replyIndicator.setVisible(false);
        replyIndicator.setManaged(false);
    }

    private void handleToggleCommentVisibility(Commentaire c) {
        String query = "UPDATE commentaire SET is_hidden = ? WHERE id_comment = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setBoolean(1, !c.isHidden());
            pstmt.setInt(2, c.getId());
            pstmt.executeUpdate();
            loadComments();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @FXML
    public void handlePublishComment() {
        String text = commentTextArea.getText();
        if (text == null || text.trim().isEmpty()) return;

        User user = SecurityController.getCurrentUser();
        if (user == null) return;

        // Ensuring role is valid for DB enum ('Patient','Psychologue')
        String roleToSave = user.getRole();
        if (!roleToSave.equalsIgnoreCase("Patient") && !roleToSave.equalsIgnoreCase("Psychologue")) {
            roleToSave = "Psychologue"; // Fallback for Admin
        } else {
            // Capitalize first letter to match enum if needed, though DB is usually case-insensitive for enums
            roleToSave = roleToSave.substring(0, 1).toUpperCase() + roleToSave.substring(1).toLowerCase();
        }

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement("INSERT INTO commentaire (id_post, auteur_id_user, auteur_role, contenu, date, nb_likes, parent_comment_id) VALUES (?, ?, ?, ?, ?, ?, ?)")) {
            pstmt.setInt(1, currentPost.getId());
            pstmt.setInt(2, user.getId());
            pstmt.setString(3, roleToSave);
            pstmt.setString(4, text);
            pstmt.setTimestamp(5, new Timestamp(System.currentTimeMillis()));
            pstmt.setInt(6, 0);
            if (replyingToCommentId != null) {
                pstmt.setInt(7, replyingToCommentId);
            } else {
                pstmt.setNull(7, Types.INTEGER);
            }

            pstmt.executeUpdate();
            commentTextArea.clear();
            handleCancelReply();
            loadComments();
        } catch (SQLException e) {
            e.printStackTrace();
            Alert alert = new Alert(Alert.AlertType.ERROR, "Erreur lors de la publication: " + e.getMessage());
            alert.showAndWait();
        }
    }

    @FXML private void handleBack() {
        User user = SecurityController.getCurrentUser();
        if (user != null && "ADMIN".equalsIgnoreCase(user.getRole())) {
            MainController.getInstance().loadView("/com/psychologie/view/admin/AdminForum.fxml");
        } else {
            MainController.getInstance().loadView("/com/psychologie/view/ForumView.fxml");
        }
    }
}
