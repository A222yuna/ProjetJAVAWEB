package tn.psy.gestioncabinet.model;

/**
 * Modèle Rating - notation d'un cabinet par un patient (1 à 5 étoiles).
 * Table : rating
 */
public class Rating {

    private int id;
<<<<<<< HEAD
    private int patientId;
=======
    /**
     * Identifiant de l'utilisateur patient (users.id_user).
     */
    private int patientIdUser;
>>>>>>> origin/gestion-cabinet
    private int cabinetId;
    private int note;

    public Rating() {
    }

<<<<<<< HEAD
    public Rating(int patientId, int cabinetId, int note) {
        this.patientId = patientId;
=======
    public Rating(int patientIdUser, int cabinetId, int note) {
        this.patientIdUser = patientIdUser;
>>>>>>> origin/gestion-cabinet
        this.cabinetId = cabinetId;
        this.note = note;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

<<<<<<< HEAD
    public int getPatientId() {
        return patientId;
    }

    public void setPatientId(int patientId) {
        this.patientId = patientId;
=======
    /**
     * Retourne l'identifiant utilisateur du patient (users.id_user).
     */
    public int getPatientIdUser() {
        return patientIdUser;
    }

    public void setPatientIdUser(int patientIdUser) {
        this.patientIdUser = patientIdUser;
    }

    // Méthodes de compatibilité avec l'ancien nom getPatientId/setPatientId
    public int getPatientId() {
        return patientIdUser;
    }

    public void setPatientId(int patientId) {
        this.patientIdUser = patientId;
>>>>>>> origin/gestion-cabinet
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
