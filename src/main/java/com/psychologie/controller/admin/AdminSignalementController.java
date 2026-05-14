package com.psychologie.controller.admin;

import com.psychologie.model.Commentaire;
import com.psychologie.model.Post;
import com.psychologie.model.User;
import com.psychologie.util.DatabaseConnection;
import com.psychologie.controller.MainController;
import com.psychologie.controller.PostDetailController;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import java.sql.*;
import java.time.format.DateTimeFormatter;

public class AdminSignalementController {

    @FXML private TableView<ReportRow> reportTable;
    @FXML private TableColumn<ReportRow, String> dateColumn;
    @FXML private TableColumn<ReportRow, String> reporterColumn;
    @FXML private TableColumn<ReportRow, String> targetTypeColumn;
    @FXML private TableColumn<ReportRow, String> reasonColumn;
    @FXML private TableColumn<ReportRow, Void> actionsColumn;

    private final ObservableList<ReportRow> allReports = FXCollections.observableArrayList();
    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    public static class ReportRow {
        int idReport;
        int idPost;
        int idComment;
        String date;
        String reporterName;
        String targetType;
        String reason;
        String targetPreview;

        public ReportRow(int idReport, int idPost, int idComment, String date, String reporterName, String targetType, String reason, String targetPreview) {
            this.idReport = idReport;
            this.idPost = idPost;
            this.idComment = idComment;
            this.date = date;
            this.reporterName = reporterName;
            this.targetType = targetType;
            this.reason = reason;
            this.targetPreview = targetPreview;
        }
    }

    @FXML
    public void initialize() {
        setupTable();
        loadReports();
    }

    private void setupTable() {
        dateColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().date));
        reporterColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().reporterName));
        targetTypeColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().targetType));
        reasonColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().reason));

        actionsColumn.setCellFactory(param -> new TableCell<ReportRow, Void>() {
            private final Button viewBtn = new Button("Voir");
            private final Button deleteBtn = new Button("Supprimer Cible");
            private final Button dismissBtn = new Button("Ignorer");
            private final HBox box = new HBox(5, viewBtn, deleteBtn, dismissBtn);

            {
                viewBtn.setStyle("-fx-background-color: #3498db; -fx-text-fill: white; -fx-font-size: 10px; -fx-cursor: hand;");
                deleteBtn.setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white; -fx-font-size: 10px; -fx-cursor: hand;");
                dismissBtn.setStyle("-fx-background-color: #95a5a6; -fx-text-fill: white; -fx-font-size: 10px; -fx-cursor: hand;");
                
                viewBtn.setOnAction(e -> handleView(getTableView().getItems().get(getIndex())));
                deleteBtn.setOnAction(e -> handleDeleteTarget(getTableView().getItems().get(getIndex())));
                dismissBtn.setOnAction(e -> handleDismiss(getTableView().getItems().get(getIndex())));
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) setGraphic(null);
                else setGraphic(box);
            }
        });

        reportTable.setItems(allReports);
    }

    private void loadReports() {
        allReports.clear();
        String query = "SELECT r.*, u.prenom, u.nom, p.titre as post_titre, c.contenu as comment_text " +
                       "FROM forum_reports r " +
                       "LEFT JOIN users u ON r.id_user_reporter = u.id_user " +
                       "LEFT JOIN post p ON r.id_post = p.id_post " +
                       "LEFT JOIN commentaire c ON r.id_comment = c.id_comment " +
                       "WHERE r.status = 'OPEN' " +
                       "ORDER BY r.date DESC";

        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {

            while (rs.next()) {
                String targetType = rs.getInt("id_post") > 0 ? "POST" : "COMMENTAIRE";
                String preview = targetType.equals("POST") ? rs.getString("post_titre") : rs.getString("comment_text");
                if (preview != null && preview.length() > 30) preview = preview.substring(0, 27) + "...";

                allReports.add(new ReportRow(
                    rs.getInt("id_report"),
                    rs.getInt("id_post"),
                    rs.getInt("id_comment"),
                    rs.getTimestamp("date").toLocalDateTime().format(formatter),
                    rs.getString("prenom") + " " + rs.getString("nom"),
                    targetType,
                    rs.getString("reason"),
                    preview
                ));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void handleView(ReportRow row) {
        if (row.idPost > 0) {
            try (Connection conn = DatabaseConnection.getConnection();
                 PreparedStatement pstmt = conn.prepareStatement("SELECT * FROM post WHERE id_post = ?")) {
                pstmt.setInt(1, row.idPost);
                ResultSet rs = pstmt.executeQuery();
                if (rs.next()) {
                    Post p = new Post();
                    p.setId(rs.getInt("id_post"));
                    p.setTitre(rs.getString("titre"));
                    p.setContenu(rs.getString("contenu"));
                    User author = new User();
                    author.setId(rs.getInt("auteur_id_user"));
                    p.setAuteur(author);
                    PostDetailController.setPost(p);
                    MainController.getInstance().loadView("/com/psychologie/view/PostDetailView.fxml");
                }
            } catch (SQLException e) { e.printStackTrace(); }
        } else {
            // For comments, we'd ideally scroll to it, but for now just showing an alert with text
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Détail du commentaire signalé");
            alert.setHeaderText("Cible: Commentaire");
            alert.setContentText("Texte: " + row.targetPreview);
            alert.showAndWait();
        }
    }

    private void handleDeleteTarget(ReportRow row) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION, "Supprimer définitivement le contenu signalé ?", ButtonType.YES, ButtonType.NO);
        alert.showAndWait().ifPresent(res -> {
            if (res == ButtonType.YES) {
                try (Connection conn = DatabaseConnection.getConnection()) {
                    if (row.idPost > 0) {
                        PreparedStatement pstmt = conn.prepareStatement("DELETE FROM post WHERE id_post = ?");
                        pstmt.setInt(1, row.idPost);
                        pstmt.executeUpdate();
                    } else {
                        PreparedStatement pstmt = conn.prepareStatement("DELETE FROM commentaire WHERE id_comment = ?");
                        pstmt.setInt(1, row.idComment);
                        pstmt.executeUpdate();
                    }
                    // Resolve report
                    PreparedStatement pstmtRes = conn.prepareStatement("UPDATE forum_reports SET status = 'RESOLVED' WHERE id_report = ?");
                    pstmtRes.setInt(1, row.idReport);
                    pstmtRes.executeUpdate();
                    loadReports();
                } catch (SQLException e) { e.printStackTrace(); }
            }
        });
    }

    private void handleDismiss(ReportRow row) {
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement("UPDATE forum_reports SET status = 'DISMISSED' WHERE id_report = ?")) {
            pstmt.setInt(1, row.idReport);
            pstmt.executeUpdate();
            loadReports();
        } catch (SQLException e) { e.printStackTrace(); }
    }
}
