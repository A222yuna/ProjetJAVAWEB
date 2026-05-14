package com.psychologie.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ActiviteProgramme {
    private Integer id;
    private ProgrammeBienEtre programme;
    private int jour;
    private LocalTime heureDebut;
    private String titre;
    private String description;
    private Integer dureeMinutes;
    private String typeActivite;
}
