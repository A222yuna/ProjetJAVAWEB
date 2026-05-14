package com.psychologie.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProgrammeBienEtre {
    private Integer id;
    private User psychologue;
    private String nom;
    private String objectif;
    private int duree;
    private String statut;
    private String image;
    private String niveauDifficulte;
    private List<ActiviteProgramme> activites;
    private List<Avis> avis;
}
