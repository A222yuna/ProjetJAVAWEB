package com.psychologie.controller;

import com.psychologie.model.Conversation;
import com.psychologie.model.Message;
import com.psychologie.model.User;
import com.psychologie.util.DatabaseConnection;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import java.sql.*;
import java.time.format.DateTimeFormatter;

public class ConversationDetailController {

    @FXML private Label chatTitleLabel;
    @FXML private VBox messagesContainer;
    @FXML private TextField messageInput;
    @FXML private ScrollPane scrollPane;

    private static Conversation currentConversation;
    private static User recipient;

    public static void setConversation(Conversation conv, User target) {
        currentConversation = conv;
        recipient = target;
    }

    @FXML
    public void initialize() {
        if (currentConversation != null) {
            if (recipient != null) {
                chatTitleLabel.setText(recipient.getPrenom() + " " + recipient.getNom());
            }
            loadMessages();
            
            // Scroll to bottom after loading
            Platform.runLater(() -> scrollPane.setVvalue(1.0));
        }
    }

    private void loadMessages() {
        messagesContainer.getChildren().clear();
        User currentUser = SecurityController.getCurrentUser();
        if (currentUser == null) return;

        String query = "SELECT * FROM message WHERE id_conversation = ? ORDER BY date_message ASC";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            
            pstmt.setInt(1, currentConversation.getId());
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                Message m = new Message();
                m.setContenuMessage(rs.getString("contenu_message"));
                m.setExpediteur_id_user(rs.getInt("expediteur_id_user"));
                Timestamp ts = rs.getTimestamp("date_message");
                if (ts != null) m.setDateMessage(ts.toLocalDateTime());
                
                messagesContainer.getChildren().add(createMessageBubble(m, currentUser.getId()));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private HBox createMessageBubble(Message m, int currentUserId) {
        boolean isMine = m.getExpediteur_id_user() == currentUserId;
        
        HBox container = new HBox();
        container.setAlignment(isMine ? Pos.CENTER_RIGHT : Pos.CENTER_LEFT);
        
        VBox bubble = new VBox(5);
        bubble.setPadding(new Insets(10, 15, 10, 15));
        bubble.setMaxWidth(400);
        
        Label text = new Label(m.getContenuMessage());
        text.setWrapText(true);
        
        Label time = new Label(m.getDateMessage().format(DateTimeFormatter.ofPattern("HH:mm")));
        time.setStyle("-fx-font-size: 9px;");

        if (isMine) {
            bubble.setStyle("-fx-background-color: #2a6f5b; -fx-background-radius: 15 15 2 15;");
            text.setStyle("-fx-text-fill: white;");
            time.setStyle("-fx-text-fill: #d4e9e2;");
        } else {
            bubble.setStyle("-fx-background-color: white; -fx-background-radius: 15 15 15 2; -fx-border-color: #dcd8d0; -fx-border-radius: 15 15 15 2;");
            text.setStyle("-fx-text-fill: #1c2824;");
            time.setStyle("-fx-text-fill: #6c757d;");
        }

        bubble.getChildren().addAll(text, time);
        container.getChildren().add(bubble);
        return container;
    }

    @FXML
    public void handleSendMessage() {
        String content = messageInput.getText().trim();
        if (content.isEmpty()) return;

        User currentUser = SecurityController.getCurrentUser();
        if (currentUser == null || recipient == null) return;

        try (Connection conn = DatabaseConnection.getConnection()) {
            String query = "INSERT INTO message (contenu_message, date_message, est_lu, expediteur_id_user, expediteur_role, destinataire_id_user, destinataire_role, id_conversation) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
            PreparedStatement pstmt = conn.prepareStatement(query);
            pstmt.setString(1, content);
            pstmt.setTimestamp(2, new Timestamp(System.currentTimeMillis()));
            pstmt.setBoolean(3, false);
            pstmt.setInt(4, currentUser.getId());
            pstmt.setString(5, currentUser.getRole());
            pstmt.setInt(6, recipient.getId());
            pstmt.setString(7, recipient.getRole());
            pstmt.setInt(8, currentConversation.getId());
            
            pstmt.executeUpdate();
            messageInput.clear();
            loadMessages();
            Platform.runLater(() -> scrollPane.setVvalue(1.0));
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @FXML
    public void handleBack() {
        MainController.getInstance().loadView("/com/psychologie/view/ChatView.fxml");
    }
}
