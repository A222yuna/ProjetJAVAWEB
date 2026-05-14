package com.psychologie.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Appointment {
    public static final String STATUS_SCHEDULED = "SCHEDULED";
    public static final String STATUS_CONFIRMED = "CONFIRMED";
    public static final String STATUS_PAID = "PAID";
    public static final String STATUS_CANCELLED = "CANCELLED";
    public static final String STATUS_COMPLETED = "COMPLETED";

    private Integer id;
    private User patient;
    private PsychologuePlan plan;
    private String status = STATUS_SCHEDULED;
    private LocalDateTime createdAt;

    public static String toDisplayStatus(String status) {
        if (status == null || status.isBlank()) return "";
        return status.toUpperCase();
    }

    /** Returns the canonical hex color for a given status. */
    public static String statusColor(String status) {
        if (status == null) return "#aaaaaa";
        return switch (status.toUpperCase()) {
            case "SCHEDULED"  -> "#f5a623";
            case "CONFIRMED"  -> "#17a2b8";
            case "PAID"       -> "#8e44ad";
            case "COMPLETED"  -> "#2a6f5b";
            case "CANCELLED"  -> "#e74c3c";
            default           -> "#aaaaaa";
        };
    }

    /** Returns a styled Label badge for a given status. */
    public static javafx.scene.control.Label statusBadge(String status) {
        javafx.scene.control.Label label = new javafx.scene.control.Label(toDisplayStatus(status));
        String color = statusColor(status);
        label.setStyle(
            "-fx-background-color: " + color + "; " +
            "-fx-text-fill: white; " +
            "-fx-padding: 3 10; " +
            "-fx-background-radius: 10; " +
            "-fx-font-size: 10px; " +
            "-fx-font-weight: bold;"
        );
        return label;
    }

    public static java.util.List<String> allStatuses() {
        return java.util.List.of(STATUS_SCHEDULED, STATUS_CONFIRMED, STATUS_PAID, STATUS_COMPLETED, STATUS_CANCELLED);
    }
}
