package com.psychologie.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Disponibilite {
    private Integer id;
    private Cabinet cabinet;
    private int jour; // 1 (Monday) to 7 (Sunday)
    private LocalTime heureDebut;
    private LocalTime heureFin;
    private int dureeConsultation;
}
