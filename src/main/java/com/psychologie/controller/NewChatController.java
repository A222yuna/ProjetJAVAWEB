package com.psychologie.controller;

import com.psychologie.model.Conversation;
import com.psychologie.model.User;
import com.psychologie.util.DatabaseConnection;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import java.sql.*;

public class NewChatController {

    @FXML private FlowPane userCardsContainer;
    @FXML private TextField searchField;

    @FXML
    public void initialize() {
        loadUsers("");
    }

    @FXML
    private void handleSearch() {
        loadUsers(searchField.getText());
    }

    private void loadUsers(String filter) {
        userCardsContainer.getChildren().clear();
        User currentUser = SecurityController.getCurrentUser();
        if (currentUser == null) return;

        // Fetch users excluding the current user
        String query = "SELECT * FROM users WHERE id_user != ?";
        if (filter != null && !filter.isEmpty()) {
            query += " AND (nom LIKE ? OR prenom LIKE ? OR email LIKE ?)";
        }
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            
            pstmt.setInt(1, currentUser.getId());
            if (filter != null && !filter.isEmpty()) {
                String likeFilter = "%" + filter + "%";
                pstmt.setString(2, likeFilter);
                pstmt.setString(3, likeFilter);
                pstmt.setString(4, likeFilter);
            }

            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                User u = new User();
                u.setId(rs.getInt("id_user"));
                u.setNom(rs.getString("nom"));
                u.setPrenom(rs.getString("prenom"));
                u.setEmail(rs.getString("email"));
                u.setRole(rs.getString("role"));
                
                userCardsContainer.getChildren().add(createUserCard(u));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private VBox createUserCard(User user) {
        VBox card = new VBox(10);
        card.getStyleClass().add("stat-card");
        card.setPrefWidth(250);
        card.setPadding(new Insets(20));
        card.setAlignment(Pos.CENTER);

        // Avatar placeholder
        StackPane avatar = new StackPane();
        avatar.setStyle("-fx-background-color: #d4e9e2; -fx-background-radius: 25; -fx-min-width: 50; -fx-min-height: 50; -fx-max-width: 50; -fx-max-height: 50;");
        Label initials = new Label((user.getPrenom().charAt(0) + "" + user.getNom().charAt(0)).toUpperCase());
        initials.setStyle("-fx-text-fill: #1abc9c; -fx-font-weight: bold; -fx-font-size: 16px;");
        avatar.getChildren().add(initials);

        Label name = new Label(user.getPrenom() + " " + user.getNom());
        name.setStyle("-fx-font-weight: bold; -fx-font-size: 14px;");
        
        HBox roleBox = new HBox(5);
        roleBox.setAlignment(Pos.CENTER);
        Label role = new Label(user.getRole());
        role.setStyle("-fx-background-color: #f8f9fa; -fx-padding: 2 8; -fx-background-radius: 10; -fx-font-size: 10px;");
        roleBox.getChildren().add(role);

        Label email = new Label(user.getEmail());
        email.getStyleClass().add("stat-label");
        email.setStyle("-fx-font-size: 11px;");

        Button startBtn = new Button("Commencer");
        startBtn.getStyleClass().add("btn-primary");
        startBtn.setMaxWidth(Double.MAX_VALUE);
        startBtn.setOnAction(e -> handleStartChat(user));

        card.getChildren().addAll(avatar, name, roleBox, email, startBtn);
        return card;
    }

    private void handleStartChat(User recipient) {
        System.out.println("Starting chat with: " + recipient.getEmail());
        User currentUser = SecurityController.getCurrentUser();
        if (currentUser == null) return;

        try (Connection conn = DatabaseConnection.getConnection()) {
            // 1. Check if conversation already exists between these two
            String checkQuery = "SELECT DISTINCT c.id_conversation FROM conversation c " +
                               "JOIN message m ON c.id_conversation = m.id_conversation " +
                               "WHERE (m.expediteur_id_user = ? AND m.destinataire_id_user = ?) " +
                               "OR (m.expediteur_id_user = ? AND m.destinataire_id_user = ?)";
            PreparedStatement pstmtCheck = conn.prepareStatement(checkQuery);
            pstmtCheck.setInt(1, currentUser.getId());
            pstmtCheck.setInt(2, recipient.getId());
            pstmtCheck.setInt(3, recipient.getId());
            pstmtCheck.setInt(4, currentUser.getId());
            ResultSet rsCheck = pstmtCheck.executeQuery();

            int convId;
            if (rsCheck.next()) {
                convId = rsCheck.getInt("id_conversation");
            } else {
                // 2. Create new conversation
                String convQuery = "INSERT INTO conversation (date_creation, statut_conversation, archiver_conversation) VALUES (?, ?, ?)";
                PreparedStatement pstmtConv = conn.prepareStatement(convQuery, Statement.RETURN_GENERATED_KEYS);
                pstmtConv.setDate(1, java.sql.Date.valueOf(java.time.LocalDate.now()));
                pstmtConv.setString(2, "active");
                pstmtConv.setBoolean(3, false);
                pstmtConv.executeUpdate();
                
                ResultSet rsKeys = pstmtConv.getGeneratedKeys();
                if (rsKeys.next()) {
                    convId = rsKeys.getInt(1);
                    // Add a first dummy message or just open it
                } else {
                    throw new SQLException("Failed to create conversation");
                }
            }

            // 3. Open the conversation detail view
            Conversation conv = new Conversation();
            conv.setId(convId);
            ConversationDetailController.setConversation(conv, recipient);
            MainController.getInstance().loadView("/com/psychologie/view/ConversationDetailView.fxml");

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleBack() {
        MainController.getInstance().loadView("/com/psychologie/view/ChatView.fxml");
    }
}
