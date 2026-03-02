package tn.psy.gestioncabinet.model;

import java.time.LocalDate;
import java.time.LocalTime;

/**
 * Modèle Creneau - un créneau horaire réservable (généré à partir d'une Disponibilite).
 * Table : creneau
 */
public class Creneau {

    private int id;
    private int disponibiliteId;
    private LocalDate dateCreneau;
    private LocalTime heure;
    private String statut; // LIBRE, RESERVE
    /**
     * Identifiant de l'utilisateur patient (users.id_user), nullable si créneau libre.
     */
    private Integer patientIdUser;

    public Creneau() {
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getDisponibiliteId() {
        return disponibiliteId;
    }

    public void setDisponibiliteId(int disponibiliteId) {
        this.disponibiliteId = disponibiliteId;
    }

    public LocalDate getDateCreneau() {
        return dateCreneau;
    }

    public void setDateCreneau(LocalDate dateCreneau) {
        this.dateCreneau = dateCreneau;
    }

    public LocalTime getHeure() {
        return heure;
    }

    public void setHeure(LocalTime heure) {
        this.heure = heure;
    }

    public String getStatut() {
        return statut;
    }

    public void setStatut(String statut) {
        this.statut = statut;
    }

    public Integer getPatientIdUser() {
        return patientIdUser;
    }

    public void setPatientIdUser(Integer patientIdUser) {
        this.patientIdUser = patientIdUser;
    }

    // Méthodes de compatibilité avec l'ancien nom getPatientId/setPatientId
    public Integer getPatientId() {
        return patientIdUser;
    }

    public void setPatientId(Integer patientId) {
        this.patientIdUser = patientId;
    }

    public boolean isLibre() {
        return "LIBRE".equalsIgnoreCase(statut);
    }
}
