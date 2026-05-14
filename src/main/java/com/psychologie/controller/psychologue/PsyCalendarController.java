package com.psychologie.controller.psychologue;

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

public class PsyCalendarController {

    @FXML private Label  monthYearLabel;
    @FXML private VBox   calendarContainer;

    private YearMonth currentMonth = YearMonth.now();

    // status → colour
    private static final Map<String, String> STATUS_COLOR = new LinkedHashMap<>();
    static {
        STATUS_COLOR.put("SCHEDULED",  "#f5a623");
        STATUS_COLOR.put("CONFIRMED",  "#17a2b8");
        STATUS_COLOR.put("PAID",       "#8e44ad");
        STATUS_COLOR.put("COMPLETED",  "#2a6f5b");
        STATUS_COLOR.put("CANCELLED",  "#e74c3c");
    }

    // day-of-week labels (Sun first, matching Symfony)
    private static final String[] DAY_HEADERS =
        {"dim.", "lun.", "mar.", "mer.", "jeu.", "ven.", "sam."};

    // ─────────────────────────────────────────────
    //  Init
    // ─────────────────────────────────────────────

    @FXML
    public void initialize() {
        renderCalendar();
    }

    // ─────────────────────────────────────────────
    //  Navigation
    // ─────────────────────────────────────────────

    @FXML private void handlePrev()  { currentMonth = currentMonth.minusMonths(1); renderCalendar(); }
    @FXML private void handleNext()  { currentMonth = currentMonth.plusMonths(1);  renderCalendar(); }
    @FXML private void handleToday() { currentMonth = YearMonth.now();             renderCalendar(); }

    // ─────────────────────────────────────────────
    //  Render
    // ─────────────────────────────────────────────

    private void renderCalendar() {
        // Month/year title
        String monthName = currentMonth.getMonth()
                .getDisplayName(TextStyle.FULL, Locale.FRENCH);
        monthYearLabel.setText(monthName + " " + currentMonth.getYear());

        // Load appointments for this month
        Map<LocalDate, List<ApptEntry>> apptsByDay = loadAppointments();

        calendarContainer.getChildren().clear();

        // ── Day-of-week header row ──────────────────
        GridPane header = makeGrid();
        for (int col = 0; col < 7; col++) {
            Label lbl = new Label(DAY_HEADERS[col]);
            lbl.setMaxWidth(Double.MAX_VALUE);
            lbl.setAlignment(Pos.CENTER_RIGHT);
            lbl.setPadding(new Insets(8, 10, 8, 10));
            lbl.setStyle("-fx-font-weight: bold; -fx-font-size: 12px; " +
                         "-fx-text-fill: #17a2b8; " +
                         "-fx-border-color: #dee2e6; -fx-border-width: 0 0 1 0;");
            header.add(lbl, col, 0);
        }
        calendarContainer.getChildren().add(header);

        // ── Day cells ──────────────────────────────
        // First day of month (0=Sun … 6=Sat)
        LocalDate firstDay = currentMonth.atDay(1);
        int startCol = firstDay.getDayOfWeek().getValue() % 7; // Mon=1→1, Sun=7→0
        int daysInMonth = currentMonth.lengthOfMonth();
        LocalDate today = LocalDate.now();

        GridPane grid = makeGrid();
        int col = startCol;
        int row = 0;

        // Fill leading empty cells from previous month
        LocalDate prevMonthDay = firstDay.minusDays(startCol);
        for (int i = 0; i < startCol; i++) {
            grid.add(makeDayCell(prevMonthDay.plusDays(i), apptsByDay, today, true), i, 0);
        }

        // Fill current month days
        for (int day = 1; day <= daysInMonth; day++) {
            LocalDate date = currentMonth.atDay(day);
            grid.add(makeDayCell(date, apptsByDay, today, false), col, row);
            col++;
            if (col == 7) { col = 0; row++; }
        }

        // Fill trailing empty cells
        if (col > 0) {
            LocalDate nextDay = currentMonth.atEndOfMonth().plusDays(1);
            int trailing = 7 - col;
            for (int i = 0; i < trailing; i++) {
                grid.add(makeDayCell(nextDay.plusDays(i), apptsByDay, today, true), col + i, row);
            }
        }

        calendarContainer.getChildren().add(grid);
        VBox.setVgrow(grid, Priority.ALWAYS);
    }

    // ─────────────────────────────────────────────
    //  Cell builder
    // ─────────────────────────────────────────────

