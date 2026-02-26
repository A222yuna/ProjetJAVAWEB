package org.example.model;
import java.time.LocalDateTime;

public class Appointment {
    private int id;
    private int patientId;
    private int planId;
    private String status; // SCHEDULED, CANCELLED, COMPLETED
    private LocalDateTime createdAt;
    private String patientName;
    private String psychologueName;
    private String dayOfWeek;
    private String period;

    public Appointment() {}

    public Appointment(int patientId, int planId) {
        this.patientId = patientId;
        this.planId = planId;
        this.status = "SCHEDULED";
    }

    // Getters and Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getPatientId() {
        return patientId;
    }

    public void setPatientId(int patientId) {
        this.patientId = patientId;
    }

    public int getPlanId() {
        return planId;
    }

    public void setPlanId(int planId) {
        this.planId = planId;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public String getPatientName() {
        return patientName;
    }

    public void setPatientName(String patientName) {
        this.patientName = patientName;
    }

    public String getPsychologueName() {
        return psychologueName;
    }

    public void setPsychologueName(String psychologueName) {
        this.psychologueName = psychologueName;
    }

    public String getDayOfWeek() {
        return dayOfWeek;
    }

    public void setDayOfWeek(String dayOfWeek) {
        this.dayOfWeek = dayOfWeek;
    }

    public String getPeriod() {
        return period;
    }

    public void setPeriod(String period) {
        this.period = period;
    }
}