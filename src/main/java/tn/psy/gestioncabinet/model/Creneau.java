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
    private Integer patientId;

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

    public Integer getPatientId() {
        return patientId;
    }

    public void setPatientId(Integer patientId) {
        this.patientId = patientId;
    }

    public boolean isLibre() {
        return "LIBRE".equalsIgnoreCase(statut);
    }
}
