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

public class PatientDashboard {

    public static VBox createDashboard() {
        VBox mainVBox = new VBox();
        mainVBox.setStyle("-fx-background-color: #f5f5f5;");

        // Header
        HBox header = createHeader();

        // Content area
        TabPane tabPane = new TabPane();
        tabPane.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);
        tabPane.setStyle("-fx-font-size: 12;");

        Tab appointmentsTab = new Tab("My Appointments", createAppointmentsTab());
        appointmentsTab.setStyle("-fx-text-base-color: #333;");

        Tab bookAppointmentTab = new Tab("Book Appointment", createBookAppointmentTab());
        bookAppointmentTab.setStyle("-fx-text-base-color: #333;");

        Tab profileTab = new Tab("Profile", createProfileTab());
        profileTab.setStyle("-fx-text-base-color: #333;");

        tabPane.getTabs().addAll(appointmentsTab, bookAppointmentTab, profileTab);

        VBox.setVgrow(tabPane, Priority.ALWAYS);
        mainVBox.getChildren().addAll(header, tabPane);

        return mainVBox;
    }

    private static HBox createHeader() {
        HBox header = new HBox();
        header.setPadding(new Insets(20));
        header.setStyle("-fx-background-color: #667eea;");
        header.setAlignment(Pos.CENTER_LEFT);
        header.setSpacing(20);

        Label titleLabel = new Label("Patient Dashboard");
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

        int patientId = SessionManager.getCurrentUser().getId();
        ObservableList<Appointment> appointments =
                FXCollections.observableArrayList(AppointmentDAO.getAppointmentsByPatient(patientId));

        // Refresh Controls
        HBox refreshBox = new HBox(10);
        refreshBox.setAlignment(Pos.TOP_RIGHT);
        refreshBox.setPadding(new Insets(0, 0, 10, 0));

        Button refreshButton = new Button("🔄 Refresh");
        refreshButton.setStyle("-fx-background-color: #667eea; -fx-text-fill: white; " +
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
                            FXCollections.observableArrayList(AppointmentDAO.getAppointmentsByPatient(patientId));

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

        TableColumn<Appointment, String> psychologueCol = new TableColumn<>("Psychologue");
        psychologueCol.setCellValueFactory(cellData -> new javafx.beans.property.SimpleStringProperty(cellData.getValue().getPsychologueName()));
        psychologueCol.setPrefWidth(150);

        TableColumn<Appointment, String> dayCol = new TableColumn<>("Day");
        dayCol.setCellValueFactory(cellData -> new javafx.beans.property.SimpleStringProperty(cellData.getValue().getDayOfWeek()));
        dayCol.setPrefWidth(100);

        TableColumn<Appointment, String> periodCol = new TableColumn<>("Period");
        periodCol.setCellValueFactory(cellData -> new javafx.beans.property.SimpleStringProperty(cellData.getValue().getPeriod()));
        periodCol.setPrefWidth(100);

        TableColumn<Appointment, String> statusCol = new TableColumn<>("Status");
        statusCol.setCellValueFactory(cellData -> new javafx.beans.property.SimpleStringProperty(cellData.getValue().getStatus()));
        statusCol.setPrefWidth(100);

        TableColumn<Appointment, Void> actionCol = new TableColumn<>("Action");
        actionCol.setPrefWidth(150);
        actionCol.setCellFactory(param -> new TableCell<Appointment, Void>() {
            private final Button updateBtn = new Button("Update");
            private final Button cancelBtn = new Button("Cancel");
            private final HBox actionBox = new HBox(5);

            {
                updateBtn.setStyle("-fx-background-color: #4a90e2; -fx-text-fill: white; -fx-padding: 5 10;");
                cancelBtn.setStyle("-fx-background-color: #ff6b6b; -fx-text-fill: white; -fx-padding: 5 10;");

                updateBtn.setOnAction(event -> {
                    Appointment appointment = getTableView().getItems().get(getIndex());
                    showUpdateDialog(appointment, getTableView());
                });

                cancelBtn.setOnAction(event -> {
                    Appointment appointment = getTableView().getItems().get(getIndex());
                    if (!appointment.getStatus().equals("CANCELLED")) {
                        AppointmentDAO.updateAppointmentStatus(appointment.getId(), "CANCELLED");
                        appointment.setStatus("CANCELLED");
                        getTableView().refresh();
                    }
                });

                actionBox.setSpacing(5);
                actionBox.getChildren().addAll(updateBtn, cancelBtn);
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

        table.getColumns().addAll(idCol, psychologueCol, dayCol, periodCol, statusCol, actionCol);

        VBox.setVgrow(table, Priority.ALWAYS);
        vbox.getChildren().addAll(
                new Label("Your Appointments"),
                table
        );

        return vbox;
    }

    private static void showUpdateDialog(Appointment appointment, TableView<Appointment> table) {
        Stage dialogStage = new Stage();
        dialogStage.setTitle("Update Appointment");
        dialogStage.setWidth(400);
        dialogStage.setHeight(300);

        VBox dialogVBox = new VBox(15);
        dialogVBox.setPadding(new Insets(20));
        dialogVBox.setStyle("-fx-background-color: white;");

        Label titleLabel = new Label("Change Your Appointment Time Slot");
        titleLabel.setFont(Font.font("Segoe UI", FontWeight.BOLD, 14));

        ComboBox<PsychologuePlan> planCombo = new ComboBox<>();
        planCombo.setPrefWidth(350);
        
        User psychologue = UserDAO.getUserById(
            PsychologuePlanDAO.getPlanById(appointment.getPlanId()).getPsychologueId()
        );
        planCombo.setItems(FXCollections.observableArrayList(
            PsychologuePlanDAO.getPlansByPsychologue(psychologue.getId())
        ));

        Label statusLabel = new Label();
        statusLabel.setStyle("-fx-font-size: 12;");

        HBox buttonBox = new HBox(10);
        buttonBox.setAlignment(Pos.CENTER);
        buttonBox.setPadding(new Insets(10, 0, 0, 0));

        Button updateButton = new Button("Update");
        updateButton.setStyle("-fx-background-color: #4a90e2; -fx-text-fill: white; -fx-padding: 8 20;");
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

        dialogVBox.getChildren().addAll(
                titleLabel,
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

    private static VBox createBookAppointmentTab() {
        VBox vbox = new VBox(15);
        vbox.setPadding(new Insets(20));
        vbox.setStyle("-fx-background-color: #f5f5f5;");

        HBox selectionBox = new HBox(15);
        selectionBox.setPadding(new Insets(15));
        selectionBox.setStyle("-fx-background-color: white; -fx-border-color: #ddd; -fx-border-radius: 5;");

        Label psychologueLabel = new Label("Select Psychologue:");
        psychologueLabel.setStyle("-fx-font-weight: bold;");

        ComboBox<User> psychologueCombo = new ComboBox<>();
        psychologueCombo.setItems(FXCollections.observableArrayList(UserDAO.getAllPsychologues()));
        psychologueCombo.setPrefWidth(200);

        Label planLabel = new Label("Select Time Slot:");
        planLabel.setStyle("-fx-font-weight: bold;");

        ComboBox<PsychologuePlan> planCombo = new ComboBox<>();
        planCombo.setPrefWidth(250);

        // Refresh psychologist list button
        Button refreshPsychButton = new Button("🔄");
        refreshPsychButton.setStyle("-fx-padding: 5 10;");
        refreshPsychButton.setOnAction(e -> {
            psychologueCombo.setItems(FXCollections.observableArrayList(UserDAO.getAllPsychologues()));
        });

        Label statusLabel = new Label();
        statusLabel.setStyle("-fx-font-size: 12;");

        Button bookButton = new Button("Book Appointment");
        bookButton.setStyle("-fx-background-color: #667eea; -fx-text-fill: white; " +
                "-fx-font-weight: bold; -fx-padding: 10 20;");
        bookButton.setOnAction(e -> {
            PsychologuePlan selectedPlan = planCombo.getValue();
            if (selectedPlan == null) {
                statusLabel.setText("Please select a time slot");
                statusLabel.setTextFill(Color.RED);
                return;
            }

            int currentCount = AppointmentDAO.countAppointmentsForPlan(selectedPlan.getId());
            if (currentCount >= selectedPlan.getMaxAppointments()) {
                statusLabel.setText("This slot is full");
                statusLabel.setTextFill(Color.RED);
                return;
            }

            Appointment appointment = new Appointment(
                    SessionManager.getCurrentUser().getId(),
                    selectedPlan.getId()
            );

            if (AppointmentDAO.createAppointment(appointment)) {
                statusLabel.setText("Appointment booked successfully!");
                statusLabel.setTextFill(Color.GREEN);
                planCombo.setValue(null);
            } else {
                statusLabel.setText("Failed to book appointment");
                statusLabel.setTextFill(Color.RED);
            }
        });

        psychologueCombo.setOnAction(e -> {
            User selected = psychologueCombo.getValue();
            if (selected != null) {
                planCombo.setItems(FXCollections.observableArrayList(
                        PsychologuePlanDAO.getPlansByPsychologue(selected.getId())
                ));
            }
        });

        selectionBox.getChildren().addAll(
                psychologueLabel, psychologueCombo, refreshPsychButton,
                planLabel, planCombo,
                bookButton
        );

        vbox.getChildren().addAll(
                new Label("Book an Appointment"),
                selectionBox,
                statusLabel
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
        refreshButton.setStyle("-fx-background-color: #667eea; -fx-text-fill: white; " +
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
}