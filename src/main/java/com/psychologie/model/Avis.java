package com.psychologie.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Avis {
    private Integer id;
    private ProgrammeBienEtre programme;
    private User psychologue;
    private int note;
    private String commentaire;
    private LocalDate dateAvis;
}
