package com.psychologie.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDate;
import java.time.LocalTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Creneau {
    public static final String STATUT_RESERVE = "RESERVE";
    public static final String STATUT_ANNULE = "ANNULE";

    private Integer id;
    private Disponibilite disponibilite;
    private User patient;
    private LocalDate dateCreneau;
    private LocalTime heure;
    private String statut = STATUT_RESERVE;
}
