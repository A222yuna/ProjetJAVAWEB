package com.psychologie.controller.psychologue;

import com.psychologie.controller.SecurityController;
import com.psychologie.model.User;
import com.psychologie.util.DatabaseConnection;
import com.psychologie.util.ExcelExportService;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.chart.*;
import javafx.scene.control.*;
import javafx.stage.FileChooser;

import java.io.File;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.sql.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class PsyStatsController {

    @FXML private Label totalLabel;
    @FXML private Label scheduledLabel;
    @FXML private Label confirmedLabel;
    @FXML private Label paidLabel;
    @FXML private Label completedLabel;
    @FXML private Label cancelledLabel;

    @FXML private LineChart<String, Number> monthlyChart;
    @FXML private CategoryAxis monthAxis;
    @FXML private PieChart statusChart;

    // Fixed order → index 0,1,2… maps to .default-color0,1,2…
    private static final LinkedHashMap<String, String> STATUS_COLORS = new LinkedHashMap<>();
    static {
        STATUS_COLORS.put("SCHEDULED",  "#f5a623");
        STATUS_COLORS.put("CONFIRMED",  "#17a2b8");
        STATUS_COLORS.put("PAID",       "#8e44ad");
        STATUS_COLORS.put("COMPLETED",  "#2a6f5b");
        STATUS_COLORS.put("CANCELLED",  "#e74c3c");
    }

    @FXML
    public void initialize() {
        monthAxis.setTickLabelRotation(-45);
        loadStats();
    }

    private void loadStats() {
        User user = SecurityController.getCurrentUser();
        if (user == null) return;
        loadCounters(user.getId());
        loadMonthlyChart(user.getId());
        loadStatusChart(user.getId());
    }

    // ── Counters ─────────────────────────────────

    private void loadCounters(int psyId) {
        String q =
            "SELECT COUNT(*) total, " +
            "COALESCE(SUM(a.status='SCHEDULED'),0) scheduled, " +
            "COALESCE(SUM(a.status='CONFIRMED'),0) confirmed, " +
            "COALESCE(SUM(a.status='PAID'),0)      paid, " +
            "COALESCE(SUM(a.status='COMPLETED'),0) completed, " +
            "COALESCE(SUM(a.status='CANCELLED'),0) cancelled " +
            "FROM appointments a " +
            "JOIN psychologue_plans p ON a.plan_id = p.id " +
            "WHERE p.psychologue_id_user = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(q)) {
            ps.setInt(1, psyId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                totalLabel    .setText(String.valueOf(rs.getInt("total")));
                scheduledLabel.setText(String.valueOf(rs.getInt("scheduled")));
                confirmedLabel.setText(String.valueOf(rs.getInt("confirmed")));
                paidLabel     .setText(String.valueOf(rs.getInt("paid")));
                completedLabel.setText(String.valueOf(rs.getInt("completed")));
                cancelledLabel.setText(String.valueOf(rs.getInt("cancelled")));
            }
        } catch (SQLException e) { e.printStackTrace(); }
    }

    // ── Monthly line chart ────────────────────────

    private void loadMonthlyChart(int psyId) {
        monthlyChart.getData().clear();
        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Nombre de RDV");

        LocalDate start = LocalDate.now().minusMonths(11).withDayOfMonth(1);
        for (int i = 0; i < 12; i++)
            series.getData().add(new XYChart.Data<>(
                start.plusMonths(i).format(DateTimeFormatter.ofPattern("yyyy-MM")), 0));

        String q =
            "SELECT DATE_FORMAT(a.created_at,'%Y-%m') mk, COUNT(*) n " +
            "FROM appointments a JOIN psychologue_plans p ON a.plan_id=p.id " +
            "WHERE p.psychologue_id_user=? AND a.created_at>=? " +
            "GROUP BY mk ORDER BY mk";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(q)) {
            ps.setInt(1, psyId);
            ps.setDate(2, Date.valueOf(start));
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                String key = rs.getString("mk");
                int    cnt = rs.getInt("n");
                series.getData().stream()
                      .filter(d -> d.getXValue().equals(key))
                      .findFirst()
                      .ifPresent(d -> d.setYValue(cnt));
            }
        } catch (SQLException e) { e.printStackTrace(); }

        monthlyChart.getData().add(series);
    }

    // ── Pie chart ─────────────────────────────────
    // JavaFX PieChart ignores node.setStyle("-fx-pie-color") reliably only when
    // the color is set via CSS .default-colorN selectors BEFORE the chart renders.
    // We inject a data-URI stylesheet so the colors are baked in at layout time.

    private void loadStatusChart(int psyId) {
        // 1. Fetch counts
        Map<String, Integer> counts = new LinkedHashMap<>();
        String q =
            "SELECT a.status, COUNT(*) n " +
            "FROM appointments a JOIN psychologue_plans p ON a.plan_id=p.id " +
            "WHERE p.psychologue_id_user=? GROUP BY a.status";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(q)) {
            ps.setInt(1, psyId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                String s = rs.getString("status");
                if (s != null && !s.isBlank())
                    counts.put(s.toUpperCase(), rs.getInt("n"));
            }
        } catch (SQLException e) { e.printStackTrace(); }

        // 2. Build slices in fixed order — index position determines default-colorN
        ObservableList<PieChart.Data> pieData = FXCollections.observableArrayList();
        List<String> sliceOrder = new ArrayList<>();
        for (String status : STATUS_COLORS.keySet()) {
            if (counts.containsKey(status)) {
                pieData.add(new PieChart.Data(status, counts.get(status)));
                sliceOrder.add(status);
            }
        }

        // 3. Build CSS that overrides default-colorN for each slice index
        StringBuilder css = new StringBuilder();
        for (int i = 0; i < sliceOrder.size(); i++) {
            String hex = STATUS_COLORS.get(sliceOrder.get(i));
            // Slice fill
            css.append(".default-color").append(i)
               .append(".chart-pie{-fx-pie-color:").append(hex).append(";}\n");
            // Legend symbol
            css.append(".default-color").append(i)
               .append(".chart-legend-item-symbol{-fx-background-color:").append(hex).append(";}\n");
        }

        // 4. Inject stylesheet BEFORE setting data so JavaFX picks it up on first layout
        statusChart.getStylesheets().clear();
        String encoded = URLEncoder.encode(css.toString(), StandardCharsets.UTF_8)
                                   .replace("+", "%20");
        statusChart.getStylesheets().add("data:text/css," + encoded);

        // 5. Set data
        statusChart.setAnimated(false);
        statusChart.setLabelsVisible(false);
        statusChart.setData(pieData);
    }

    // ── Excel export ──────────────────────────────

    @FXML
    public void handleExportExcel() {
        User user = SecurityController.getCurrentUser();
        if (user == null) return;

        FileChooser chooser = new FileChooser();
        chooser.setTitle("Enregistrer le fichier Excel");
        chooser.setInitialFileName("statistiques_" +
            LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")) + ".xlsx");
        chooser.getExtensionFilters().add(
            new FileChooser.ExtensionFilter("Fichier Excel (*.xlsx)", "*.xlsx"));

        File dest = chooser.showSaveDialog(totalLabel.getScene().getWindow());
        if (dest == null) return;

        try {
            ExcelExportService.export(user.getId(), user.getPrenom() + " " + user.getNom(), dest);
            Alert ok = new Alert(Alert.AlertType.INFORMATION);
            ok.setTitle("Export réussi");
            ok.setHeaderText(null);
            ok.setContentText("Fichier enregistré :\n" + dest.getAbsolutePath());
            ok.showAndWait();
            try { java.awt.Desktop.getDesktop().open(dest); } catch (Exception ignored) {}
        } catch (Exception e) {
            e.printStackTrace();
            new Alert(Alert.AlertType.ERROR, "Erreur : " + e.getMessage()).showAndWait();
        }
    }
}
