package com.psychologie.controller.admin;

import com.psychologie.controller.MainController;
import com.psychologie.controller.SecurityController;
import com.psychologie.model.Post;
import com.psychologie.model.User;
import com.psychologie.util.DatabaseConnection;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.geometry.Insets;
import java.sql.*;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

public class AdminForumController {

    @FXML private TableView<Post> postTable;
    @FXML private TableColumn<Post, String> dateColumn;
    @FXML private TableColumn<Post, String> titreColumn;
    @FXML private TableColumn<Post, Post> auteurColumn;
    @FXML private TableColumn<Post, String> categorieColumn;
    @FXML private TableColumn<Post, String> statsColumn;
    @FXML private TableColumn<Post, Void> actionsColumn;

    @FXML private TextField searchField;
    @FXML private ComboBox<String> sortCombo;

    private ObservableList<Post> allPosts = FXCollections.observableArrayList();
    private ObservableList<Post> filteredPosts = FXCollections.observableArrayList();
    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    @FXML
    public void initialize() {
        sortCombo.setItems(FXCollections.observableArrayList("Plus récents", "Plus anciens"));
        sortCombo.setValue("Plus récents");
        
        setupTable();
        loadPosts();
        
        searchField.textProperty().addListener((obs, oldVal, newVal) -> applyFilters());
        sortCombo.setOnAction(e -> applyFilters());
    }

