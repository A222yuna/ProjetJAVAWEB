package com.psychoapp.model;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class Utilisateur {

    public enum Role { Admin, Psychologue, Patient }

    private int           idUser;
    private String        nom;
    private String        prenom;
    private String        email;
    private String        motDePasse;
    private Role          role;
    private LocalDate     dateInscription;
    private boolean       estActif;
    private boolean       emailVerifie;
    private LocalDateTime derniereConnexion;
    private String        statutValidation; // 'en_attente', 'approuve', 'rejete'

    public Utilisateur() {}

    // ── Getters & Setters ─────────────────────────────────────────────────────

    public int getIdUser()                        { return idUser; }
    public void setIdUser(int idUser)             { this.idUser = idUser; }

    public String getNom()                        { return nom; }
    public void setNom(String nom)                { this.nom = nom; }

    public String getPrenom()                     { return prenom; }
    public void setPrenom(String prenom)          { this.prenom = prenom; }

    public String getEmail()                      { return email; }
    public void setEmail(String email)            { this.email = email; }

    public String getMotDePasse()                 { return motDePasse; }
    public void setMotDePasse(String motDePasse)  { this.motDePasse = motDePasse; }

    public Role getRole()                         { return role; }
    public void setRole(Role role)                { this.role = role; }

    public LocalDate getDateInscription()                      { return dateInscription; }
    public void setDateInscription(LocalDate d)                { this.dateInscription = d; }

    public boolean isEstActif()                   { return estActif; }
    public void setEstActif(boolean estActif)     { this.estActif = estActif; }

    public boolean isEmailVerifie()               { return emailVerifie; }
    public void setEmailVerifie(boolean b)        { this.emailVerifie = b; }

    public LocalDateTime getDerniereConnexion()               { return derniereConnexion; }
    public void setDerniereConnexion(LocalDateTime dt)        { this.derniereConnexion = dt; }

    public String getStatutValidation()                       { return statutValidation; }
    public void setStatutValidation(String statutValidation)  { this.statutValidation = statutValidation; }

    @Override
    public String toString() {
        return prenom + " " + nom + " (" + email + ")";
    }
}
