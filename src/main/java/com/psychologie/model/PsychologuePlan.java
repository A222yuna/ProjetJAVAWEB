package com.psychologie.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PsychologuePlan {
    public static final List<String> DAY_OF_WEEK_CHOICES = List.of(
        "MONDAY", "TUESDAY", "WEDNESDAY", "THURSDAY", "FRIDAY", "SATURDAY", "SUNDAY"
    );
    public static final List<String> PERIOD_CHOICES = List.of("DAY", "NIGHT");

    private Integer id;
    private User psychologue;
    private String dayOfWeek;
    private String period;
    private int maxAppointments = 5;
    private LocalDateTime createdAt;
    private List<Appointment> appointments;
}