    private void setupTable() {
        dateColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getDate().format(formatter)));
        
        titreColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getTitre()));
        titreColumn.setCellFactory(param -> new TableCell<Post, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setGraphic(null);
                } else {
                    Hyperlink link = new Hyperlink(item);
                    link.setStyle("-fx-text-fill: #3498db; -fx-underline: false;");
                    link.setOnAction(e -> handleViewDetail(getTableView().getItems().get(getIndex())));
                    setGraphic(link);
                }
            }
        });

        auteurColumn.setCellValueFactory(cellData -> new javafx.beans.property.SimpleObjectProperty<>(cellData.getValue()));
        auteurColumn.setCellFactory(param -> new TableCell<Post, Post>() {
            @Override
            protected void updateItem(Post item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null || item.getAuteur() == null) {
                    setGraphic(null);
                } else {
                    HBox box = new HBox(5);
                    box.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
                    Label name = new Label(item.getAuteur().getPrenom() + " " + item.getAuteur().getNom());
                    Label roleBadge = new Label(item.getAuteur().getRole().toUpperCase());
                    roleBadge.setStyle("-fx-padding: 2 6; -fx-background-radius: 5; -fx-font-size: 9px; -fx-text-fill: white; -fx-font-weight: bold;");
                    if ("PSYCHOLOGUE".equalsIgnoreCase(item.getAuteur().getRole())) {
                        roleBadge.setStyle(roleBadge.getStyle() + "-fx-background-color: #3498db;");
                    } else if ("ADMIN".equalsIgnoreCase(item.getAuteur().getRole())) {
                        roleBadge.setStyle(roleBadge.getStyle() + "-fx-background-color: #2c3e50;");
                    } else {
                        roleBadge.setStyle(roleBadge.getStyle() + "-fx-background-color: #1abc9c;");
                    }
                    box.getChildren().addAll(name, roleBadge);
                    setGraphic(box);
                }
            }
        });

        categorieColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getCategorie()));
        categorieColumn.setCellFactory(param -> new TableCell<Post, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setGraphic(null);
                } else {
                    Label label = new Label(item.toUpperCase());
                    label.setStyle("-fx-background-color: #7f8c8d; -fx-text-fill: white; -fx-padding: 2 8; -fx-background-radius: 10; -fx-font-size: 10px; -fx-font-weight: bold;");
                    setGraphic(label);
                }
            }
        });

        statsColumn.setCellValueFactory(cellData -> {
            Post p = cellData.getValue();
            return new SimpleStringProperty("❤ " + p.getNbLikes() + "  💬 " + (p.getCommentaires() != null ? p.getCommentaires().size() : 0));
        });

        actionsColumn.setCellFactory(param -> new TableCell<Post, Void>() {
            private final Button deleteBtn = new Button("Supprimer");
            {
                deleteBtn.setStyle("-fx-background-color: white; -fx-border-color: #e74c3c; -fx-text-fill: #e74c3c; -fx-border-radius: 5; -fx-font-size: 10px; -fx-cursor: hand;");
                deleteBtn.setOnAction(e -> handleDelete(getTableView().getItems().get(getIndex())));
            }
            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) setGraphic(null);
                else setGraphic(deleteBtn);
            }
        });

        postTable.setItems(filteredPosts);
    }

    private void loadPosts() {
        allPosts.clear();
        String query = "SELECT p.*, u.prenom, u.nom, u.role FROM post p " +
                       "LEFT JOIN users u ON p.auteur_id_user = u.id_user " +
                       "ORDER BY p.date DESC";
        
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            
            while (rs.next()) {
                Post p = new Post();
                p.setId(rs.getInt("id_post"));
                p.setTitre(rs.getString("titre") != null ? rs.getString("titre") : "Sans titre");
                p.setCategorie(rs.getString("categorie") != null ? rs.getString("categorie") : "Général");
                p.setNbLikes(rs.getInt("nb_likes"));
                p.setDate(rs.getTimestamp("date") != null ? rs.getTimestamp("date").toLocalDateTime() : java.time.LocalDateTime.now());
                
                // Count comments for this post
                int postId = rs.getInt("id_post");
                String commentQuery = "SELECT COUNT(*) FROM commentaire WHERE id_post = ?";
                try (PreparedStatement pstmtComm = conn.prepareStatement(commentQuery)) {
                    pstmtComm.setInt(1, postId);
                    try (ResultSet rsComm = pstmtComm.executeQuery()) {
                        if (rsComm.next()) {
                            int count = rsComm.getInt(1);
                            p.setCommentaires(new java.util.ArrayList<>(java.util.Collections.nCopies(count, null)));
                        }
                    }
                }

                User author = new User();
                author.setId(rs.getInt("auteur_id_user"));
                author.setPrenom(rs.getString("prenom") != null ? rs.getString("prenom") : "Anonyme");
                author.setNom(rs.getString("nom") != null ? rs.getString("nom") : "");
                author.setRole(rs.getString("role") != null ? rs.getString("role") : "Patient");
                p.setAuteur(author);

                allPosts.add(p);
            }
            applyFilters();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    public void applyFilters() {
        if (allPosts == null || filteredPosts == null) return;

        String search = (searchField != null && searchField.getText() != null) ? searchField.getText().toLowerCase() : "";
        List<Post> filtered = allPosts.stream()
            .filter(p -> {
                String titre = p.getTitre() != null ? p.getTitre().toLowerCase() : "";
                String cat = p.getCategorie() != null ? p.getCategorie().toLowerCase() : "";
                return titre.contains(search) || cat.contains(search);
            })
            .collect(Collectors.toList());
        
        String sortVal = (sortCombo != null && sortCombo.getValue() != null) ? sortCombo.getValue() : "Plus récents";
        if ("Plus anciens".equals(sortVal)) {
            filtered.sort((p1, p2) -> {
                if (p1.getDate() == null || p2.getDate() == null) return 0;
                return p1.getDate().compareTo(p2.getDate());
            });
        } else {
            filtered.sort((p1, p2) -> {
                if (p1.getDate() == null || p2.getDate() == null) return 0;
                return p2.getDate().compareTo(p1.getDate());
            });
        }
        
        filteredPosts.setAll(filtered);
    }

    @FXML
    private void handleNewPost() {
        User user = SecurityController.getCurrentUser();
        if (user == null) return;

        Dialog<Post> dialog = new Dialog<>();
        dialog.setTitle("Nouveau Post");
        dialog.setHeaderText("Créer une nouvelle discussion administrative");

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
        }
    }

    private void handleViewDetail(Post post) {
        System.out.println("View detail for: " + post.getTitre());
        com.psychologie.controller.PostDetailController.setPost(post);
        MainController.getInstance().loadView("/com/psychologie/view/PostDetailView.fxml");
    }

    private void handleDelete(Post post) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION, "Supprimer cette publication ?", ButtonType.YES, ButtonType.NO);
        alert.showAndWait().ifPresent(res -> {
            if (res == ButtonType.YES) {
                try (Connection conn = DatabaseConnection.getConnection();
                     PreparedStatement pstmt = conn.prepareStatement("DELETE FROM post WHERE id_post = ?")) {
                    pstmt.setInt(1, post.getId());
                    pstmt.executeUpdate();
                    loadPosts();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }
}
