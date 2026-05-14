package com.psychologie.controller.patient;

import com.psychologie.controller.SecurityController;
import com.psychologie.model.User;
import com.psychologie.util.DatabaseConnection;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.*;

import java.sql.*;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.format.TextStyle;
import java.util.*;

public class PatientCalendarController {

    @FXML private Label monthYearLabel;
    @FXML private VBox  calendarContainer;

    private YearMonth currentMonth = YearMonth.now();

    private static final Map<String, String> STATUS_COLOR = new LinkedHashMap<>();
    static {
        STATUS_COLOR.put("SCHEDULED",  "#f5a623");
        STATUS_COLOR.put("CONFIRMED",  "#17a2b8");
        STATUS_COLOR.put("PAID",       "#8e44ad");
        STATUS_COLOR.put("COMPLETED",  "#2a6f5b");
        STATUS_COLOR.put("CANCELLED",  "#e74c3c");
    }

    private static final String[] DAY_HEADERS =
        {"dim.", "lun.", "mar.", "mer.", "jeu.", "ven.", "sam."};

    @FXML public void initialize()  { renderCalendar(); }
    @FXML private void handlePrev()  { currentMonth = currentMonth.minusMonths(1); renderCalendar(); }
    @FXML private void handleNext()  { currentMonth = currentMonth.plusMonths(1);  renderCalendar(); }
    @FXML private void handleToday() { currentMonth = YearMonth.now();             renderCalendar(); }

    // ─────────────────────────────────────────────
    //  Render
    // ─────────────────────────────────────────────

    private void renderCalendar() {
        String monthName = currentMonth.getMonth()
                .getDisplayName(TextStyle.FULL, Locale.FRENCH);
        monthYearLabel.setText(monthName + " " + currentMonth.getYear());

        Map<LocalDate, List<ApptEntry>> byDay = loadAppointments();

        calendarContainer.getChildren().clear();

        // Day-of-week header
        GridPane header = makeGrid();
        for (int col = 0; col < 7; col++) {
            Label lbl = new Label(DAY_HEADERS[col]);
            lbl.setMaxWidth(Double.MAX_VALUE);
            lbl.setAlignment(Pos.CENTER_RIGHT);
            lbl.setPadding(new Insets(8, 10, 8, 10));
            lbl.setStyle("-fx-font-weight: bold; -fx-font-size: 12px; " +
                         "-fx-text-fill: #2a6f5b; " +
                         "-fx-border-color: #dee2e6; -fx-border-width: 0 0 1 0;");
            header.add(lbl, col, 0);
        }
        calendarContainer.getChildren().add(header);

        // Day cells
        LocalDate firstDay    = currentMonth.atDay(1);
        int       startCol    = firstDay.getDayOfWeek().getValue() % 7;
        int       daysInMonth = currentMonth.lengthOfMonth();
        LocalDate today       = LocalDate.now();

        GridPane grid = makeGrid();
        int col = startCol, row = 0;

        // Leading cells (prev month)
        LocalDate prev = firstDay.minusDays(startCol);
        for (int i = 0; i < startCol; i++)
            grid.add(makeDayCell(prev.plusDays(i), byDay, today, true), i, 0);

        // Current month
        for (int day = 1; day <= daysInMonth; day++) {
            LocalDate date = currentMonth.atDay(day);
            grid.add(makeDayCell(date, byDay, today, false), col, row);
            if (++col == 7) { col = 0; row++; }
        }

        // Trailing cells (next month)
        if (col > 0) {
            LocalDate next = currentMonth.atEndOfMonth().plusDays(1);
            for (int i = 0; i < 7 - col; i++)
                grid.add(makeDayCell(next.plusDays(i), byDay, today, true), col + i, row);
        }

        calendarContainer.getChildren().add(grid);
        VBox.setVgrow(grid, Priority.ALWAYS);
    }

    // ─────────────────────────────────────────────
    //  Cell builder
    // ─────────────────────────────────────────────