    private VBox makeDayCell(LocalDate date,
                              Map<LocalDate, List<ApptEntry>> apptsByDay,
                              LocalDate today,
                              boolean otherMonth) {
        VBox cell = new VBox(3);
        cell.setPadding(new Insets(6, 8, 6, 8));
        cell.setMinHeight(90);
        cell.setMaxWidth(Double.MAX_VALUE);
        GridPane.setHgrow(cell, Priority.ALWAYS);
        GridPane.setVgrow(cell, Priority.ALWAYS);

        boolean isToday = date.equals(today);

        // Background
        String bg = otherMonth ? "#f8f9fa"
                  : isToday    ? "#fffde7"
                  :              "white";
        cell.setStyle("-fx-background-color: " + bg + "; " +
                      "-fx-border-color: #dee2e6; -fx-border-width: 0 1 1 0;");

        // Day number
        Label dayNum = new Label(String.valueOf(date.getDayOfMonth()));
        dayNum.setAlignment(Pos.TOP_RIGHT);
        dayNum.setMaxWidth(Double.MAX_VALUE);
        String numColor = otherMonth ? "#adb5bd"
                        : isToday   ? "#0d6efd"
                        :             "#495057";
        String numWeight = isToday ? "-fx-font-weight: bold;" : "";
        dayNum.setStyle("-fx-font-size: 12px; -fx-text-fill: " + numColor + "; " + numWeight);

        cell.getChildren().add(dayNum);

        // Appointment badges
        List<ApptEntry> appts = apptsByDay.getOrDefault(date, Collections.emptyList());
        for (ApptEntry a : appts) {
            String color = STATUS_COLOR.getOrDefault(a.status.toUpperCase(), "#aaaaaa");
            Label badge = new Label("● " + a.patientName);
            badge.setMaxWidth(Double.MAX_VALUE);
            badge.setPadding(new Insets(2, 6, 2, 6));
            badge.setStyle(
                "-fx-background-color: " + color + "22; " +  // 22 = ~13% opacity
                "-fx-text-fill: " + color + "; " +
                "-fx-font-size: 10px; -fx-font-weight: bold; " +
                "-fx-background-radius: 4; -fx-cursor: hand;");
            badge.setEllipsisString("…");
            badge.setWrapText(false);

            // Tooltip with full info
            Tooltip tip = new Tooltip(
                a.patientName + "\n" +
                a.dayOfWeek + " " + a.period + "\n" +
                "Statut : " + a.status);
            tip.setStyle("-fx-font-size: 11px;");
            Tooltip.install(badge, tip);

            cell.getChildren().add(badge);
        }

        return cell;
    }

    // ─────────────────────────────────────────────
    //  Grid factory
    // ─────────────────────────────────────────────

    private GridPane makeGrid() {
        GridPane g = new GridPane();
        g.setMaxWidth(Double.MAX_VALUE);
        HBox.setHgrow(g, Priority.ALWAYS);
        for (int i = 0; i < 7; i++) {
            ColumnConstraints cc = new ColumnConstraints();
            cc.setPercentWidth(100.0 / 7);
            cc.setHgrow(Priority.ALWAYS);
            g.getColumnConstraints().add(cc);
        }
        return g;
    }

    // ─────────────────────────────────────────────
    //  DB — load appointments for current month
    // ─────────────────────────────────────────────

    private Map<LocalDate, List<ApptEntry>> loadAppointments() {
        Map<LocalDate, List<ApptEntry>> map = new HashMap<>();
        User psy = SecurityController.getCurrentUser();
        if (psy == null) return map;

        // We map appointments to calendar dates using created_at date
        // (appointments don't have a specific date — they reference a plan's day_of_week)
        // So we show them on the NEXT occurrence of that day_of_week within the displayed month
        String query =
            "SELECT a.id, a.status, a.created_at, " +
            "       p.day_of_week, p.period, " +
            "       u.prenom, u.nom " +
            "FROM appointments a " +
            "JOIN psychologue_plans p ON a.plan_id = p.id " +
            "JOIN users u ON a.patient_id_user = u.id_user " +
            "WHERE p.psychologue_id_user = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(query)) {
            ps.setInt(1, psy.getId());
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                String dayOfWeek = rs.getString("day_of_week"); // e.g. "MONDAY"
                String status    = rs.getString("status");
                String prenom    = rs.getString("prenom");
                String nom       = rs.getString("nom");
                String period    = rs.getString("period");

                // Find the date in currentMonth that matches this day_of_week
                LocalDate date = findDateInMonth(dayOfWeek, currentMonth);
                if (date == null) continue;

                ApptEntry entry = new ApptEntry();
                entry.status      = status != null ? status : "SCHEDULED";
                entry.patientName = (prenom + " " + nom).trim();
                entry.dayOfWeek   = dayOfWeek;
                entry.period      = period;

                map.computeIfAbsent(date, k -> new ArrayList<>()).add(entry);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return map;
    }

    /**
     * Returns the first date in the given month that falls on the given day-of-week.
     * e.g. "MONDAY" in May 2026 → 2026-05-04
     */
    private LocalDate findDateInMonth(String dayOfWeekStr, YearMonth month) {
        DayOfWeek target;
        try {
            target = DayOfWeek.valueOf(dayOfWeekStr.toUpperCase());
        } catch (IllegalArgumentException e) {
            return null;
        }
        LocalDate first = month.atDay(1);
        // Walk forward until we hit the right day
        for (int i = 0; i < 7; i++) {
            if (first.plusDays(i).getDayOfWeek() == target) {
                return first.plusDays(i);
            }
        }
        return null;
    }

    // ─────────────────────────────────────────────
    //  Data class
    // ─────────────────────────────────────────────

    private static class ApptEntry {
        String status;
        String patientName;
        String dayOfWeek;
        String period;
    }
}
