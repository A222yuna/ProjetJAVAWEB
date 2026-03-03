package org.example.controller;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

import org.example.dao.AppointmentDAO;
import org.example.dao.PsychologuePlanDAO;
import org.example.dao.UserDAO;
import org.example.model.Appointment;
import org.example.model.PsychologuePlan;
import org.example.model.User;
import org.example.util.NotificationService;
import org.example.util.ValidationUtil;
import org.example.util.session.SessionManager;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.Separator;
import javafx.scene.control.Spinner;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

public class PsychologueDashboard {

    public static VBox createDashboard() {
        VBox mainVBox = new VBox();
        mainVBox.setStyle("-fx-background-color: #f5f5f5;");

        HBox header = createHeader();

        TabPane tabPane = new TabPane();
        tabPane.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);
        tabPane.setStyle("-fx-font-size: 12;");

        Tab appointmentsTab = new Tab("Appointments", createAppointmentsTab());
        Tab scheduleTab = new Tab("My Schedule", createScheduleTab());
        Tab profileTab = new Tab("Profile", createProfileTab());

        tabPane.getTabs().addAll(appointmentsTab, scheduleTab, profileTab);
        VBox.setVgrow(tabPane, Priority.ALWAYS);
        mainVBox.getChildren().addAll(header, tabPane);
        return mainVBox;
    }

    // ── Header ────────────────────────────────────────────────────────────────
    private static HBox createHeader() {
        HBox header = new HBox();
        header.setPadding(new Insets(20));
        header.setStyle("-fx-background-color: #764ba2;");
        header.setAlignment(Pos.CENTER_LEFT);
        header.setSpacing(20);

        Label titleLabel = new Label("Psychologue Dashboard");
        titleLabel.setFont(Font.font("Segoe UI", FontWeight.BOLD, 20));
        titleLabel.setTextFill(Color.WHITE);

        Label welcomeLabel = new Label("Welcome, " + SessionManager.getCurrentUser().getFullName());
        welcomeLabel.setFont(Font.font("Segoe UI", 14));
        welcomeLabel.setTextFill(Color.WHITE);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button logoutButton = new Button("Logout");
        logoutButton.setStyle("-fx-background-color: #ff6b6b; -fx-text-fill: white; "
                + "-fx-font-weight: bold; -fx-padding: 8 20;");
        logoutButton.setOnAction(e -> MainController.showLogin(MainController.getPrimaryStage()));

        header.getChildren().addAll(titleLabel, welcomeLabel, spacer, logoutButton);
        return header;
    }

    // ── Appointments tab ──────────────────────────────────────────────────────
    private static VBox createAppointmentsTab() {
        VBox vbox = new VBox(15);
        vbox.setPadding(new Insets(20));
        vbox.setStyle("-fx-background-color: #f5f5f5;");

        int psychologueId = SessionManager.getCurrentUser().getId();
        ObservableList<Appointment> appointments
                = FXCollections.observableArrayList(AppointmentDAO.getAppointmentsByPsychologue(psychologueId));

        HBox refreshBox = new HBox(10);
        refreshBox.setAlignment(Pos.TOP_RIGHT);

        Button refreshButton = new Button("🔄 Refresh");
        refreshButton.setStyle("-fx-background-color: #764ba2; -fx-text-fill: white; "
                + "-fx-font-weight: bold; -fx-padding: 8 15; -fx-font-size: 12;");
        Label refreshStatus = new Label();
        refreshStatus.setStyle("-fx-font-size: 11; -fx-text-fill: #666;");

        refreshButton.setOnAction(e -> {
            refreshStatus.setText("Refreshing…");
            refreshButton.setDisable(true);
            new Thread(() -> {
                try {
                    Thread.sleep(500);
                } catch (InterruptedException ignored) {
                }
                ObservableList<Appointment> updated = FXCollections.observableArrayList(
                        AppointmentDAO.getAppointmentsByPsychologue(psychologueId));
                Platform.runLater(() -> {
                    appointments.setAll(updated);
                    refreshStatus.setText("✓ Updated at "
                            + LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss")));
                    refreshButton.setDisable(false);
                });
            }).start();
        });

        refreshBox.getChildren().addAll(refreshButton, refreshStatus);
        vbox.getChildren().add(refreshBox);

        if (appointments.isEmpty()) {
            Label noAppt = new Label("No appointments scheduled.");
            noAppt.setStyle("-fx-font-size: 14; -fx-text-fill: #999;");
            vbox.getChildren().add(noAppt);
            return vbox;
        }

        TableView<Appointment> table = new TableView<>(appointments);
        table.setPrefHeight(400);

        TableColumn<Appointment, Integer> idCol = new TableColumn<>("ID");
        idCol.setCellValueFactory(c -> new javafx.beans.property.SimpleIntegerProperty(c.getValue().getId()).asObject());
        idCol.setPrefWidth(50);

        TableColumn<Appointment, String> patientCol = new TableColumn<>("Patient");
        patientCol.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(c.getValue().getPatientName()));
        patientCol.setPrefWidth(150);

        TableColumn<Appointment, String> dayCol = new TableColumn<>("Day");
        dayCol.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(c.getValue().getDayOfWeek()));
        dayCol.setPrefWidth(100);

        TableColumn<Appointment, String> periodCol = new TableColumn<>("Period");
        periodCol.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(c.getValue().getPeriod()));
        periodCol.setPrefWidth(100);

        TableColumn<Appointment, String> statusCol = new TableColumn<>("Status");
        statusCol.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(c.getValue().getStatus()));
        statusCol.setPrefWidth(120);

        // Actions: Complete / Cancel with confirmation
        TableColumn<Appointment, Void> actionCol = new TableColumn<>("Actions");
        actionCol.setPrefWidth(200);
        actionCol.setCellFactory(param -> new TableCell<>() {
            private final Button completeBtn = new Button("Complete");
            private final Button cancelBtn = new Button("Cancel");
            private final HBox box = new HBox(5, completeBtn, cancelBtn);

            {
                completeBtn.setStyle("-fx-background-color: #51cf66; -fx-text-fill: white; -fx-padding: 5 10;");
                cancelBtn.setStyle("-fx-background-color: #ff6b6b; -fx-text-fill: white; -fx-padding: 5 10;");

                completeBtn.setOnAction(event -> {
                    Appointment appt = getTableView().getItems().get(getIndex());

                    // ── Validation ────────────────────────────────────────────
                    if (!"SCHEDULED".equals(appt.getStatus())) {
                        showAlert(Alert.AlertType.WARNING, "Invalid Action",
                                "Only SCHEDULED appointments can be marked as completed.\n"
                                + "Current status: " + appt.getStatus());
                        return;
                    }

                    Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
                    confirm.setTitle("Complete Appointment");
                    confirm.setHeaderText("Mark as completed?");
                    confirm.setContentText("Patient: " + appt.getPatientName()
                            + "\nDay: " + appt.getDayOfWeek()
                            + "\nPeriod: " + appt.getPeriod());
                    confirm.showAndWait().ifPresent(r -> {
                        if (r == ButtonType.OK) {
                            if (AppointmentDAO.updateAppointmentStatus(appt.getId(), "COMPLETED")) {
                                appt.setStatus("COMPLETED");
                                getTableView().refresh();
                            } else {
                                showAlert(Alert.AlertType.ERROR, "Error", "Update failed. Please try again.");
                            }
                        }
                    });
                });

                cancelBtn.setOnAction(event -> {
                    Appointment appt = getTableView().getItems().get(getIndex());

                    if (!"SCHEDULED".equals(appt.getStatus())) {
                        showAlert(Alert.AlertType.WARNING, "Invalid Action",
                                "Only SCHEDULED appointments can be cancelled.\n"
                                + "Current status: " + appt.getStatus());
                        return;
                    }

                    Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
                    confirm.setTitle("Cancel Appointment");
                    confirm.setHeaderText("Cancel this appointment?");
                    confirm.setContentText("Patient: " + appt.getPatientName()
                            + "\nDay: " + appt.getDayOfWeek()
                            + "\nPeriod: " + appt.getPeriod()
                            + "\n\nThe patient will lose their slot.");
                    confirm.showAndWait().ifPresent(r -> {
                        if (r == ButtonType.OK) {
                            if (AppointmentDAO.updateAppointmentStatus(appt.getId(), "CANCELLED")) {
                                // Notify patient
                                org.example.model.User patientUser = org.example.dao.UserDAO.getUserById(appt.getPatientId());
                                if (patientUser != null) {
                                    NotificationService.notifyAppointmentCancelled(
                                            patientUser.getFullName(), patientUser.getEmail(), patientUser.getPhone(),
                                            appt.getDayOfWeek(), appt.getPeriod(), SessionManager.getCurrentUser().getFullName());
                                }
                                appt.setStatus("CANCELLED");
                                getTableView().refresh();
                            } else {
                                showAlert(Alert.AlertType.ERROR, "Error", "Update failed. Please try again.");
                            }
                        }
                    });
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || getIndex() < 0) {
                    setGraphic(null);
                    return;
                }
                Appointment appt = getTableView().getItems().get(getIndex());
                setGraphic("SCHEDULED".equals(appt.getStatus()) ? box : null);
            }
        });

        table.getColumns().addAll(idCol, patientCol, dayCol, periodCol, statusCol, actionCol);
        VBox.setVgrow(table, Priority.ALWAYS);
        vbox.getChildren().addAll(new Label("Patient Appointments"), table);
        return vbox;
    }

    // ── Schedule tab ──────────────────────────────────────────────────────────
    private static VBox createScheduleTab() {
        VBox vbox = new VBox(15);
        vbox.setPadding(new Insets(20));
        vbox.setStyle("-fx-background-color: #f5f5f5;");

        int psychologueId = SessionManager.getCurrentUser().getId();
        ObservableList<PsychologuePlan> plans
                = FXCollections.observableArrayList(PsychologuePlanDAO.getPlansByPsychologue(psychologueId));

        // ── Add schedule form ──────────────────────────────────────────────────
        HBox addPlanBox = new HBox(10);
        addPlanBox.setPadding(new Insets(15));
        addPlanBox.setAlignment(Pos.CENTER_LEFT);
        addPlanBox.setStyle("-fx-background-color: white; -fx-border-color: #ddd; -fx-border-radius: 5;");

        ComboBox<String> dayCombo = new ComboBox<>();
        dayCombo.setItems(FXCollections.observableArrayList(
                "MONDAY", "TUESDAY", "WEDNESDAY", "THURSDAY", "FRIDAY", "SATURDAY", "SUNDAY"));
        dayCombo.setPromptText("Select Day");
        dayCombo.setPrefWidth(130);

        ComboBox<String> periodCombo = new ComboBox<>();
        periodCombo.setItems(FXCollections.observableArrayList("DAY", "NIGHT"));
        periodCombo.setPromptText("Select Period");
        periodCombo.setPrefWidth(120);

        Spinner<Integer> maxSpinner = new Spinner<>(1, 20, 5);
        maxSpinner.setPrefWidth(100);
        maxSpinner.setEditable(true); // allow manual entry

        Label statusLabel = new Label();
        statusLabel.setStyle("-fx-font-size: 12;");

        Button addButton = new Button("Add Schedule");
        addButton.setStyle("-fx-background-color: #764ba2; -fx-text-fill: white; "
                + "-fx-font-weight: bold; -fx-padding: 8 15;");

        addButton.setOnAction(e -> {
            // ── Validation ─────────────────────────────────────────────────────
            boolean hasError = false;

            if (dayCombo.getValue() == null) {
                statusLabel.setText("⚠ Please select a day.");
                statusLabel.setTextFill(Color.RED);
                hasError = true;
            }
            if (periodCombo.getValue() == null) {
                statusLabel.setText("⚠ Please select a period.");
                statusLabel.setTextFill(Color.RED);
                hasError = true;
            }
            if (dayCombo.getValue() == null || periodCombo.getValue() == null) {
                return;
            }

            // Validate max appointments (in case user typed manually in spinner)
            int maxValue;
            try {
                maxValue = Integer.parseInt(maxSpinner.getEditor().getText().trim());
            } catch (NumberFormatException ex) {
                statusLabel.setText("⚠ Max appointments must be a number.");
                statusLabel.setTextFill(Color.RED);
                return;
            }
            String maxError = ValidationUtil.validateMaxAppointments(maxValue);
            if (maxError != null) {
                statusLabel.setText("⚠ " + maxError);
                statusLabel.setTextFill(Color.RED);
                return;
            }

            // ── Check for duplicate slot (same day + period for this psychologue) ──
            boolean duplicate = plans.stream().anyMatch(p
                    -> p.getDayOfWeek().equals(dayCombo.getValue())
                    && p.getPeriod().equals(periodCombo.getValue()));
            if (duplicate) {
                statusLabel.setText("⚠ You already have a slot on "
                        + dayCombo.getValue() + " – " + periodCombo.getValue() + ".");
                statusLabel.setTextFill(Color.RED);
                return;
            }

            // ── All valid → persist ────────────────────────────────────────────
            PsychologuePlan plan = new PsychologuePlan(
                    psychologueId, dayCombo.getValue(), periodCombo.getValue(), maxValue);

            if (PsychologuePlanDAO.createPlan(plan)) {
                // Re-fetch to get the auto-generated ID
                ObservableList<PsychologuePlan> fresh
                        = FXCollections.observableArrayList(
                                PsychologuePlanDAO.getPlansByPsychologue(psychologueId));
                plans.setAll(fresh);

                dayCombo.setValue(null);
                periodCombo.setValue(null);
                maxSpinner.getValueFactory().setValue(5);
                statusLabel.setText("✓ Schedule slot added successfully!");
                statusLabel.setTextFill(Color.GREEN);
            } else {
                statusLabel.setText("✗ Failed to add schedule. Please try again.");
                statusLabel.setTextFill(Color.RED);
            }
        });

        addPlanBox.getChildren().addAll(
                new Label("Day:"), dayCombo,
                new Label("Period:"), periodCombo,
                new Label("Max Appts:"), maxSpinner,
                addButton);

        // ── Refresh button ─────────────────────────────────────────────────────
        HBox refreshBox = new HBox(10);
        refreshBox.setAlignment(Pos.TOP_RIGHT);
        refreshBox.setPadding(new Insets(10, 0, 0, 0));

        Button refreshButton = new Button("🔄 Refresh Schedules");
        refreshButton.setStyle("-fx-background-color: #764ba2; -fx-text-fill: white; "
                + "-fx-font-weight: bold; -fx-padding: 8 15; -fx-font-size: 12;");
        Label refreshStatus = new Label();
        refreshStatus.setStyle("-fx-font-size: 11; -fx-text-fill: #666;");

        refreshButton.setOnAction(e -> {
            refreshStatus.setText("Refreshing…");
            refreshButton.setDisable(true);
            new Thread(() -> {
                try {
                    Thread.sleep(500);
                } catch (InterruptedException ignored) {
                }
                ObservableList<PsychologuePlan> updated = FXCollections.observableArrayList(
                        PsychologuePlanDAO.getPlansByPsychologue(psychologueId));
                Platform.runLater(() -> {
                    plans.setAll(updated);
                    refreshStatus.setText("✓ Updated at "
                            + LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss")));
                    refreshButton.setDisable(false);
                });
            }).start();
        });

        refreshBox.getChildren().addAll(refreshButton, refreshStatus);

        // ── Schedule table ─────────────────────────────────────────────────────
        TableView<PsychologuePlan> table = new TableView<>(plans);
        table.setPrefHeight(300);

        TableColumn<PsychologuePlan, Integer> idCol = new TableColumn<>("ID");
        idCol.setCellValueFactory(c -> new javafx.beans.property.SimpleIntegerProperty(c.getValue().getId()).asObject());
        idCol.setPrefWidth(50);

        TableColumn<PsychologuePlan, String> dayCol = new TableColumn<>("Day");
        dayCol.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(c.getValue().getDayOfWeek()));
        dayCol.setPrefWidth(120);

        TableColumn<PsychologuePlan, String> periodCol = new TableColumn<>("Period");
        periodCol.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(c.getValue().getPeriod()));
        periodCol.setPrefWidth(100);

        TableColumn<PsychologuePlan, Integer> maxCol = new TableColumn<>("Max Appointments");
        maxCol.setCellValueFactory(c -> new javafx.beans.property.SimpleIntegerProperty(c.getValue().getMaxAppointments()).asObject());
        maxCol.setPrefWidth(150);

        // Booked count column
        TableColumn<PsychologuePlan, Integer> bookedCol = new TableColumn<>("Booked");
        bookedCol.setCellValueFactory(c -> new javafx.beans.property.SimpleIntegerProperty(
                AppointmentDAO.countAppointmentsForPlan(c.getValue().getId())).asObject());
        bookedCol.setPrefWidth(80);

        // Delete with confirmation + block if appointments exist
        TableColumn<PsychologuePlan, Void> actionCol = new TableColumn<>("Action");
        actionCol.setPrefWidth(100);
        actionCol.setCellFactory(param -> new TableCell<>() {
            private final Button deleteBtn = new Button("Delete");

            {
                deleteBtn.setStyle("-fx-background-color: #ff6b6b; -fx-text-fill: white; -fx-padding: 5 10;");
                deleteBtn.setOnAction(event -> {
                    PsychologuePlan plan = getTableView().getItems().get(getIndex());

                    // ── Validation: block deletion if active appointments exist ──
                    int booked = AppointmentDAO.countAppointmentsForPlan(plan.getId());
                    if (booked > 0) {
                        showAlert(Alert.AlertType.WARNING,
                                "Cannot Delete",
                                "This slot has " + booked + " active (SCHEDULED) appointment(s).\n"
                                + "Please cancel or complete them first.");
                        return;
                    }

                    // ── Confirmation ───────────────────────────────────────────
                    Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
                    confirm.setTitle("Delete Schedule Slot");
                    confirm.setHeaderText("Delete this slot?");
                    confirm.setContentText("Day: " + plan.getDayOfWeek()
                            + "\nPeriod: " + plan.getPeriod()
                            + "\n\nThis action cannot be undone.");
                    confirm.showAndWait().ifPresent(r -> {
                        if (r == ButtonType.OK) {
                            if (PsychologuePlanDAO.deletePlan(plan.getId())) {
                                getTableView().getItems().remove(getIndex());
                            } else {
                                showAlert(Alert.AlertType.ERROR, "Error",
                                        "Failed to delete slot. Please try again.");
                            }
                        }
                    });
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : deleteBtn);
            }
        });

        table.getColumns().addAll(idCol, dayCol, periodCol, maxCol, bookedCol, actionCol);
        VBox.setVgrow(table, Priority.ALWAYS);

        vbox.getChildren().addAll(
                new Label("Manage Your Schedule"),
                addPlanBox,
                statusLabel,
                new Separator(),
                refreshBox,
                new Label("Your Time Slots"),
                table);

        return vbox;
    }

    // ── Profile tab ───────────────────────────────────────────────────────────
    private static VBox createProfileTab() {
        VBox vbox = new VBox(15);
        vbox.setPadding(new Insets(20));
        vbox.setStyle("-fx-background-color: #f5f5f5;");

        User user = SessionManager.getCurrentUser();

        Button refreshButton = new Button("🔄 Refresh Profile");
        refreshButton.setStyle("-fx-background-color: #764ba2; -fx-text-fill: white; "
                + "-fx-font-weight: bold; -fx-padding: 8 15;");
        Label refreshStatus = new Label();
        refreshStatus.setStyle("-fx-font-size: 11; -fx-text-fill: #666;");

        VBox profileBox = new VBox(10);
        profileBox.setPadding(new Insets(20));
        profileBox.setStyle("-fx-background-color: white; -fx-border-color: #ddd; -fx-border-radius: 5;");

        addProfileField(profileBox, "Username", user.getUsername());
        addProfileField(profileBox, "Full Name", user.getFullName());
        addProfileField(profileBox, "Email", user.getEmail());
        addProfileField(profileBox, "Phone", user.getPhone());
        addProfileField(profileBox, "Role", user.getRole());

        refreshButton.setOnAction(e -> {
            refreshStatus.setText("Refreshing…");
            refreshButton.setDisable(true);
            new Thread(() -> {
                try {
                    Thread.sleep(500);
                } catch (InterruptedException ignored) {
                }
                User updated = UserDAO.getUserById(user.getId());
                if (updated != null) {
                    Platform.runLater(() -> {
                        SessionManager.setCurrentUser(updated);
                        profileBox.getChildren().clear();
                        addProfileField(profileBox, "Username", updated.getUsername());
                        addProfileField(profileBox, "Full Name", updated.getFullName());
                        addProfileField(profileBox, "Email", updated.getEmail());
                        addProfileField(profileBox, "Phone", updated.getPhone());
                        addProfileField(profileBox, "Role", updated.getRole());
                        refreshStatus.setText("✓ Updated at "
                                + LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss")));
                        refreshButton.setDisable(false);
                    });
                }
            }).start();
        });

        HBox buttonBox = new HBox(10);
        buttonBox.setAlignment(Pos.TOP_RIGHT);
        buttonBox.getChildren().addAll(refreshButton, refreshStatus);

        vbox.getChildren().addAll(new Label("Your Profile"), buttonBox, profileBox);
        return vbox;
    }

    // ── Shared helpers ────────────────────────────────────────────────────────
    private static void addProfileField(VBox container, String label, String value) {
        HBox field = new HBox(10);
        field.setPadding(new Insets(10, 0, 10, 0));
        Label lbl = new Label(label + ":");
        lbl.setStyle("-fx-font-weight: bold; -fx-min-width: 120;");
        lbl.setPrefWidth(120);
        Label val = new Label(value != null ? value : "N/A");
        val.setStyle("-fx-text-fill: #555;");
        field.getChildren().addAll(lbl, val);
        container.getChildren().add(field);
    }

    private static void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
