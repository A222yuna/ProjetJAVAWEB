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
import javafx.scene.Scene;
import javafx.scene.control.Button;
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
import javafx.stage.Stage;

public class PsychologueDashboard {

    public static VBox createDashboard() {
        VBox mainVBox = new VBox();
        mainVBox.setStyle("-fx-background-color: #f5f5f5;");

        // Header
        HBox header = createHeader();

        // Content area
        TabPane tabPane = new TabPane();
        tabPane.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);
        tabPane.setStyle("-fx-font-size: 12;");

        Tab appointmentsTab = new Tab("Appointments", createAppointmentsTab());
        appointmentsTab.setStyle("-fx-text-base-color: #333;");

        Tab scheduleTab = new Tab("My Schedule", createScheduleTab());
        scheduleTab.setStyle("-fx-text-base-color: #333;");

        Tab profileTab = new Tab("Profile", createProfileTab());
        profileTab.setStyle("-fx-text-base-color: #333;");

        tabPane.getTabs().addAll(appointmentsTab, scheduleTab, profileTab);

        VBox.setVgrow(tabPane, Priority.ALWAYS);
        mainVBox.getChildren().addAll(header, tabPane);

        return mainVBox;
    }

    private static HBox createHeader() {
        HBox header = new HBox();
        header.setPadding(new Insets(20));
        header.setStyle("-fx-background-color: #764ba2;");
        header.setAlignment(Pos.CENTER_LEFT);
        header.setSpacing(20);

        Label titleLabel = new Label("Psychologue Dashboard");
        titleLabel.setFont(Font.font("Segoe UI", FontWeight.BOLD, 20));
        titleLabel.setTextFill(Color.WHITE);

        User currentUser = SessionManager.getCurrentUser();
        Label welcomeLabel = new Label("Welcome, " + currentUser.getFullName());
        welcomeLabel.setFont(Font.font("Segoe UI", 14));
        welcomeLabel.setTextFill(Color.WHITE);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button logoutButton = new Button("Logout");
        logoutButton.setStyle("-fx-background-color: #ff6b6b; -fx-text-fill: white; " +
                "-fx-font-weight: bold; -fx-padding: 8 20;");
        logoutButton.setOnAction(e -> MainController.showLogin(MainController.getPrimaryStage()));

        header.getChildren().addAll(titleLabel, welcomeLabel, spacer, logoutButton);
        return header;
    }

    private static VBox createAppointmentsTab() {
        VBox vbox = new VBox(15);
        vbox.setPadding(new Insets(20));
        vbox.setStyle("-fx-background-color: #f5f5f5;");

        int psychologueId = SessionManager.getCurrentUser().getId();
        ObservableList<Appointment> appointments =
                FXCollections.observableArrayList(AppointmentDAO.getAppointmentsByPsychologue(psychologueId));

        // Refresh Controls
        HBox refreshBox = new HBox(10);
        refreshBox.setAlignment(Pos.TOP_RIGHT);
        refreshBox.setPadding(new Insets(0, 0, 10, 0));

        Button refreshButton = new Button("🔄 Refresh");
        refreshButton.setStyle("-fx-background-color: #764ba2; -fx-text-fill: white; " +
                "-fx-font-weight: bold; -fx-padding: 8 15; -fx-font-size: 12;");

        Label refreshStatus = new Label();
        refreshStatus.setStyle("-fx-font-size: 11; -fx-text-fill: #666;");

        refreshButton.setOnAction(e -> {
            refreshStatus.setText("Refreshing...");
            refreshButton.setDisable(true);

            new Thread(() -> {
                try {
                    Thread.sleep(500);
                    ObservableList<Appointment> updatedAppointments =
                            FXCollections.observableArrayList(AppointmentDAO.getAppointmentsByPsychologue(psychologueId));

                    Platform.runLater(() -> {
                        appointments.clear();
                        appointments.addAll(updatedAppointments);
                        refreshStatus.setText("✓ Updated at " + LocalTime.now().format(
                                DateTimeFormatter.ofPattern("HH:mm:ss")));
                        refreshButton.setDisable(false);
                    });
                } catch (InterruptedException ex) {
                    ex.printStackTrace();
                }
            }).start();
        });

        refreshBox.getChildren().addAll(refreshButton, refreshStatus);
        vbox.getChildren().add(refreshBox);

        if (appointments.isEmpty()) {
            Label noAppointments = new Label("No appointments scheduled");
            noAppointments.setStyle("-fx-font-size: 14; -fx-text-fill: #999;");
            vbox.getChildren().add(noAppointments);
            return vbox;
        }

        TableView<Appointment> table = new TableView<>();
        table.setItems(appointments);
        table.setPrefHeight(400);

        TableColumn<Appointment, Integer> idCol = new TableColumn<>("ID");
        idCol.setCellValueFactory(cellData -> new javafx.beans.property.SimpleIntegerProperty(cellData.getValue().getId()).asObject());
        idCol.setPrefWidth(50);

        TableColumn<Appointment, String> patientCol = new TableColumn<>("Patient");
        patientCol.setCellValueFactory(cellData -> new javafx.beans.property.SimpleStringProperty(cellData.getValue().getPatientName()));
        patientCol.setPrefWidth(150);

        TableColumn<Appointment, String> dayCol = new TableColumn<>("Day");
        dayCol.setCellValueFactory(cellData -> new javafx.beans.property.SimpleStringProperty(cellData.getValue().getDayOfWeek()));
        dayCol.setPrefWidth(100);

        TableColumn<Appointment, String> periodCol = new TableColumn<>("Period");
        periodCol.setCellValueFactory(cellData -> new javafx.beans.property.SimpleStringProperty(cellData.getValue().getPeriod()));
        periodCol.setPrefWidth(100);

        TableColumn<Appointment, String> statusCol = new TableColumn<>("Status");
        statusCol.setCellValueFactory(cellData -> new javafx.beans.property.SimpleStringProperty(cellData.getValue().getStatus()));
        statusCol.setPrefWidth(120);

        TableColumn<Appointment, Void> actionCol = new TableColumn<>("Actions");
        actionCol.setPrefWidth(240);
        actionCol.setCellFactory(param -> new TableCell<Appointment, Void>() {
            private final Button updateBtn = new Button("Update");
            private final Button completeBtn = new Button("Complete");
            private final Button cancelBtn = new Button("Cancel");
            private final HBox actionBox = new HBox(5);

            {
                updateBtn.setStyle("-fx-background-color: #4a90e2; -fx-text-fill: white; -fx-padding: 5 8; -fx-font-size: 11;");
                completeBtn.setStyle("-fx-background-color: #51cf66; -fx-text-fill: white; -fx-padding: 5 8; -fx-font-size: 11;");
                cancelBtn.setStyle("-fx-background-color: #ff6b6b; -fx-text-fill: white; -fx-padding: 5 8; -fx-font-size: 11;");

                updateBtn.setOnAction(event -> {
                    Appointment appointment = getTableView().getItems().get(getIndex());
                    showUpdateDialog(appointment, getTableView());
                });

                completeBtn.setOnAction(event -> {
                    Appointment appointment = getTableView().getItems().get(getIndex());
                    AppointmentDAO.updateAppointmentStatus(appointment.getId(), "COMPLETED");
                    appointment.setStatus("COMPLETED");
                    getTableView().refresh();
                });

                cancelBtn.setOnAction(event -> {
                    Appointment appointment = getTableView().getItems().get(getIndex());
                    AppointmentDAO.updateAppointmentStatus(appointment.getId(), "CANCELLED");
                    appointment.setStatus("CANCELLED");
                    getTableView().refresh();
                });

                actionBox.setSpacing(5);
                actionBox.getChildren().addAll(updateBtn, completeBtn, cancelBtn);
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || getIndex() < 0) {
                    setGraphic(null);
                } else {
                    Appointment appointment = getTableView().getItems().get(getIndex());
                    if (appointment.getStatus().equals("SCHEDULED")) {
                        setGraphic(actionBox);
                    } else {
                        setGraphic(null);
                    }
                }
            }
        });

        table.getColumns().addAll(idCol, patientCol, dayCol, periodCol, statusCol, actionCol);

        VBox.setVgrow(table, Priority.ALWAYS);
        vbox.getChildren().addAll(
                new Label("Patient Appointments"),
                table
        );

        return vbox;
    }

    private static VBox createScheduleTab() {
        VBox vbox = new VBox(15);
        vbox.setPadding(new Insets(20));
        vbox.setStyle("-fx-background-color: #f5f5f5;");

        int psychologueId = SessionManager.getCurrentUser().getId();
        ObservableList<PsychologuePlan> plans =
                FXCollections.observableArrayList(PsychologuePlanDAO.getPlansByPsychologue(psychologueId));

        HBox addPlanBox = new HBox(10);
        addPlanBox.setPadding(new Insets(15));
        addPlanBox.setStyle("-fx-background-color: white; -fx-border-color: #ddd; -fx-border-radius: 5;");

        ComboBox<String> dayCombo = new ComboBox<>();
        dayCombo.setItems(FXCollections.observableArrayList(
                "MONDAY", "TUESDAY", "WEDNESDAY", "THURSDAY", "FRIDAY", "SATURDAY", "SUNDAY"
        ));
        dayCombo.setPromptText("Select Day");
        dayCombo.setPrefWidth(120);

        ComboBox<String> periodCombo = new ComboBox<>();
        periodCombo.setItems(FXCollections.observableArrayList("DAY", "NIGHT"));
        periodCombo.setPromptText("Select Period");
        periodCombo.setPrefWidth(120);

        Spinner<Integer> maxAppointmentsSpinner = new Spinner<>(1, 20, 5);
        maxAppointmentsSpinner.setPrefWidth(100);

        Label statusLabel = new Label();
        statusLabel.setStyle("-fx-font-size: 12;");

        Button addButton = new Button("Add Schedule");
        addButton.setStyle("-fx-background-color: #764ba2; -fx-text-fill: white; " +
                "-fx-font-weight: bold; -fx-padding: 8 15;");
        addButton.setOnAction(e -> {
            if (dayCombo.getValue() == null || periodCombo.getValue() == null) {
                statusLabel.setText("Please select day and period");
                statusLabel.setTextFill(Color.RED);
                return;
            }

            PsychologuePlan plan = new PsychologuePlan(
                    psychologueId,
                    dayCombo.getValue(),
                    periodCombo.getValue(),
                    maxAppointmentsSpinner.getValue()
            );

            if (PsychologuePlanDAO.createPlan(plan)) {
                plans.add(plan);
                dayCombo.setValue(null);
                periodCombo.setValue(null);
                maxAppointmentsSpinner.getValueFactory().setValue(5);
                statusLabel.setText("Schedule added successfully!");
                statusLabel.setTextFill(Color.GREEN);
            } else {
                statusLabel.setText("Failed to add schedule");
                statusLabel.setTextFill(Color.RED);
            }
        });

        addPlanBox.getChildren().addAll(
                new Label("Day:"), dayCombo,
                new Label("Period:"), periodCombo,
                new Label("Max Appointments:"), maxAppointmentsSpinner,
                addButton
        );

        // Refresh button for schedules
        HBox refreshBox = new HBox(10);
        refreshBox.setAlignment(Pos.TOP_RIGHT);
        refreshBox.setPadding(new Insets(10, 0, 0, 0));

        Button refreshButton = new Button("🔄 Refresh Schedules");
        refreshButton.setStyle("-fx-background-color: #764ba2; -fx-text-fill: white; " +
                "-fx-font-weight: bold; -fx-padding: 8 15; -fx-font-size: 12;");

        Label refreshStatus = new Label();
        refreshStatus.setStyle("-fx-font-size: 11; -fx-text-fill: #666;");

        refreshButton.setOnAction(e -> {
            refreshStatus.setText("Refreshing...");
            refreshButton.setDisable(true);

            new Thread(() -> {
                try {
                    Thread.sleep(500);
                    ObservableList<PsychologuePlan> updatedPlans =
                            FXCollections.observableArrayList(PsychologuePlanDAO.getPlansByPsychologue(psychologueId));

                    Platform.runLater(() -> {
                        plans.clear();
                        plans.addAll(updatedPlans);
                        refreshStatus.setText("✓ Updated at " + LocalTime.now().format(
                                DateTimeFormatter.ofPattern("HH:mm:ss")));
                        refreshButton.setDisable(false);
                    });
                } catch (InterruptedException ex) {
                    ex.printStackTrace();
                }
            }).start();
        });

        refreshBox.getChildren().addAll(refreshButton, refreshStatus);

        // Schedule table
        TableView<PsychologuePlan> table = new TableView<>(plans);
        table.setPrefHeight(300);

        TableColumn<PsychologuePlan, Integer> idCol = new TableColumn<>("ID");
        idCol.setCellValueFactory(cellData -> new javafx.beans.property.SimpleIntegerProperty(cellData.getValue().getId()).asObject());
        idCol.setPrefWidth(50);

        TableColumn<PsychologuePlan, String> dayCol = new TableColumn<>("Day");
        dayCol.setCellValueFactory(cellData -> new javafx.beans.property.SimpleStringProperty(cellData.getValue().getDayOfWeek()));
        dayCol.setPrefWidth(120);

        TableColumn<PsychologuePlan, String> periodCol = new TableColumn<>("Period");
        periodCol.setCellValueFactory(cellData -> new javafx.beans.property.SimpleStringProperty(cellData.getValue().getPeriod()));
        periodCol.setPrefWidth(100);

        TableColumn<PsychologuePlan, Integer> maxCol = new TableColumn<>("Max Appointments");
        maxCol.setCellValueFactory(cellData -> new javafx.beans.property.SimpleIntegerProperty(cellData.getValue().getMaxAppointments()).asObject());
        maxCol.setPrefWidth(150);

        TableColumn<PsychologuePlan, Void> actionCol = new TableColumn<>("Action");
        actionCol.setPrefWidth(100);
        actionCol.setCellFactory(param -> new TableCell<PsychologuePlan, Void>() {
            private final Button deleteBtn = new Button("Delete");

            {
                deleteBtn.setStyle("-fx-background-color: #ff6b6b; -fx-text-fill: white; -fx-padding: 5 10;");
                deleteBtn.setOnAction(event -> {
                    PsychologuePlan plan = getTableView().getItems().get(getIndex());
                    if (PsychologuePlanDAO.deletePlan(plan.getId())) {
                        getTableView().getItems().remove(getIndex());
                    }
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : deleteBtn);
            }
        });

        table.getColumns().addAll(idCol, dayCol, periodCol, maxCol, actionCol);

        VBox.setVgrow(table, Priority.ALWAYS);
        vbox.getChildren().addAll(
                new Label("Manage Your Schedule"),
                addPlanBox,
                statusLabel,
                new Separator(),
                refreshBox,
                new Label("Your Time Slots"),
                table
        );

        return vbox;
    }

    private static VBox createProfileTab() {
        VBox vbox = new VBox(15);
        vbox.setPadding(new Insets(20));
        vbox.setStyle("-fx-background-color: #f5f5f5;");

        User user = SessionManager.getCurrentUser();

        // Refresh button for profile
        Button refreshButton = new Button("🔄 Refresh Profile");
        refreshButton.setStyle("-fx-background-color: #764ba2; -fx-text-fill: white; " +
                "-fx-font-weight: bold; -fx-padding: 8 15;");

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
            refreshStatus.setText("Refreshing...");
            refreshButton.setDisable(true);

            new Thread(() -> {
                try {
                    Thread.sleep(500);
                    User updatedUser = UserDAO.getUserById(user.getId());

                    if (updatedUser != null) {
                        Platform.runLater(() -> {
                            SessionManager.setCurrentUser(updatedUser);
                            // Update all profile fields
                            profileBox.getChildren().clear();
                            addProfileField(profileBox, "Username", updatedUser.getUsername());
                            addProfileField(profileBox, "Full Name", updatedUser.getFullName());
                            addProfileField(profileBox, "Email", updatedUser.getEmail());
                            addProfileField(profileBox, "Phone", updatedUser.getPhone());
                            addProfileField(profileBox, "Role", updatedUser.getRole());

                            refreshStatus.setText("✓ Updated at " + LocalTime.now().format(
                                    DateTimeFormatter.ofPattern("HH:mm:ss")));
                            refreshButton.setDisable(false);
                        });
                    }
                } catch (InterruptedException ex) {
                    ex.printStackTrace();
                }
            }).start();
        });

        HBox buttonBox = new HBox(10);
        buttonBox.setAlignment(Pos.TOP_RIGHT);
        buttonBox.getChildren().addAll(refreshButton, refreshStatus);

        vbox.getChildren().addAll(
                new Label("Your Profile"),
                buttonBox,
                profileBox
        );

        return vbox;
    }

    private static void addProfileField(VBox container, String label, String value) {
        HBox field = new HBox(10);
        field.setPadding(new Insets(10, 0, 10, 0));

        Label labelWidget = new Label(label + ":");
        labelWidget.setStyle("-fx-font-weight: bold; -fx-min-width: 120;");
        labelWidget.setPrefWidth(120);

        Label valueWidget = new Label(value != null ? value : "N/A");
        valueWidget.setStyle("-fx-text-fill: #555;");

        field.getChildren().addAll(labelWidget, valueWidget);
        container.getChildren().add(field);
    }

    private static void showUpdateDialog(Appointment appointment, TableView<Appointment> table) {
        Stage dialogStage = new Stage();
        dialogStage.setTitle("Update Appointment");
        dialogStage.setWidth(400);
        dialogStage.setHeight(340);

        VBox dialogVBox = new VBox(15);
        dialogVBox.setPadding(new Insets(20));
        dialogVBox.setStyle("-fx-background-color: white;");

        Label titleLabel = new Label("Reassign Appointment Time Slot");
        titleLabel.setFont(Font.font("Segoe UI", FontWeight.BOLD, 14));

        ComboBox<PsychologuePlan> planCombo = new ComboBox<>();
        planCombo.setPrefWidth(350);
        
        int psychologueId = SessionManager.getCurrentUser().getId();
        planCombo.setItems(FXCollections.observableArrayList(
            PsychologuePlanDAO.getPlansByPsychologue(psychologueId)
        ));

        Label statusLabel = new Label();
        statusLabel.setStyle("-fx-font-size: 12;");

        HBox buttonBox = new HBox(10);
        buttonBox.setAlignment(Pos.CENTER);
        buttonBox.setPadding(new Insets(10, 0, 0, 0));

        Button updateButton = new Button("Update");
        updateButton.setStyle("-fx-background-color: #764ba2; -fx-text-fill: white; -fx-padding: 8 20;");
        updateButton.setOnAction(e -> {
            PsychologuePlan selectedPlan = planCombo.getValue();
            if (selectedPlan == null) {
                statusLabel.setText("Please select a time slot");
                statusLabel.setTextFill(Color.RED);
                return;
            }

            if (selectedPlan.getId() == appointment.getPlanId()) {
                statusLabel.setText("Please select a different time slot");
                statusLabel.setTextFill(Color.RED);
                return;
            }

            int currentCount = AppointmentDAO.countAppointmentsForPlan(selectedPlan.getId());
            if (currentCount >= selectedPlan.getMaxAppointments()) {
                statusLabel.setText("This slot is full");
                statusLabel.setTextFill(Color.RED);
                return;
            }

            if (AppointmentDAO.updateAppointment(appointment.getId(), selectedPlan.getId())) {
                statusLabel.setText("Appointment updated successfully!");
                statusLabel.setTextFill(Color.GREEN);
                
                new Thread(() -> {
                    try {
                        Thread.sleep(1000);
                        Platform.runLater(() -> {
                            dialogStage.close();
                            appointment.setPlanId(selectedPlan.getId());
                            table.refresh();
                        });
                    } catch (InterruptedException ex) {
                        ex.printStackTrace();
                    }
                }).start();
            } else {
                statusLabel.setText("Failed to update appointment");
                statusLabel.setTextFill(Color.RED);
            }
        });

        Button cancelButton = new Button("Cancel");
        cancelButton.setStyle("-fx-background-color: #f0f0f0; -fx-text-fill: #333; -fx-padding: 8 20;");
        cancelButton.setOnAction(e -> dialogStage.close());

        buttonBox.getChildren().addAll(updateButton, cancelButton);

        Label patientLabel = new Label("Patient: " + appointment.getPatientName());
        patientLabel.setStyle("-fx-font-size: 11; -fx-text-fill: #666;");

        dialogVBox.getChildren().addAll(
                titleLabel,
                patientLabel,
                new Label("Current: " + appointment.getDayOfWeek() + " - " + appointment.getPeriod()),
                new Separator(),
                new Label("Select New Time Slot:"),
                planCombo,
                statusLabel,
                buttonBox
        );

        Scene scene = new Scene(dialogVBox);
        dialogStage.setScene(scene);
        dialogStage.show();
    }
}