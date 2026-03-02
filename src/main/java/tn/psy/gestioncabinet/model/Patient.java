package tn.psy.gestioncabinet.model;

/**
 * Modèle Patient - pour authentification
 * Table : patient
 */
public class Patient {

    private int idPatient;
    private String nom;
    private String email;
    private String telephone;
    private String motDePasse;

    public Patient() {
    }

    public Patient(String nom, String email, String motDePasse) {
        this.nom = nom;
        this.email = email;
        this.motDePasse = motDePasse;
    }

    public Patient(int idPatient, String nom, String email, String telephone, String motDePasse) {
        this.idPatient = idPatient;
        this.nom = nom;
        this.email = email;
        this.telephone = telephone;
        this.motDePasse = motDePasse;
    }

    public int getIdPatient() {
        return idPatient;
    }

    public void setIdPatient(int idPatient) {
        this.idPatient = idPatient;
    }

    public String getNom() {
        return nom;
    }

    public void setNom(String nom) {
        this.nom = nom;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getTelephone() {
        return telephone;
    }

    public void setTelephone(String telephone) {
        this.telephone = telephone;
    }

    public String getMotDePasse() {
        return motDePasse;
    }

    public void setMotDePasse(String motDePasse) {
        this.motDePasse = motDePasse;
    }
}
