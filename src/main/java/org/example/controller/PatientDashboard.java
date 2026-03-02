package org.example.controller;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

import org.example.dao.AppointmentDAO;
import org.example.dao.PsychologuePlanDAO;
import org.example.dao.UserDAO;
import org.example.model.Appointment;
import org.example.model.PsychologuePlan;
import org.example.model.User;
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
import javafx.scene.control.ListCell;
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

public class PatientDashboard {

    public static VBox createDashboard() {
        VBox mainVBox = new VBox();
        mainVBox.setStyle("-fx-background-color: #f5f5f5;");

        HBox header = createHeader();

        TabPane tabPane = new TabPane();
        tabPane.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);
        tabPane.setStyle("-fx-font-size: 12;");

        Tab appointmentsTab = new Tab("My Appointments", createAppointmentsTab());
        Tab bookAppointmentTab = new Tab("Book Appointment", createBookAppointmentTab());
        Tab profileTab = new Tab("Profile", createProfileTab());

        tabPane.getTabs().addAll(appointmentsTab, bookAppointmentTab, profileTab);
        VBox.setVgrow(tabPane, Priority.ALWAYS);
        mainVBox.getChildren().addAll(header, tabPane);
        return mainVBox;
    }

    // ── Header ────────────────────────────────────────────────────────────────
    private static HBox createHeader() {
        HBox header = new HBox();
        header.setPadding(new Insets(20));
        header.setStyle("-fx-background-color: #667eea;");
        header.setAlignment(Pos.CENTER_LEFT);
        header.setSpacing(20);

        Label titleLabel = new Label("Patient Dashboard");
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

    // ── My Appointments tab ───────────────────────────────────────────────────
    private static VBox createAppointmentsTab() {
        VBox vbox = new VBox(15);
        vbox.setPadding(new Insets(20));
        vbox.setStyle("-fx-background-color: #f5f5f5;");

        int patientId = SessionManager.getCurrentUser().getId();
        ObservableList<Appointment> appointments
                = FXCollections.observableArrayList(AppointmentDAO.getAppointmentsByPatient(patientId));

        HBox refreshBox = new HBox(10);
        refreshBox.setAlignment(Pos.TOP_RIGHT);

        Button refreshButton = new Button("🔄 Refresh");
        refreshButton.setStyle("-fx-background-color: #667eea; -fx-text-fill: white; "
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
                ObservableList<Appointment> updated
                        = FXCollections.observableArrayList(AppointmentDAO.getAppointmentsByPatient(patientId));
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

        TableColumn<Appointment, String> psychCol = new TableColumn<>("Psychologue");
        psychCol.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(c.getValue().getPsychologueName()));
        psychCol.setPrefWidth(150);

        TableColumn<Appointment, String> dayCol = new TableColumn<>("Day");
        dayCol.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(c.getValue().getDayOfWeek()));
        dayCol.setPrefWidth(100);

        TableColumn<Appointment, String> periodCol = new TableColumn<>("Period");
        periodCol.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(c.getValue().getPeriod()));
        periodCol.setPrefWidth(100);

        TableColumn<Appointment, String> statusCol = new TableColumn<>("Status");
        statusCol.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(c.getValue().getStatus()));
        statusCol.setPrefWidth(100);

        // Cancel action with confirmation dialog
        TableColumn<Appointment, Void> actionCol = new TableColumn<>("Action");
        actionCol.setPrefWidth(100);
        actionCol.setCellFactory(param -> new TableCell<>() {
            private final Button cancelBtn = new Button("Cancel");

            {
                cancelBtn.setStyle("-fx-background-color: #ff6b6b; -fx-text-fill: white; -fx-padding: 5 10;");
                cancelBtn.setOnAction(event -> {
                    Appointment appt = getTableView().getItems().get(getIndex());

                    // ── Validation: only SCHEDULED appointments can be cancelled ──
                    if (!"SCHEDULED".equals(appt.getStatus())) {
                        showAlert(Alert.AlertType.WARNING,
                                "Cannot Cancel",
                                "Only SCHEDULED appointments can be cancelled.\n"
                                + "This appointment is already " + appt.getStatus() + ".");
                        return;
                    }

                    // ── Confirmation dialog ────────────────────────────────────
                    Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
                    confirm.setTitle("Cancel Appointment");
                    confirm.setHeaderText("Cancel this appointment?");
                    confirm.setContentText("Psychologue: " + appt.getPsychologueName()
                            + "\nDay: " + appt.getDayOfWeek()
                            + "\nPeriod: " + appt.getPeriod()
                            + "\n\nThis action cannot be undone.");
                    confirm.showAndWait().ifPresent(response -> {
                        if (response == ButtonType.OK) {
                            if (AppointmentDAO.updateAppointmentStatus(appt.getId(), "CANCELLED")) {
                                appt.setStatus("CANCELLED");
                                getTableView().refresh();
                            } else {
                                showAlert(Alert.AlertType.ERROR, "Error",
                                        "Failed to cancel the appointment. Please try again.");
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
                setGraphic("SCHEDULED".equals(appt.getStatus()) ? cancelBtn : null);
            }
        });

        table.getColumns().addAll(idCol, psychCol, dayCol, periodCol, statusCol, actionCol);
        VBox.setVgrow(table, Priority.ALWAYS);
        vbox.getChildren().addAll(new Label("Your Appointments"), table);
        return vbox;
    }

    // ── Book Appointment tab ──────────────────────────────────────────────────
    private static VBox createBookAppointmentTab() {
        VBox vbox = new VBox(15);
        vbox.setPadding(new Insets(20));
        vbox.setStyle("-fx-background-color: #f5f5f5;");

        HBox selectionBox = new HBox(15);
        selectionBox.setPadding(new Insets(15));
        selectionBox.setAlignment(Pos.CENTER_LEFT);
        selectionBox.setStyle("-fx-background-color: white; -fx-border-color: #ddd; -fx-border-radius: 5;");

        Label psychologueLabel = new Label("Select Psychologue:");
        psychologueLabel.setStyle("-fx-font-weight: bold;");
        ComboBox<User> psychologueCombo = new ComboBox<>();
        psychologueCombo.setItems(FXCollections.observableArrayList(UserDAO.getAllPsychologues()));
        psychologueCombo.setPrefWidth(200);
        psychologueCombo.setPromptText("-- Choose a psychologue --");

        // Custom cell to show full name
        psychologueCombo.setCellFactory(lv -> new ListCell<>() {
            @Override
            protected void updateItem(User u, boolean empty) {
                super.updateItem(u, empty);
                setText(empty || u == null ? null : u.getFullName());
            }
        });
        psychologueCombo.setButtonCell(new ListCell<>() {
            @Override
            protected void updateItem(User u, boolean empty) {
                super.updateItem(u, empty);
                setText(empty || u == null ? "-- Choose a psychologue --" : u.getFullName());
            }
        });

        Label planLabel = new Label("Select Time Slot:");
        planLabel.setStyle("-fx-font-weight: bold;");
        ComboBox<PsychologuePlan> planCombo = new ComboBox<>();
        planCombo.setPrefWidth(250);
        planCombo.setPromptText("-- Choose a time slot --");
        planCombo.setDisable(true);

        // Custom cell to show day + period + available slots
        planCombo.setCellFactory(lv -> new ListCell<>() {
            @Override
            protected void updateItem(PsychologuePlan p, boolean empty) {
                super.updateItem(p, empty);
                if (empty || p == null) {
                    setText(null);
                    return;
                }
                int booked = AppointmentDAO.countAppointmentsForPlan(p.getId());
                int available = p.getMaxAppointments() - booked;
                setText(p.getDayOfWeek() + " – " + p.getPeriod()
                        + "  (" + available + "/" + p.getMaxAppointments() + " slots available)");
            }
        });
        planCombo.setButtonCell(new ListCell<>() {
            @Override
            protected void updateItem(PsychologuePlan p, boolean empty) {
                super.updateItem(p, empty);
                if (empty || p == null) {
                    setText("-- Choose a time slot --");
                    return;
                }
                int booked = AppointmentDAO.countAppointmentsForPlan(p.getId());
                int available = p.getMaxAppointments() - booked;
                setText(p.getDayOfWeek() + " – " + p.getPeriod()
                        + "  (" + available + "/" + p.getMaxAppointments() + " slots available)");
            }
        });

        Button refreshPsychButton = new Button("🔄");
        refreshPsychButton.setStyle("-fx-padding: 5 10;");
        refreshPsychButton.setOnAction(e
                -> psychologueCombo.setItems(FXCollections.observableArrayList(UserDAO.getAllPsychologues())));

        // ── When a psychologue is selected, load their slots ──────────────────
        psychologueCombo.setOnAction(e -> {
            User selected = psychologueCombo.getValue();
            if (selected != null) {
                planCombo.setItems(FXCollections.observableArrayList(
                        PsychologuePlanDAO.getPlansByPsychologue(selected.getId())));
                planCombo.setValue(null);
                planCombo.setDisable(false);
            } else {
                planCombo.getItems().clear();
                planCombo.setDisable(true);
            }
        });

        Label statusLabel = new Label();
        statusLabel.setStyle("-fx-font-size: 12;");

        Button bookButton = new Button("Book Appointment");
        bookButton.setStyle("-fx-background-color: #667eea; -fx-text-fill: white; "
                + "-fx-font-weight: bold; -fx-padding: 10 20;");

        bookButton.setOnAction(e -> {
            // ── Validation ────────────────────────────────────────────────────
            if (psychologueCombo.getValue() == null) {
                statusLabel.setText("⚠ Please select a psychologue.");
                statusLabel.setTextFill(Color.RED);
                return;
            }
            PsychologuePlan selectedPlan = planCombo.getValue();
            if (selectedPlan == null) {
                statusLabel.setText("⚠ Please select a time slot.");
                statusLabel.setTextFill(Color.RED);
                return;
            }

            // Check slot capacity (re-query to get fresh count)
            int currentCount = AppointmentDAO.countAppointmentsForPlan(selectedPlan.getId());
            if (currentCount >= selectedPlan.getMaxAppointments()) {
                statusLabel.setText("⚠ This slot is full ("
                        + selectedPlan.getMaxAppointments() + "/"
                        + selectedPlan.getMaxAppointments() + "). Please choose another.");
                statusLabel.setTextFill(Color.RED);
                // Refresh plan combo to show updated slot availability
                User selectedUser = psychologueCombo.getValue();
                if (selectedUser != null) {
                    planCombo.setItems(FXCollections.observableArrayList(
                            PsychologuePlanDAO.getPlansByPsychologue(selectedUser.getId())));
                }
                return;
            }

            // Check if patient already has a SCHEDULED appointment for this plan
            boolean alreadyBooked = AppointmentDAO.getAppointmentsByPatient(
                    SessionManager.getCurrentUser().getId())
                    .stream()
                    .anyMatch(a -> a.getPlanId() == selectedPlan.getId()
                    && "SCHEDULED".equals(a.getStatus()));
            if (alreadyBooked) {
                statusLabel.setText("⚠ You already have a scheduled appointment for this slot.");
                statusLabel.setTextFill(Color.RED);
                return;
            }

            // ── Confirmation ──────────────────────────────────────────────────
            Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
            confirm.setTitle("Confirm Booking");
            confirm.setHeaderText("Book appointment?");
            confirm.setContentText(
                    "Psychologue: " + psychologueCombo.getValue().getFullName()
                    + "\nDay: " + selectedPlan.getDayOfWeek()
                    + "\nPeriod: " + selectedPlan.getPeriod());
            confirm.showAndWait().ifPresent(response -> {
                if (response == ButtonType.OK) {
                    Appointment appt = new Appointment(
                            SessionManager.getCurrentUser().getId(),
                            selectedPlan.getId());

                    if (AppointmentDAO.createAppointment(appt)) {
                        statusLabel.setText("✓ Appointment booked successfully!");
                        statusLabel.setTextFill(Color.GREEN);
                        planCombo.setValue(null);
                        psychologueCombo.setValue(null);
                        planCombo.setDisable(true);
                    } else {
                        statusLabel.setText("✗ Failed to book. Please try again.");
                        statusLabel.setTextFill(Color.RED);
                    }
                }
            });
        });

        selectionBox.getChildren().addAll(
                psychologueLabel, psychologueCombo, refreshPsychButton,
                planLabel, planCombo, bookButton);

        vbox.getChildren().addAll(
                new Label("Book an Appointment"),
                selectionBox,
                statusLabel);

        return vbox;
    }

    // ── Profile tab ───────────────────────────────────────────────────────────
    private static VBox createProfileTab() {
        VBox vbox = new VBox(15);
        vbox.setPadding(new Insets(20));
        vbox.setStyle("-fx-background-color: #f5f5f5;");

        User user = SessionManager.getCurrentUser();

        Button refreshButton = new Button("🔄 Refresh Profile");
        refreshButton.setStyle("-fx-background-color: #667eea; -fx-text-fill: white; "
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