    private VBox makeDayCell(LocalDate date,
                              Map<LocalDate, List<ApptEntry>> byDay,
                              LocalDate today, boolean otherMonth) {
        VBox cell = new VBox(3);
        cell.setPadding(new Insets(6, 8, 6, 8));
        cell.setMinHeight(90);
        cell.setMaxWidth(Double.MAX_VALUE);
        GridPane.setHgrow(cell, Priority.ALWAYS);
        GridPane.setVgrow(cell, Priority.ALWAYS);

        boolean isToday = date.equals(today);
        String bg = otherMonth ? "#f8f9fa" : isToday ? "#fffde7" : "white";
        cell.setStyle("-fx-background-color:" + bg + ";" +
                      "-fx-border-color:#dee2e6; -fx-border-width:0 1 1 0;");

        // Day number
        Label dayNum = new Label(String.valueOf(date.getDayOfMonth()));
        dayNum.setMaxWidth(Double.MAX_VALUE);
        dayNum.setAlignment(Pos.TOP_RIGHT);
        String numColor = otherMonth ? "#adb5bd" : isToday ? "#0d6efd" : "#495057";
        dayNum.setStyle("-fx-font-size:12px; -fx-text-fill:" + numColor + ";"
                + (isToday ? "-fx-font-weight:bold;" : ""));
        cell.getChildren().add(dayNum);

        // Appointment badges
        for (ApptEntry a : byDay.getOrDefault(date, Collections.emptyList())) {
            String color = STATUS_COLOR.getOrDefault(a.status.toUpperCase(), "#aaaaaa");
            Label badge = new Label("● " + a.psyName);
            badge.setMaxWidth(Double.MAX_VALUE);
            badge.setPadding(new Insets(2, 6, 2, 6));
            badge.setStyle(
                "-fx-background-color:" + color + "22;" +
                "-fx-text-fill:" + color + ";" +
                "-fx-font-size:10px; -fx-font-weight:bold;" +
                "-fx-background-radius:4; -fx-cursor:hand;");
            Tooltip.install(badge, new Tooltip(
                "Dr. " + a.psyName + "\n" +
                a.dayOfWeek + " " + a.period + "\n" +
                "Statut : " + a.status));
            cell.getChildren().add(badge);
        }
        return cell;
    }

    private GridPane makeGrid() {
        GridPane g = new GridPane();
        g.setMaxWidth(Double.MAX_VALUE);
        for (int i = 0; i < 7; i++) {
            ColumnConstraints cc = new ColumnConstraints();
            cc.setPercentWidth(100.0 / 7);
            cc.setHgrow(Priority.ALWAYS);
            g.getColumnConstraints().add(cc);
        }
        return g;
    }

    // ─────────────────────────────────────────────
    //  DB — patient's appointments
    // ─────────────────────────────────────────────

    private Map<LocalDate, List<ApptEntry>> loadAppointments() {
        Map<LocalDate, List<ApptEntry>> map = new HashMap<>();
        User patient = SecurityController.getCurrentUser();
        if (patient == null) return map;

        String q =
            "SELECT a.status, p.day_of_week, p.period, " +
            "       u.prenom, u.nom " +
            "FROM appointments a " +
            "JOIN psychologue_plans p ON a.plan_id = p.id " +
            "JOIN users u ON p.psychologue_id_user = u.id_user " +
            "WHERE a.patient_id_user = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(q)) {
            ps.setInt(1, patient.getId());
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                String dayOfWeek = rs.getString("day_of_week");
                LocalDate date   = findDateInMonth(dayOfWeek, currentMonth);
                if (date == null) continue;

                ApptEntry e = new ApptEntry();
                e.status    = rs.getString("status") != null ? rs.getString("status") : "SCHEDULED";
                e.psyName   = rs.getString("prenom") + " " + rs.getString("nom");
                e.dayOfWeek = dayOfWeek;
                e.period    = rs.getString("period");
                map.computeIfAbsent(date, k -> new ArrayList<>()).add(e);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return map;
    }

    private LocalDate findDateInMonth(String dayOfWeekStr, YearMonth month) {
        try {
            DayOfWeek target = DayOfWeek.valueOf(dayOfWeekStr.toUpperCase());
            LocalDate first  = month.atDay(1);
            for (int i = 0; i < 7; i++)
                if (first.plusDays(i).getDayOfWeek() == target)
                    return first.plusDays(i);
        } catch (IllegalArgumentException ignored) {}
        return null;
    }

    private static class ApptEntry {
        String status, psyName, dayOfWeek, period;
    }
}
