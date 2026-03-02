package tn.psy.gestioncabinet.model;

/**
 * Modèle Rating - notation d'un cabinet par un patient (1 à 5 étoiles).
 * Table : rating
 */
public class Rating {

    private int id;
    private int patientId;
    private int cabinetId;
    private int note;

    public Rating() {
    }

    public Rating(int patientId, int cabinetId, int note) {
        this.patientId = patientId;
        this.cabinetId = cabinetId;
        this.note = note;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getPatientId() {
        return patientId;
    }

    public void setPatientId(int patientId) {
        this.patientId = patientId;
    }

    public int getCabinetId() {
        return cabinetId;
    }

    public void setCabinetId(int cabinetId) {
        this.cabinetId = cabinetId;
    }

    public int getNote() {
        return note;
    }

    public void setNote(int note) {
        this.note = note;
    }
}
