// PlanningModel.java
package org.example.model;

import java.time.LocalDateTime;

public class PsychologuePlan {
    private int id;
    private int psychologueId;
    private String dayOfWeek; // MONDAY-SUNDAY
    private String period; // DAY or NIGHT
    private int maxAppointments;
    private LocalDateTime createdAt;

    public PsychologuePlan() {}

    public PsychologuePlan(int psychologueId, String dayOfWeek, String period, int maxAppointments) {
        this.psychologueId = psychologueId;
        this.dayOfWeek = dayOfWeek;
        this.period = period;
        this.maxAppointments = maxAppointments;
    }

    // Getters and Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getPsychologueId() {
        return psychologueId;
    }

    public void setPsychologueId(int psychologueId) {
        this.psychologueId = psychologueId;
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

    public int getMaxAppointments() {
        return maxAppointments;
    }

    public void setMaxAppointments(int maxAppointments) {
        this.maxAppointments = maxAppointments;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}