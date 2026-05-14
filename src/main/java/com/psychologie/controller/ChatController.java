package com.psychologie.controller;

import com.psychologie.model.Conversation;
import com.psychologie.model.User;
import com.psychologie.util.DatabaseConnection;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import java.sql.*;

public class ChatController {

    @FXML private Label totalConvLabel;
    @FXML private Label activeConvLabel;
    @FXML private ListView<Conversation> conversationListView;

    private ObservableList<Conversation> conversationData = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        setupListView();
        loadConversations();
    }

    private void setupListView() {
        conversationListView.setCellFactory(param -> new ListCell<>() {
            @Override
            protected void updateItem(Conversation item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setGraphic(null);
                } else {
                    VBox vbox = new VBox(5);
                    Label title = new Label("Conversation #" + String.format("%03d", item.getId()));
                    title.setStyle("-fx-font-weight: bold; -fx-font-size: 14px;");
                    
                    Label details = new Label("Sécurisé end-to-end • " + item.getDateCreation());
                    details.setStyle("-fx-font-size: 11px; -fx-text-fill: #6c757d;");
                    
                    vbox.getChildren().addAll(title, details);

                    HBox hbox = new HBox(10);
                    hbox.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
                    
                    Label status = new Label(item.getStatutConversation().toUpperCase());
                    status.setStyle("-fx-font-size: 10px; -fx-padding: 2 8; -fx-background-radius: 10; " +
                            (item.getStatutConversation().equals("active") ? "-fx-background-color: #e7f5ed; -fx-text-fill: #1b7a43;" : "-fx-background-color: #f1f3f5; -fx-text-fill: #6c757d;"));
                    
                    hbox.getChildren().addAll(vbox, new javafx.scene.layout.Region());
                    HBox.setHgrow(vbox, javafx.scene.layout.Priority.ALWAYS);
                    hbox.getChildren().add(status);
                    
                    setGraphic(hbox);
                    
                    // Open conversation on click
                    setOnMouseClicked(event -> {
                        if (event.getClickCount() == 1 && item != null) {
                            handleOpenConversation(item);
                        }
                    });
                }
            }
        });
        conversationListView.setItems(conversationData);
    }

    private void handleOpenConversation(Conversation conv) {
        User currentUser = SecurityController.getCurrentUser();
        if (currentUser == null) return;

        // Fetch the recipient (the other person in the conversation)
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(
                 "SELECT u.* FROM users u " +
                 "JOIN message m ON (u.id_user = m.expediteur_id_user OR u.id_user = m.destinataire_id_user) " +
                 "WHERE m.id_conversation = ? AND u.id_user != ? LIMIT 1")) {
            
            pstmt.setInt(1, conv.getId());
            pstmt.setInt(2, currentUser.getId());
            ResultSet rs = pstmt.executeQuery();
            
            if (rs.next()) {
                User target = new User();
                target.setId(rs.getInt("id_user"));
                target.setNom(rs.getString("nom"));
                target.setPrenom(rs.getString("prenom"));
                target.setEmail(rs.getString("email"));
                target.setRole(rs.getString("role"));
                
                ConversationDetailController.setConversation(conv, target);
                MainController.getInstance().loadView("/com/psychologie/view/ConversationDetailView.fxml");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void loadConversations() {
        conversationData.clear();
        int activeCount = 0;
        User user = SecurityController.getCurrentUser();
        if (user == null) return;

        // Fetch conversations where the user is either the sender or receiver of the last message, 
        // or where they are explicitly part of the conversation if you have a participants table.
        // Assuming 'conversation' table and a logic to filter by user.
        String query = "SELECT DISTINCT c.* FROM conversation c " +
                       "JOIN message m ON c.id_conversation = m.id_conversation " +
                       "WHERE m.expediteur_id_user = ? OR m.destinataire_id_user = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            
            pstmt.setInt(1, user.getId());
            pstmt.setInt(2, user.getId());
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                Conversation c = new Conversation();
                c.setId(rs.getInt("id_conversation"));
                Date date = rs.getDate("date_creation");
                if (date != null) c.setDateCreation(date.toLocalDate());
                c.setStatutConversation(rs.getString("statut_conversation"));
                c.setArchiverConversation(rs.getBoolean("archiver_conversation"));
                
                conversationData.add(c);
                if ("active".equalsIgnoreCase(c.getStatutConversation())) activeCount++;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        totalConvLabel.setText(String.valueOf(conversationData.size()));
        activeConvLabel.setText(String.valueOf(activeCount));
    }

    @FXML public void handleNewChat() { 
        MainController.getInstance().loadView("/com/psychologie/view/NewChatView.fxml");
    }
}
