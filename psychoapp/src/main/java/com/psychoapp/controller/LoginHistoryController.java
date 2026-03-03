package com.psychoapp.controller;

import com.psychoapp.util.DatabaseConnection;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;

import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ResourceBundle;

public class LoginHistoryController implements Initializable {

    @FXML private TableView<HistoryRow>          tableView;
    @FXML private TableColumn<HistoryRow, String>  colEmail;
    @FXML private TableColumn<HistoryRow, String>  colRole;
    @FXML private TableColumn<HistoryRow, String>  colTime;
    @FXML private Label                          statusLabel;

    private final ObservableList<HistoryRow> data = FXCollections.observableArrayList();

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        colEmail.setCellValueFactory(new PropertyValueFactory<>("email"));
        colRole.setCellValueFactory(new PropertyValueFactory<>("role"));
        colTime.setCellValueFactory(cell ->
                new javafx.beans.property.SimpleStringProperty(
                        cell.getValue().loginTime == null
                                ? ""
                                : cell.getValue().loginTime.toString()
                ));
        tableView.setItems(data);
        loadLogs();
    }

    @FXML
    private void handleRefresh() {
        loadLogs();
    }

    private void loadLogs() {
        data.clear();
        String sql = "SELECT email, role, derniere_connexion " +
                     "FROM users WHERE derniere_connexion IS NOT NULL " +
                     "ORDER BY derniere_connexion DESC LIMIT 200";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                HistoryRow row = new HistoryRow();
                row.email = rs.getString("email");
                row.role  = rs.getString("role");
                Timestamp ts = rs.getTimestamp("derniere_connexion");
                row.loginTime = ts != null ? ts.toLocalDateTime() : null;
                data.add(row);
            }
            if (statusLabel != null) {
                statusLabel.setText(data.size() + " connexions affichees.");
            }
        } catch (SQLException e) {
            if (statusLabel != null) {
                statusLabel.setText("Erreur : " + e.getMessage());
            }
        }
    }

    // simple row holder, no new table needed
    public static class HistoryRow {
        public String email;
        public String role;
        public LocalDateTime loginTime;

        public String getEmail()     { return email; }
        public String getRole()      { return role; }
    }
}

