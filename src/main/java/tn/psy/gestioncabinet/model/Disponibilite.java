package tn.psy.gestioncabinet.model;

import java.time.LocalTime;

/**
 * Modèle Disponibilite - plage horaire récurrente par cabinet (ex: Lundi 8h-18h, créneaux 30 min).
 * Table : disponibilite
 */
public class Disponibilite {

    private int id;
    private int cabinetId;
    private int jour; // 1 = Lundi ... 7 = Dimanche
    private LocalTime heureDebut;
    private LocalTime heureFin;
    private int dureeConsultation; // minutes

    public Disponibilite() {
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getCabinetId() {
        return cabinetId;
    }

    public void setCabinetId(int cabinetId) {
        this.cabinetId = cabinetId;
    }

    public int getJour() {
        return jour;
    }

    public void setJour(int jour) {
        this.jour = jour;
    }

    public LocalTime getHeureDebut() {
        return heureDebut;
    }

    public void setHeureDebut(LocalTime heureDebut) {
        this.heureDebut = heureDebut;
    }

    public LocalTime getHeureFin() {
        return heureFin;
    }

    public void setHeureFin(LocalTime heureFin) {
        this.heureFin = heureFin;
    }

    public int getDureeConsultation() {
        return dureeConsultation;
    }

    public void setDureeConsultation(int dureeConsultation) {
        this.dureeConsultation = dureeConsultation;
    }
}
