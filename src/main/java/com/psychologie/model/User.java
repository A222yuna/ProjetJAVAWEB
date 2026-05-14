package com.psychologie.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class User {
    private Integer id;
    private String nom;
    private String prenom;
    private String telephone;
    private String email;
    private String motDePasse;
    private String role; // Patient, Psychologue, Admin
    private LocalDate dateInscription;
    private boolean estActif = true;
    private boolean emailVerifie = false;
    private LocalDateTime derniereConnexion;
    private String statutValidation = "approuve";
    private String photoProfil;
    private int failedAttempts = 0;
    private LocalDateTime lockedAt;
}
