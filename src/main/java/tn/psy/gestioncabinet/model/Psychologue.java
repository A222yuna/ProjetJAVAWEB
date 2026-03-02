package tn.psy.gestioncabinet.model;

/**
 * Modèle Psychologue
 * Table : psychologue
 */
public class Psychologue {

    private int idPsy;
    private String nom;
    private String specialite;
    private String email;
    private String telephone;
    private String motDePasse;

    public Psychologue() {
    }

    public Psychologue(String nom, String specialite, String email, String telephone, String motDePasse) {
        this.nom = nom;
        this.specialite = specialite;
        this.email = email;
        this.telephone = telephone;
        this.motDePasse = motDePasse;
    }

    public Psychologue(int idPsy, String nom, String specialite, String email, String telephone, String motDePasse) {
        this.idPsy = idPsy;
        this.nom = nom;
        this.specialite = specialite;
        this.email = email;
        this.telephone = telephone;
        this.motDePasse = motDePasse;
    }

    public int getIdPsy() {
        return idPsy;
    }

    public void setIdPsy(int idPsy) {
        this.idPsy = idPsy;
    }

    public String getNom() {
        return nom;
    }

    public void setNom(String nom) {
        this.nom = nom;
    }

    public String getSpecialite() {
        return specialite;
    }

    public void setSpecialite(String specialite) {
        this.specialite = specialite;
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

    @Override
    public String toString() {
        return nom + " (" + specialite + ")";
    }
}
